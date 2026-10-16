package cat.catsdevs.blurmika;

import android.content.Context;
import android.os.Build;
import android.provider.Settings;
import android.animation.ValueAnimator;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

/** Motion helpers adapted for BlurMika; animations respect Android's motion settings. */
final class Motion {
    private Motion() {}

    static boolean enabled(Context context) {
        try {
            if (Build.VERSION.SDK_INT >= 26) return ValueAnimator.areAnimatorsEnabled();
            return Settings.Global.getFloat(context.getContentResolver(),
                    Settings.Global.ANIMATOR_DURATION_SCALE, 1f) > 0f;
        } catch (Throwable ignored) {
            return true;
        }
    }

    /** Frutiger Aero-style entrance: fade, float upward and gently scale into place. */
    static void rise(final View view) {
        if (view == null) return;
        view.animate().withEndAction(null).cancel();
        if (!enabled(view.getContext())) {
            reset(view);
            return;
        }
        view.setAlpha(0f);
        view.setScaleX(0.975f);
        view.setScaleY(0.975f);
        view.setTranslationY(Math.max(view.getHeight(), 1) * 0.035f);
        view.post(() -> {
            if (!view.isAttachedToWindow()) return;
            view.animate().alpha(1f).scaleX(1f).scaleY(1f).translationY(0f)
                    .setDuration(320L)
                    .setInterpolator(new DecelerateInterpolator(1.4f))
                    .withEndAction(() -> reset(view))
                    .start();
        });
    }

    /** Directional section transition for future in-plugin settings/navigation surfaces. */
    static void navigate(final View view, final float direction, final Runnable replace) {
        if (view == null || replace == null) return;
        view.animate().withEndAction(null).cancel();
        if (!enabled(view.getContext())) {
            replace.run();
            return;
        }
        view.animate().alpha(0.72f)
                .translationX(-direction * 6f * view.getResources().getDisplayMetrics().density)
                .setDuration(90L)
                .withEndAction(() -> {
                    replace.run();
                    enter(view, direction);
                }).start();
    }

    static void enter(View view, float direction) {
        if (view == null) return;
        view.animate().withEndAction(null).cancel();
        if (!enabled(view.getContext())) {
            reset(view);
            return;
        }
        view.setAlpha(0.65f);
        view.setTranslationX(direction * 12f * view.getResources().getDisplayMetrics().density);
        view.animate().alpha(1f).translationX(0f).setDuration(180L)
                .setInterpolator(new DecelerateInterpolator()).start();
    }

    private static void reset(View view) {
        view.setAlpha(1f);
        view.setScaleX(1f);
        view.setScaleY(1f);
        view.setTranslationX(0f);
        view.setTranslationY(0f);
    }
}
