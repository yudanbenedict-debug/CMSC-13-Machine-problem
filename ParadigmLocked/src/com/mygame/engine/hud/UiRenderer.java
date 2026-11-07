package com.mygame.engine.hud;

import com.mygame.engine.core.AssetCache;
import com.mygame.engine.core.Img;
import com.mygame.engine.helpers.constants.UiLayout;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;

public final class UiRenderer {
    private static final Color TEXT = new Color(222, 235, 230);
    private static final Color DIM_TEXT = new Color(110, 110, 110);
    private static final Color CORRECT = new Color(0x7FB05A);
    private static final Color WRONG = new Color(0xD05A5A);
    public static final Color ABANDON_HIGHLIGHT = new Color(0xFA5555);
    private static final Color HOVER_GLOW = new Color(255, 236, 170, 150);

    private UiRenderer() {
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

    public static void drawGameplayQuestion(Graphics2D g, String question) {
        UiText.Block block = UiText.fit(question, UiFontManager.question(28, Font.PLAIN), 28, 13,
            UiLayout.QUESTION_WIDTH,
            UiLayout.QUESTION_HEIGHT, 1.05f);
        UiText.drawBlock(g, block, UiLayout.QUESTION_X,
            UiLayout.QUESTION_Y, TEXT, null);
    }


    public static void drawAbandonPrompt(Graphics2D g) {
        Font font = UiFontManager.dorris(30, Font.BOLD);
        String before = "Do you really wish to ";
        String word = "Abandon";
        String after = " this question?";
        float total = UiText.width(before, font) + UiText.width(word, font) + UiText.width(after, font);
        float x = UiLayout.PANEL_X
            + (UiLayout.PANEL_WIDTH - total) / 2f;
        float baseline = UiLayout.PANEL_Y + 75;
        UiText.draw(g, before, font, x, baseline, TEXT);
        x += UiText.width(before, font);
        UiText.draw(g, word, font, x, baseline, ABANDON_HIGHLIGHT,
            new Color(0xFA, 0x55, 0x55, 160));
        x += UiText.width(word, font);
        UiText.draw(g, after, font, x, baseline, TEXT);
        UiText.drawCentered(g, "Abandoning counts as a wrong answer.", UiFontManager.dorris(17, Font.BOLD),
            UiLayout.PANEL_X
                + UiLayout.PANEL_WIDTH / 2f,
            baseline + 36, new Color(190, 190, 190), null);
    }

    public static void drawChoice(Graphics2D g, String label, String text, Rectangle bounds,
                                  boolean hovered, boolean selected, boolean nullified,
                                  ChoiceButton.Result result) {
        boolean highlighted = selected || result == ChoiceButton.Result.CORRECT;
        BufferedImage border = AssetCache.get(highlighted ? Img.CHOICES_BORDER_SELECTED : Img.CHOICES_BORDER);
        if (border != null) {
            g.drawImage(border, bounds.x, bounds.y, bounds.width, bounds.height, null);
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
        int textHeight = bounds.height - 34;  // stay above the border's bottom flourish
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

    public static boolean isChoiceTruncated(String text, Rectangle bounds) {
        return text != null && !text.isBlank() && choiceBlock(text, bounds).truncated;
    }

    // Joins lone "{" lines onto the previous line.
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

    // Full view of a long choice over the question area.
    public static void drawChoicePreview(Graphics2D g, String label, String text) {
        int x = UiLayout.QUESTION_X - 15;
        int y = UiLayout.QUESTION_AREA_Y;
        int width = UiLayout.QUESTION_WIDTH + 30;
        int height = UiLayout.STATUS_MESSAGE_Y - 24 - y;
        g.setColor(new Color(12, 12, 16, 250));
        g.fill(new java.awt.geom.RoundRectangle2D.Double(x, y, width, height, 16, 16));
        g.setColor(new Color(229, 190, 94, 200));
        g.setStroke(new BasicStroke(1.5f));
        g.draw(new java.awt.geom.RoundRectangle2D.Double(x, y, width, height, 16, 16));
        g.setStroke(new BasicStroke(1f));
        UiText.draw(g, "CHOICE " + label, UiFontManager.dorris(16, Font.BOLD), x + 16, y + 24,
            new Color(229, 190, 94));
        String body = compactCode(text);
        // one column first, two columns if it still doesn't fit
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

}
