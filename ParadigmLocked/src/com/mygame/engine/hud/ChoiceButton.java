package com.mygame.engine.hud;

import com.mygame.engine.core.SoundManager;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;

public class ChoiceButton {
    /** How the choice should look after an answer is locked in. */
    public enum Result { NONE, CORRECT, WRONG }

    private final Rectangle bounds;
    private final String label;
    private String text;
    private final Runnable action;
    private boolean hovered;
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
        TemporaryUiRenderer.drawChoice(g, label, text, bounds, hovered, selected, nullified, result);
    }

    public void updateHover(Point logicalPoint) {
        boolean nowHovered = enabled && !nullified && bounds.contains(logicalPoint);
        if (nowHovered && !hovered) {
            SoundManager.playMenuHover();
        }
        hovered = nowHovered;
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

    public void setText(String text) {
        this.text = text;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public boolean isSelected() {
        return selected;
    }

    /** NullSlinger removed this (wrong) choice: drawn crossed out and no longer clickable. */
    public void setNullified(boolean nullified) {
        this.nullified = nullified;
        if (nullified) {
            hovered = false;
            pressed = false;
            selected = false;
        }
    }

    public boolean isNullified() {
        return nullified;
    }

    public void setResult(Result result) {
        this.result = result;
    }

    /** Clears per-question state (selection, nullify, result) and re-enables the button. */
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
            hovered = false;
            pressed = false;
        }
    }

    public boolean isHovered() {
        return hovered;
    }

    public String getText() {
        return text;
    }

    public String getLabel() {
        return label;
    }

    /** Long choice that doesn't fit its box: the gameplay screen shows a full preview on hover. */
    public boolean needsPreview() {
        return !nullified && TemporaryUiRenderer.isChoiceTruncated(text, bounds);
    }

    public Rectangle getBounds() {
        return new Rectangle(bounds);
    }
}
