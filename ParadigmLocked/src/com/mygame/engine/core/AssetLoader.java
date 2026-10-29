package com.mygame.engine.core;

import com.mygame.engine.helpers.gamequestions.FileLoader;
import com.mygame.engine.hud.UiFontManager;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public final class AssetLoader {
    private static final String[] IMAGE_ASSETS = {
        "UI_LOADING_BACKGROUND_1|images/backgrounds/LOADING_SCREEN_BACKGROUND_VARIANT_1.png",
        "UI_LOADING_BACKGROUND_2|images/backgrounds/LOADING_SCREEN_BACKGROUND_VARIANT_2.png",
        "UI_MAIN_GAME_BACKGROUND|images/backgrounds/MAIN_GAME_BACKGROUND_STATIC.png",
        "UI_OVERLAY_MENU_DEFAULT|images/backgrounds/OVERLAY_MENU_DEFAULT.png",
        "UI_OVERLAY_MENU_PAUSE|images/backgrounds/OVERLAY_MENU_PAUSE.png",
        "UI_CHOICES_BORDER|images/buttons/CHOICES_BORDER.png",
        "UI_CHOICES_BORDER_SELECTED|images/buttons/CHOICES_BORDER_SELECTED.png",
        "UI_RADIAL_FANCY_BORDER|images/buttons/FANCY_BORDER.png",
        "UI_MENU_FANCY_BORDER|images/buttons/FANCY_BORDER.png",
        "UI_RADIAL_FANCY_BORDER_ABANDON_QUESTION|images/buttons/FANCY_BORDER_ABANDON_QUESTION.png",
        "UI_RADIAL_FANCY_BORDER_ANSWER|images/buttons/FANCY_BORDER_ANSWER.png",
        "UI_RADIAL_FANCY_BORDER_CONSUMABLE|images/buttons/FANCY_BORDER_CONSUMABLE.png",
        "UI_RADIAL_FANCY_BORDER_CONTACT_HELP|images/buttons/FANCY_BORDER_CONTACT_HELP.png",
        "UI_PAUSE_VOLUME_BAR|images/buttons/PAAUSE_MENU_VOLUME_BAR.png",
        "UI_MAIN_MENU_LOGO|images/mainmenu/MAIN_MENU_LOGO_STATIC.png",
        "UI_MAIN_MENU_BACKGROUND|images/mainmenu/MAIN_MENU_STATIC.png"
    };

    private static final String[] FONT_ASSETS = {
        "DORRIS|fonts/Dorris Regular.ttf",
        "OPEN_SAUCE_SANS_BOLD|fonts/OpenSauceSans-Bold.ttf",
        "OPEN_SAUCE_SANS_EXTRA_BOLD|fonts/OpenSauceSans-ExtraBold.ttf",
        "OPEN_SAUCE_SANS_LIGHT|fonts/OpenSauceSans-Light.ttf",
        "ARPONA|fonts/Fontspring-DEMO-arpona-bold.otf",
        "NOTO_SERIF|fonts/NotoSerifEthiopic-VariableFont_wdth,wght.ttf",
        "LUCKIEST_GUY|fonts/LuckiestGuy-Regular.ttf"
    };

    private static final String[] CSV_ASSETS = {
        "assets/MCQ/programming_questions.csv",
        "assets/MCQ/theoretical_questions.csv",
        "assets/MCQ/questions.csv"
    };

    private final List<LoadTask> tasks = new ArrayList<>();
    private int nextTask;
    private boolean questionsPreloaded;

    public AssetLoader() {
        for (String asset : IMAGE_ASSETS) {
            String[] parts = asset.split("\\|", 2);
            tasks.add(() -> AssetCache.load(parts[0], parts[1]));
        }
        for (String asset : FONT_ASSETS) {
            String[] parts = asset.split("\\|", 2);
            tasks.add(() -> UiFontManager.preload(parts[0], parts[1]));
        }
        for (String path : CSV_ASSETS) {
            tasks.add(() -> preloadQuestions(path));
        }
        discoverAudioTasks();
    }

    public static void prepareLoadingScreenAssets() {
        AssetCache.load("UI_LOADING_BACKGROUND_1", "images/backgrounds/LOADING_SCREEN_BACKGROUND_VARIANT_1.png");
        AssetCache.load("UI_LOADING_BACKGROUND_2", "images/backgrounds/LOADING_SCREEN_BACKGROUND_VARIANT_2.png");
        UiFontManager.preload("OPEN_SAUCE_SANS_LIGHT", "fonts/OpenSauceSans-Light.ttf");
    }

    public boolean loadNext() {
        if (nextTask >= tasks.size()) {
            return false;
        }
        tasks.get(nextTask++).load();
        return true;
    }

    public double progress() {
        return tasks.isEmpty() ? 1.0 : (double) nextTask / tasks.size();
    }

    public boolean isComplete() {
        return nextTask >= tasks.size();
    }

    private void preloadQuestions(String path) {
        if (!questionsPreloaded) {
            FileLoader fileLoader = new FileLoader();
            fileLoader.getQuestions();
            questionsPreloaded = true;
        }
        try {
            Files.readAllBytes(Path.of(path));
        } catch (IOException exception) {
            System.err.println("Unable to preload CSV asset: " + path
                + " (" + exception.getMessage() + ")");
        }
    }

    private void discoverAudioTasks() {
        Path audioDirectory = Path.of("assets", "audios");
        if (!Files.isDirectory(audioDirectory)) {
            return;
        }
        try (Stream<Path> files = Files.walk(audioDirectory)) {
            files.filter(Files::isRegularFile)
                .filter(path -> path.toString().toLowerCase().endsWith(".wav"))
                .forEach(path -> {
                    String name = audioDirectory.relativize(path).toString()
                        .replace('\\', '/').replace(".wav", "");
                    tasks.add(() -> SoundManager.load(name, "audios/" + audioDirectory.relativize(path)
                        .toString().replace('\\', '/')));
                });
        } catch (IOException exception) {
            System.err.println("Unable to scan audio assets: " + exception.getMessage());
        }
    }

    @FunctionalInterface
    private interface LoadTask {
        void load();
    }
}
