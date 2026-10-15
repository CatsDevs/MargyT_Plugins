package cat.catsdevs.meandme;

import android.app.Activity;
import android.graphics.Color;

import java.util.Calendar;

import cat.narezany.margyt.plugin.MargyPlugin;

/**
 * MeAndMe: experimental automatic day/night colour adjustment.
 * The public MargyT API has no render/frame hook for motion blur.
 */
public final class MeAndMe extends MargyPlugin {
    private static volatile boolean nightMode = true;
    private static final int DAY_BACKGROUND = Color.rgb(245, 245, 245);
    private static final int NIGHT_BACKGROUND = Color.rgb(24, 24, 27);

    @Override
    public void onStart(android.content.Context context) {
        updateMode();
        margyt().log("MeAndMe loaded. Automatic day/night mode enabled.");
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

        // MargyT's public hook does not tell backgrounds apart from text/icons.
        // Limit changes to near-black/near-white colours to reduce side effects.
        if (!nightMode && red <= 38 && green <= 38 && blue <= 38) {
            return Color.argb(alpha, Color.red(DAY_BACKGROUND),
                Color.green(DAY_BACKGROUND), Color.blue(DAY_BACKGROUND));
        }

        if (nightMode && red >= 232 && green >= 232 && blue >= 232) {
            return Color.argb(alpha, Color.red(NIGHT_BACKGROUND),
                Color.green(NIGHT_BACKGROUND), Color.blue(NIGHT_BACKGROUND));
        }

        return original;
    }
}
