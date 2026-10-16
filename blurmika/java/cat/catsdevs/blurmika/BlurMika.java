package cat.catsdevs.blurmika;

import android.app.Activity;
import android.content.Context;
import android.view.View;

import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;

import cat.narezany.margyt.plugin.MargyPlugin;

/**
 * BlurMika 1.3.0 — uses the bundled Motion helper for Frutiger Aero-style
 * screen entrances while respecting Android's reduced-motion settings.
 */
public final class BlurMika extends MargyPlugin {
    private static final long RESUME_DEDUPE_MS = 650L;
    private static final Map<Activity, Long> lastAnimations =
            Collections.synchronizedMap(new WeakHashMap<Activity, Long>());

    @Override
    public void onStart(Context context) {
        margyt().log("BlurMika 1.3.0 loaded. Motion helper connected.");
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
        if (activity.isFinishing() || isDestroyed(activity)) return;

        long now = android.os.SystemClock.uptimeMillis();
        Long previous = lastAnimations.get(activity);
        if (!newlyCreated && previous != null
                && now - previous.longValue() < RESUME_DEDUPE_MS) return;

        final View content = activity.findViewById(android.R.id.content);
        if (content == null) return;
        lastAnimations.put(activity, Long.valueOf(now));

        content.post(() -> {
            if (activity.isFinishing() || isDestroyed(activity)
                    || !content.isAttachedToWindow()) return;

            if (content.getWidth() <= 0 || content.getHeight() <= 0) {
                content.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
                    @Override
                    public void onLayoutChange(View view, int left, int top, int right, int bottom,
                                               int oldLeft, int oldTop, int oldRight, int oldBottom) {
                        if (view.getWidth() <= 0 || view.getHeight() <= 0) return;
                        view.removeOnLayoutChangeListener(this);
                        if (!activity.isFinishing() && !isDestroyed(activity)
                                && view.isAttachedToWindow()) Motion.rise(view);
                    }
                });
                return;
            }

            Motion.rise(content);
        });
    }

    private static boolean isDestroyed(Activity activity) {
        return android.os.Build.VERSION.SDK_INT >= 17 && activity.isDestroyed();
    }

    @Override
    public void onStop() {
        lastAnimations.clear();
    }
}
