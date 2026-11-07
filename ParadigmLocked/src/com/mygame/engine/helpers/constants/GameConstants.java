package com.mygame.engine.helpers.constants;

// Game rules, timing and audio levels. Screen positions/sizes live in UiLayout.
public final class GameConstants {
    public static final int LOGICAL_WIDTH = 1280;
    public static final int LOGICAL_HEIGHT = 720;
    public static final int DEFAULT_WINDOW_WIDTH = 1190;
    public static final int DEFAULT_WINDOW_HEIGHT = 700;
    public static final double LOADING_SCREEN_MINIMUM_SECONDS = 5.0;

    // these are the rules for scoring, the timer, hints and consumables
    public static final double QUESTION_TIME_SECONDS = 60.0;
    public static final int SCORE_CORRECT = 100;
    public static final int SCORE_TIME_BONUS_PER_SECOND = 2;
    public static final int SCORE_WRONG_PENALTY = 50;
    public static final int DYNAMO_HINT_USES = 3;
    public static final double CONSUMABLE_DROP_CHANCE = 0.35;
    public static final int SCRIPTERS_SACRIFICE_SAFE_USES = 2;
    public static final int QUESTION_RETRY_DELAY = 5; // questions asked before a missed one comes back

    // these control how long the briefing and end screens stay up
    public static final double END_SCREEN_DELAY_SECONDS = 1.2;
    public static final double END_SCREEN_SECONDS = 6.0;
    public static final double BRIEFING_SECONDS = 8.0; // pre-game briefing (timer paused)
    public static final double BRIEFING_MIN_SECONDS = 0.6; // can't be skipped before this
    public static final double BRIEFING_FADE_SECONDS = 0.5;

    // these are the volume levels for music and sound effects
    public static final float MUSIC_VOLUME_LEVEL = 0.35f;
    public static final float MENU_MUSIC_DUCKED_LEVEL = 0.18f; // menu theme after the title screen
    public static final float SFX_VOLUME_LEVEL = 0.8f; // all sound effects -20%
    public static final float HINT_PROMPT_VOLUME_LEVEL = 0.6f; // hint prompt -40%
    public static final float RADIAL_HOVER_VOLUME_LEVEL = 0.4f; // radial hover at 40%

    private GameConstants() {
    }
}
