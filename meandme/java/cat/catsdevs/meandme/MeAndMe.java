package cat.catsdevs.meandme;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;

import java.util.Calendar;

import cat.narezany.margyt.plugin.MargyPlugin;

/**
 * MeAndMe 1.1.0: automatic day/night colour adjustment.
 * The public build API currently does not expose the settings callbacks
 * found in the supplied classes.jar, so this build uses automatic mode.
 */
public final class MeAndMe extends MargyPlugin {
    private static volatile boolean nightMode = true;

    @Override
    public void onStart(Context context) {
        updateMode();
        margyt().log("MeAndMe 1.1.0 loaded. Automatic day/night mode enabled.");
    }

    @Override
    public void onActivityResumed(Activity activity) {
        updateMode();
    }

    private void updateMode() {
        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        // Day theme from 07:00 to 18:59; dark theme from 19:00 to 06:59.
        nightMode = hour < 7 || hour >= 19;
    }

    @Override
    public int onColour(int original) {
        int alpha = Color.alpha(original);
        if (alpha < 80) return original;

        int red = Color.red(original);
        int green = Color.green(original);
        int blue = Color.blue(original);

        // The public colour hook cannot distinguish backgrounds from text/icons.
        // Restrict changes to near-black and near-white colours.
        if (!nightMode && red <= 38 && green <= 38 && blue <= 38) {
            return Color.argb(alpha, 245, 245, 245);
        }

        if (nightMode && red >= 232 && green >= 232 && blue >= 232) {
            return Color.argb(alpha, 24, 24, 27);
        }

        return original;
    }
}
