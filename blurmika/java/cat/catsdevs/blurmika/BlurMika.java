package cat.catsdevs.blurmika;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;

import cat.narezany.margyt.plugin.MargyPlugin;

/**
 * BlurMika 1.1.0 — Frutiger Aero-inspired screen transitions.
 *
 * Uses MargyT's API 2 onScreen callback to react to in-app screen changes,
 * and Activity lifecycle as a fallback for app entry / newly created screens.
 * It animates existing content only; it does not replace TikTok's UI.
 */
public final class BlurMika extends MargyPlugin {
    private static final long DURATION_MS = 360L;
    private static final float START_ALPHA = 0.0f;
    private static final float START_SCALE = 0.975f;
    private static final float START_TRANSLATION_DP = 12.0f;

    private static final Map<Activity, String> lastScreens =
            Collections.synchronizedMap(new WeakHashMap<Activity, String>());
    private static final Map<Activity, Long> lastAnimations =
            Collections.synchronizedMap(new WeakHashMap<Activity, Long>());

    @Override
    public void onStart(Context context) {
        margyt().log("BlurMika 1.1.0 loaded. Aero screen transitions enabled.");
    }

    @Override
    public void onActivityCreated(final Activity activity) {
        if (activity == null) return;
        animateIfRecentAnimationMissing(activity, 0L);
    }

    @Override
    public void onScreen(final Activity activity, final String name) {
        if (activity == null) return;
        String safeName = name == null ? "" : name;
        String previous = lastScreens.get(activity);
        if (safeName.equals(previous)) return;
        lastScreens.put(activity, safeName);
        animateIfRecentAnimationMissing(activity, 650L);
    }

    private void animateIfRecentAnimationMissing(final Activity activity, long dedupeWindowMs) {
        Long last = lastAnimations.get(activity);
        long now = android.os.SystemClock.uptimeMillis();
        if (last != null && now - last.longValue() < dedupeWindowMs) return;
        lastAnimations.put(activity, Long.valueOf(now));

        final View content = activity.findViewById(android.R.id.content);
        if (content == null) return;

        content.post(new Runnable() {
            @Override
            public void run() {
                if (activity.isFinishing() || content.getWidth() <= 0 || content.getHeight() <= 0) {
                    return;
                }

                float density = content.getResources().getDisplayMetrics().density;
                content.animate().cancel();
                content.setAlpha(START_ALPHA);
                content.setScaleX(START_SCALE);
                content.setScaleY(START_SCALE);
                content.setTranslationY(START_TRANSLATION_DP * density);
                content.animate()
                        .alpha(1.0f)
                        .scaleX(1.0f)
                        .scaleY(1.0f)
                        .translationY(0.0f)
                        .setDuration(DURATION_MS)
                        .setInterpolator(new DecelerateInterpolator(1.6f))
                        .withEndAction(new Runnable() {
                            @Override
                            public void run() {
                                content.setAlpha(1.0f);
                                content.setScaleX(1.0f);
                                content.setScaleY(1.0f);
                                content.setTranslationY(0.0f);
                            }
                        })
                        .start();
            }
        });
    }

    @Override
    public void onStop() {
        lastScreens.clear();
        lastAnimations.clear();
    }
}
