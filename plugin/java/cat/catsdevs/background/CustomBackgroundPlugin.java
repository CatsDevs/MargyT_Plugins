package cat.catsdevs.background;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.text.InputType;
import android.widget.EditText;

import cat.narezany.margyt.plugin.MargyPlugin;

public final class CustomBackgroundPlugin extends MargyPlugin {
    private static final String PREF_COLOUR = "background_colour";
    private static final int DEFAULT_COLOUR = Color.rgb(32, 33, 36);

    @Override
    public void onStart(Context context) {
        margyt().addSettingsRow(
            "Dark background colour",
            "Choose a preset",
            () -> setColour(DEFAULT_COLOUR)
        );
        margyt().addSettingsRow(
            "Preset: Midnight blue",
            "#172554",
            () -> setColour(Color.rgb(23, 37, 84))
        );
        margyt().addSettingsRow(
            "Preset: Forest",
            "#16352B",
            () -> setColour(Color.rgb(22, 53, 43))
        );
        margyt().addSettingsRow(
            "Preset: Purple",
            "#35204F",
            () -> setColour(Color.rgb(53, 32, 79))
        );
        margyt().addSettingsRow(
            "Custom HEX",
            "Enter a colour such as #202124",
            this::showHexDialog
        );
    }

    private void setColour(int colour) {
        margyt().prefs().edit().putInt(PREF_COLOUR, colour).apply();
        margyt().log("Background colour set to " + String.format("#%06X", (0xFFFFFF & colour)));
        Activity activity = margyt().screen();
        if (activity != null) {
            margyt().showWindow("Custom Background", "Colour saved. Restart TikTok to apply it everywhere.");
        }
    }

    private void showHexDialog() {
        Activity activity = margyt().screen();
        if (activity == null) {
            margyt().log("Could not open HEX dialog: no active screen");
            return;
        }

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
                    margyt().showWindow("Invalid HEX", "Use six hexadecimal digits, for example #202124.");
                    return;
                }
                setColour(Color.parseColor(value));
            })
            .show();
    }

    @Override
    public int onColour(int original) {
        int alpha = Color.alpha(original);
        if (alpha < 80) return original;

        int red = Color.red(original);
        int green = Color.green(original);
        int blue = Color.blue(original);

        // The public plugin API does not identify whether a colour belongs to a
        // background or a foreground. Only replace near-black colours to reduce
        // the chance of recolouring text and icons.
        if (red <= 48 && green <= 48 && blue <= 48) {
            int selected = margyt().prefs().getInt(PREF_COLOUR, DEFAULT_COLOUR);
            return Color.argb(alpha, Color.red(selected), Color.green(selected), Color.blue(selected));
        }
        return original;
    }
}
