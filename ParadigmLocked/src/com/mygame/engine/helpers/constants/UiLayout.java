package com.mygame.engine.helpers.constants;

import static com.mygame.engine.helpers.constants.GameConstants.LOGICAL_WIDTH;

// Screen positions and sizes (logical 1280x720 pixels).
public final class UiLayout {
    // this is for the loading bar
    public static final int LOADING_BAR_INSET = 100;
    public static final int LOADING_BAR_Y = 650;
    public static final int LOADING_BAR_HEIGHT = 6;

    // these place the main menu buttons and logo
    public static final int MENU_BUTTON_X = 100;
    public static final int MENU_BUTTON_WIDTH = 330;
    public static final int MENU_BUTTON_HEIGHT = 64;
    public static final int MENU_BUTTON_START_Y = 320;
    public static final int MENU_BUTTON_SPACING = 80;
    public static final int MENU_LOGO_WIDTH = 620;
    public static final int MENU_LOGO_HEIGHT = MENU_LOGO_WIDTH * 300 / 1000; // keeps the PNG's 1000x300 ratio
    public static final int MENU_LOGO_X = (LOGICAL_WIDTH - MENU_LOGO_WIDTH) / 2;
    public static final int MENU_LOGO_Y = 45;

    // this is for the overlay frame used by How to Play, Credits and Pause
    public static final int OVERLAY_X = 250;
    public static final int OVERLAY_Y = 65;
    public static final int OVERLAY_WIDTH = 780;
    public static final int OVERLAY_HEIGHT = 590;
    public static final int OVERLAY_CONTENT_X = 400;
    public static final int OVERLAY_CONTENT_TOP = 205;
    public static final int OVERLAY_CONTENT_BOTTOM = 555;
    public static final int OVERLAY_SCROLLBAR_RIGHT_INSET = 15;
    public static final int OVERLAY_SCROLLBAR_WIDTH = 6;
    public static final int OVERLAY_BACK_Y = 585;
    public static final int OVERLAY_BACK_WIDTH = 260;
    public static final int OVERLAY_BACK_HEIGHT = 45;

    // these place the pause menu title, options and volume bar
    public static final int PAUSE_TITLE_FONT_SIZE = 104;
    public static final int PAUSE_TITLE_GAME_BASELINE = 232;
    public static final int PAUSE_TITLE_PAUSED_BASELINE = 328;
    public static final int PAUSE_OPTION_START_Y = 408;
    public static final int PAUSE_OPTION_SPACING = 50;
    public static final int PAUSE_VOLUME_WIDTH = 267; // keeps the PNG's 10:3 ratio
    public static final int PAUSE_VOLUME_HEIGHT = 80;
    public static final int PAUSE_VOLUME_X = (LOGICAL_WIDTH - PAUSE_VOLUME_WIDTH) / 2;
    public static final int PAUSE_VOLUME_Y = 546;
    public static final int PAUSE_VOLUME_ICON_WIDTH = PAUSE_VOLUME_WIDTH * 24 / 100; // speaker icon (mute toggle)

    // these place the HUD at the top of the game screen
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

    // these place the question, status line and answer choices
    public static final int QUESTION_X = 420;
    public static final int QUESTION_Y = 130;
    public static final int QUESTION_WIDTH = 800;
    public static final int QUESTION_HEIGHT = 250;
    public static final int QUESTION_AREA_Y = 115;
    public static final int STATUS_MESSAGE_Y = 404;
    public static final int PANEL_X = 400;
    public static final int PANEL_Y = 415;
    public static final int PANEL_WIDTH = 830;
    public static final int CHOICE_WIDTH = 400;
    public static final int CHOICE_HEIGHT = 118;
    public static final int CHOICE_LEFT_X = 410;
    public static final int CHOICE_RIGHT_X = 830;
    public static final int CHOICE_TOP_Y = 428;
    public static final int CHOICE_BOTTOM_Y = 556;
    public static final int ABANDON_CHOICE_WIDTH = 190;
    public static final int ABANDON_CHOICE_HEIGHT = 55;
    public static final int ABANDON_CHOICE_LEFT_X = 590;
    public static final int ABANDON_CHOICE_RIGHT_X = 850;
    public static final int ABANDON_CHOICE_Y = 560;

    // this is for the consumable panel
    public static final int CONSUMABLE_PANEL_X = 590;
    public static final int CONSUMABLE_PANEL_Y = 425;
    public static final int CONSUMABLE_PANEL_WIDTH = 460;
    public static final int CONSUMABLE_PANEL_HEIGHT = 270;
    public static final int CONSUMABLE_ROW_HEIGHT = 72;

    // these size and place the radial option wheel
    public static final int RADIAL_CENTER_X = -30;
    public static final int RADIAL_CENTER_Y = 720;
    public static final int RADIAL_DIAL_RADIUS = 130;
    public static final double RADIAL_ARROW_DELAY = 0.145; // weighted arrow delay for beautification
    public static final int RADIAL_ITEM_RADIUS = 260;
    public static final int RADIAL_LABEL_WIDTH = 230;
    public static final int RADIAL_LABEL_HEIGHT = 44;

    private UiLayout() {
    }
}
