package cat.catsdevs.background;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.text.InputType;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Switch;

import cat.narezany.margyt.plugin.MargyPlugin;

/** Configurable neutral interface palette for MargyT API 4. */
public final class CustomBackgroundPlugin extends MargyPlugin {
    private static final String KEY_ENABLED = "theme_enabled";
    private static final String KEY_BG = "theme_background";
    private static final String KEY_SURFACE = "theme_surface";
    private static final String KEY_TEXT = "theme_text";

    private static final int DEFAULT_BG = Color.rgb(24, 25, 30);
    private static final int DEFAULT_SURFACE = Color.rgb(43, 45, 53);
    private static final int DEFAULT_TEXT = Color.rgb(242, 243, 247);

    private static volatile boolean enabled = true;
    private static volatile int background = DEFAULT_BG;
    private static volatile int surface = DEFAULT_SURFACE;
    private static volatile int text = DEFAULT_TEXT;

    @Override
    public void onStart(android.content.Context context) {
        loadSettings();
        margyt().log("Color Studio loaded");
    }

    @Override
    public void onAccountChanged(String uid) {
        // Keep palettes separate between accounts and the signed-out guest.
        loadSettings();
    }

    private SharedPreferences settings() {
        return margyt().accountPrefs();
    }

    private void loadSettings() {
        try {
            SharedPreferences p = settings();
            enabled = p.getBoolean(KEY_ENABLED, true);
            background = p.getInt(KEY_BG, DEFAULT_BG);
            surface = p.getInt(KEY_SURFACE, DEFAULT_SURFACE);
            text = p.getInt(KEY_TEXT, DEFAULT_TEXT);
        } catch (Throwable error) {
            enabled = false;
            margyt().log("Color Studio settings could not be loaded: " + error);
        }
    }

    @Override
    public boolean onOpenSettings(Activity activity) {
        if (activity == null || activity.isFinishing()) return false;
        showSettings(activity);
        return true;
    }

    private void showSettings(Activity activity) {
        final SharedPreferences p = settings();
        LinearLayout layout = new LinearLayout(activity);
        layout.setOrientation(LinearLayout.VERTICAL);
        int pad = (int) (18 * activity.getResources().getDisplayMetrics().density);
        layout.setPadding(pad, pad / 2, pad, 0);

        Switch toggle = new Switch(activity);
        toggle.setText("Enable theme");
        toggle.setTextSize(16);
        toggle.setChecked(p.getBoolean(KEY_ENABLED, true));
        layout.addView(toggle, new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        EditText bg = colourField(activity, p.getInt(KEY_BG, DEFAULT_BG));
        EditText card = colourField(activity, p.getInt(KEY_SURFACE, DEFAULT_SURFACE));
        EditText fg = colourField(activity, p.getInt(KEY_TEXT, DEFAULT_TEXT));

        addLabel(activity, layout, "Background HEX");
        layout.addView(bg);
        addLabel(activity, layout, "Cards / dividers HEX");
        layout.addView(card);
        addLabel(activity, layout, "Text HEX");
        layout.addView(fg);

        TextView note = new TextView(activity);
        note.setText("Use #RRGGBB. Only near-grey interface colours are changed. MargyT's public colour hook cannot reliably distinguish text, backgrounds and icons.");
        note.setTextSize(12);
        note.setPadding(0, pad / 2, 0, 0);
        layout.addView(note);

        new AlertDialog.Builder(activity)
            .setTitle("Color Studio")
            .setView(layout)
            .setNegativeButton("Cancel", null)
            .setNeutralButton("Reset", (dialog, which) -> {
                p.edit().putBoolean(KEY_ENABLED, true)
                    .putInt(KEY_BG, DEFAULT_BG)
                    .putInt(KEY_SURFACE, DEFAULT_SURFACE)
                    .putInt(KEY_TEXT, DEFAULT_TEXT).apply();
                loadSettings();
                margyt().log("Color Studio palette reset");
            })
            .setPositiveButton("Save", (dialog, which) -> {
                Integer newBg = parseColour(bg.getText().toString());
                Integer newSurface = parseColour(card.getText().toString());
                Integer newText = parseColour(fg.getText().toString());
                if (newBg == null || newSurface == null || newText == null) {
                    margyt().log("Color Studio: invalid HEX. Use #RRGGBB.");
                    return;
                }
                p.edit().putBoolean(KEY_ENABLED, toggle.isChecked())
                    .putInt(KEY_BG, newBg)
                    .putInt(KEY_SURFACE, newSurface)
                    .putInt(KEY_TEXT, newText).apply();
                loadSettings();
                margyt().log("Color Studio settings saved");
            })
            .show();
    }

    private static void addLabel(Activity activity, LinearLayout layout, String value) {
        TextView label = new TextView(activity);
        label.setText(value);
        label.setTextSize(13);
        label.setPadding(0, 12, 0, 4);
        layout.addView(label);
    }

    private static EditText colourField(Activity activity, int colour) {
        EditText field = new EditText(activity);
        field.setSingleLine(true);
        field.setHint("#RRGGBB");
        field.setText(String.format("#%06X", 0xFFFFFF & colour));
        field.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS);
        return field;
    }

    private static Integer parseColour(String raw) {
        if (raw == null) return null;
        String value = raw.trim();
        if (!value.startsWith("#")) value = "#" + value;
        if (!value.matches("#[0-9a-fA-F]{6}")) return null;
        try {
            return Color.parseColor(value);
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }

    @Override
    public int onColour(int original) {
        if (!enabled) return original;
        int alpha = Color.alpha(original);
        if (alpha < 80) return original;

        int r = Color.red(original);
        int g = Color.green(original);
        int b = Color.blue(original);
        int max = Math.max(r, Math.max(g, b));
        int min = Math.min(r, Math.min(g, b));

        // Preserve saturated colours such as brand accents, icons and video.
        if (max - min > 18) return original;

        float level = (0.2126f * r + 0.7152f * g + 0.0722f * b) / 255f;
        int mapped = level < 0.5f
            ? blend(background, surface, level * 2f)
            : blend(surface, text, (level - 0.5f) * 2f);
        return Color.argb(alpha, Color.red(mapped), Color.green(mapped), Color.blue(mapped));
    }

    private static int blend(int from, int to, float amount) {
        float t = Math.max(0f, Math.min(1f, amount));
        int r = Math.round(Color.red(from) + (Color.red(to) - Color.red(from)) * t);
        int g = Math.round(Color.green(from) + (Color.green(to) - Color.green(from)) * t);
        int b = Math.round(Color.blue(from) + (Color.blue(to) - Color.blue(from)) * t);
        return Color.rgb(r, g, b);
    }
}
