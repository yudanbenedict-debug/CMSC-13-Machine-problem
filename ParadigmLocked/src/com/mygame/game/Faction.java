package com.mygame.game;

import java.awt.Color;

// Teams; the loading screen art decides which one you play.
public enum Faction {
    DARKWEBKING("Dark Web King", new Color(0xF0, 0x8A, 0x3C), "UI_LOADING_BACKGROUND_DARKWEBKING"),
    IMMUTABLEMOTHER("Immutable Mother", new Color(0x8F, 0xA3, 0xFF), "UI_LOADING_BACKGROUND_IMMUTABLEMOTHER");

    public final String displayName;
    public final Color color;
    public final String loadingBackgroundKey;

    Faction(String displayName, Color color, String loadingBackgroundKey) {
        this.displayName = displayName;
        this.color = color;
        this.loadingBackgroundKey = loadingBackgroundKey;
    }

    public Faction opponent() {
        return this == DARKWEBKING ? IMMUTABLEMOTHER : DARKWEBKING;
    }

    private static Faction player = IMMUTABLEMOTHER;

    public static Faction player() {
        return player;
    }

    public static Faction enemy() {
        return player.opponent();
    }

    public static void setPlayer(Faction faction) {
        player = faction;
    }
}
