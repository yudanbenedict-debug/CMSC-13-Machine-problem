package com.mygame.engine.core;

// these are the global game states that drive the gameplay flow

public enum GameState {
  LOADING,
    TITLE, // "press any key" title intro between loading and the main menu
    MENU,
    SIDE_SELECT, // choose Dark Web King or Immutable Mother before a match
    CREDITS,
    HOWTOPLAY, 
    PLAYING, //continue game will use playing gamestate but will import a savestate (make 3 save slots ig idk)
      PAUSED,
    WON,
    LOST,
    LEADERBOARD
}

