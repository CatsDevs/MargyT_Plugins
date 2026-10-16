package cat.catsdevs.blurmika;

import android.app.Activity;
import android.content.Context;
import android.view.View;

import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;

import cat.narezany.margyt.plugin.MargyPlugin;

/**
 * BlurMika 1.2.0 — Frutiger Aero-inspired motion transitions.
 * Uses the Motion helper and respects Android's reduced/disabled animation setting.
 */
public final class BlurMika extends MargyPlugin {
    private static final long RESUME_DEDUPE_MS = 650L;
    private static final Map<Activity, Long> lastAnimations =
            Collections.synchronizedMap(new WeakHashMap<Activity, Long>());

    @Override
    public void onStart(Context context) {
        margyt().log("BlurMika 1.2.0 loaded. Motion transitions enabled.");
    }

    @Override
    public void onActivityCreated(final Activity activity) {
        if (activity != null) animateActivity(activity, true);
    }

    @Override
    public void onActivityResumed(final Activity activity) {
        if (activity != null) animateActivity(activity, false);
    }

    private void animateActivity(final Activity activity, boolean newlyCreated) {
        if (activity.isFinishing() || activity.isDestroyed()) return;
        long now = android.os.SystemClock.uptimeMillis();
        Long previous = lastAnimations.get(activity);
        if (!newlyCreated && previous != null && now - previous.longValue() < RESUME_DEDUPE_MS) return;

        final View content = activity.findViewById(android.R.id.content);
        if (content == null) return;
        lastAnimations.put(activity, Long.valueOf(now));

        content.post(() -> {
            if (activity.isFinishing() || activity.isDestroyed()
                    || !content.isAttachedToWindow() || content.getWidth() <= 0 || content.getHeight() <= 0) {
                return;
            }
            Motion.rise(content);
        });
    }

    @Override
    public void onStop() {
        lastAnimations.clear();
    }
}
