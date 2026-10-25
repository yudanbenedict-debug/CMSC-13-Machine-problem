package com.mygame.ui;

import com.mygame.engine.core.AssetCache;
import com.mygame.engine.helpers.constants.GameConstants;
import com.mygame.engine.hud.UiFontManager;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public final class OverlayMenuRenderer {
    private static final Color TEXT = new Color(246, 242, 229);
    private static final Color MUTED_TEXT = new Color(209, 205, 192);
    private static final Color ACCENT = new Color(229, 190, 94);
    private static final Color VOLUME_FILL = new Color(0x98AAFF);

    private OverlayMenuRenderer() {
    }

    public static void drawBackdrop(Graphics2D graphics) {
    }

    public static void drawInfoOverlay(Graphics2D graphics, String title, String[] lines,
                                       double scroll, boolean backHovered) {
        drawFrame(graphics, "UI_OVERLAY_MENU_DEFAULT");
        drawCenteredText(graphics, title.toUpperCase(),
            UiFontManager.arpona(42, Font.BOLD), GameConstants.LOGICAL_WIDTH / 2, 155, TEXT);
        drawScrollableBody(graphics, lines, scroll);
        drawTextButton(graphics, "BACK TO MENU", getBackBounds(), backHovered);
    }

    public static void drawPauseOverlay(Graphics2D graphics, boolean continueHovered,
                                        boolean howToPlayHovered, boolean menuHovered,
                                        double volume, boolean muted) {
        drawFrame(graphics, "UI_OVERLAY_MENU_PAUSE");
        drawCenteredText(graphics, "GAME", UiFontManager.arpona(
            GameConstants.PAUSE_TITLE_FONT_SIZE, Font.BOLD),
            GameConstants.LOGICAL_WIDTH / 2, 145, TEXT);
        drawCenteredText(graphics, "PAUSED", UiFontManager.arpona(
            GameConstants.PAUSE_TITLE_FONT_SIZE, Font.BOLD),
            GameConstants.LOGICAL_WIDTH / 2, 190, TEXT);

        int centerX = GameConstants.LOGICAL_WIDTH / 2;
        drawTextButton(graphics, "CONTINUE", centeredBounds(centerX,
            GameConstants.PAUSE_OPTION_START_Y), continueHovered);
        drawTextButton(graphics, "HOW TO PLAY", centeredBounds(centerX,
            GameConstants.PAUSE_OPTION_START_Y + GameConstants.PAUSE_OPTION_SPACING),
            howToPlayHovered);
        drawTextButton(graphics, "BACK TO MENU", centeredBounds(centerX,
            GameConstants.PAUSE_OPTION_START_Y + GameConstants.PAUSE_OPTION_SPACING * 2),
            menuHovered);
        drawVolumeBar(graphics, volume, muted);
    }

    public static Rectangle getBackBounds() {
        return new Rectangle(
            (GameConstants.LOGICAL_WIDTH - GameConstants.OVERLAY_MENU_BACK_WIDTH) / 2,
            GameConstants.OVERLAY_MENU_BACK_Y,
            GameConstants.OVERLAY_MENU_BACK_WIDTH,
            GameConstants.OVERLAY_MENU_BACK_HEIGHT);
    }

    public static Rectangle getPauseOptionBounds(int index) {
        return centeredBounds(GameConstants.LOGICAL_WIDTH / 2,
            GameConstants.PAUSE_OPTION_START_Y + GameConstants.PAUSE_OPTION_SPACING * index);
    }

    public static Rectangle getPauseVolumeBounds() {
        return new Rectangle(GameConstants.PAUSE_VOLUME_X, GameConstants.PAUSE_VOLUME_Y,
            GameConstants.PAUSE_VOLUME_WIDTH, GameConstants.PAUSE_VOLUME_HEIGHT);
    }

    public static Rectangle getPauseMuteBounds() {
        Rectangle volumeBounds = getPauseVolumeBounds();
        return new Rectangle(volumeBounds.x, volumeBounds.y,
            GameConstants.PAUSE_VOLUME_ICON_WIDTH, volumeBounds.height);
    }

    public static double scroll(double current, int wheelRotation, String[] lines) {
        int contentHeight = Math.max(1, wrapLines(lines).size() * 38);
        int viewportHeight = GameConstants.OVERLAY_MENU_CONTENT_BOTTOM
            - GameConstants.OVERLAY_MENU_CONTENT_TOP;
        double maxScroll = Math.max(0, contentHeight - viewportHeight);
        return Math.max(0, Math.min(maxScroll, current + wheelRotation * 38));
    }

    private static void drawFrame(Graphics2D graphics, String assetKey) {
        drawBackdrop(graphics);
        BufferedImage frame = AssetCache.get(assetKey);
        if (frame != null) {
            graphics.drawImage(frame, GameConstants.OVERLAY_MENU_X, GameConstants.OVERLAY_MENU_Y,
                GameConstants.OVERLAY_MENU_WIDTH, GameConstants.OVERLAY_MENU_HEIGHT, null);
        }
    }

    private static void drawScrollableBody(Graphics2D graphics, String[] lines, double scroll) {
        int contentX = GameConstants.OVERLAY_MENU_CONTENT_X;
        int contentWidth = GameConstants.OVERLAY_MENU_WIDTH
            - (contentX - GameConstants.OVERLAY_MENU_X) * 2
            - GameConstants.OVERLAY_MENU_SCROLLBAR_RIGHT_INSET;
        int contentTop = GameConstants.OVERLAY_MENU_CONTENT_TOP;
        int contentBottom = GameConstants.OVERLAY_MENU_CONTENT_BOTTOM;
        List<String> wrappedLines = wrapLines(lines);

        Graphics2D clipped = (Graphics2D) graphics.create();
        clipped.clip(new Rectangle(contentX, contentTop, contentWidth, contentBottom - contentTop));
        clipped.setFont(UiFontManager.luckiestGuy(22, Font.PLAIN));
        FontMetrics metrics = clipped.getFontMetrics();
        int y = contentTop + metrics.getAscent() - (int) scroll;
        for (String line : wrappedLines) {
            drawCenteredText(clipped, line, clipped.getFont(),
                GameConstants.LOGICAL_WIDTH / 2, y, MUTED_TEXT);
            y += 38;
        }
        clipped.dispose();

        int contentHeight = Math.max(1, wrappedLines.size() * 38);
        int viewportHeight = contentBottom - contentTop;
        if (contentHeight > viewportHeight) {
            int trackX = GameConstants.OVERLAY_MENU_X + GameConstants.OVERLAY_MENU_WIDTH
                - GameConstants.OVERLAY_MENU_SCROLLBAR_RIGHT_INSET;
            graphics.setColor(new Color(246, 242, 229, 100));
            graphics.fillRect(trackX, contentTop, GameConstants.OVERLAY_MENU_SCROLLBAR_WIDTH,
                viewportHeight);
            int thumbHeight = Math.max(30, viewportHeight * viewportHeight / contentHeight);
            int thumbY = contentTop + (int) ((viewportHeight - thumbHeight)
                * (scroll / (contentHeight - viewportHeight)));
            graphics.setColor(ACCENT);
            graphics.fillRect(trackX, thumbY, GameConstants.OVERLAY_MENU_SCROLLBAR_WIDTH, thumbHeight);
        }
    }

    private static void drawVolumeBar(Graphics2D graphics, double volume, boolean muted) {
        Rectangle bounds = getPauseVolumeBounds();
        BufferedImage frame = AssetCache.get("UI_PAUSE_VOLUME_BAR");
        int inset = 8;
        int trackX = bounds.x + inset + GameConstants.PAUSE_VOLUME_ICON_WIDTH;
        int trackY = bounds.y + bounds.height / 2 - 7;
        int trackWidth = bounds.width - GameConstants.PAUSE_VOLUME_ICON_WIDTH - inset * 2;
        graphics.setColor(VOLUME_FILL);
        graphics.fill(new RoundRectangle2D.Double(trackX, trackY,
            trackWidth * (muted ? 0 : volume), 14, 14, 14));
        if (frame != null) {
            graphics.drawImage(frame, bounds.x, bounds.y, bounds.width, bounds.height, null);
        }
    }

    private static List<String> wrapLines(String[] lines) {
        int contentWidth = GameConstants.OVERLAY_MENU_WIDTH
            - (GameConstants.OVERLAY_MENU_CONTENT_X - GameConstants.OVERLAY_MENU_X) * 2
            - GameConstants.OVERLAY_MENU_SCROLLBAR_RIGHT_INSET;
        BufferedImage metricsImage = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        Graphics2D metricsGraphics = metricsImage.createGraphics();
        metricsGraphics.setFont(UiFontManager.luckiestGuy(22, Font.PLAIN));
        FontMetrics metrics = metricsGraphics.getFontMetrics();
        List<String> wrapped = new ArrayList<>();
        for (String paragraph : lines) {
            StringBuilder currentLine = new StringBuilder();
            for (String word : paragraph.split("\\s+")) {
                String candidate = currentLine.length() == 0
                    ? word : currentLine + " " + word;
                if (metrics.stringWidth(candidate) <= contentWidth) {
                    currentLine.append(currentLine.length() == 0 ? word : " " + word);
                    continue;
                }
                if (currentLine.length() > 0) {
                    wrapped.add(currentLine.toString());
                    currentLine.setLength(0);
                }
                appendWordWithinWidth(wrapped, currentLine, word, metrics, contentWidth);
            }
            if (currentLine.length() > 0) {
                wrapped.add(currentLine.toString());
            }
        }
        metricsGraphics.dispose();
        return wrapped;
    }

    private static void appendWordWithinWidth(List<String> wrapped, StringBuilder currentLine,
                                               String word, FontMetrics metrics, int maxWidth) {
        for (int index = 0; index < word.length(); index++) {
            String candidate = currentLine.toString() + word.charAt(index);
            if (metrics.stringWidth(candidate) > maxWidth && currentLine.length() > 0) {
                wrapped.add(currentLine.toString());
                currentLine.setLength(0);
            }
            currentLine.append(word.charAt(index));
        }
    }

    private static Rectangle centeredBounds(int centerX, int y) {
        return new Rectangle(centerX - 180, y - 30, 360, 48);
    }

    private static void drawTextButton(Graphics2D graphics, String text, Rectangle bounds,
                                       boolean hovered) {
        graphics.setColor(hovered ? ACCENT : TEXT);
        graphics.setFont(UiFontManager.notoSerif(25, Font.BOLD));
        FontMetrics metrics = graphics.getFontMetrics();
        graphics.drawString(text, bounds.x + (bounds.width - metrics.stringWidth(text)) / 2,
            bounds.y + (bounds.height - metrics.getHeight()) / 2 + metrics.getAscent());
    }

    private static void drawCenteredText(Graphics2D graphics, String text, Font font,
                                         int centerX, int baseline, Color color) {
        graphics.setFont(font);
        graphics.setColor(color);
        graphics.drawString(text, centerX - graphics.getFontMetrics().stringWidth(text) / 2,
            baseline);
    }
}
