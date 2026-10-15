package cat.catsdevs.background;

import android.app.Activity;
import android.app.AlertDialog;
import android.graphics.Color;
import android.text.InputType;
import android.widget.EditText;

import cat.narezany.margyt.plugin.MargyPlugin;

public final class CustomBackgroundPlugin extends MargyPlugin {
    private static final String PREF_COLOUR = "background_colour";
    private static final int DEFAULT_COLOUR = Color.rgb(32, 33, 36);
    private static volatile int selectedColour = DEFAULT_COLOUR;
    private static boolean dialogShown = false;

    @Override
    public void onStart(android.content.Context context) {
        selectedColour = margyt().prefs().getInt(PREF_COLOUR, DEFAULT_COLOUR);
        margyt().log("Custom Background loaded. Current colour: " +
            String.format("#%06X", 0xFFFFFF & selectedColour));
    }

    @Override
    public void onActivityResumed(Activity activity) {
        if (dialogShown) return;
        dialogShown = true;
        activity.runOnUiThread(() -> showColourPicker(activity));
    }

    private void showColourPicker(Activity activity) {
        String[] options = {
            "Default dark (#202124)",
            "Midnight blue (#172554)",
            "Forest (#16352B)",
            "Purple (#35204F)",
            "Custom HEX…"
        };
        new AlertDialog.Builder(activity)
            .setTitle("Custom Background")
            .setItems(options, (dialog, which) -> {
                switch (which) {
                    case 0: setColour(DEFAULT_COLOUR); break;
                    case 1: setColour(Color.rgb(23, 37, 84)); break;
                    case 2: setColour(Color.rgb(22, 53, 43)); break;
                    case 3: setColour(Color.rgb(53, 32, 79)); break;
                    case 4: showHexDialog(activity); break;
                    default: break;
                }
            })
            .setOnCancelListener(dialog -> margyt().log("Background colour unchanged"))
            .show();
    }

    private void showHexDialog(Activity activity) {
        EditText input = new EditText(activity);
        input.setSingleLine(true);
        input.setHint("#202124");
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS);

        new AlertDialog.Builder(activity)
            .setTitle("Custom background HEX")
            .setMessage("Enter a 6-digit HEX colour, for example #202124")
            .setView(input)
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Apply", (dialog, which) -> {
                String value = input.getText().toString().trim();
                if (!value.startsWith("#")) value = "#" + value;
                if (!value.matches("#[0-9a-fA-F]{6}")) {
                    margyt().log("Invalid HEX colour: " + value);
                    return;
                }
                setColour(Color.parseColor(value));
            })
            .show();
    }

    private void setColour(int colour) {
        selectedColour = colour;
        margyt().prefs().edit().putInt(PREF_COLOUR, colour).apply();
        margyt().log("Background colour set to " +
            String.format("#%06X", 0xFFFFFF & colour) +
            ". Restart TikTok to apply consistently.");
    }

    @Override
    public int onColour(int original) {
        int alpha = Color.alpha(original);
        if (alpha < 80) return original;

        if (Color.red(original) <= 48 &&
            Color.green(original) <= 48 &&
            Color.blue(original) <= 48) {
            int selected = selectedColour;
            return Color.argb(alpha, Color.red(selected),
                Color.green(selected), Color.blue(selected));
        }
        return original;
    }
}
