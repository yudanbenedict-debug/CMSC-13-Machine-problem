package com.mygame.engine.hud;

import com.mygame.engine.core.AssetCache;
import com.mygame.engine.helpers.constants.GameConstants;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Rectangle;

public final class TemporaryUiRenderer {
    private static final Color BACKGROUND = new Color(15, 22, 30);
    private static final Color TOP_BAR = new Color(28, 63, 78);
    private static final Color PANEL = new Color(34, 47, 61);
    private static final Color TEXT = new Color(222, 235, 230);
    static {
        AssetCache.load("UI_CHOICES_BORDER", "images/buttons/CHOICES_BORDER.png");
        AssetCache.load("UI_CHOICES_BORDER_SELECTED", "images/buttons/CHOICES_BORDER_SELECTED.png");
    }

    private TemporaryUiRenderer() {
    }

    public static void drawScreenBackground(Graphics2D g) {
        g.setColor(BACKGROUND);
        g.fillRect(0, 0, GameConstants.LOGICAL_WIDTH, GameConstants.LOGICAL_HEIGHT);
        g.setColor(TOP_BAR);
        g.fillRect(0, 0, GameConstants.LOGICAL_WIDTH, 18);
    }

    public static void drawButton(Graphics2D g, Rectangle bounds, String label,
                                  boolean hovered, boolean pressed, boolean enabled) {
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
        g.setFont(UiFontManager.dorris(22, Font.BOLD));
        int textWidth = g.getFontMetrics().stringWidth(label);
        int textX = bounds.x + (bounds.width - textWidth) / 2;
        int textY = bounds.y + (bounds.height - g.getFontMetrics().getHeight()) / 2 + g.getFontMetrics().getAscent();
        g.drawString(label, textX, textY);
    }

    public static void drawGameplayQuestion(Graphics2D g, String question, String optionA,
                                            String optionB, String optionC, String optionD) {
        g.setColor(TEXT);
        g.setFont(UiFontManager.dorris(21, Font.PLAIN));
        drawWrappedText(g, question, GameConstants.TEMPORARY_GAMEPLAY_QUESTION_TEXT_X,
            GameConstants.TEMPORARY_GAMEPLAY_QUESTION_TEXT_Y,
            GameConstants.TEMPORARY_GAMEPLAY_QUESTION_TEXT_WIDTH, 28, 4);
        }

        public static void drawGameplayTopPanel(Graphics2D g) {
        g.setColor(PANEL);
        g.fillRoundRect(GameConstants.TEMPORARY_GAMEPLAY_TOP_PANEL_X,
            GameConstants.TEMPORARY_GAMEPLAY_TOP_PANEL_Y,
            GameConstants.TEMPORARY_GAMEPLAY_TOP_PANEL_WIDTH,
            GameConstants.TEMPORARY_GAMEPLAY_TOP_PANEL_HEIGHT, 12, 12);
        }

        public static void drawGameplayChoicesPanel(Graphics2D g) {
        g.setColor(PANEL);
        g.fillRoundRect(GameConstants.TEMPORARY_GAMEPLAY_CHOICES_PANEL_X,
            GameConstants.TEMPORARY_GAMEPLAY_CHOICES_PANEL_Y,
            GameConstants.TEMPORARY_GAMEPLAY_CHOICES_PANEL_WIDTH,
            GameConstants.TEMPORARY_GAMEPLAY_CHOICES_PANEL_HEIGHT, 12, 12);
        }

    public static void drawOptionState(Graphics2D g, String title, String description) {
        g.setColor(TEXT);
        g.setFont(UiFontManager.dorris(24, Font.BOLD));
        g.drawString(title, GameConstants.TEMPORARY_GAMEPLAY_CHOICES_PANEL_X + 30,
            GameConstants.TEMPORARY_GAMEPLAY_CHOICES_PANEL_Y + 65);
        g.setFont(UiFontManager.dorris(17, Font.PLAIN));
        drawWrappedText(g, description, GameConstants.TEMPORARY_GAMEPLAY_CHOICES_PANEL_X + 30,
            GameConstants.TEMPORARY_GAMEPLAY_CHOICES_PANEL_Y + 105,
            GameConstants.TEMPORARY_GAMEPLAY_CHOICES_PANEL_WIDTH - 60, 22, 4);
    }

        public static void drawChoice(Graphics2D g, String label, String text, Rectangle bounds,
                      boolean hovered, boolean pressed, boolean selected) {
        String borderKey = selected ? "UI_CHOICES_BORDER_SELECTED" : "UI_CHOICES_BORDER";
        if (AssetCache.get(borderKey) != null) {
            g.drawImage(AssetCache.get(borderKey), bounds.x, bounds.y,
                bounds.width, bounds.height, null);
        }
        g.setColor(TEXT);
        if (text.isBlank()) {
            g.setFont(UiFontManager.dorris(28, Font.BOLD));
            int textWidth = g.getFontMetrics().stringWidth(label);
            int textY = bounds.y + (bounds.height - g.getFontMetrics().getHeight()) / 2
                + g.getFontMetrics().getAscent();
            g.drawString(label, bounds.x + (bounds.width - textWidth) / 2, textY);
        } else {
            g.setFont(UiFontManager.dorris(18, Font.BOLD));
            g.drawString(label + ".", bounds.x + 16, bounds.y + 32);
            g.setFont(UiFontManager.dorris(17, Font.PLAIN));
            drawWrappedText(g, text, bounds.x + 48, bounds.y + 23,
                GameConstants.TEMPORARY_GAMEPLAY_CHOICE_TEXT_WIDTH, 20, 2);
        }
    }

    private static void drawWrappedText(Graphics2D g, String text, int x, int y,
                                        int maxWidth, int lineHeight, int maxLines) {
        FontMetrics metrics = g.getFontMetrics();
        int currentY = y;
        int lineCount = 0;
        for (String paragraph : text.replace("<br>", "\n").split("\\n")) {
            StringBuilder line = new StringBuilder();
            for (String word : paragraph.split("\\s+")) {
                String candidate = line.length() == 0 ? word : line + " " + word;
                if (metrics.stringWidth(candidate) > maxWidth && line.length() > 0) {
                    g.drawString(line.toString(), x, currentY);
                    currentY += lineHeight;
                    lineCount++;
                    line.setLength(0);
                    if (lineCount >= maxLines) {
                        return;
                    }
                }
                line.append(line.length() == 0 ? word : " " + word);
            }
            if (line.length() > 0 && lineCount < maxLines) {
                g.drawString(line.toString(), x, currentY);
                currentY += lineHeight;
                lineCount++;
            }
            if (lineCount >= maxLines) {
                return;
            }
        }
    }
}
