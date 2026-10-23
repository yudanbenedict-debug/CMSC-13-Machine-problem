package com.mygame.engine.hud;

import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;

public class ChoiceButton {
    private final Rectangle bounds;
    private final String label;
    private String text;
    private final Runnable action;
    private boolean hovered;
    private boolean pressed;
    private boolean selected;
    private boolean enabled = true;

    public ChoiceButton(int x, int y, int width, int height, String label,
                        String text, Runnable action) {
        this.bounds = new Rectangle(x, y, width, height);
        this.label = label;
        this.text = text;
        this.action = action;
    }

    public void draw(Graphics2D g) {
        TemporaryUiRenderer.drawChoice(g, label, text, bounds, hovered, pressed, selected);
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

    public void setText(String text) {
        this.text = text;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        if (!enabled) {
            hovered = false;
            pressed = false;
        }
    }

    public Rectangle getBounds() {
        return new Rectangle(bounds);
    }
}
