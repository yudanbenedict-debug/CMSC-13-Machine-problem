package com.mygame.engine.hud;

import java.awt.Font;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class UiFontManager {
    private static final Map<String, Font> FONTS = new ConcurrentHashMap<>();
    // deriveFont() creates a new Font object every call, and the UI calls it many times per frame.
    // Derived fonts are cached by key|style|size so each size is only created once.
    private static final Map<String, Font> DERIVED = new ConcurrentHashMap<>();
    private static final Font FALLBACK = new Font(Font.SERIF, Font.PLAIN, 18);

    private UiFontManager() {
    }

    public static Font dorris(float size, int style) {
        return font("DORRIS", size, style);
    }

    public static void preload(String key, String path) {
        if (FONTS.containsKey(key)) {
            return;
        }
        for (Path candidate : List.of(
                Path.of("assets", path),
                Path.of("ParadigmLocked", "assets", path),
                Path.of("..", "assets", path),
                Path.of("..", "ParadigmLocked", "assets", path))) {
            if (Files.isRegularFile(candidate)) {
                try (InputStream input = Files.newInputStream(candidate)) {
                    FONTS.put(key, Font.createFont(Font.TRUETYPE_FONT, input));
                    DERIVED.clear(); // drop any fallback fonts cached before this font existed
                    return;
                } catch (IOException | java.awt.FontFormatException exception) {
                    System.err.println("Unable to load font: " + candidate);
                    return;
                }
            }
        }
        System.err.println("Unable to find font: " + path);
    }

    public static Font question(float size, int style) {
        return font("QUESTION_FONT", size, style);
    }

    public static Font choices(float size, int style) {
        return font("CHOICES_FONT", size, style);
    }

    public static Font openSauceSansBold(float size, int style) {
        return font("OPEN_SAUCE_SANS_BOLD", size, style);
    }

    public static Font openSauceSansLight(float size, int style) {
        return font("OPEN_SAUCE_SANS_LIGHT", size, style);
    }

    public static Font arpona(float size, int style) {
        return font("ARPONA", size, style);
    }

    public static Font notoSerif(float size, int style) {
        return font("NOTO_SERIF", size, style);
    }

    public static Font luckiestGuy(float size, int style) {
        return font("LUCKIEST_GUY", size, style);
    }

    private static Font font(String key, float size, int style) {
        String cacheKey = key + '|' + style + '|' + size;
        Font cached = DERIVED.get(cacheKey);
        if (cached == null) {
            cached = FONTS.getOrDefault(key, FALLBACK).deriveFont(style, size);
            DERIVED.put(cacheKey, cached);
        }
        return cached;
    }

}
