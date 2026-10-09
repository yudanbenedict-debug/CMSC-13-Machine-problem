package com.mygame.ui;

import com.mygame.engine.core.GameStateManager;

public class HowToPlayState extends InfoState {
    public HowToPlayState(GameStateManager stateManager) {
        super(stateManager, "HOW TO PLAY", new String[] {
            "Answer programming-paradigm questions to damage enemy nodes.",
            "Correct answers move you toward the opposing Paradigm Core.",
            "Incorrect answers damage your allied side.",
            "The gameplay controls will be expanded after the GUI shell is stable."
        });
    }
}
