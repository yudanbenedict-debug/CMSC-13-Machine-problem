package com.mygame.engine.core;

// Every sound the game loads (assets/audios/<NAME>.wav). THEME / _THEME names are looping music.
public enum Sfx {
    THEME_MAIN_MENU, MAIN_GAME_THEME,
    UI_MENU_HOVER_00, UI_MENU_HOVER_01, UI_MENU_HOVER_02, UI_MENU_ONTITLESTART,
    RADIAL_CHOICES_HOVER_01, HOW_TO_PLAY_MENU_ONSELECT, CREDIT_MENU_ONSELECT,
    HINT_PROMPT, DAMAGE_TICK, LOW_HP_ALERT, GAME_WON, GAME_LOST, CONSUMABLE_SPAWN,
    CONSUMABLE_NULLSLINGER_USED, CONSUMABLE_SCRIPTERS_SACRIFICE_USED, CONSUMABLE_REFRESHER_USED;

    public String path() {
        return "audios/" + name() + ".wav";
    }

    public boolean isMusic() {
        return name().startsWith("THEME") || name().endsWith("_THEME");
    }
}
