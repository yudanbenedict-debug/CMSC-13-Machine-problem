package com.mygame.ui;

import com.mygame.engine.core.AssetCache;
import com.mygame.engine.core.SoundManager;
import com.mygame.engine.helpers.constants.GameConstants;
import com.mygame.engine.hud.UiFontManager;
import com.mygame.engine.hud.UiText;
import com.mygame.game.Consumable;
import com.mygame.game.ConsumableInventory;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.function.Predicate;

/*
    Consumable option panel: three rows of  [picture]  name  amount.
    - CONSUMABLE_BORDER frames the whole list.
    - Each row uses CONSUMABLE_CHOICES_BORDER, or CONSUMABLE_CHOICES_BORDER_SELECTED when selected.
    - Click a row (its name) once to select it (text glows), click it again to use it.
    - A depleted Scripter's Sacrifice shows a DISCARD button next to its row.
 */
public final class ConsumablePanel {
    private static final Color TEXT = new Color(222, 235, 230);
    private static final Color DIM = new Color(120, 120, 120);
    private static final Color GOLD = new Color(229, 190, 94);
    private static final Consumable[] ROWS = Consumable.values();

    private final ConsumableInventory inventory;
    private final Predicate<Consumable> useAction;
    private final Runnable discardAction;
    private Consumable selected;
    private int hoveredRow = -1;
    private boolean discardHovered;

    public ConsumablePanel(ConsumableInventory inventory, Predicate<Consumable> useAction,
                           Runnable discardAction) {
        this.inventory = inventory;
        this.useAction = useAction;
        this.discardAction = discardAction;
    }

    public void clearSelection() {
        selected = null;
    }

    public Consumable selected() {
        return selected;
    }

    private static Rectangle panelBounds() {
        return new Rectangle(GameConstants.CONSUMABLE_PANEL_X, GameConstants.CONSUMABLE_PANEL_Y,
            GameConstants.CONSUMABLE_PANEL_WIDTH, GameConstants.CONSUMABLE_PANEL_HEIGHT);
    }

    private static Rectangle rowBounds(int row) {
        Rectangle panel = panelBounds();
        // rows sit inside the two side ornaments of CONSUMABLE_BORDER
        return new Rectangle(panel.x + 42, panel.y + 22 + row * GameConstants.CONSUMABLE_ROW_HEIGHT,
            panel.width - 84, GameConstants.CONSUMABLE_ROW_HEIGHT);
    }

    private Rectangle discardBounds() {
        Rectangle row = rowBounds(Consumable.SCRIPTERS_SACRIFICE.ordinal());
        Rectangle panel = panelBounds();
        return new Rectangle(panel.x + panel.width + 6, row.y + 8, 120, 34);
    }

    private boolean discardVisible() {
        return inventory.scriptersDepleted();
    }

    public void draw(Graphics2D g) {
        Rectangle panel = panelBounds();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        drawFrame(g, panel);
        for (int row = 0; row < ROWS.length; row++) {
            drawRow(g, row, ROWS[row]);
        }
        if (discardVisible()) {
            Rectangle bounds = discardBounds();
            UiText.drawInBox(g, "DISCARD", UiFontManager.dorris(20, Font.BOLD), bounds,
                new Color(0xFA5555), discardHovered ? new Color(0xFA, 0x55, 0x55, 170) : null);
        }
    }

    /*
        CONSUMABLE_BORDER is a square (390x390) image with an ornament on each side. Stretching it to the
        wide panel squashed and blurred the ornaments, so each half is drawn with the SAME x/y scale
        (no distortion, only downscaled = sharp) and pinned to the panel's left / right edge.
     */
    private static void drawFrame(Graphics2D g, Rectangle panel) {
        BufferedImage frame = AssetCache.get("CONSUMABLE_BORDER");
        if (frame == null) {
            return;
        }
        int half = frame.getWidth() / 2;
        double scale = panel.height / (double) frame.getHeight();
        int drawnHalf = (int) Math.round(half * scale);
        g.drawImage(frame, panel.x, panel.y, panel.x + drawnHalf, panel.y + panel.height,
            0, 0, half, frame.getHeight(), null);
        g.drawImage(frame, panel.x + panel.width - drawnHalf, panel.y, panel.x + panel.width, panel.y + panel.height,
            half, 0, frame.getWidth(), frame.getHeight(), null);
    }

    private void drawRow(Graphics2D g, int row, Consumable consumable) {
        Rectangle bounds = rowBounds(row);
        boolean isSelected = consumable == selected;
        boolean available = inventory.canUse(consumable);
        BufferedImage border = AssetCache.get(isSelected
            ? "CONSUMABLE_CHOICES_BORDER_SELECTED" : "CONSUMABLE_CHOICES_BORDER");
        if (border != null) {
            g.drawImage(border, bounds.x, bounds.y, bounds.width, bounds.height, null);
        }

        int iconSize = 40;
        int iconY = bounds.y + 6;
        BufferedImage icon = AssetCache.get(consumable.imageKey);
        if (icon != null) {
            java.awt.Composite old = g.getComposite();
            if (!available) {
                g.setComposite(java.awt.AlphaComposite.getInstance(java.awt.AlphaComposite.SRC_OVER, 0.35f));
            }
            g.drawImage(icon, bounds.x + 6, iconY, iconSize, iconSize, null);
            g.setComposite(old);
        }

        Color color = available ? TEXT : DIM;
        Color glow = isSelected ? UiText.DEFAULT_GLOW
            : row == hoveredRow && available ? new Color(255, 236, 170, 140) : null;
        float baseline = iconY + iconSize / 2f + 7;
        UiText.draw(g, consumable.displayName, UiFontManager.dorris(21, Font.BOLD),
            bounds.x + iconSize + 18, baseline, color, glow);

        String amount = amountText(consumable);
        Font amountFont = UiFontManager.luckiestGuy(20, Font.PLAIN);
        UiText.draw(g, amount, amountFont, bounds.x + bounds.width - 10 - UiText.width(amount, amountFont),
            baseline, available ? GOLD : DIM, null);
    }

    private String amountText(Consumable consumable) {
        int count = inventory.count(consumable);
        if (consumable == Consumable.SCRIPTERS_SACRIFICE && inventory.scriptersDepleted()) {
            return "x0!";
        }
        return "x" + count;
    }

    /** One-line hint for the status bar while this panel is open. */
    public String statusText() {
        if (selected == null) {
            return "Pick a consumable. Click again to use it.";
        }
        if (selected == Consumable.SCRIPTERS_SACRIFICE && inventory.scriptersDepleted()) {
            return "Overused: still doubles damage, but hurts you every turn until discarded.";
        }
        return selected.displayName + ": " + selected.description + " Click again to use.";
    }

    public void mouseMoved(Point point) {
        int now = -1;
        for (int row = 0; row < ROWS.length; row++) {
            if (rowBounds(row).contains(point)) {
                now = row;
            }
        }
        if (now >= 0 && now != hoveredRow && inventory.canUse(ROWS[now])) {
            SoundManager.playMenuHover();
        }
        hoveredRow = now;
        discardHovered = discardVisible() && discardBounds().contains(point);
    }

    public void mousePressed(Point point) {
        if (discardVisible() && discardBounds().contains(point)) {
            discardAction.run();
            selected = null;
            return;
        }
        for (int row = 0; row < ROWS.length; row++) {
            if (!rowBounds(row).contains(point)) {
                continue;
            }
            Consumable consumable = ROWS[row];
            if (!inventory.canUse(consumable)) {
                selected = null;
                return;
            }
            if (selected == consumable) {
                if (useAction.test(consumable)) {
                    selected = null;
                }
            } else {
                selected = consumable;
            }
            return;
        }
    }
}
