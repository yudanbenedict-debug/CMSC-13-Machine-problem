package com.mygame.ui;

import com.mygame.engine.core.GameStateManager;
import java.util.List;

public class CreditsState extends InfoState {
    public CreditsState(GameStateManager stateManager) {
        super(stateManager, "CREDITS", List.of(
            InfoBlock.image("UI_MAIN_MENU_LOGO", 110, null),
            InfoBlock.heading("Created By"),
            InfoBlock.body("Francis Kyle A.S."),
            InfoBlock.body("Dan Benedict G.Y."),
            InfoBlock.heading("Art Designs"),
            InfoBlock.body("Lorenzo Badinas"),
            InfoBlock.heading("Original Game Creators"),
            InfoBlock.body("Valve (Deadlock)"),
            InfoBlock.heading("Special Thanks To"),
            InfoBlock.body("Pocari Sweat"),
            InfoBlock.gap(10),
            InfoBlock.subheading("CMSC 13: Survey of Programming Paradigms"),
            InfoBlock.body("University of the Philippines Tacloban College")
        ), "CREDIT_MENU_ONSELECT");
    }
}
