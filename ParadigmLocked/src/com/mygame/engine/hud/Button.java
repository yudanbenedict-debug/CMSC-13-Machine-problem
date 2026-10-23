package com.mygame.engine.hud;

import com.mygame.engine.core.AssetCache;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;

public class Button {
    private final Rectangle bounds;
    private final String label;
    private final String assetKey;
    private final Runnable action;
    private boolean hovered;
    private boolean pressed;
    private boolean enabled = true;

    public Button(int x, int y, int width, int height, String label, String assetKey, Runnable action) {
        this.bounds = new Rectangle(x, y, width, height);
        this.label = label;
        this.assetKey = assetKey;
        this.action = action;
    }

    public void draw(Graphics2D g) {
        BufferedImage image = assetKey == null ? null : AssetCache.get(assetKey);
        if (image != null) {
            g.drawImage(image, bounds.x, bounds.y, bounds.width, bounds.height, null);
            g.setColor(Color.WHITE);
            g.setFont(UiFontManager.dorris(22, Font.BOLD));
            int textWidth = g.getFontMetrics().stringWidth(label);
            int textY = bounds.y + (bounds.height - g.getFontMetrics().getHeight()) / 2
                    + g.getFontMetrics().getAscent();
            g.drawString(label, bounds.x + (bounds.width - textWidth) / 2, textY);
            return;
        }

        TemporaryUiRenderer.drawButton(g, bounds, label, hovered, pressed, enabled);
    }

    public void updateHover(Point logicalPoint) {
        hovered = enabled && bounds.contains(logicalPoint);
    }

    public void press(Point logicalPoint) {
        pressed = enabled && bounds.contains(logicalPoint);
    }

    public void release(Point logicalPoint) {
        boolean shouldActivate = pressed && enabled && bounds.contains(logicalPoint);
        pressed = false;
        if (shouldActivate && action != null) {
            action.run();
        }
    }

    public Rectangle getBounds() {
        return new Rectangle(bounds);
    }

    public boolean isHovered() {
        return hovered;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        if (!enabled) {
            hovered = false;
            pressed = false;
        }
    }
}
