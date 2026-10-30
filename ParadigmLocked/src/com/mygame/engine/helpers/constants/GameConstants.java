package com.mygame.engine.helpers.constants;

public final class GameConstants {
    public static final int LOGICAL_WIDTH = 1280;
    public static final int LOGICAL_HEIGHT = 720;
    public static final int DEFAULT_WINDOW_WIDTH = 1190;
    public static final int DEFAULT_WINDOW_HEIGHT = 700;
    public static final double LOADING_SCREEN_MINIMUM_SECONDS = 5.0;
    public static final int LOADING_SCREEN_BAR_INSET = 100;
    public static final int LOADING_SCREEN_BAR_Y = 650;
    public static final int LOADING_SCREEN_BAR_HEIGHT = 6;
    public static final int OVERLAY_MENU_X = 250;
    public static final int OVERLAY_MENU_Y = 65;
    public static final int OVERLAY_MENU_WIDTH = 780;
    public static final int OVERLAY_MENU_HEIGHT = 590;
    //first content size
    public static final int OVERLAY_MENU_CONTENT_X = 400;
    public static final int OVERLAY_MENU_CONTENT_TOP = 205;
    public static final int OVERLAY_MENU_CONTENT_BOTTOM = 555;
    public static final int OVERLAY_MENU_SCROLLBAR_RIGHT_INSET = 15;
    public static final int OVERLAY_MENU_SCROLLBAR_WIDTH = 6;
    public static final int OVERLAY_MENU_BACK_Y = 585;
    public static final int OVERLAY_MENU_BACK_WIDTH = 260;
    public static final int OVERLAY_MENU_BACK_HEIGHT = 45;
    //pause menu context
    //title font size = game paused
    //pause volume = png bar
    public static final int PAUSE_TITLE_FONT_SIZE = 104;
    public static final int PAUSE_TITLE_GAME_BASELINE = 232;
    public static final int PAUSE_TITLE_PAUSED_BASELINE = 328;
    public static final int PAUSE_OPTION_START_Y = 408;
    public static final int PAUSE_OPTION_SPACING = 50;
    // volume bar keeps the PNG's 500x150 aspect ratio (10:3)
    public static final int PAUSE_VOLUME_WIDTH = 267;
    public static final int PAUSE_VOLUME_HEIGHT = 80;
    public static final int PAUSE_VOLUME_X = (LOGICAL_WIDTH - PAUSE_VOLUME_WIDTH) / 2;
    public static final int PAUSE_VOLUME_Y = 546;
    // speaker icon area (clicking it toggles mute): 24% of the bar, matching the PNG's icon circle
    public static final int PAUSE_VOLUME_ICON_WIDTH = PAUSE_VOLUME_WIDTH * 24 / 100;

    public static final int TEMPORARY_MENU_BUTTON_X = 100;
    public static final int TEMPORARY_MENU_BUTTON_WIDTH = 330;
    public static final int TEMPORARY_MENU_BUTTON_HEIGHT = 64;
    public static final int TEMPORARY_MENU_BUTTON_START_Y = 320;
    public static final int TEMPORARY_MENU_BUTTON_SPACING = 80;

    public static final int TEMPORARY_INFO_BUTTON_X = 90;
    public static final int TEMPORARY_INFO_BUTTON_Y = 590;
    public static final int TEMPORARY_INFO_BUTTON_WIDTH = 250;
    public static final int TEMPORARY_INFO_BUTTON_HEIGHT = 64;

    public static final int TEMPORARY_GAMEPLAY_PANEL_X = 400;
    public static final int TEMPORARY_GAMEPLAY_PANEL_Y = 115;
    public static final int TEMPORARY_GAMEPLAY_PANEL_WIDTH = 830;
    public static final int TEMPORARY_GAMEPLAY_PANEL_HEIGHT = 275;
    public static final int TEMPORARY_GAMEPLAY_CHOICES_PANEL_X = 400;
    public static final int TEMPORARY_GAMEPLAY_CHOICES_PANEL_Y = 415;
    public static final int TEMPORARY_GAMEPLAY_CHOICES_PANEL_WIDTH = 830;
    public static final int TEMPORARY_GAMEPLAY_CHOICES_PANEL_HEIGHT = 290;
    public static final int TEMPORARY_GAMEPLAY_CHOICE_WIDTH = 400;
    public static final int TEMPORARY_GAMEPLAY_CHOICE_HEIGHT = 118;
    public static final int TEMPORARY_GAMEPLAY_CHOICE_LEFT_X = 410;
    public static final int TEMPORARY_GAMEPLAY_CHOICE_RIGHT_X = 830;
    public static final int TEMPORARY_GAMEPLAY_CHOICE_TOP_Y = 428;
    public static final int TEMPORARY_GAMEPLAY_CHOICE_BOTTOM_Y = 556;
    public static final int TEMPORARY_GAMEPLAY_CHOICE_TEXT_WIDTH = 340;
    public static final int TEMPORARY_ABANDON_CHOICE_WIDTH = 190;
    public static final int TEMPORARY_ABANDON_CHOICE_HEIGHT = 55;
    public static final int TEMPORARY_ABANDON_CHOICE_LEFT_X = 590;
    public static final int TEMPORARY_ABANDON_CHOICE_RIGHT_X = 850;
    public static final int TEMPORARY_ABANDON_CHOICE_Y = 560;
    public static final int TEMPORARY_GAMEPLAY_QUESTION_TEXT_X = 420;
    public static final int TEMPORARY_GAMEPLAY_QUESTION_TEXT_Y = 130;
    public static final int TEMPORARY_GAMEPLAY_QUESTION_TEXT_WIDTH = 800;
    public static final int TEMPORARY_GAMEPLAY_ACTION_Y = 550;
    public static final int TEMPORARY_GAMEPLAY_ACTION_WIDTH = 250;
    public static final int TEMPORARY_GAMEPLAY_ACTION_HEIGHT = 64;
    public static final int TEMPORARY_GAMEPLAY_ANSWER_X = 80;
    public static final int TEMPORARY_GAMEPLAY_HELP_X = 350;
    public static final int TEMPORARY_GAMEPLAY_SKIP_X = 620;
    public static final int TEMPORARY_GAMEPLAY_CONSUMABLE_X = 890;
    public static final int TEMPORARY_GAMEPLAY_CONSUMABLE_WIDTH = 300;

    // Move radial menu left or right (pixels)
    public static final int TEMPORARY_RADIAL_CENTER_X = -30;
    // Move radial menu up or down (pixels)
    public static final int TEMPORARY_RADIAL_CENTER_Y = 720;
    // Dial size and the inner edge of ring
    public static final int TEMPORARY_RADIAL_DIAL_RADIUS = 130;
    // Weighted arrow delay for beautification
    public static final double TEMPORARY_RADIAL_ARROW_DELAY = 0.145;
    // Radial Options distance.
    public static final int TEMPORARY_RADIAL_ITEM_RADIUS = 260;
    // Border Unscaled startin dimensions
    public static final int TEMPORARY_RADIAL_LABEL_WIDTH = 230;
    public static final int TEMPORARY_RADIAL_LABEL_HEIGHT = 44;
    public static final int TEMPORARY_MENU_LOGO_X = (LOGICAL_WIDTH - 620) / 2;
    public static final int TEMPORARY_MENU_LOGO_Y = 45;
    public static final int TEMPORARY_MENU_LOGO_WIDTH = 620;
    public static final int TEMPORARY_MENU_LOGO_HEIGHT = 124;

    public static final int TEMPORARY_GAMEPLAY_QUESTION_TEXT_HEIGHT = 250;

    // ---- HUD (top of the gameplay screen) ----
    public static final int PAUSE_BUTTON_X = 18;
    public static final int PAUSE_BUTTON_Y = 18;
    public static final int PAUSE_BUTTON_SIZE = 56;
    public static final int HUD_NODE_STATUS_X = 92;
    public static final int HUD_NODE_STATUS_Y = 16;
    public static final int HUD_TIMER_CENTER_X = 727;
    public static final int MINIMAP_X = 985;
    public static final int MINIMAP_Y = 12;
    public static final int MINIMAP_WIDTH = 275;
    public static final int MINIMAP_HEIGHT = 92;
    public static final int STATUS_MESSAGE_Y = 404;

    // ---- Rules: scoring, timer, hints, consumables ----
    public static final double QUESTION_TIME_SECONDS = 60.0;
    public static final int SCORE_CORRECT = 100;
    public static final int SCORE_TIME_BONUS_PER_SECOND = 2;
    public static final int SCORE_WRONG_PENALTY = 50;
    public static final int DYNAMO_HINT_USES = 3;
    public static final double CONSUMABLE_DROP_CHANCE = 0.35;
    public static final int SCRIPTERS_SACRIFICE_SAFE_USES = 2;
    public static final double END_SCREEN_DELAY_SECONDS = 1.2;
    public static final double END_SCREEN_SECONDS = 6.0;
    // true = the player fights for the Immutable Mother, false = for the Dark Web King
    public static final boolean PLAYER_SIDE_IMMUTABLE_MOTHER = true;

    // ---- Consumable panel ----
    public static final int CONSUMABLE_PANEL_X = 560;
    public static final int CONSUMABLE_PANEL_Y = 420;
    public static final int CONSUMABLE_PANEL_WIDTH = 520;
    public static final int CONSUMABLE_PANEL_HEIGHT = 285;
    public static final int CONSUMABLE_ROW_HEIGHT = 80;

    // ---- Audio ----
    public static final float MUSIC_VOLUME_LEVEL = 0.35f;

    private GameConstants() {
    }
}
