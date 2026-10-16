package cat.catsdevs.blurmika;

import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.provider.Settings;
import android.view.View;
import android.view.animation.PathInterpolator;

import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;

import cat.narezany.margyt.plugin.MargyPlugin;

/**
 * BlurMika 1.2.0 — Frutiger Aero-inspired screen motion.
 * Motion behavior is adapted locally because MargyT's internal Motion helper
 * is package-private and cannot be referenced directly from a third-party plugin.
 */
public final class BlurMika extends MargyPlugin {
    private static final long RESUME_DEDUPE_MS = 650L;
    private static final Map<Activity, Long> lastAnimations =
            Collections.synchronizedMap(new WeakHashMap<Activity, Long>());

    @Override
    public void onStart(Context context) {
        margyt().log("BlurMika 1.2.0 loaded. Motion-style transitions enabled.");
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
        if (!newlyCreated && previous != null && now - previous.longValue() < RESUME_DEDUPE_MS) {
            return;
        }

        final View content = activity.findViewById(android.R.id.content);
        if (content == null) return;
        lastAnimations.put(activity, Long.valueOf(now));

        content.post(() -> {
            if (activity.isFinishing() || isDestroyed(activity) || !content.isAttachedToWindow()) return;
            if (content.getWidth() <= 0 || content.getHeight() <= 0) {
                content.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
                    @Override
                    public void onLayoutChange(View view, int left, int top, int right, int bottom,
                                               int oldLeft, int oldTop, int oldRight, int oldBottom) {
                        if (view.getWidth() <= 0 || view.getHeight() <= 0) return;
                        view.removeOnLayoutChangeListener(this);
                        animateEntry(activity, view);
                    }
                });
                return;
            }
            animateEntry(activity, content);
        });
    }

    /** Motion.rise/enter behavior, respecting the device's reduced-motion setting. */
    private void animateEntry(Activity activity, View view) {
        if (activity.isFinishing() || isDestroyed(activity) || !view.isAttachedToWindow()) return;

        view.animate().withEndAction(null).cancel();
        if (!animationsEnabled(activity)) {
            view.setAlpha(1f);
            view.setScaleX(1f);
            view.setScaleY(1f);
            view.setTranslationX(0f);
            view.setTranslationY(0f);
            return;
        }

        float density = view.getResources().getDisplayMetrics().density;
        view.setAlpha(0f);
        view.setScaleX(0.985f);
        view.setScaleY(0.985f);
        view.setTranslationY(Math.max(view.getHeight(), 1));
        view.setTranslationX(5f * density);
        view.animate()
                .alpha(1f)
                .scaleX(1f)
                .scaleY(1f)
                .translationX(0f)
                .translationY(0f)
                .setDuration(280L)
                .setInterpolator(new PathInterpolator(0.2f, 0f, 0f, 1f))
                .withEndAction(() -> {
                    view.setAlpha(1f);
                    view.setScaleX(1f);
                    view.setScaleY(1f);
                    view.setTranslationX(0f);
                    view.setTranslationY(0f);
                })
                .start();
    }

    private static boolean animationsEnabled(Context context) {
        try {
            if (Build.VERSION.SDK_INT >= 26) return ValueAnimator.areAnimatorsEnabled();
            return Settings.Global.getFloat(context.getContentResolver(),
                    Settings.Global.ANIMATOR_DURATION_SCALE, 1f) > 0f;
        } catch (Throwable ignored) {
            return true;
        }
    }

    private static boolean isDestroyed(Activity activity) {
        return Build.VERSION.SDK_INT >= 17 && activity.isDestroyed();
    }

    @Override
    public void onStop() {
        lastAnimations.clear();
    }
}
