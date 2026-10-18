package dev.aeroglass;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.view.View;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import cat.narezany.margyt.plugin.MargyPlugin;

/**
 * Aero Glass 1.0 -- lays a Frutiger Aero sheen over every TikTok screen.
 *
 * It does not walk the view tree or touch TikTok's colours: one drawable is
 * added to each window's overlay, which draws on top and takes no touches.
 * That keeps it independent of TikTok's obfuscated layouts, and cheap.
 */
public final class AeroGlass extends MargyPlugin {
    private static final String KEY_ON = "glass_on";
    private static final String KEY_LEVEL = "glass_level";

    private final Handler main = new Handler(Looper.getMainLooper());
    private final Map<Activity, Dressed> dressed =
            Collections.synchronizedMap(new WeakHashMap<Activity, Dressed>());

    /** What was put on one window, kept so it can be taken off again. */
    private static final class Dressed {
        final WeakReference<View> decor;
        final Glare glare = new Glare();
        final View.OnLayoutChangeListener listener;

        Dressed(View view) {
            decor = new WeakReference<View>(view);
            listener = new View.OnLayoutChangeListener() {
                @Override
                public void onLayoutChange(View v, int l, int t, int r, int b,
                                           int ol, int ot, int or, int ob) {
                    glare.setBounds(0, 0, r - l, b - t);
                }
            };
            view.addOnLayoutChangeListener(listener);
            view.getOverlay().add(glare);
            if (view.getWidth() > 0 && view.getHeight() > 0) {
                glare.setBounds(0, 0, view.getWidth(), view.getHeight());
            }
        }

        void remove() {
            View view = decor.get();
            if (view == null) return;
            view.removeOnLayoutChangeListener(listener);
            view.getOverlay().remove(glare);
            view.invalidate();
        }
    }

    // ------------------------------------------------------------- the hooks

    @Override
    public void onStart(Context context) {
        margyt().log("Aero Glass 1.0 loaded.");
    }

    @Override
    public void onActivityCreated(Activity activity) {
        dress(activity);
    }

    @Override
    public void onActivityResumed(Activity activity) {
        dress(activity);
    }

    @Override
    public void onStop() {
        main.post(new Runnable() {
            @Override
            public void run() {
                for (Activity activity : snapshot()) undress(activity);
                dressed.clear();
            }
        });
    }

    // ------------------------------------------------------------- the sheen

    private void dress(Activity activity) {
        try {
            if (activity == null || activity.isFinishing()) return;
            if (Build.VERSION.SDK_INT < 18) return;   // no ViewOverlay before this
            // the mod's own windows are painted their own way
            if (activity.getClass().getName().startsWith("cat.narezany.margyt.")) return;
            if (!margyt().prefs().getBoolean(KEY_ON, true)) {
                undress(activity);
                return;
            }
            if (activity.getWindow() == null) return;
            View decor = activity.getWindow().getDecorView();
            if (decor == null) return;

            Dressed d = dressed.get(activity);
            if (d == null) {
                d = new Dressed(decor);
                dressed.put(activity, d);
            }
            d.glare.setStrength(margyt().prefs().getInt(KEY_LEVEL, 2));
        } catch (Throwable error) {
            // a hook that throws drops the plugin for the whole process
            margyt().log("Aero Glass: " + error);
        }
    }

    private void undress(Activity activity) {
        Dressed d = dressed.remove(activity);
        if (d != null) d.remove();
    }

    private List<Activity> snapshot() {
        synchronized (dressed) {
            return new ArrayList<Activity>(dressed.keySet());
        }
    }

    /** Re-apply the settings to every window already dressed, and the one in front. */
    private void refresh() {
        main.post(new Runnable() {
            @Override
            public void run() {
                try {
                    for (Activity activity : snapshot()) dress(activity);
                } catch (Throwable error) {
                    margyt().log("Aero Glass: " + error);
                }
            }
        });
    }

}
