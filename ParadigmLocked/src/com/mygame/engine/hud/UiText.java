package com.mygame.engine.hud;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.font.FontRenderContext;
import java.awt.font.TextAttribute;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.text.AttributedString;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/*
    Central text helper: measuring, wrapping, auto-fitting and the GLOW effect.

    - QUESTION_FONT / CHOICES_FONT ("Kobani") are Arabic fonts: their digits and , ; % * " glyphs
      are Arabic shapes (5 shows as a different symbol). Those characters are automatically drawn
      with a plain sans-serif fallback so code snippets stay correct.
    - Text layouts are cached (LRU), so calling these every frame is cheap.
    - glow(...) is the one place that draws glowing text; anything that wants a hover/selected glow
      just calls UiText.draw(..., glowColor) with a non-null glow color.
 */
public final class UiText {
    public static final Color DEFAULT_GLOW = new Color(255, 236, 170);
    private static final FontRenderContext FRC = new FontRenderContext(null, true, true);
    private static final String FALLBACK_CHARS = "0123456789,;%*\"";
    private static final Map<String, Object> CACHE = Collections.synchronizedMap(
        new LinkedHashMap<>(256, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<String, Object> eldest) {
                return size() > 600;
            }
        });

    private UiText() {
    }

    /** Result of fitting text into a box: the chosen lines and their metrics. */
    public static final class Block {
        public final List<TextLayout> lines;
        public final float lineHeight;
        public final float ascent;
        public final float height;
        public final float size;
        public final boolean truncated;

        Block(List<TextLayout> lines, float lineHeight, float ascent, float size, boolean truncated) {
            this.lines = lines;
            this.lineHeight = lineHeight;
            this.ascent = ascent;
            this.height = lines.isEmpty() ? 0 : lineHeight * (lines.size() - 1) + ascent + 4;
            this.size = size;
            this.truncated = truncated;
        }

        public float widest() {
            float widest = 0;
            for (TextLayout line : lines) {
                widest = Math.max(widest, line.getAdvance());
            }
            return widest;
        }
    }

    // ------------------------------------------------------------------ single line

    public static TextLayout layout(String text, Font font) {
        if (text == null || text.isEmpty()) {
            text = " ";
        }
        String key = "L|" + font.getFontName() + '|' + font.getStyle() + '|' + font.getSize2D() + '|' + text;
        Object cached = CACHE.get(key);
        if (cached instanceof TextLayout) {
            return (TextLayout) cached;
        }
        TextLayout layout = new TextLayout(attributed(text, font).getIterator(), FRC);
        CACHE.put(key, layout);
        return layout;
    }

    public static float width(String text, Font font) {
        return text == null || text.isEmpty() ? 0 : layout(text, font).getAdvance();
    }

    public static void draw(Graphics2D g, String text, Font font, float x, float baseline, Color color) {
        draw(g, text, font, x, baseline, color, null);
    }

    /** Draws text; when glow is not null the text gets a soft glow behind it. */
    public static void draw(Graphics2D g, String text, Font font, float x, float baseline,
                            Color color, Color glow) {
        if (text == null || text.isEmpty()) {
            return;
        }
        drawLayout(g, layout(text, font), x, baseline, color, glow);
    }

    public static void drawCentered(Graphics2D g, String text, Font font, float centerX,
                                    float baseline, Color color, Color glow) {
        draw(g, text, font, centerX - width(text, font) / 2f, baseline, color, glow);
    }

    /** Draws a line centered both ways inside a box. */
    public static void drawInBox(Graphics2D g, String text, Font font, java.awt.Rectangle box,
                                 Color color, Color glow) {
        TextLayout layout = layout(text, font);
        float baseline = box.y + (box.height + layout.getAscent() - layout.getDescent()) / 2f;
        drawLayout(g, layout, box.x + (box.width - layout.getAdvance()) / 2f, baseline, color, glow);
    }

    public static void drawLayout(Graphics2D g, TextLayout layout, float x, float baseline,
                                  Color color, Color glow) {
        Object oldAa = g.getRenderingHint(RenderingHints.KEY_ANTIALIASING);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        if (glow != null) {
            Shape outline = layout.getOutline(AffineTransform.getTranslateInstance(x, baseline));
            java.awt.Stroke oldStroke = g.getStroke();
            // glow thickness follows the text size so small text doesn't turn into a blob
            float scale = Math.max(0.35f, Math.min(1.5f, layout.getAscent() / 20f));
            float[] widths = {11f * scale, 7f * scale, 3.5f * scale};
            int[] alphas = {28, 55, 95};
            for (int index = 0; index < widths.length; index++) {
                g.setStroke(new BasicStroke(widths[index], BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g.setColor(new Color(glow.getRed(), glow.getGreen(), glow.getBlue(),
                    alphas[index] * glow.getAlpha() / 255));
                g.draw(outline);
            }
            g.setStroke(oldStroke);
        }
        g.setColor(color);
        layout.draw(g, x, baseline);
        if (oldAa != null) {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, oldAa);
        }
    }

    // ------------------------------------------------------------------ multi line

    /** Wraps (keeping code indentation and <br>/newlines) without changing the font size. */
    public static Block wrap(String text, Font font, int maxWidth, float lineSpacing, int maxLines) {
        return fitInternal(text, font.getFamily(), font, font.getSize2D(), font.getSize2D(),
            maxWidth, Integer.MAX_VALUE, lineSpacing, maxLines);
    }

    /**
     * Picks the largest font size (maxSize..minSize) whose wrapped text fits in width x height.
     * If even minSize overflows, the text is cut with an ellipsis so it never bleeds out.
     */
    public static Block fit(String text, Font baseFont, float maxSize, float minSize,
                            int width, int height, float lineSpacing) {
        String key = "F|" + baseFont.getFontName() + '|' + baseFont.getStyle() + '|' + maxSize + '|'
            + minSize + '|' + width + '|' + height + '|' + lineSpacing + '|' + text;
        Object cached = CACHE.get(key);
        if (cached instanceof Block) {
            return (Block) cached;
        }
        Block block = fitInternal(text, baseFont.getFamily(), baseFont, maxSize, minSize,
            width, height, lineSpacing, Integer.MAX_VALUE);
        CACHE.put(key, block);
        return block;
    }

    /** Draws a block with its first baseline at (x, top + ascent). */
    public static void drawBlock(Graphics2D g, Block block, float x, float top, Color color, Color glow) {
        float baseline = top + block.ascent;
        for (TextLayout line : block.lines) {
            drawLayout(g, line, x, baseline, color, glow);
            baseline += block.lineHeight;
        }
    }

    private static Block fitInternal(String text, String family, Font baseFont, float maxSize,
                                     float minSize, int width, int height, float lineSpacing,
                                     int maxLines) {
        String clean = text == null ? "" : text.replace("\r", "").replace("<br>", "\n").replace("\t", "    ");
        for (float size = maxSize; size >= minSize; size -= 1f) {
            Font font = baseFont.deriveFont(size);
            List<String> lines = wrapLines(clean, font, width);
            float lineHeight = lineHeight(font, lineSpacing);
            float ascent = layout("Ag", font).getAscent();
            float needed = lineHeight * (lines.size() - 1) + ascent + 4;
            if ((needed <= height && lines.size() <= maxLines) || size - 1f < minSize) {
                boolean truncated = false;
                int allowed = Math.min(maxLines, height == Integer.MAX_VALUE ? lines.size()
                    : Math.max(1, (int) ((height - ascent - 4) / lineHeight) + 1));
                if (lines.size() > allowed) {
                    lines = new ArrayList<>(lines.subList(0, allowed));
                    lines.set(allowed - 1, ellipsize(lines.get(allowed - 1), font, width));
                    truncated = true;
                }
                List<TextLayout> layouts = new ArrayList<>();
                for (String line : lines) {
                    layouts.add(layout(line.isEmpty() ? " " : line, font));
                }
                return new Block(layouts, lineHeight, ascent, size, truncated);
            }
        }
        return new Block(List.of(), 0, 0, minSize, false);
    }

    private static float lineHeight(Font font, float spacing) {
        TextLayout probe = layout("Ag", font);
        return (probe.getAscent() + probe.getDescent()) * spacing;
    }

    private static String ellipsize(String line, Font font, int width) {
        String result = line;
        while (!result.isEmpty() && width(result + "...", font) > width) {
            result = result.substring(0, result.length() - 1);
        }
        return result.stripTrailing() + "...";
    }

    public static List<String> wrapLines(String text, Font font, int maxWidth) {
        List<String> wrapped = new ArrayList<>();
        for (String paragraph : text.split("\n", -1)) {
            int indentCount = 0;
            while (indentCount < paragraph.length() && paragraph.charAt(indentCount) == ' ') {
                indentCount++;
            }
            String indent = " ".repeat(Math.min(indentCount, 8));
            String body = paragraph.trim();
            if (body.isEmpty()) {
                wrapped.add("");
                continue;
            }
            StringBuilder line = new StringBuilder(indent);
            boolean lineHasWord = false;
            for (String word : body.split("\\s+")) {
                String candidate = lineHasWord ? line + " " + word : line + word;
                if (width(candidate, font) <= maxWidth) {
                    line.setLength(0);
                    line.append(candidate);
                    lineHasWord = true;
                    continue;
                }
                if (lineHasWord) {
                    wrapped.add(line.toString());
                    line.setLength(0);
                    line.append(indent);
                }
                // a single word wider than the box gets split by characters
                for (char character : word.toCharArray()) {
                    if (width(line.toString() + character, font) > maxWidth && line.length() > indent.length()) {
                        wrapped.add(line.toString());
                        line.setLength(0);
                        line.append(indent);
                    }
                    line.append(character);
                }
                lineHasWord = true;
            }
            if (line.length() > 0) {
                wrapped.add(line.toString());
            }
        }
        return wrapped;
    }

    private static AttributedString attributed(String text, Font font) {
        AttributedString attributed = new AttributedString(text);
        attributed.addAttribute(TextAttribute.FONT, font);
        if (needsFallback(font)) {
            Font fallback = new Font(Font.SANS_SERIF, font.getStyle(), Math.round(font.getSize2D()))
                .deriveFont(font.getSize2D());
            for (int index = 0; index < text.length(); index++) {
                char character = text.charAt(index);
                if (FALLBACK_CHARS.indexOf(character) >= 0 || font.canDisplay(character) == false) {
                    attributed.addAttribute(TextAttribute.FONT, fallback, index, index + 1);
                }
            }
        }
        return attributed;
    }

    private static boolean needsFallback(Font font) {
        String family = font.getFamily();
        return family != null && family.toLowerCase().contains("kobani");
    }
}
