package com.mygame.engine.hud;

import java.awt.Font;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class UiFontManager {
    private static final Map<String, Font> FONTS = new HashMap<>();
    private static final Font FALLBACK = new Font(Font.SERIF, Font.PLAIN, 18);

    private UiFontManager() {
    }

    public static Font dorris(float size, int style) {
        return FONTS.getOrDefault("DORRIS", FALLBACK).deriveFont(style, size);
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
                    return;
                } catch (IOException | java.awt.FontFormatException exception) {
                    System.err.println("Unable to load font: " + candidate);
                    return;
                }
            }
        }
        System.err.println("Unable to find font: " + path);
    }

    public static Font openSauceSansLight(float size, int style) {
        Font font = FONTS.get("OPEN_SAUCE_SANS_LIGHT");
        return (font == null ? FALLBACK : font).deriveFont(style, size);
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
        return FONTS.getOrDefault(key, FALLBACK).deriveFont(style, size);
    }

}
