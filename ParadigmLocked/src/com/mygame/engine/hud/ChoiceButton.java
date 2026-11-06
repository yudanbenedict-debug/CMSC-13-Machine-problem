package com.mygame.engine.hud;

import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;

public class ChoiceButton {
    public enum Result { NONE, CORRECT, WRONG }

    private final Rectangle bounds;
    private final String label;
    private String text;
    private final Runnable action;
    private final Hover hover = new Hover();
    private boolean pressed;
    private boolean selected;
    private boolean enabled = true;
    private boolean nullified;
    private Result result = Result.NONE;

    public ChoiceButton(int x, int y, int width, int height, String label,
                        String text, Runnable action) {
        this.bounds = new Rectangle(x, y, width, height);
        this.label = label;
        this.text = text;
        this.action = action;
    }

    public void draw(Graphics2D g) {
        UiRenderer.drawChoice(g, label, text, bounds, hover.isHovered(), selected, nullified, result);
    }

    public void updateHover(Point logicalPoint) {
        hover.update(enabled && !nullified && bounds.contains(logicalPoint));
    }

    public void press(Point logicalPoint) {
        pressed = enabled && !nullified && bounds.contains(logicalPoint);
    }

    public void release(Point logicalPoint) {
        boolean shouldActivate = pressed && enabled && !nullified && bounds.contains(logicalPoint);
        pressed = false;
        if (shouldActivate && action != null) {
            action.run();
        }
    }


    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public boolean isSelected() {
        return selected;
    }

    // NullSlinger: crossed out and unclickable.
    public void setNullified(boolean nullified) {
        this.nullified = nullified;
        if (nullified) {
            hover.clear();
            pressed = false;
            selected = false;
        }
    }


    public void setResult(Result result) {
        this.result = result;
    }

    public void reset(String newText) {
        text = newText;
        selected = false;
        nullified = false;
        result = Result.NONE;
        setEnabled(true);
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        if (!enabled) {
            hover.clear();
            pressed = false;
        }
    }

    public boolean isHovered() {
        return hover.isHovered();
    }

    public String getText() {
        return text;
    }

    public String getLabel() {
        return label;
    }

    public boolean needsPreview() {
        return !nullified && UiRenderer.isChoiceTruncated(text, bounds);
    }

    public Rectangle getBounds() {
        return new Rectangle(bounds);
    }
}
