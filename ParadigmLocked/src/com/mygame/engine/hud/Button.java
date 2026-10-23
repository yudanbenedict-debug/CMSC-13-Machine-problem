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
            return;
        }

        Color fill = !enabled
                ? new Color(70, 70, 78)
                : pressed
                ? new Color(120, 178, 176)
                : hovered
                ? new Color(76, 126, 151)
                : new Color(36, 48, 62);
        g.setColor(fill);
        g.fillRoundRect(bounds.x, bounds.y, bounds.width, bounds.height, 12, 12);
        g.setColor(hovered && enabled ? new Color(178, 236, 224) : new Color(120, 144, 156));
        g.drawRoundRect(bounds.x, bounds.y, bounds.width, bounds.height, 12, 12);
        g.setColor(enabled ? Color.WHITE : new Color(160, 160, 160));
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 22));
        int textWidth = g.getFontMetrics().stringWidth(label);
        int textX = bounds.x + (bounds.width - textWidth) / 2;
        int textY = bounds.y + (bounds.height - g.getFontMetrics().getHeight()) / 2 + g.getFontMetrics().getAscent();
        g.drawString(label, textX, textY);
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
