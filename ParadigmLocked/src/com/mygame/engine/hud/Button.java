package com.mygame.engine.hud;

import com.mygame.engine.core.AssetCache;
import com.mygame.engine.core.Img;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;

public class Button {
    private final Rectangle bounds;
    private final String label;
    private final Img image;
    private final Runnable action;
    private final boolean backgroundVisible;
    private final Hover hover = new Hover();
    private boolean pressed;
    private boolean enabled = true;

    public Button(int x, int y, int width, int height, String label, Img image, Runnable action) {
        this(x, y, width, height, label, image, action, true);
    }

    public Button(int x, int y, int width, int height, String label, Img image,
                  Runnable action, boolean backgroundVisible) {
        this.bounds = new Rectangle(x, y, width, height);
        this.label = label;
        this.image = image;
        this.action = action;
        this.backgroundVisible = backgroundVisible;
    }

    public void draw(Graphics2D g) {
        BufferedImage art = image == null ? null : AssetCache.get(image);
        if (art != null) {
            g.drawImage(art, bounds.x, bounds.y, bounds.width, bounds.height, null);
        } else if (backgroundVisible && label != null) {
            UiRenderer.drawButton(g, bounds, "", hover.isHovered(), pressed, enabled);
        }
        if (label != null && !label.isEmpty()) {
            Color color = enabled ? Color.WHITE : new Color(160, 160, 160);
            UiText.drawInBox(g, label, UiFontManager.dorris(22, Font.BOLD), bounds, color,
                hover.isHovered() ? UiText.DEFAULT_GLOW : null);
        }
    }

    public void updateHover(Point logicalPoint) {
        hover.update(enabled && bounds.contains(logicalPoint));
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
        return hover.isHovered();
    }


    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        if (!enabled) {
            hover.clear();
            pressed = false;
        }
    }
}
