package com.mygame.game;

import java.awt.Color;

/*
    The two bosses / teams. Which one you fight for is decided by the loading screen:
    the Dark Web King (orange) loading art means you are on the Hidden King's team,
    the Immutable Mother (blue) art means you are on hers. The other one is the enemy.
    The team decides the HUD colors and which WIN / LOSE overlay is shown.
 */
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

    // ---- the player's team for this launch (set by LoadingState) ----
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
