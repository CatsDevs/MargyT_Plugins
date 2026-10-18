package dev.aeroglass;

import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.Shader;
import android.graphics.ColorFilter;
import android.graphics.drawable.Drawable;

/**
 * The Aero sheen: three soft gradients drawn over a screen.
 *
 * Shaders are built when the size or the strength changes, never in draw(),
 * so drawing costs three rectangles and no allocation.
 */
final class Glare extends Drawable {
    /** Multiplier per level; index 0 is unused. */
    private static final float[] STRENGTH = {0f, 0.55f, 1f, 1.5f};

    private final Paint gloss = new Paint();
    private final Paint sheen = new Paint();
    private final Paint glow = new Paint();
    private int level = 2;
    private boolean ready;

    void setLevel(int wanted) {
        if (wanted < 1) wanted = 1;
        if (wanted > 3) wanted = 3;
        if (wanted == level) return;
        level = wanted;
        rebuild(getBounds());
        invalidateSelf();
    }

    @Override
    protected void onBoundsChange(Rect bounds) {
        rebuild(bounds);
    }

    private void rebuild(Rect b) {
        int w = b.width();
        int h = b.height();
        if (w <= 0 || h <= 0) {
            ready = false;
            return;
        }
        float k = STRENGTH[level];
        // glossy highlight across the top, white fading out
        gloss.setShader(new LinearGradient(0, b.top, 0, b.top + h * 0.40f,
                argb(0.16f * k, 255, 255, 255), argb(0f, 255, 255, 255),
                Shader.TileMode.CLAMP));
        // Aero blue reflection falling diagonally from the top-left corner
        sheen.setShader(new LinearGradient(b.left, b.top, b.left + w * 0.90f, b.top + h * 0.55f,
                argb(0.12f * k, 58, 160, 232), argb(0f, 58, 160, 232),
                Shader.TileMode.CLAMP));
        // cyan glow rising from the bottom edge
        glow.setShader(new LinearGradient(0, b.bottom, 0, b.bottom - h * 0.30f,
                argb(0.14f * k, 90, 220, 255), argb(0f, 90, 220, 255),
                Shader.TileMode.CLAMP));
        ready = true;
    }

    private static int argb(float alpha, int r, int g, int b) {
        if (alpha > 1f) alpha = 1f;
        if (alpha < 0f) alpha = 0f;
        return ((int) (alpha * 255f + 0.5f) << 24) | (r << 16) | (g << 8) | b;
    }

    @Override
    public void draw(Canvas canvas) {
        if (!ready) return;
        Rect b = getBounds();
        canvas.drawRect(b, sheen);
        canvas.drawRect(b, gloss);
        canvas.drawRect(b, glow);
    }

    @Override
    public void setAlpha(int alpha) {
        gloss.setAlpha(alpha);
        sheen.setAlpha(alpha);
        glow.setAlpha(alpha);
    }

    @Override
    public void setColorFilter(ColorFilter filter) {
        gloss.setColorFilter(filter);
        sheen.setColorFilter(filter);
        glow.setColorFilter(filter);
    }

    @Override
    public int getOpacity() {
        return PixelFormat.TRANSLUCENT;
    }
}
