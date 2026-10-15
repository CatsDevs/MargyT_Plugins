package cat.catsdevs.meandme;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.widget.Toast;

import java.util.Calendar;

import cat.narezany.margyt.plugin.MargyPlugin;

/**
 * MeAndMe: configurable day/night colour adjustment.
 * Motion Blur is not bundled in this version; it needs the separate engine
 * implementation from the supplied classes.jar.
 */
public final class MeAndMe extends MargyPlugin {
    private static final String PREF_THEME_MODE = "meandme_theme_mode";
    private static final int MODE_AUTO = 0;
    private static final int MODE_LIGHT = 1;
    private static final int MODE_DARK = 2;

    private static volatile boolean nightMode = true;

    @Override
    public void onStart(Context context) {
        updateMode();
        margyt().log("MeAndMe 1.1.0 loaded.");
    }

    @Override
    public void onActivityResumed(Activity activity) {
        updateMode();
    }

    @Override
    public boolean onOpenSettings(Activity activity) {
        try {
            SharedPreferences prefs = margyt().prefs();
            int current = prefs.getInt(PREF_THEME_MODE, MODE_AUTO);
            String[] choices = {
                "Автоматически (07:00–18:59 / 19:00–06:59)",
                "Всегда светлая",
                "Всегда тёмная"
            };

            new AlertDialog.Builder(activity)
                .setTitle("MeAndMe — тема")
                .setSingleChoiceItems(choices, current, (dialog, which) -> {
                    prefs.edit().putInt(PREF_THEME_MODE, which).apply();
                    updateMode();
                    dialog.dismiss();
                    Toast.makeText(activity,
                        which == MODE_AUTO ? "Автоматическая тема" :
                        which == MODE_LIGHT ? "Светлая тема" : "Тёмная тема",
                        Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Закрыть", null)
                .show();
        } catch (Exception e) {
            margyt().log("MeAndMe settings error: " + e);
        }
        return true;
    }

    @Override
    public void onSettingsChanged(String key) {
        updateMode();
    }

    private void updateMode() {
        int mode = MODE_AUTO;
        try {
            mode = margyt().prefs().getInt(PREF_THEME_MODE, MODE_AUTO);
        } catch (Exception ignored) {
            // Use automatic mode if preferences are not available yet.
        }

        if (mode == MODE_LIGHT) {
            nightMode = false;
        } else if (mode == MODE_DARK) {
            nightMode = true;
        } else {
            int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
            nightMode = hour < 7 || hour >= 19;
        }
    }

    @Override
    public int onColour(int original) {
        int alpha = Color.alpha(original);
        if (alpha < 80) return original;

        int red = Color.red(original);
        int green = Color.green(original);
        int blue = Color.blue(original);

        // The API does not identify whether a colour belongs to a background,
        // text or icon, so only near-black and near-white colours are adjusted.
        if (!nightMode && red <= 38 && green <= 38 && blue <= 38) {
            return Color.argb(alpha, 245, 245, 245);
        }
        if (nightMode && red >= 232 && green >= 232 && blue >= 232) {
            return Color.argb(alpha, 24, 24, 27);
        }
        return original;
    }
}
