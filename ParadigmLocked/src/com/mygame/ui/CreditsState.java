package com.mygame.ui;

import com.mygame.engine.core.GameStateManager;

public class CreditsState extends InfoState {
    public CreditsState(GameStateManager stateManager) {
        super(stateManager, "CREDITS", new String[] {
            "ParadigmLocked",
            "Created by Francis Kyle A.S. and Dan Benedict G.A.Y.",
            "An educational game about programming paradigms.",
            "Visual assets will be connected as they become available."
        });
    }
}
