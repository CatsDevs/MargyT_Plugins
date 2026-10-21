package cat.catsdevs.musicisland;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;

import cat.narezany.margyt.plugin.MargyPlugin;

/**
 * Music Island UI prototype.
 *
 * The MargyT plugin API currently does not expose a MediaSessionManager
 * notification-listener component. This version deliberately does not fake
 * track metadata; it provides the UI shell until the host exposes a supported
 * media-session bridge or notification-listener service.
 */
public final class MusicIsland extends MargyPlugin {
    private static final Map<Activity, View> islands =
            Collections.synchronizedMap(new WeakHashMap<Activity, View>());

    @Override
    public void onStart(Context context) {
        margyt().log("Music Island loaded. Media-session bridge is not available yet.");
    }

    @Override
    public void onActivityResumed(Activity activity) {
        if (activity == null || activity.isFinishing()) return;
        if (activity.getClass().getName().startsWith("cat.narezany.margyt.")) return;
        if (Build.VERSION.SDK_INT < 21) return;

        View content = activity.findViewById(android.R.id.content);
        if (!(content instanceof ViewGroup)) return;

        View old = islands.get(activity);
        if (old != null && old.getParent() == content) return;
        if (old != null && old.getParent() instanceof ViewGroup) {
            ((ViewGroup) old.getParent()).removeView(old);
        }

        FrameLayout island = createIsland(activity);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                dp(activity, 300), ViewGroup.LayoutParams.WRAP_CONTENT,
                Gravity.TOP | Gravity.CENTER_HORIZONTAL);
        params.topMargin = dp(activity, 12);

        try {
            ((ViewGroup) content).addView(island, params);
            islands.put(activity, island);
        } catch (Throwable error) {
            margyt().log("Music Island: unable to attach UI: " + error);
        }
    }

    private FrameLayout createIsland(final Activity activity) {
        final FrameLayout shell = new FrameLayout(activity);
        GradientDrawable bg = new GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM,
                new int[] { Color.rgb(35, 37, 43), Color.rgb(14, 15, 19) });
        bg.setCornerRadius(dp(activity, 28));
        bg.setStroke(dp(activity, 1), Color.argb(70, 255, 255, 255));
        shell.setBackground(bg);
        shell.setElevation(dp(activity, 12));
        shell.setPadding(dp(activity, 15), dp(activity, 12),
                dp(activity, 15), dp(activity, 12));

        LinearLayout row = new LinearLayout(activity);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setOrientation(LinearLayout.HORIZONTAL);

        TextView disc = new TextView(activity);
        disc.setText("♫");
        disc.setTextColor(Color.WHITE);
        disc.setTextSize(25);
        disc.setGravity(Gravity.CENTER);
        GradientDrawable discBg = new GradientDrawable(
                GradientDrawable.Orientation.TL_BR,
                new int[] { Color.rgb(107, 91, 220), Color.rgb(36, 173, 190) });
        discBg.setCornerRadius(dp(activity, 13));
        disc.setBackground(discBg);
        row.addView(disc, new LinearLayout.LayoutParams(dp(activity, 46), dp(activity, 46)));

        LinearLayout textColumn = new LinearLayout(activity);
        textColumn.setOrientation(LinearLayout.VERTICAL);
        textColumn.setPadding(dp(activity, 12), 0, dp(activity, 8), 0);
        LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);

        TextView title = new TextView(activity);
        title.setText("Music Island");
        title.setTextColor(Color.WHITE);
        title.setTextSize(14);
        title.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        title.setMaxLines(1);
        textColumn.addView(title);

        TextView subtitle = new TextView(activity);
        subtitle.setText("Waiting for media-session support");
        subtitle.setTextColor(Color.rgb(185, 189, 198));
        subtitle.setTextSize(11);
        subtitle.setMaxLines(2);
        textColumn.addView(subtitle);
        row.addView(textColumn, textParams);

        TextView status = new TextView(activity);
        status.setText("•••");
        status.setTextColor(Color.WHITE);
        status.setTextSize(18);
        status.setGravity(Gravity.CENTER);
        row.addView(status, new LinearLayout.LayoutParams(dp(activity, 28), dp(activity, 46)));

        shell.addView(row, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        shell.setOnClickListener(v -> {
            boolean expanded = shell.getTag() != null && Boolean.TRUE.equals(shell.getTag());
            shell.setTag(Boolean.valueOf(!expanded));
            subtitle.setText(expanded
                    ? "Waiting for media-session support"
                    : "UI ready • track metadata needs a host bridge");
            shell.setContentDescription(expanded
                    ? "Music Island collapsed"
                    : "Music Island details");
        });
        shell.setContentDescription("Music Island. Tap for integration status.");
        return shell;
    }

    private static int dp(Context context, int value) {
        return (int) (value * context.getResources().getDisplayMetrics().density + 0.5f);
    }

    @Override
    public void onStop() {
        synchronized (islands) {
            for (Map.Entry<Activity, View> entry : islands.entrySet()) {
                View view = entry.getValue();
                if (view != null && view.getParent() instanceof ViewGroup) {
                    ((ViewGroup) view.getParent()).removeView(view);
                }
            }
            islands.clear();
        }
    }
}
