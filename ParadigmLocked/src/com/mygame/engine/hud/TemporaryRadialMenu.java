package com.mygame.engine.hud;

import com.mygame.engine.core.AssetCache;
import com.mygame.engine.core.SoundManager;
import com.mygame.engine.helpers.algorithms.EasingMethods;
import com.mygame.engine.helpers.algorithms.TransformMethods;
import com.mygame.engine.helpers.constants.GameConstants;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;
import java.util.function.Consumer;

public class TemporaryRadialMenu {
    public enum Option {
        ANSWER(70, new Color(0x7FB05A), "FANCY_BORDER_ANSWER", "Answer"),
        CONTACT_HELP(49, new Color(0xC8B560), "FANCY_BORDER_CONTACT_HELP", "Contact\nDeclarative Dynamo"),
        ABANDON(28, new Color(0xD05A5A), "FANCY_BORDER_ABANDON_QUESTION", "Abandon Question"),
        CONSUMABLE(7, new Color(0x6A8CC8), "FANCY_BORDER_CONSUMABLE", "Consumable");

        private final double angle;
        private final Color color;
        private final String assetKey;
        private final String label;

        Option(double angle, Color color, String assetName, String label) {
            this.angle = angle;
            this.color = color;
            this.assetKey = "UI_RADIAL_" + assetName;
            this.label = label;
        }

        public String label() {
            return label;
        }

        public String description() {
            return switch (this) {
                case ANSWER -> "Choose an answer below.";
                case CONTACT_HELP -> "Declarative Dynamo hint panel.";
                case ABANDON -> "Do you really wish to Abandon this question?";
                case CONSUMABLE -> "Choose a consumable for this turn.";
            };
        }
    }

    private static final double SECTOR = 21;
    private static final double IDLE_ANGLE = 0;
    private final double centerX = GameConstants.TEMPORARY_RADIAL_CENTER_X;
    private final double centerY = GameConstants.TEMPORARY_RADIAL_CENTER_Y;
    private final double dialRadius = GameConstants.TEMPORARY_RADIAL_DIAL_RADIUS;
    private final double itemRadius = GameConstants.TEMPORARY_RADIAL_ITEM_RADIUS;
    private final Option[] options = Option.values();
    private final double[] scales = new double[options.length];
    private int hovered = -1;
    private int active = -1;
    private double arrowAngle = IDLE_ANGLE;
    private double arrowTarget = IDLE_ANGLE;
    private double arrowDelayRemaining;
    private double wedge;
    private Consumer<Option> selectionListener;

    public TemporaryRadialMenu() {
        for (int index = 0; index < options.length; index++) {
            scales[index] = 1.0;
        }
    }

    public void setSelectionListener(Consumer<Option> selectionListener) {
        this.selectionListener = selectionListener;
    }

    public Option getActiveOption() {
        return active < 0 ? null : options[active];
    }

    /** Programmatically opens an option (e.g. jump back to ANSWER after using a consumable). */
    public void select(Option option) {
        for (int index = 0; index < options.length; index++) {
            if (options[index] == option) {
                active = index;
            }
        }
        if (selectionListener != null) {
            selectionListener.accept(option);
        }
    }

    public void clearSelection() {
        active = -1;
        if (selectionListener != null) {
            selectionListener.accept(null);
        }
    }

    public void update(double deltaTime) {
        int focused = focusIndex();
        double targetArrow = focused >= 0 ? options[focused].angle : IDLE_ANGLE;
        if (Double.compare(targetArrow, arrowTarget) != 0) {
            arrowTarget = targetArrow;
            arrowDelayRemaining = GameConstants.TEMPORARY_RADIAL_ARROW_DELAY;
        }
        if (arrowDelayRemaining > 0) {
            arrowDelayRemaining = Math.max(0, arrowDelayRemaining - Math.max(0, deltaTime));
        } else {
            arrowAngle = EasingMethods.approachAngle(arrowAngle, targetArrow, 14, deltaTime);
        }
        wedge = EasingMethods.approach(wedge, focused >= 0 ? 1 : 0, 12, deltaTime);

        for (int index = 0; index < options.length; index++) {
            double targetScale = active >= 0 && index == active ? 1.25 : 1.0;
            scales[index] = EasingMethods.approach(scales[index], targetScale, 12, deltaTime);
        }
    }

    public void draw(Graphics2D originalGraphics) {
        Graphics2D graphics = (Graphics2D) originalGraphics.create();
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        drawDial(graphics);
        int focused = focusIndex();
        for (int index = 0; index < options.length; index++) {
            if (index != focused) {
                drawItem(graphics, index);
            }
        }
        if (focused >= 0) {
            drawItem(graphics, focused);
        }
        graphics.dispose();
    }

    public void mouseMoved(Point point) {
        int picked = pick(point.x, point.y);
        if (picked >= 0 && picked != hovered) {
            // pooled clip, so fast sweeps across the options stack into a clicking ratchet
            SoundManager.playSFX("RADIAL_CHOICES_HOVER_01");
        }
        hovered = picked;
    }

    public void mousePressed(Point point) {
        int selected = pick(point.x, point.y);
        if (selected >= 0) {
            active = active == selected ? -1 : selected;
            if (selectionListener != null) {
                selectionListener.accept(active < 0 ? null : options[active]);
            }
        }
    }

    public void mouseExited() {
        hovered = -1;
    }

    private int focusIndex() {
        return hovered >= 0 ? hovered : active;
    }

    private int pick(double mouseX, double mouseY) {
        double distance = TransformMethods.distance(centerX, centerY, mouseX, mouseY);
        if (distance < dialRadius || distance > itemRadius + GameConstants.TEMPORARY_RADIAL_LABEL_WIDTH / 2.0 + 40) {
            return -1;
        }

        double mouseAngle = TransformMethods.screenAngle(centerX, centerY, mouseX, mouseY);
        int best = -1;
        double bestDifference = SECTOR / 2;
        for (int index = 0; index < options.length; index++) {
            double difference = Math.abs(TransformMethods.shortestAngleDifference(mouseAngle, options[index].angle));
            if (difference < bestDifference) {
                bestDifference = difference;
                best = index;
            }
        }
        return best;
    }

    private void drawDial(Graphics2D graphics) {
        double diameter = dialRadius * 2;
        Ellipse2D dial = new Ellipse2D.Double(centerX - dialRadius, centerY - dialRadius, diameter, diameter);
        graphics.setColor(new Color(40, 40, 40));
        graphics.fill(dial);
        graphics.setColor(new Color(150, 150, 150, (int) (110 * wedge)));
        graphics.fill(new Arc2D.Double(centerX - dialRadius, centerY - dialRadius, diameter, diameter,
                arrowAngle - SECTOR / 2, SECTOR, Arc2D.PIE));
        graphics.setColor(Color.WHITE);
        graphics.setStroke(new BasicStroke(3f));
        graphics.draw(dial);

        double tail = -dialRadius * 0.85;
        double length = dialRadius * 0.95;
        double head = dialRadius * 0.35;
        double width = dialRadius * 0.10;
        Path2D arrow = new Path2D.Double();
        arrow.moveTo(tail, -width);
        arrow.lineTo(length - head, -width);
        arrow.lineTo(length - head, -head / 2);
        arrow.lineTo(length, 0);
        arrow.lineTo(length - head, head / 2);
        arrow.lineTo(length - head, width);
        arrow.lineTo(tail, width);
        arrow.closePath();
        AffineTransform oldTransform = graphics.getTransform();
        graphics.transform(TransformMethods.radialTransform(centerX, centerY, arrowAngle, 1));
        graphics.setColor(Color.BLACK);
        graphics.fill(arrow);
        graphics.setColor(Color.WHITE);
        graphics.draw(arrow);
        graphics.setTransform(oldTransform);
    }

    private void drawItem(Graphics2D graphics, int index) {
        Option option = options[index];
        java.awt.geom.Point2D.Double position = TransformMethods.polarToScreen(centerX, centerY, itemRadius, option.angle);
        BufferedImage border = AssetCache.get(active == index
            ? option.assetKey : "UI_RADIAL_FANCY_BORDER");

        AffineTransform oldTransform = graphics.getTransform();
        graphics.transform(TransformMethods.radialTransform(position.x, position.y, option.angle, scales[index]));
        if (border != null) {
            graphics.drawImage(border,
                    -GameConstants.TEMPORARY_RADIAL_LABEL_WIDTH / 2,
                    -GameConstants.TEMPORARY_RADIAL_LABEL_HEIGHT / 2,
                    GameConstants.TEMPORARY_RADIAL_LABEL_WIDTH,
                    GameConstants.TEMPORARY_RADIAL_LABEL_HEIGHT, null);
        } else {
            graphics.setColor(new Color(20, 20, 20));
            graphics.fillRoundRect(-GameConstants.TEMPORARY_RADIAL_LABEL_WIDTH / 2,
                    -GameConstants.TEMPORARY_RADIAL_LABEL_HEIGHT / 2,
                    GameConstants.TEMPORARY_RADIAL_LABEL_WIDTH,
                    GameConstants.TEMPORARY_RADIAL_LABEL_HEIGHT, 14, 14);
            graphics.setColor(option.color);
            graphics.drawRoundRect(-GameConstants.TEMPORARY_RADIAL_LABEL_WIDTH / 2,
                    -GameConstants.TEMPORARY_RADIAL_LABEL_HEIGHT / 2,
                    GameConstants.TEMPORARY_RADIAL_LABEL_WIDTH,
                    GameConstants.TEMPORARY_RADIAL_LABEL_HEIGHT, 14, 14);
        }
        Color glow = index == active ? UiText.DEFAULT_GLOW
            : index == hovered ? new Color(255, 236, 170, 140) : null;
        if (option == Option.CONTACT_HELP) {
            // "Contact" is left-aligned with the start of "Declarative Dynamo" (2pt smaller than before)
            Font nameFont = UiFontManager.dorris(14, Font.BOLD);
            float nameX = -UiText.width("Declarative Dynamo", nameFont) / 2f;
            UiText.draw(graphics, "Contact", UiFontManager.dorris(10, Font.BOLD), nameX, -6, Color.WHITE, glow);
            UiText.draw(graphics, "Declarative Dynamo", nameFont, nameX, 11, Color.WHITE, glow);
        } else {
            Font font = UiFontManager.dorris(option == Option.ABANDON ? 15 : 18, Font.BOLD);
            java.awt.font.TextLayout layout = UiText.layout(option.label, font);
            float baseline = (layout.getAscent() - layout.getDescent()) / 2f;
            UiText.drawLayout(graphics, layout, -layout.getAdvance() / 2f, baseline, Color.WHITE, glow);
        }
        graphics.setTransform(oldTransform);
    }
}
