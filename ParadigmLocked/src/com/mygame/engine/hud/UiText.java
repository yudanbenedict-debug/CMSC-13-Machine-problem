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
import java.util.Set;

// Text helper: measure, wrap, auto-fit, glow; digits/symbols fall back to sans for the Kobani fonts.
public final class UiText {
    public static final Color DEFAULT_GLOW = new Color(255, 236, 170);
    private static final FontRenderContext FRC = new FontRenderContext(null, true, true);
    private static final String FALLBACK_CHARS = "0123456789,;%*\"";
    private static final Color CODE_KEYWORD = new Color(0x569CD6);
    private static final Color CODE_CONTROL = new Color(0xC586C0);
    private static final Color CODE_TYPE = new Color(0x4EC9B0);
    private static final Color CODE_FUNCTION = new Color(0xDCDCAA);
    private static final Color CODE_STRING = new Color(0xCE9178);
    private static final Color CODE_NUMBER = new Color(0xB5CEA8);
    private static final Color CODE_COMMENT = new Color(0x6A9955);
    private static final Color CODE_BLANK = new Color(0xE5BE5E);
    private static final Set<String> KEYWORDS = Set.of("int", "void", "char", "double", "float", "long",
        "short", "bool", "boolean", "byte", "string", "object", "var", "class", "struct", "interface", "enum",
        "abstract", "public", "private", "protected", "internal", "static", "final", "const", "readonly",
        "new", "this", "super", "base", "extends", "implements", "import", "using", "namespace", "package",
        "override", "virtual", "sealed", "true", "false", "null", "out", "ref", "in", "get", "set", "event",
        "let", "where", "data", "instance", "module", "deriving", "unsigned");
    private static final Set<String> CONTROL = Set.of("return", "if", "else", "for", "foreach", "while", "do",
        "switch", "case", "break", "continue", "try", "catch", "finally", "throw", "throws", "default",
        "yield", "of", "then");
    private static final Map<String, Object> CACHE = Collections.synchronizedMap(
        new LinkedHashMap<>(256, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<String, Object> eldest) {
                return size() > 600;
            }
        });

    private UiText() {
    }

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

    public static TextLayout layout(String text, Font font) {
        return layout(text, font, false);
    }

    public static TextLayout layout(String text, Font font, boolean code) {
        if (text == null || text.isEmpty()) {
            text = " ";
        }
        String key = (code ? "C|" : "L|") + font.getFontName() + '|' + font.getStyle() + '|' + font.getSize2D()
            + '|' + text;
        Object cached = CACHE.get(key);
        if (cached instanceof TextLayout) {
            return (TextLayout) cached;
        }
        AttributedString attributed = attributed(text, font);
        if (code) {
            highlight(attributed, text);
        }
        TextLayout layout = new TextLayout(attributed.getIterator(), FRC);
        CACHE.put(key, layout);
        return layout;
    }

    public static float width(String text, Font font) {
        return text == null || text.isEmpty() ? 0 : layout(text, font).getAdvance();
    }

    public static void draw(Graphics2D g, String text, Font font, float x, float baseline, Color color) {
        draw(g, text, font, x, baseline, color, null);
    }

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
            Shape[] rings = glowShapes(layout);
            int[] alphas = {28, 55, 95};
            AffineTransform oldTransform = g.getTransform();
            g.translate(x, baseline);
            for (int index = 0; index < rings.length; index++) {
                g.setColor(new Color(glow.getRed(), glow.getGreen(), glow.getBlue(),
                    alphas[index] * glow.getAlpha() / 255));
                g.fill(rings[index]);
            }
            g.setTransform(oldTransform);
        }
        g.setColor(color);
        layout.draw(g, x, baseline);
        if (oldAa != null) {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, oldAa);
        }
    }

    private static final Map<TextLayout, Shape[]> GLOW_CACHE = Collections.synchronizedMap(new java.util.WeakHashMap<>());

    // the three stroked outlines behind glowing text, built once per text layout instead of every frame
    private static Shape[] glowShapes(TextLayout layout) {
        Shape[] cached = GLOW_CACHE.get(layout);
        if (cached != null) {
            return cached;
        }
        Shape outline = layout.getOutline(null);
        float scale = Math.max(0.35f, Math.min(1.5f, layout.getAscent() / 20f)); // glow scales with text size
        float[] widths = {11f * scale, 7f * scale, 3.5f * scale};
        Shape[] rings = new Shape[widths.length];
        for (int index = 0; index < widths.length; index++) {
            rings[index] = new java.awt.geom.Path2D.Float(new BasicStroke(widths[index], BasicStroke.CAP_ROUND,
                BasicStroke.JOIN_ROUND).createStrokedShape(outline));
        }
        GLOW_CACHE.put(layout, rings);
        return rings;
    }

    public static Block wrap(String text, Font font, int maxWidth, float lineSpacing, int maxLines) {
        return fitInternal(text, font.getFamily(), font, font.getSize2D(), font.getSize2D(),
            maxWidth, Integer.MAX_VALUE, lineSpacing, maxLines, false, false);
    }

    // Largest font size that fits the box; truncates with "..." if even minSize overflows.
    public static Block fit(String text, Font baseFont, float maxSize, float minSize,
                            int width, int height, float lineSpacing) {
        return fit(text, baseFont, maxSize, minSize, width, height, lineSpacing, false, false);
    }

    public static Block fit(String text, Font baseFont, float maxSize, float minSize,
                            int width, int height, float lineSpacing, boolean code, boolean skipFirstLine) {
        String key = "F|" + baseFont.getFontName() + '|' + baseFont.getStyle() + '|' + maxSize + '|'
            + minSize + '|' + width + '|' + height + '|' + lineSpacing + '|' + code + '|' + skipFirstLine
            + '|' + text;
        Object cached = CACHE.get(key);
        if (cached instanceof Block) {
            return (Block) cached;
        }
        Block block = fitInternal(text, baseFont.getFamily(), baseFont, maxSize, minSize,
            width, height, lineSpacing, Integer.MAX_VALUE, code, skipFirstLine);
        CACHE.put(key, block);
        return block;
    }

    public static void drawBlock(Graphics2D g, Block block, float x, float top, Color color, Color glow) {
        float baseline = top + block.ascent;
        for (TextLayout line : block.lines) {
            drawLayout(g, line, x, baseline, color, glow);
            baseline += block.lineHeight;
        }
    }

    private static Block fitInternal(String text, String family, Font baseFont, float maxSize,
                                     float minSize, int width, int height, float lineSpacing,
                                     int maxLines, boolean code, boolean skipFirstLine) {
        String clean = text == null ? "" : text.replace("\r", "").replace("<br>", "\n").replace("\t", "    ");
        for (float size = maxSize; size >= minSize; size -= 1f) {
            Font font = baseFont.deriveFont(size);
            List<String> lines = new ArrayList<>();
            List<Boolean> colored = new ArrayList<>();
            boolean highlighting = code;
            String[] paragraphs = clean.split("\n", -1);
            for (int index = 0; index < paragraphs.length; index++) {
                if (paragraphs[index].trim().equalsIgnoreCase("Expected output:")) {
                    highlighting = false;
                }
                boolean colorLine = highlighting && !(skipFirstLine && index == 0);
                for (String line : wrapLines(paragraphs[index], font, width)) {
                    lines.add(line);
                    colored.add(colorLine);
                }
            }
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
                for (int index = 0; index < lines.size(); index++) {
                    String line = lines.get(index);
                    layouts.add(layout(line.isEmpty() ? " " : line, font, colored.get(index)));
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
            String indent = " ".repeat(Math.min(indentCount, 32));
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

    private static void highlight(AttributedString attributed, String text) {
        int length = text.length();
        int index = 0;
        while (index < length && text.charAt(index) == ' ') {
            index++;
        }
        if (index < length && text.charAt(index) == '#') {
            color(attributed, index, length, CODE_CONTROL);
            return;
        }
        while (index < length) {
            char character = text.charAt(index);
            char next = index + 1 < length ? text.charAt(index + 1) : 0;
            char previous = index > 0 ? text.charAt(index - 1) : ' ';
            if (character == '/' && next == '/'
                || character == '-' && next == '-' && Character.isWhitespace(previous)) {
                color(attributed, index, length, CODE_COMMENT);
                return;
            }
            if (character == '"' || character == '\'' && !Character.isLetterOrDigit(previous)) {
                int end = index + 1;
                while (end < length && text.charAt(end) != character) {
                    end += text.charAt(end) == '\\' ? 2 : 1;
                }
                end = Math.min(length, end + 1);
                color(attributed, previous == '$' ? index - 1 : index, end, CODE_STRING);
                index = end;
                continue;
            }
            if (Character.isDigit(character) && !Character.isLetterOrDigit(previous) && previous != '_') {
                int end = index;
                while (end < length && (Character.isLetterOrDigit(text.charAt(end)) || text.charAt(end) == '.')) {
                    end++;
                }
                color(attributed, index, end, CODE_NUMBER);
                index = end;
                continue;
            }
            if (Character.isLetter(character) || character == '_') {
                int end = index;
                while (end < length && (Character.isLetterOrDigit(text.charAt(end)) || text.charAt(end) == '_')) {
                    end++;
                }
                String word = text.substring(index, end);
                int after = end;
                while (after < length && text.charAt(after) == ' ') {
                    after++;
                }
                Color wordColor = word.length() >= 3 && word.chars().allMatch(letter -> letter == '_') ? CODE_BLANK
                    : CONTROL.contains(word) ? CODE_CONTROL
                    : KEYWORDS.contains(word) ? CODE_KEYWORD
                    : after < length && text.charAt(after) == '(' ? CODE_FUNCTION
                    : Character.isUpperCase(character) ? CODE_TYPE : null;
                if (wordColor != null) {
                    color(attributed, index, end, wordColor);
                }
                index = end;
                continue;
            }
            index++;
        }
    }

    private static void color(AttributedString attributed, int from, int to, Color color) {
        if (from < to) {
            attributed.addAttribute(TextAttribute.FOREGROUND, color, from, to);
        }
    }

    private static boolean needsFallback(Font font) {
        String family = font.getFamily();
        return family != null && family.toLowerCase().contains("kobani");
    }
}
