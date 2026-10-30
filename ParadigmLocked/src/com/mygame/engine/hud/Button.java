package com.mygame.engine.hud;

import com.mygame.engine.core.AssetCache;
import com.mygame.engine.core.SoundManager;
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
    private final boolean backgroundVisible;
    private boolean hovered;
    private boolean pressed;
    private boolean enabled = true;

    public Button(int x, int y, int width, int height, String label, String assetKey, Runnable action) {
        this(x, y, width, height, label, assetKey, action, true);
    }

    public Button(int x, int y, int width, int height, String label, String assetKey,
                  Runnable action, boolean backgroundVisible) {
        this.bounds = new Rectangle(x, y, width, height);
        this.label = label;
        this.assetKey = assetKey;
        this.action = action;
        this.backgroundVisible = backgroundVisible;
    }

    public void draw(Graphics2D g) {
        BufferedImage image = assetKey == null ? null : AssetCache.get(assetKey);
        if (image != null) {
            g.drawImage(image, bounds.x, bounds.y, bounds.width, bounds.height, null);
        } else if (backgroundVisible && label != null) {
            TemporaryUiRenderer.drawButton(g, bounds, "", hovered, pressed, enabled);
        }
        if (label != null && !label.isEmpty()) {
            Color color = enabled ? Color.WHITE : new Color(160, 160, 160);
            UiText.drawInBox(g, label, UiFontManager.dorris(22, Font.BOLD), bounds, color,
                hovered ? UiText.DEFAULT_GLOW : null);
        }
    }

    public void updateHover(Point logicalPoint) {
        boolean nowHovered = enabled && bounds.contains(logicalPoint);
        if (nowHovered && !hovered) {
            SoundManager.playMenuHover();
        }
        hovered = nowHovered;
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

    public boolean isPressed() {
        return pressed;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        if (!enabled) {
            hovered = false;
            pressed = false;
        }
    }
}
