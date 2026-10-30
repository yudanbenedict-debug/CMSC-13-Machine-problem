package com.mygame.ui;

import com.mygame.engine.core.AssetCache;
import com.mygame.engine.helpers.constants.GameConstants;
import com.mygame.engine.hud.UiFontManager;
import com.mygame.engine.hud.UiText;
import com.mygame.game.ParadigmSpace;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;

/*
    Everything drawn along the top of the gameplay screen:
    pause button (top-left), foremost-node status of both sides, timer + score, Paradigm Space minimap.
 */
public final class GameHud {
    private static final Color TEXT = new Color(222, 235, 230);
    private static final Color ALLY = new Color(0x7FB05A);
    private static final Color ENEMY = new Color(0xD05A5A);
    private static final Color GOLD = new Color(229, 190, 94);
    private static final Color DEAD = new Color(80, 80, 80);

    private GameHud() {
    }

    public static Rectangle pauseBounds() {
        return new Rectangle(GameConstants.PAUSE_BUTTON_X, GameConstants.PAUSE_BUTTON_Y,
            GameConstants.PAUSE_BUTTON_SIZE, GameConstants.PAUSE_BUTTON_SIZE);
    }

    public static void drawPauseButton(Graphics2D g, boolean hovered) {
        Rectangle bounds = pauseBounds();
        BufferedImage image = AssetCache.get("UI_PAUSE_BUTTON");
        if (hovered) {
            g.setColor(new Color(255, 236, 170, 60));
            g.fill(new Ellipse2D.Double(bounds.x - 4, bounds.y - 4, bounds.width + 8, bounds.height + 8));
        }
        if (image != null) {
            g.drawImage(image, bounds.x, bounds.y, bounds.width, bounds.height, null);
        } else {
            g.setColor(Color.GRAY);
            g.fillRect(bounds.x + 16, bounds.y + 12, 8, 32);
            g.fillRect(bounds.x + 32, bounds.y + 12, 8, 32);
        }
    }

    public static void drawNodeStatus(Graphics2D g, ParadigmSpace space, double time) {
        int x = GameConstants.HUD_NODE_STATUS_X;
        int y = GameConstants.HUD_NODE_STATUS_Y;
        boolean lowHp = space.ally().totalHp() == 1;
        drawSideRow(g, x, y, "YOU", space.ally(), ALLY, true, lowHp, time);
        drawSideRow(g, x, y + 44, "ENEMY", space.enemy(), ENEMY, false, false, time);
    }

    private static void drawSideRow(Graphics2D g, int x, int y, String who, ParadigmSpace.Side side,
                                    Color color, boolean pointsUp, boolean pulse, double time) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        Polygon triangle = pointsUp
            ? new Polygon(new int[] {x, x + 18, x + 9}, new int[] {y + 22, y + 22, y + 6}, 3)
            : new Polygon(new int[] {x, x + 18, x + 9}, new int[] {y + 6, y + 6, y + 22}, 3);
        g.setColor(color);
        g.fill(triangle);

        ParadigmSpace.Node node = side.foremost();
        String name = node == null ? "DESTROYED" : node.name.toUpperCase();
        Color nameColor = pulse && (Math.sin(time * 8) > 0) ? ENEMY : TEXT;
        UiText.draw(g, who + "  " + name, UiFontManager.dorris(16, Font.BOLD), x + 26, y + 17, nameColor,
            pulse ? new Color(0xD0, 0x5A, 0x5A, 150) : null);

        int barX = x + 26;
        int barY = y + 24;
        int barWidth = 230;
        g.setColor(new Color(255, 255, 255, 40));
        g.fill(new RoundRectangle2D.Double(barX, barY, barWidth, 8, 8, 8));
        if (node != null) {
            g.setColor(color);
            g.fill(new RoundRectangle2D.Double(barX, barY, barWidth * node.hp() / (double) node.maxHp, 8, 8, 8));
            UiText.draw(g, node.hp() + "/" + node.maxHp + " HP", UiFontManager.dorris(14, Font.BOLD),
                barX + barWidth + 10, barY + 8, TEXT);
        }
    }

    public static void drawTimerAndScore(Graphics2D g, double timeLeft, int score, boolean running) {
        int centerX = GameConstants.HUD_TIMER_CENTER_X;
        int seconds = (int) Math.ceil(Math.max(0, timeLeft));
        boolean urgent = running && seconds <= 10;
        UiText.drawCentered(g, "TIME LEFT: " + seconds, UiFontManager.luckiestGuy(28, Font.PLAIN),
            centerX, 48, urgent ? ENEMY : TEXT, urgent ? new Color(0xD0, 0x5A, 0x5A, 170) : null);
        UiText.drawCentered(g, score + " (POINTS)", UiFontManager.luckiestGuy(24, Font.PLAIN),
            centerX, 86, GOLD, null);
    }

    public static void drawMinimap(Graphics2D g, ParadigmSpace space, double time) {
        int x = GameConstants.MINIMAP_X;
        int y = GameConstants.MINIMAP_Y;
        int width = GameConstants.MINIMAP_WIDTH;
        int height = GameConstants.MINIMAP_HEIGHT;
        g.setColor(new Color(10, 10, 14, 170));
        g.fill(new RoundRectangle2D.Double(x, y, width, height, 14, 14));
        g.setColor(new Color(229, 190, 94, 180));
        g.setStroke(new BasicStroke(1.5f));
        g.draw(new RoundRectangle2D.Double(x, y, width, height, 14, 14));
        UiText.drawCentered(g, "PARADIGM SPACE", UiFontManager.dorris(13, Font.BOLD), x + width / 2f, y + 19,
            GOLD, null);

        ParadigmSpace.Node[] allyNodes = space.ally().nodes();
        ParadigmSpace.Node[] enemyNodes = space.enemy().nodes();
        int count = allyNodes.length + enemyNodes.length;
        double step = (width - 32) / (double) (count - 1);
        int laneY = y + 50;
        g.setColor(new Color(255, 255, 255, 60));
        g.drawLine(x + 16, laneY, x + width - 16, laneY);

        int allyFront = space.ally().foremostIndex();
        int enemyFront = space.enemy().foremostIndex();
        for (int slot = 0; slot < count; slot++) {
            boolean isAlly = slot < allyNodes.length;
            int nodeIndex = isAlly ? allyNodes.length - 1 - slot : slot - allyNodes.length;
            ParadigmSpace.Node node = isAlly ? allyNodes[nodeIndex] : enemyNodes[nodeIndex];
            boolean foremost = nodeIndex == (isAlly ? allyFront : enemyFront);
            double cx = x + 16 + step * slot;
            double radius = node.tier == ParadigmSpace.Tier.CORE ? 6.5 : 4.5;
            Color color = isAlly ? ALLY : ENEMY;
            if (node.destroyed()) {
                g.setColor(DEAD);
                g.draw(new Ellipse2D.Double(cx - radius, laneY - radius, radius * 2, radius * 2));
            } else {
                g.setColor(color);
                g.fill(new Ellipse2D.Double(cx - radius, laneY - radius, radius * 2, radius * 2));
            }
            if (foremost && !node.destroyed()) {
                double ring = radius + 3 + Math.sin(time * 5) * 1.5;
                g.setColor(new Color(255, 255, 255, 200));
                g.draw(new Ellipse2D.Double(cx - ring, laneY - ring, ring * 2, ring * 2));
            }
        }
        g.setStroke(new BasicStroke(1f));
        UiText.draw(g, "YOU", UiFontManager.dorris(12, Font.BOLD), x + 12, y + 80, ALLY);
        UiText.draw(g, "ENEMY", UiFontManager.dorris(12, Font.BOLD),
            x + width - 12 - UiText.width("ENEMY", UiFontManager.dorris(12, Font.BOLD)), y + 80, ENEMY);
    }
}
