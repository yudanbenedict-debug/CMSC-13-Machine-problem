package com.mygame.engine.hud;

import com.mygame.engine.core.AssetCache;
import com.mygame.engine.helpers.constants.GameConstants;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Rectangle;

public final class TemporaryUiRenderer {
    private static final Color BACKGROUND = new Color(15, 22, 30);
    private static final Color TEXT = new Color(222, 235, 230);
    private static final Color DIM_TEXT = new Color(110, 110, 110);
    private static final Color CORRECT = new Color(0x7FB05A);
    private static final Color WRONG = new Color(0xD05A5A);
    public static final Color ABANDON_HIGHLIGHT = new Color(0xFA5555);
    private static final Color HOVER_GLOW = new Color(255, 236, 170, 150);

    private TemporaryUiRenderer() {
    }

    public static void drawScreenBackground(Graphics2D g) {
        g.setColor(BACKGROUND);
        g.fillRect(0, 0, GameConstants.LOGICAL_WIDTH, GameConstants.LOGICAL_HEIGHT);
    }

    public static void drawButton(Graphics2D g, Rectangle bounds, String label,
                                  boolean hovered, boolean pressed, boolean enabled) {
        Color fill = !enabled ? new Color(70, 70, 78)
                : pressed ? new Color(120, 178, 176)
                : hovered ? new Color(76, 126, 151)
                : new Color(36, 48, 62);
        g.setColor(fill);
        g.fillRoundRect(bounds.x, bounds.y, bounds.width, bounds.height, 12, 12);
        g.setColor(hovered && enabled ? new Color(178, 236, 224) : new Color(120, 144, 156));
        g.drawRoundRect(bounds.x, bounds.y, bounds.width, bounds.height, 12, 12);
        if (label != null && !label.isEmpty()) {
            UiText.drawInBox(g, label, UiFontManager.dorris(22, Font.BOLD), bounds,
                enabled ? Color.WHITE : new Color(160, 160, 160), null);
        }
    }

    /** Question text in QUESTION_FONT, auto-sized so long questions / code never bleed out. */
    public static void drawGameplayQuestion(Graphics2D g, String question) {
        UiText.Block block = UiText.fit(question, UiFontManager.question(28, Font.PLAIN), 28, 13,
            GameConstants.TEMPORARY_GAMEPLAY_QUESTION_TEXT_WIDTH,
            GameConstants.TEMPORARY_GAMEPLAY_QUESTION_TEXT_HEIGHT, 1.05f);
        UiText.drawBlock(g, block, GameConstants.TEMPORARY_GAMEPLAY_QUESTION_TEXT_X,
            GameConstants.TEMPORARY_GAMEPLAY_QUESTION_TEXT_Y, TEXT, null);
    }

    public static void drawOptionState(Graphics2D g, String title, String description) {
        int x = GameConstants.TEMPORARY_GAMEPLAY_CHOICES_PANEL_X + 30;
        int y = GameConstants.TEMPORARY_GAMEPLAY_CHOICES_PANEL_Y;
        UiText.draw(g, title.replace('\n', ' '), UiFontManager.dorris(26, Font.BOLD), x, y + 50, TEXT);
        UiText.Block block = UiText.wrap(description, UiFontManager.dorris(18, Font.PLAIN),
            GameConstants.TEMPORARY_GAMEPLAY_CHOICES_PANEL_WIDTH - 60, 1.15f, 4);
        UiText.drawBlock(g, block, x, y + 68, TEXT, null);
    }

    /** "Do you really wish to Abandon this question?" with "Abandon" in #fa5555. */
    public static void drawAbandonPrompt(Graphics2D g) {
        Font font = UiFontManager.choices(34, Font.PLAIN);
        String before = "Do you really wish to ";
        String word = "Abandon";
        String after = " this question?";
        float total = UiText.width(before, font) + UiText.width(word, font) + UiText.width(after, font);
        float x = GameConstants.TEMPORARY_GAMEPLAY_CHOICES_PANEL_X
            + (GameConstants.TEMPORARY_GAMEPLAY_CHOICES_PANEL_WIDTH - total) / 2f;
        float baseline = GameConstants.TEMPORARY_GAMEPLAY_CHOICES_PANEL_Y + 75;
        UiText.draw(g, before, font, x, baseline, TEXT);
        x += UiText.width(before, font);
        UiText.draw(g, word, font, x, baseline, ABANDON_HIGHLIGHT,
            new Color(0xFA, 0x55, 0x55, 160));
        x += UiText.width(word, font);
        UiText.draw(g, after, font, x, baseline, TEXT);
        UiText.drawCentered(g, "Abandoning counts as a wrong answer.", UiFontManager.choices(18, Font.PLAIN),
            GameConstants.TEMPORARY_GAMEPLAY_CHOICES_PANEL_X
                + GameConstants.TEMPORARY_GAMEPLAY_CHOICES_PANEL_WIDTH / 2f,
            baseline + 36, new Color(190, 190, 190), null);
    }

    public static void drawChoice(Graphics2D g, String label, String text, Rectangle bounds,
                                  boolean hovered, boolean selected, boolean nullified,
                                  ChoiceButton.Result result) {
        boolean highlighted = selected || result == ChoiceButton.Result.CORRECT;
        String borderKey = highlighted ? "UI_CHOICES_BORDER_SELECTED" : "UI_CHOICES_BORDER";
        if (AssetCache.get(borderKey) != null) {
            g.drawImage(AssetCache.get(borderKey), bounds.x, bounds.y, bounds.width, bounds.height, null);
        }

        Color color = nullified ? DIM_TEXT
            : result == ChoiceButton.Result.CORRECT ? CORRECT
            : result == ChoiceButton.Result.WRONG ? WRONG : TEXT;
        Color glow = nullified ? null
            : result == ChoiceButton.Result.CORRECT ? new Color(0x7F, 0xB0, 0x5A, 200)
            : result == ChoiceButton.Result.WRONG ? new Color(0xD0, 0x5A, 0x5A, 200)
            : selected ? UiText.DEFAULT_GLOW
            : hovered ? HOVER_GLOW : null;

        if (text == null || text.isBlank()) {
            UiText.drawInBox(g, label, UiFontManager.choices(32, Font.PLAIN),
                new Rectangle(bounds.x, bounds.y, bounds.width, (int) (bounds.height * 0.8)), color, glow);
            return;
        }

        int textX = bounds.x + 52;
        int textTop = bounds.y + 8;
        int textWidth = bounds.width - 64;
        int textHeight = bounds.height - 34; // keep clear of the flourish at the bottom of the border
        UiText.Block block = choiceBlock(text, bounds);
        float top = textTop + Math.max(0, (textHeight - block.height) / 2f);
        UiText.draw(g, label + ".", UiFontManager.choices(28, Font.PLAIN), bounds.x + 14,
            top + block.ascent, color, glow);

        Graphics2D clipped = (Graphics2D) g.create();
        clipped.clipRect(textX - 8, bounds.y, textWidth + 16, bounds.height - 22);
        UiText.drawBlock(clipped, block, textX, top, color, glow);
        clipped.dispose();

        if (nullified) {
            g.setColor(new Color(0xD0, 0x5A, 0x5A, 190));
            g.setStroke(new BasicStroke(3f));
            float midY = top + block.height / 2f;
            g.drawLine(bounds.x + 10, Math.round(midY), bounds.x + bounds.width - 10, Math.round(midY));
            g.setStroke(new BasicStroke(1f));
        }
    }

    private static UiText.Block choiceBlock(String text, Rectangle bounds) {
        return UiText.fit(compactCode(text), UiFontManager.choices(26, Font.PLAIN), 26, 11,
            bounds.width - 64, bounds.height - 34, 0.92f);
    }

    /** True when a choice is too long for its box (it then gets an "..." and a hover preview). */
    public static boolean isChoiceTruncated(String text, Rectangle bounds) {
        return text != null && !text.isBlank() && choiceBlock(text, bounds).truncated;
    }

    /** Pulls lone "{" lines up onto the previous line so code snippets take fewer lines. */
    public static String compactCode(String text) {
        if (text == null || !text.contains("<br>")) {
            return text;
        }
        String[] lines = text.split("<br>", -1);
        StringBuilder result = new StringBuilder();
        for (String line : lines) {
            if (line.trim().equals("{") && result.length() > 0) {
                result.append(" {");
            } else {
                if (result.length() > 0) {
                    result.append("<br>");
                }
                result.append(line);
            }
        }
        return result.toString();
    }

    /**
     * Full-size preview of a long (code) choice, drawn over the question area while it is
     * hovered or selected, so truncated choices are still fully readable.
     */
    public static void drawChoicePreview(Graphics2D g, String label, String text) {
        int x = GameConstants.TEMPORARY_GAMEPLAY_QUESTION_TEXT_X - 15;
        int y = GameConstants.TEMPORARY_GAMEPLAY_PANEL_Y;
        int width = GameConstants.TEMPORARY_GAMEPLAY_QUESTION_TEXT_WIDTH + 30;
        int height = GameConstants.STATUS_MESSAGE_Y - 24 - y;
        g.setColor(new Color(12, 12, 16, 250));
        g.fill(new java.awt.geom.RoundRectangle2D.Double(x, y, width, height, 16, 16));
        g.setColor(new Color(229, 190, 94, 200));
        g.setStroke(new BasicStroke(1.5f));
        g.draw(new java.awt.geom.RoundRectangle2D.Double(x, y, width, height, 16, 16));
        g.setStroke(new BasicStroke(1f));
        UiText.draw(g, "CHOICE " + label, UiFontManager.dorris(16, Font.BOLD), x + 16, y + 24,
            new Color(229, 190, 94));
        String body = compactCode(text);
        // try one column first; very long snippets are split into two columns
        UiText.Block single = UiText.fit(body, UiFontManager.choices(22, Font.PLAIN), 22, 12,
            width - 32, height - 48, 0.95f);
        if (!single.truncated || !body.contains("<br>")) {
            UiText.drawBlock(g, single, x + 16, y + 40, TEXT, null);
            return;
        }
        String[] lines = body.split("<br>", -1);
        int half = (lines.length + 1) / 2;
        String left = String.join("<br>", java.util.Arrays.copyOfRange(lines, 0, half));
        String right = String.join("<br>", java.util.Arrays.copyOfRange(lines, half, lines.length));
        int columnWidth = (width - 48) / 2;
        UiText.Block leftBlock = UiText.fit(left, UiFontManager.choices(20, Font.PLAIN), 20, 10,
            columnWidth, height - 48, 0.95f);
        UiText.Block rightBlock = UiText.fit(right, UiFontManager.choices(leftBlock.size, Font.PLAIN),
            leftBlock.size, 10, columnWidth, height - 48, 0.95f);
        UiText.drawBlock(g, leftBlock, x + 16, y + 40, TEXT, null);
        g.setColor(new Color(229, 190, 94, 90));
        g.drawLine(x + 24 + columnWidth, y + 40, x + 24 + columnWidth, y + height - 10);
        UiText.drawBlock(g, rightBlock, x + 32 + columnWidth, y + 40, TEXT, null);
    }

    public static void drawWrappedText(Graphics2D g, String text, int x, int y,
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
