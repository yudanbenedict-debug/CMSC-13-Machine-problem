package com.mygame.engine.hud;

import java.awt.Font;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public final class UiFontManager {
    private static final Font DORRIS = loadDorris();

    private UiFontManager() {
    }

    public static Font dorris(float size, int style) {
        return DORRIS.deriveFont(style, size);
    }

    private static Font loadDorris() {
        List<Path> paths = List.of(
                Path.of("assets", "fonts", "Dorris Regular.ttf"),
                Path.of("ParadigmLocked", "assets", "fonts", "Dorris Regular.ttf"),
                Path.of("..", "assets", "fonts", "Dorris Regular.ttf"),
                Path.of("..", "ParadigmLocked", "assets", "fonts", "Dorris Regular.ttf"));

        for (Path path : paths) {
            if (Files.isRegularFile(path)) {
                try (InputStream input = Files.newInputStream(path)) {
                    return Font.createFont(Font.TRUETYPE_FONT, input);
                } catch (IOException | java.awt.FontFormatException exception) {
                    System.err.println("Unable to load Dorris font: " + path);
                }
            }
        }
        return new Font(Font.SERIF, Font.PLAIN, 18);
    }
}
