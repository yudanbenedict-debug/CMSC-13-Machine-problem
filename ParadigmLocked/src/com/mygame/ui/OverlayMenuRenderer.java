package com.mygame.ui;

import com.mygame.engine.core.AssetCache;
import com.mygame.engine.helpers.constants.GameConstants;
import com.mygame.engine.hud.UiFontManager;
import com.mygame.engine.hud.UiText;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

public final class OverlayMenuRenderer {
    private static final Color TEXT = new Color(246, 242, 229);
    private static final Color MUTED_TEXT = new Color(209, 205, 192);
    private static final Color ACCENT = new Color(229, 190, 94);
    private static final Color SUBHEADING = new Color(0x98AAFF);
    private static final Color VOLUME_FILL = new Color(0x98AAFF);
    private static final Color HOVER_GLOW = new Color(255, 236, 170, 200);
    // the info screens never change, so their layout is computed once per block list
    private static final Map<List<InfoBlock>, Layout> LAYOUT_CACHE =
        Collections.synchronizedMap(new IdentityHashMap<>());

    private OverlayMenuRenderer() {
    }

    // ------------------------------------------------------------------ info overlays

    private static final class Placed {
        final InfoBlock block;
        final int y;
        final int height;
        final UiText.Block text;

        Placed(InfoBlock block, int y, int height, UiText.Block text) {
            this.block = block;
            this.y = y;
            this.height = height;
            this.text = text;
        }
    }

    private static final class Layout {
        final List<Placed> items = new ArrayList<>();
        int height;
    }

    private static int contentX() {
        return GameConstants.OVERLAY_MENU_CONTENT_X;
    }

    private static int contentWidth() {
        return GameConstants.OVERLAY_MENU_WIDTH
            - (GameConstants.OVERLAY_MENU_CONTENT_X - GameConstants.OVERLAY_MENU_X) * 2
            - GameConstants.OVERLAY_MENU_SCROLLBAR_RIGHT_INSET;
    }

    private static Font fontFor(InfoBlock.Kind kind) {
        switch (kind) {
            case HEADING:
                return UiFontManager.luckiestGuy(32, Font.PLAIN);
            case SUBHEADING:
                return UiFontManager.dorris(24, Font.BOLD);
            default:
                return UiFontManager.notoSerif(18, Font.PLAIN);
        }
    }

    private static Layout layout(List<InfoBlock> blocks) {
        Layout cached = LAYOUT_CACHE.get(blocks);
        if (cached != null) {
            return cached;
        }
        Layout layout = new Layout();
        int y = 0;
        for (InfoBlock block : blocks) {
            switch (block.kind) {
                case GAP:
                    y += block.height;
                    break;
                case IMAGE: {
                    UiText.Block caption = block.text == null ? null
                        : UiText.wrap(block.text, UiFontManager.dorris(16, Font.BOLD), contentWidth(), 1.1f, 3);
                    int height = block.height + (caption == null ? 0 : (int) caption.height + 6);
                    layout.items.add(new Placed(block, y, height, caption));
                    y += height + 14;
                    break;
                }
                default: {
                    float spacing = block.kind == InfoBlock.Kind.BODY ? 1.2f : 1.05f;
                    UiText.Block text = UiText.wrap(block.text, fontFor(block.kind), contentWidth(), spacing, 99);
                    int top = block.kind == InfoBlock.Kind.HEADING && y > 0 ? 18
                        : block.kind == InfoBlock.Kind.SUBHEADING && y > 0 ? 8 : 0;
                    y += top;
                    layout.items.add(new Placed(block, y, (int) text.height, text));
                    y += (int) text.height + (block.kind == InfoBlock.Kind.BODY ? 12 : 8);
                }
            }
        }
        layout.height = Math.max(1, y);
        LAYOUT_CACHE.put(blocks, layout);
        return layout;
    }

    public static void drawInfoOverlay(Graphics2D graphics, String title, List<InfoBlock> blocks,
                                       double scroll, boolean backHovered) {
        drawFrame(graphics, "UI_OVERLAY_MENU_DEFAULT");
        UiText.drawCentered(graphics, title.toUpperCase(), UiFontManager.arpona(42, Font.BOLD),
            GameConstants.LOGICAL_WIDTH / 2f, 155, TEXT, null);
        drawScrollableBody(graphics, blocks, scroll);
        drawTextButton(graphics, "BACK", getBackBounds(), backHovered);
    }

    public static double scroll(double current, double wheelRotation, List<InfoBlock> blocks) {
        int viewportHeight = GameConstants.OVERLAY_MENU_CONTENT_BOTTOM - GameConstants.OVERLAY_MENU_CONTENT_TOP;
        double maxScroll = Math.max(0, layout(blocks).height - viewportHeight);
        return Math.max(0, Math.min(maxScroll, current + wheelRotation * 40));
    }

    private static void drawScrollableBody(Graphics2D graphics, List<InfoBlock> blocks, double scroll) {
        Layout layout = layout(blocks);
        int contentTop = GameConstants.OVERLAY_MENU_CONTENT_TOP;
        int contentBottom = GameConstants.OVERLAY_MENU_CONTENT_BOTTOM;
        int viewportHeight = contentBottom - contentTop;
        float centerX = GameConstants.LOGICAL_WIDTH / 2f;

        Graphics2D clipped = (Graphics2D) graphics.create();
        clipped.clip(new Rectangle(contentX() - 10, contentTop, contentWidth() + 20, viewportHeight));
        for (Placed item : layout.items) {
            int top = contentTop + item.y - (int) scroll;
            if (top > contentBottom || top + item.height < contentTop) {
                continue;
            }
            if (item.block.kind == InfoBlock.Kind.IMAGE) {
                BufferedImage image = AssetCache.get(item.block.imageKey);
                if (image != null) {
                    int height = item.block.height;
                    int width = image.getWidth() * height / image.getHeight();
                    clipped.drawImage(image, (int) (centerX - width / 2f), top, width, height, null);
                }
                if (item.text != null) {
                    drawCenteredBlock(clipped, item.text, centerX, top + item.block.height + 6, MUTED_TEXT, null);
                }
                continue;
            }
            Color color = item.block.kind == InfoBlock.Kind.HEADING ? ACCENT
                : item.block.kind == InfoBlock.Kind.SUBHEADING ? SUBHEADING : MUTED_TEXT;
            Color glow = item.block.kind == InfoBlock.Kind.HEADING ? new Color(229, 190, 94, 90) : null;
            drawCenteredBlock(clipped, item.text, centerX, top, color, glow);
            if (item.block.kind == InfoBlock.Kind.HEADING) {
                int lineY = top + (int) item.height + 3;
                clipped.setColor(new Color(229, 190, 94, 120));
                clipped.drawLine((int) centerX - 120, lineY, (int) centerX + 120, lineY);
            }
        }
        clipped.dispose();

        int contentHeight = layout.height;
        if (contentHeight > viewportHeight) {
            int trackX = contentX() + contentWidth() + 14;
            graphics.setColor(new Color(246, 242, 229, 100));
            graphics.fillRect(trackX, contentTop, GameConstants.OVERLAY_MENU_SCROLLBAR_WIDTH, viewportHeight);
            int thumbHeight = Math.max(30, viewportHeight * viewportHeight / contentHeight);
            int thumbY = contentTop + (int) ((viewportHeight - thumbHeight)
                * (scroll / (contentHeight - viewportHeight)));
            graphics.setColor(ACCENT);
            graphics.fillRect(trackX, thumbY, GameConstants.OVERLAY_MENU_SCROLLBAR_WIDTH, thumbHeight);
        }
    }

    private static void drawCenteredBlock(Graphics2D g, UiText.Block block, float centerX, float top,
                                          Color color, Color glow) {
        float baseline = top + block.ascent;
        for (java.awt.font.TextLayout line : block.lines) {
            UiText.drawLayout(g, line, centerX - line.getAdvance() / 2f, baseline, color, glow);
            baseline += block.lineHeight;
        }
    }

    public static Rectangle getBackBounds() {
        return new Rectangle(
            (GameConstants.LOGICAL_WIDTH - GameConstants.OVERLAY_MENU_BACK_WIDTH) / 2,
            GameConstants.OVERLAY_MENU_BACK_Y,
            GameConstants.OVERLAY_MENU_BACK_WIDTH,
            GameConstants.OVERLAY_MENU_BACK_HEIGHT);
    }

    // ------------------------------------------------------------------ pause overlay

    public static void drawPauseOverlay(Graphics2D graphics, int hoveredOption, double volume,
                                        boolean muted, boolean muteHovered) {
        drawFrame(graphics, "UI_OVERLAY_MENU_PAUSE");
        Font titleFont = UiFontManager.arpona(GameConstants.PAUSE_TITLE_FONT_SIZE, Font.BOLD);
        float centerX = GameConstants.LOGICAL_WIDTH / 2f;
        UiText.drawCentered(graphics, "GAME", titleFont, centerX, GameConstants.PAUSE_TITLE_GAME_BASELINE, TEXT, null);
        UiText.drawCentered(graphics, "PAUSED", titleFont, centerX, GameConstants.PAUSE_TITLE_PAUSED_BASELINE, TEXT, null);

        String[] labels = {"CONTINUE", "HOW TO PLAY", "BACK TO MENU"};
        for (int index = 0; index < labels.length; index++) {
            drawTextButton(graphics, labels[index], getPauseOptionBounds(index), hoveredOption == index);
        }
        drawVolumeBar(graphics, volume, muted, muteHovered);
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

    /** Slider track (x range) used for dragging: from the speaker icon to the inner right edge. */
    public static int volumeTrackStart() {
        return GameConstants.PAUSE_VOLUME_X + GameConstants.PAUSE_VOLUME_ICON_WIDTH;
    }

    public static int volumeTrackEnd() {
        return GameConstants.PAUSE_VOLUME_X + Math.round(GameConstants.PAUSE_VOLUME_WIDTH * 475f / 500f);
    }

    private static void drawVolumeBar(Graphics2D graphics, double volume, boolean muted, boolean muteHovered) {
        Rectangle bounds = getPauseVolumeBounds();
        // inner pill of the PNG (500x150): x 25..475, y 25..125, radius 50 -> the fill matches its border
        double sx = bounds.width / 500.0;
        double sy = bounds.height / 150.0;
        Shape inner = new RoundRectangle2D.Double(bounds.x + 25 * sx, bounds.y + 25 * sy,
            450 * sx, 100 * sy, 100 * sy, 100 * sy);
        if (!muted) {
            double fillEnd = volumeTrackStart() + (volumeTrackEnd() - volumeTrackStart()) * volume;
            Graphics2D clipped = (Graphics2D) graphics.create();
            clipped.clip(inner);
            clipped.setColor(VOLUME_FILL);
            clipped.fill(new Rectangle((int) (bounds.x + 25 * sx), bounds.y,
                (int) Math.ceil(fillEnd - (bounds.x + 25 * sx)), bounds.height));
            clipped.dispose();
        }
        BufferedImage frame = AssetCache.get(muted ? "UI_PAUSE_VOLUME_BAR" : "UI_PAUSE_AUDIOBAR_ACTIVE");
        if (frame == null) {
            frame = AssetCache.get("UI_PAUSE_VOLUME_BAR");
        }
        if (frame != null) {
            graphics.drawImage(frame, bounds.x, bounds.y, bounds.width, bounds.height, null);
        }
        if (muted) {
            Rectangle mute = getPauseMuteBounds();
            graphics.setColor(new Color(0xD05A5A));
            graphics.setStroke(new BasicStroke(4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            graphics.drawLine(mute.x + mute.width / 4, mute.y + mute.height / 4,
                mute.x + mute.width * 3 / 4 + 4, mute.y + mute.height * 3 / 4);
            graphics.setStroke(new BasicStroke(1f));
        }
        if (muteHovered) {
            Rectangle mute = getPauseMuteBounds();
            graphics.setColor(new Color(255, 236, 170, 50));
            graphics.fillOval(mute.x + 6, mute.y + 8, mute.width - 4, mute.height - 16);
        }
    }

    // ------------------------------------------------------------------ shared

    private static void drawFrame(Graphics2D graphics, String assetKey) {
        BufferedImage frame = AssetCache.get(assetKey);
        if (frame != null) {
            graphics.drawImage(frame, GameConstants.OVERLAY_MENU_X, GameConstants.OVERLAY_MENU_Y,
                GameConstants.OVERLAY_MENU_WIDTH, GameConstants.OVERLAY_MENU_HEIGHT, null);
        }
    }

    private static Rectangle centeredBounds(int centerX, int y) {
        return new Rectangle(centerX - 180, y - 24, 360, 42);
    }

    private static void drawTextButton(Graphics2D graphics, String text, Rectangle bounds, boolean hovered) {
        UiText.drawInBox(graphics, text, UiFontManager.notoSerif(25, Font.BOLD), bounds,
            hovered ? ACCENT : TEXT, hovered ? HOVER_GLOW : null);
    }
}
