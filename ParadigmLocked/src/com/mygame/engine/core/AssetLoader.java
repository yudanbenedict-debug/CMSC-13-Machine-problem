package com.mygame.engine.core;

import com.mygame.engine.helpers.gamequestions.FileLoader;
import com.mygame.engine.hud.UiFontManager;
import java.util.ArrayList;
import java.util.List;

// Loads every image, font, sound and the questions one step per frame so the loading bar can move.
public final class AssetLoader {
    private static final String[][] FONTS = {
        {"DORRIS", "fonts/Dorris Regular.ttf"},
        {"OPEN_SAUCE_SANS_LIGHT", "fonts/OpenSauceSans-Light.ttf"},
        {"ARPONA", "fonts/Fontspring-DEMO-arpona-bold.otf"},
        {"NOTO_SERIF", "fonts/NotoSerifEthiopic-VariableFont_wdth,wght.ttf"},
        {"LUCKIEST_GUY", "fonts/LuckiestGuy-Regular.ttf"},
        {"QUESTION_FONT", "fonts/QUESTION_FONT.ttf"},
        {"CHOICES_FONT", "fonts/CHOICES_FONT.ttf"}
    };

    private final List<Runnable> tasks = new ArrayList<>();
    private int nextTask;

    public AssetLoader() {
        for (Img img : Img.values()) {
            tasks.add(() -> AssetCache.load(img));
        }
        for (String[] font : FONTS) {
            tasks.add(() -> UiFontManager.preload(font[0], font[1]));
        }
        tasks.add(() -> new FileLoader().getQuestions());
        for (Sfx sound : Sfx.values()) {
            tasks.add(() -> SoundManager.load(sound));
        }
    }

    // What the loading screen itself needs before the rest loads.
    public static void prepareLoadingScreenAssets() {
        AssetCache.load(Img.LOADING_BACKGROUND_DARKWEBKING);
        AssetCache.load(Img.LOADING_BACKGROUND_IMMUTABLEMOTHER);
        UiFontManager.preload("OPEN_SAUCE_SANS_LIGHT", "fonts/OpenSauceSans-Light.ttf");
    }

    public boolean loadNext() {
        if (nextTask >= tasks.size()) {
            return false;
        }
        tasks.get(nextTask++).run();
        return true;
    }

    public double progress() {
        return tasks.isEmpty() ? 1.0 : (double) nextTask / tasks.size();
    }

    public boolean isComplete() {
        return nextTask >= tasks.size();
    }
}
