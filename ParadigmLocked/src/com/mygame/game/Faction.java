package com.mygame.game;

import com.mygame.engine.core.Img;
import java.awt.Color;

// The two teams; picked on the side-select screen (the loading art only pre-highlights one).
public enum Faction {
    DARKWEBKING("Dark Web King", new Color(0xF0, 0x8A, 0x3C), new Color(0xFF, 0x4A, 0x3D),
        Img.LOADING_BACKGROUND_DARKWEBKING, Img.OVERLAY_DARKWEBKING_WIN, Img.OVERLAY_DARKWEBKING_LOSE),
    IMMUTABLEMOTHER("Immutable Mother", new Color(0x8F, 0xA3, 0xFF), new Color(0x4F, 0x7B, 0xFF),
        Img.LOADING_BACKGROUND_IMMUTABLEMOTHER, Img.OVERLAY_IMMUTABLEMOTHER_WIN, Img.OVERLAY_IMMUTABLEMOTHER_LOSE);

    public final String displayName;
    public final Color color;
    public final Color circuitColor;
    public final Img loadingBackground;
    public final Img winOverlay;
    public final Img loseOverlay;

    Faction(String displayName, Color color, Color circuitColor, Img loadingBackground, Img winOverlay,
            Img loseOverlay) {
        this.displayName = displayName;
        this.color = color;
        this.circuitColor = circuitColor;
        this.loadingBackground = loadingBackground;
        this.winOverlay = winOverlay;
        this.loseOverlay = loseOverlay;
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
