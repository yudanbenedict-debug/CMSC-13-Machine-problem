package com.mygame.engine.core;

import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import javax.imageio.ImageIO;

// Loaded images by Img, plus cached downscaled copies so big images aren't resized every frame.
public final class AssetCache {
    private static final Map<Img, BufferedImage> IMAGES = new ConcurrentHashMap<>();
    private static final Map<String, BufferedImage> SCALED = new ConcurrentHashMap<>();
    private static final Set<Img> REPORTED_MISSING = ConcurrentHashMap.newKeySet();

    private AssetCache() {
    }

    public static void load(Img img) {
        if (IMAGES.containsKey(img)) {
            return;
        }
        try (InputStream input = Resources.open(img.path)) {
            if (input == null) {
                throw new IOException("not found in the JAR or the assets folder");
            }
            IMAGES.put(img, toCompatibleImage(ImageIO.read(input)));
        } catch (IOException e) {
            System.err.println("[AssetCache] Failed to load " + img.path + ": " + e.getMessage());
        }
    }

    // Returns null (and reports it once) when the image failed to load, so the caller can draw a fallback.
    public static BufferedImage get(Img img) {
        BufferedImage image = IMAGES.get(img);
        if (image == null && REPORTED_MISSING.add(img)) {
            System.err.println("[AssetCache] Missing image: " + img + " (" + img.path + ")");
        }
        return image;
    }

    public static BufferedImage getScaled(Img img, int width, int height) {
        BufferedImage source = get(img);
        return source == null ? null : getScaled(img, 0, 0, source.getWidth(), source.getHeight(), width, height);
    }

    // Downscaled copy of the (x, y, w, h) part of an image, cached.
    public static BufferedImage getScaled(Img img, int x, int y, int w, int h, int width, int height) {
        String cacheKey = img + "|" + x + '|' + y + '|' + w + '|' + h + '|' + width + '|' + height;
        BufferedImage cached = SCALED.get(cacheKey);
        if (cached != null) {
            return cached;
        }
        BufferedImage source = get(img);
        if (source == null) {
            return null;
        }
        BufferedImage current = source.getSubimage(x, y, w, h);
        int currentWidth = w;
        int currentHeight = h;
        // halve step by step (bilinear) until the target size: much smoother than one big jump
        do {
            currentWidth = Math.max(width, currentWidth / 2);
            currentHeight = Math.max(height, currentHeight / 2);
            BufferedImage step = new BufferedImage(currentWidth, currentHeight, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = step.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g.drawImage(current, 0, 0, currentWidth, currentHeight, null);
            g.dispose();
            current = step;
        } while (currentWidth != width || currentHeight != height);
        current = toCompatibleImage(current);
        SCALED.put(cacheKey, current);
        return current;
    }

    // Copies the image into the screen's pixel format once, so every later draw skips a conversion.
    private static BufferedImage toCompatibleImage(BufferedImage source) {
        if (source == null || GraphicsEnvironment.isHeadless()) {
            return source;
        }
        try {
            GraphicsConfiguration config = GraphicsEnvironment.getLocalGraphicsEnvironment()
                .getDefaultScreenDevice().getDefaultConfiguration();
            if (source.getColorModel().equals(config.getColorModel(source.getTransparency()))) {
                return source;
            }
            BufferedImage compatible = config.createCompatibleImage(
                source.getWidth(), source.getHeight(), source.getTransparency());
            Graphics2D g = compatible.createGraphics();
            g.setComposite(AlphaComposite.Src);
            g.drawImage(source, 0, 0, null);
            g.dispose();
            return compatible;
        } catch (RuntimeException e) {
            return source; // never fail loading just because the optimization failed
        }
    }
}
