package cat.catsdevs.blurmika;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;

import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;

import cat.narezany.margyt.plugin.MargyPlugin;

/**
 * BlurMika — soft Frutiger Aero-inspired screen entrance motion.
 *
 * The public MargyT plugin API has lifecycle callbacks but no dedicated
 * navigation-transition hook. This animates each newly created Activity's
 * content view, which gives app-entry and activity-change motion without
 * replacing the app's UI or requiring extra permissions.
 */
public final class BlurMika extends MargyPlugin {
    private static final long DURATION_MS = 380L;
    private static final Map<Activity, Boolean> animatedActivities =
            Collections.synchronizedMap(new WeakHashMap<Activity, Boolean>());

    @Override
    public void onStart(Context context) {
        margyt().log("BlurMika 1.0.0 loaded. Aero motion enabled.");
    }

    @Override
    public void onActivityCreated(final Activity activity) {
        if (activity == null || animatedActivities.containsKey(activity)) return;
        animatedActivities.put(activity, Boolean.TRUE);

        final View content = activity.findViewById(android.R.id.content);
        if (content == null) return;

        content.post(new Runnable() {
            @Override
            public void run() {
                if (activity.isFinishing() || content.getWidth() == 0 || content.getHeight() == 0) {
                    return;
                }

                float density = content.getResources().getDisplayMetrics().density;
                AnimationSet entrance = new AnimationSet(true);
                entrance.setInterpolator(new DecelerateInterpolator(1.7f));
                entrance.setDuration(DURATION_MS);
                entrance.setFillAfter(false);

                AlphaAnimation fade = new AlphaAnimation(0.0f, 1.0f);
                fade.setDuration(DURATION_MS);

                ScaleAnimation scale = new ScaleAnimation(
                        0.965f, 1.0f, 0.965f, 1.0f,
                        Animation.RELATIVE_TO_SELF, 0.5f,
                        Animation.RELATIVE_TO_SELF, 0.5f);
                scale.setDuration(DURATION_MS);

                TranslateAnimation floatUp = new TranslateAnimation(
                        0.0f, 0.0f, 14.0f * density, 0.0f);
                floatUp.setDuration(DURATION_MS);

                entrance.addAnimation(fade);
                entrance.addAnimation(scale);
                entrance.addAnimation(floatUp);
                content.startAnimation(entrance);
            }
        });
    }

    @Override
    public void onStop() {
        animatedActivities.clear();
    }
}
