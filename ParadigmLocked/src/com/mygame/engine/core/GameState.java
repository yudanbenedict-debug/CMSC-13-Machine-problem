package com.mygame.engine.core;

/*
  Our Global Game States. These are the main Gameplay flow drivers.
 */

public enum GameState {
  LOADING,
    TITLE, // "press any key" title intro between loading and the main menu
    MENU,
    CREDITS,
    HOWTOPLAY, 
    PLAYING, //continue game will use playing gamestate but will import a savestate (make 3 save slots ig idk)
      PAUSED,
    WON,
    LOST,
    LEADERBOARD
}

