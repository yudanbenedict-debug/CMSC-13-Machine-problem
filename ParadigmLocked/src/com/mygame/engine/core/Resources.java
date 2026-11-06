package com.mygame.engine.core;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

// Opens an asset (path relative to the assets folder) from the JAR first, then from the assets folder on disk.
public final class Resources {
    private static final String[] FOLDERS = {"assets", "ParadigmLocked/assets", "../assets", "."};

    private Resources() {
    }

    public static InputStream open(String assetPath) throws IOException {
        String clean = assetPath.startsWith("/") ? assetPath.substring(1) : assetPath;
        if (clean.startsWith("assets/")) {
            clean = clean.substring("assets/".length());
        }
        InputStream bundled = Resources.class.getResourceAsStream("/" + clean);
        if (bundled != null) {
            return bundled;
        }
        for (String folder : FOLDERS) {
            Path candidate = Path.of(folder, clean);
            if (Files.isRegularFile(candidate)) {
                return Files.newInputStream(candidate);
            }
        }
        return null;
    }
}
