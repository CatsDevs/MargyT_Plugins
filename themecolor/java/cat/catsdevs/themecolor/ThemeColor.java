package cat.catsdevs.themecolor;

import android.app.Activity;
import android.app.AlertDialog;
import android.graphics.Color;
import android.text.InputType;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import cat.narezany.margyt.plugin.MargyPlugin;

public final class ThemeColor extends MargyPlugin {
    private static final String KEY_BG_ON = "background_enabled";
    private static final String KEY_TEXT_ON = "text_enabled";
    private static final String KEY_BG = "background_colour";
    private static final String KEY_TEXT = "text_colour";
    private static final String KEY_STRENGTH = "strength";
    private static final int DEFAULT_BG = Color.rgb(32, 33, 36);
    private static final int DEFAULT_TEXT = Color.rgb(241, 243, 244);

    private volatile boolean backgroundEnabled;
    private volatile boolean textEnabled;
    private volatile int backgroundColour = DEFAULT_BG;
    private volatile int textColour = DEFAULT_TEXT;
    private volatile int strength = 100;

    @Override public void onStart(android.content.Context context) {
        loadSettings();
        margyt().log("ThemeColor loaded.");
    }

    // Kept without @Override to compile with older MargyT API stubs.
    public void onAccountChanged(String uid) { loadSettings(); }

    private void loadSettings() {
        android.content.SharedPreferences p = margyt().prefs();
        backgroundEnabled = p.getBoolean(KEY_BG_ON, true);
        textEnabled = p.getBoolean(KEY_TEXT_ON, false);
        backgroundColour = p.getInt(KEY_BG, DEFAULT_BG);
        textColour = p.getInt(KEY_TEXT, DEFAULT_TEXT);
        strength = Math.max(0, Math.min(100, p.getInt(KEY_STRENGTH, 100)));
    }

    // MargyT discovers this public settings method where supported.
    public boolean onOpenSettings(Activity activity) {
        if (activity == null || activity.isFinishing()) return false;
        showSettings(activity);
        return true;
    }

    private void showSettings(final Activity activity) {
        String[] options = {
            "Фон: " + (backgroundEnabled ? "ВКЛ" : "ВЫКЛ"),
            "Текст: " + (textEnabled ? "ВКЛ" : "ВЫКЛ"),
            "Цвет фона: " + hex(backgroundColour),
            "Цвет текста: " + hex(textColour),
            "Готовые темы",
            "Интенсивность: " + strength + "%",
            "Сбросить настройки"
        };
        new AlertDialog.Builder(activity).setTitle("ThemeColor — оформление")
            .setItems(options, (dialog, which) -> {
                switch (which) {
                    case 0:
                        backgroundEnabled = !backgroundEnabled;
                        saveBoolean(KEY_BG_ON, backgroundEnabled);
                        showSettings(activity);
                        break;
                    case 1:
                        textEnabled = !textEnabled;
                        saveBoolean(KEY_TEXT_ON, textEnabled);
                        showSettings(activity);
                        break;
                    case 2: askColour(activity, "Цвет фона", backgroundColour, true); break;
                    case 3: askColour(activity, "Цвет текста", textColour, false); break;
                    case 4: showPresets(activity); break;
                    case 5: askStrength(activity); break;
                    case 6:
                        margyt().prefs().edit().clear().apply();
                        loadSettings();
                        showSettings(activity);
                        break;
                    default: break;
                }
            }).setNegativeButton("Закрыть", null).show();
    }

    private void showPresets(final Activity activity) {
        String[] names = {"Графит", "Океан", "Лес", "Фиолетовый", "Светлая"};
        final int[] colours = {
            Color.rgb(32, 33, 36), Color.rgb(23, 37, 84),
            Color.rgb(22, 53, 43), Color.rgb(53, 32, 79),
            Color.rgb(245, 247, 250)
        };
        new AlertDialog.Builder(activity).setTitle("Цвет фона")
            .setItems(names, (dialog, which) -> {
                backgroundColour = colours[which];
                backgroundEnabled = true;
                margyt().prefs().edit().putInt(KEY_BG, backgroundColour)
                    .putBoolean(KEY_BG_ON, true).apply();
                showSettings(activity);
            }).setNegativeButton("Назад", (dialog, which) -> showSettings(activity)).show();
    }

    private void askColour(final Activity activity, String title, int current, final boolean isBackground) {
        final EditText input = new EditText(activity);
        input.setSingleLine(true);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS);
        input.setHint("#202124");
        input.setText(hex(current));
        new AlertDialog.Builder(activity).setTitle(title + " (HEX)")
            .setMessage("Введите HEX из 6 символов, например #202124")
            .setView(input)
            .setNegativeButton("Отмена", (dialog, which) -> showSettings(activity))
            .setPositiveButton("Применить", (dialog, which) -> {
                String value = input.getText().toString().trim();
                if (!value.startsWith("#")) value = "#" + value;
                if (!value.matches("#[0-9a-fA-F]{6}")) {
                    new AlertDialog.Builder(activity).setTitle("ThemeColor")
                        .setMessage("Неверный HEX. Пример: #202124")
                        .setPositiveButton("OK", (d, w) -> showSettings(activity)).show();
                    return;
                }
                int parsed = Color.parseColor(value);
                if (isBackground) {
                    backgroundColour = parsed;
                    backgroundEnabled = true;
                    margyt().prefs().edit().putInt(KEY_BG, parsed).putBoolean(KEY_BG_ON, true).apply();
                } else {
                    textColour = parsed;
                    textEnabled = true;
                    margyt().prefs().edit().putInt(KEY_TEXT, parsed).putBoolean(KEY_TEXT_ON, true).apply();
                }
                showSettings(activity);
            }).show();
    }

    private void askStrength(final Activity activity) {
        LinearLayout layout = new LinearLayout(activity);
        layout.setOrientation(LinearLayout.VERTICAL);
        int pad = (int) (20 * activity.getResources().getDisplayMetrics().density);
        layout.setPadding(pad, pad, pad, pad);
        final TextView value = new TextView(activity);
        value.setText(strength + "%");
        final SeekBar bar = new SeekBar(activity);
        bar.setMax(100);
        bar.setProgress(strength);
        bar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                value.setText(progress + "%");
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });
        layout.addView(value);
        layout.addView(bar, new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        new AlertDialog.Builder(activity).setTitle("Интенсивность перекраски").setView(layout)
            .setNegativeButton("Отмена", (dialog, which) -> showSettings(activity))
            .setPositiveButton("Сохранить", (dialog, which) -> {
                strength = bar.getProgress();
                margyt().prefs().edit().putInt(KEY_STRENGTH, strength).apply();
                showSettings(activity);
            }).show();
    }

    private void saveBoolean(String key, boolean value) {
        margyt().prefs().edit().putBoolean(key, value).apply();
    }

    private static String hex(int colour) {
        return String.format("#%06X", 0xFFFFFF & colour);
    }

    private int blend(int original, int target) {
        int amount = strength;
        return Color.argb(Color.alpha(original),
            Color.red(original) + (Color.red(target) - Color.red(original)) * amount / 100,
            Color.green(original) + (Color.green(target) - Color.green(original)) * amount / 100,
            Color.blue(original) + (Color.blue(target) - Color.blue(original)) * amount / 100);
    }

    @Override public int onColour(int original) {
        if (Color.alpha(original) < 80 || strength == 0) return original;
        int r = Color.red(original), g = Color.green(original), b = Color.blue(original);
        if (backgroundEnabled && r <= 48 && g <= 48 && b <= 48) return blend(original, backgroundColour);
        if (textEnabled && r >= 238 && g >= 238 && b >= 238) return blend(original, textColour);
        return original;
    }
}