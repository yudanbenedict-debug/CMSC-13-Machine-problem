package com.mygame.engine.hud;

import com.mygame.engine.helpers.constants.GameConstants;
import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

// Procedural circuit-board overlay: a pre-drawn trace mask tinted to any color, plus glowing signal pulses.
public final class CircuitBackground {
    public static final Color NEUTRAL = new Color(0x39, 0xC4, 0x6B);
    public static final Color BLUE = new Color(0x4F, 0x7B, 0xFF);
    public static final Color RED = new Color(0xFF, 0x4A, 0x3D);
    private static final int WIDTH = GameConstants.LOGICAL_WIDTH;
    private static final int HEIGHT = GameConstants.LOGICAL_HEIGHT;
    private static final int GRID = 20;
    private static CircuitBackground menuInstance;

    private final int[] mask = new int[WIDTH * HEIGHT];
    private final List<float[]> paths = new ArrayList<>();
    private final Pulse[] pulses;
    private final Random random;
    private final float opacity;
    // opaque frame = background art + tinted circuit, rebuilt only when the color changes (opaque draws are cheap)
    private final BufferedImage tinted = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
    // GPU-cached copy of `tinted` (editing tinted's pixels directly stops Java2D from caching it)
    private BufferedImage display;
    private BufferedImage baseSource;
    private int[] basePixels;
    private final int[] tintedPixels = ((DataBufferInt) tinted.getRaster().getDataBuffer()).getData();
    private int tintedKey = Integer.MIN_VALUE;
    private double red = NEUTRAL.getRed();
    private double green = NEUTRAL.getGreen();
    private double blue = NEUTRAL.getBlue();
    private double strength = 0.6;
    private Color target = NEUTRAL;
    private double targetStrength = 0.6;
    private long lastNanos = System.nanoTime();
    private double clock;

    private static final class Pulse {
        int path;
        double distance;
        double speed;
    }

    public CircuitBackground(long seed, float opacity) {
        this.random = new Random(seed);
        this.opacity = opacity;
        generate();
        pulses = new Pulse[18];
        for (int index = 0; index < pulses.length; index++) {
            pulses[index] = new Pulse();
            respawn(pulses[index]);
            pulses[index].distance = random.nextDouble() * pathLength(paths.get(pulses[index].path));
        }
    }

    // Shared instance for the menu screens so the animation stays continuous between them.
    public static CircuitBackground menu() {
        if (menuInstance == null) {
            menuInstance = new CircuitBackground(13L, 0.9f);
        }
        return menuInstance;
    }

    // Color to ease toward; strength 0..1 controls how bright / vibrant the traces get.
    public void setTarget(Color color, double vibrancy) {
        target = color;
        targetStrength = Math.max(0, Math.min(1, vibrancy));
    }

    // menu cycle: green -> blue -> red -> green, 6 seconds per leg with a smooth blend
    public void setMenuCycle() {
        double phase = (clock / 6.0) % 3.0;
        int leg = (int) phase;
        double t = smooth(phase - leg);
        Color[] cycle = {NEUTRAL, BLUE, RED, NEUTRAL};
        setTarget(mix(cycle[leg], cycle[leg + 1], t), 0.6);
    }

    // Draws the background art (may be null) with the circuit on top, then the moving pulses.
    public void draw(Graphics2D g, BufferedImage base) {
        if (base != baseSource) {
            baseSource = base;
            basePixels = readBase(base);
            tintedKey = Integer.MIN_VALUE;
        }
        long now = System.nanoTime();
        double dt = Math.min(0.1, (now - lastNanos) / 1e9);
        lastNanos = now;
        clock += dt;

        double ease = 1 - Math.exp(-2.5 * dt);
        red += (target.getRed() - red) * ease;
        green += (target.getGreen() - green) * ease;
        blue += (target.getBlue() - blue) * ease;
        strength += (targetStrength - strength) * ease;
        if (retintIfNeeded() || display == null) {
            if (display == null) {
                display = createDisplayImage();
            }
            Graphics2D copy = display.createGraphics();
            copy.drawImage(tinted, 0, 0, null);
            copy.dispose();
        }
        g.drawImage(display, 0, 0, null);
        drawPulses(g, dt);
    }

    // re-colors the mask only when the color moved by a visible step (cheap enough to do during fades)
    private static BufferedImage createDisplayImage() {
        if (java.awt.GraphicsEnvironment.isHeadless()) {
            return new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
        }
        return java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice()
            .getDefaultConfiguration().createCompatibleImage(WIDTH, HEIGHT, java.awt.Transparency.OPAQUE);
    }

    private boolean retintIfNeeded() {
        int r = (int) Math.round(red) & ~3;
        int gr = (int) Math.round(green) & ~3;
        int b = (int) Math.round(blue) & ~3;
        int s = (int) Math.round(strength * 40);
        int key = (((r << 16) | (gr << 8) | b) << 6) | s;
        if (key == tintedKey) {
            return false;
        }
        tintedKey = key;
        float scale = opacity * (0.35f + 0.9f * (float) strength);
        for (int index = 0; index < mask.length; index++) {
            int alpha = Math.min(255, (int) (mask[index] * scale));
            int under = basePixels[index];
            // standard "over" blend: base + (tint - base) * alpha
            int br = (under >> 16) & 0xFF;
            int bg = (under >> 8) & 0xFF;
            int bb = under & 0xFF;
            tintedPixels[index] = ((br + (r - br) * alpha / 255) << 16)
                | ((bg + (gr - bg) * alpha / 255) << 8) | (bb + (b - bb) * alpha / 255);
        }
        return true;
    }

    private static int[] readBase(BufferedImage base) {
        int[] pixels = new int[WIDTH * HEIGHT];
        if (base == null) {
            java.util.Arrays.fill(pixels, 0x0F161E);
            return pixels;
        }
        BufferedImage sized = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = sized.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(base, 0, 0, WIDTH, HEIGHT, null);
        g.dispose();
        return sized.getRGB(0, 0, WIDTH, HEIGHT, pixels, 0, WIDTH);
    }

    private void drawPulses(Graphics2D g, double dt) {
        Object oldAa = g.getRenderingHint(RenderingHints.KEY_ANTIALIASING);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        Composite oldComposite = g.getComposite();
        Color bright = mix(new Color((int) red, (int) green, (int) blue), Color.WHITE, 0.45);
        float alphaScale = Math.min(1f, opacity * (0.45f + 0.7f * (float) strength));
        for (Pulse pulse : pulses) {
            float[] path = paths.get(pulse.path);
            pulse.distance += pulse.speed * dt;
            if (pulse.distance >= pathLength(path)) {
                respawn(pulse);
                continue;
            }
            float[] point = pointAt(path, pulse.distance);
            double[] radii = {9, 5, 2.4};
            float[] alphas = {0.10f, 0.25f, 0.9f};
            for (int layer = 0; layer < radii.length; layer++) {
                g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alphas[layer] * alphaScale));
                g.setColor(layer == 2 ? Color.WHITE : bright);
                g.fill(new Ellipse2D.Double(point[0] - radii[layer], point[1] - radii[layer],
                    radii[layer] * 2, radii[layer] * 2));
            }
        }
        g.setComposite(oldComposite);
        if (oldAa != null) {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, oldAa);
        }
    }

    private void respawn(Pulse pulse) {
        pulse.path = random.nextInt(paths.size());
        pulse.distance = 0;
        pulse.speed = 90 + random.nextDouble() * 140;
    }

    private void generate() {
        BufferedImage image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

        for (int chip = 0; chip < 6; chip++) {
            drawChip(g);
        }
        for (int trace = 0; trace < 95; trace++) {
            float[] path = randomTrace();
            if (path != null) {
                paths.add(path);
                drawTrace(g, path);
            }
        }
        g.dispose();

        int[] argb = image.getRGB(0, 0, WIDTH, HEIGHT, null, 0, WIDTH);
        double cx = WIDTH / 2.0;
        double cy = HEIGHT / 2.0;
        for (int y = 0; y < HEIGHT; y++) {
            for (int x = 0; x < WIDTH; x++) {
                int index = y * WIDTH + x;
                // faint tinted glow toward the edges so the screen has the team's mood even between traces
                double dx = (x - cx) / cx;
                double dy = (y - cy) / cy;
                int vignette = (int) (22 * Math.min(1, (dx * dx + dy * dy) * 0.6));
                mask[index] = Math.max(argb[index] >>> 24, vignette);
            }
        }
    }

    // walks the grid in 8 directions (mostly straight, sometimes 45 degrees) to make one trace
    private float[] randomTrace() {
        int x = random.nextInt(WIDTH / GRID) * GRID;
        int y = random.nextInt(HEIGHT / GRID) * GRID;
        int[][] directions = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}, {1, 1}, {1, -1}, {-1, 1}, {-1, -1}};
        int direction = random.nextInt(4);
        List<Float> points = new ArrayList<>();
        points.add((float) x);
        points.add((float) y);
        int segments = 2 + random.nextInt(4);
        for (int segment = 0; segment < segments; segment++) {
            int steps = 2 + random.nextInt(7);
            int nx = x + directions[direction][0] * steps * GRID;
            int ny = y + directions[direction][1] * steps * GRID;
            if (nx < 0 || ny < 0 || nx > WIDTH || ny > HEIGHT) {
                break;
            }
            x = nx;
            y = ny;
            points.add((float) x);
            points.add((float) y);
            direction = turn(direction);
        }
        if (points.size() < 4) {
            return null;
        }
        float[] path = new float[points.size()];
        for (int index = 0; index < path.length; index++) {
            path[index] = points.get(index);
        }
        return path;
    }

    // straight traces turn 90 degrees or bend 45 degrees; diagonals straighten out again
    private int turn(int direction) {
        if (direction >= 4) {
            return random.nextInt(4);
        }
        if (random.nextDouble() < 0.35) {
            return 4 + random.nextInt(4);
        }
        return direction < 2 ? 2 + random.nextInt(2) : random.nextInt(2);
    }

    private void drawTrace(Graphics2D g, float[] path) {
        Path2D.Float shape = new Path2D.Float();
        shape.moveTo(path[0], path[1]);
        for (int index = 2; index < path.length; index += 2) {
            shape.lineTo(path[index], path[index + 1]);
        }
        g.setColor(new Color(255, 255, 255, 18));
        g.setStroke(new BasicStroke(7f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.draw(shape);
        g.setColor(new Color(255, 255, 255, 120));
        g.setStroke(new BasicStroke(1.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.draw(shape);
        drawPad(g, path[0], path[1]);
        drawPad(g, path[path.length - 2], path[path.length - 1]);
    }

    private void drawPad(Graphics2D g, float x, float y) {
        g.setColor(new Color(255, 255, 255, 40));
        g.fill(new Ellipse2D.Float(x - 7, y - 7, 14, 14));
        g.setColor(new Color(255, 255, 255, 170));
        g.setStroke(new BasicStroke(1.6f));
        g.draw(new Ellipse2D.Float(x - 3.5f, y - 3.5f, 7, 7));
    }

    private void drawChip(Graphics2D g) {
        int width = (4 + random.nextInt(5)) * GRID;
        int height = (3 + random.nextInt(4)) * GRID;
        int x = random.nextInt((WIDTH - width) / GRID) * GRID;
        int y = random.nextInt((HEIGHT - height) / GRID) * GRID;
        g.setColor(new Color(255, 255, 255, 14));
        g.fill(new RoundRectangle2D.Float(x, y, width, height, 8, 8));
        g.setColor(new Color(255, 255, 255, 90));
        g.setStroke(new BasicStroke(1.4f));
        g.draw(new RoundRectangle2D.Float(x, y, width, height, 8, 8));
        for (int pin = x + 10; pin < x + width - 6; pin += 10) {
            g.drawLine(pin, y - 6, pin, y);
            g.drawLine(pin, y + height, pin, y + height + 6);
        }
    }

    private static float pathLength(float[] path) {
        float length = 0;
        for (int index = 2; index < path.length; index += 2) {
            length += (float) Math.hypot(path[index] - path[index - 2], path[index + 1] - path[index - 1]);
        }
        return length;
    }

    private static float[] pointAt(float[] path, double distance) {
        for (int index = 2; index < path.length; index += 2) {
            float segment = (float) Math.hypot(path[index] - path[index - 2], path[index + 1] - path[index - 1]);
            if (distance <= segment) {
                float t = (float) (distance / segment);
                return new float[] {path[index - 2] + (path[index] - path[index - 2]) * t,
                    path[index - 1] + (path[index + 1] - path[index - 1]) * t};
            }
            distance -= segment;
        }
        return new float[] {path[path.length - 2], path[path.length - 1]};
    }

    private static double smooth(double t) {
        return t * t * (3 - 2 * t);
    }

    public static Color mix(Color from, Color to, double t) {
        double clamped = Math.max(0, Math.min(1, t));
        return new Color((int) (from.getRed() + (to.getRed() - from.getRed()) * clamped),
            (int) (from.getGreen() + (to.getGreen() - from.getGreen()) * clamped),
            (int) (from.getBlue() + (to.getBlue() - from.getBlue()) * clamped));
    }
}
