package com.mygame.ui;

import com.mygame.engine.core.GameStateManager;
import com.mygame.engine.helpers.constants.GameConstants;
import java.util.List;

public class HowToPlayState extends InfoState {
    public HowToPlayState(GameStateManager stateManager) {
        super(stateManager, "HOW TO PLAY", List.of(
            InfoBlock.heading("About"),
            InfoBlock.subheading("Purpose"),
            InfoBlock.body("ParadigmLocked is an educational game that follows the premise of the popular game "
                + "\"Are You Smarter Than a 5th Grader?\". It is an interactive learning experience about "
                + "CMSC 13: Programming Paradigms that tests how well you know them."),

            InfoBlock.heading("Mechanics"),
            InfoBlock.subheading("QnA System"),
            InfoBlock.body("Every turn asks one question. A correct answer damages the enemy's foremost node; "
                + "an incorrect answer damages yours. Each question has a " + (int) GameConstants.QUESTION_TIME_SECONDS
                + " second timer - running out of time counts as a wrong answer."),
            InfoBlock.subheading("Node Progression"),
            InfoBlock.body("Both sides guard the Paradigm Space with the same chain of nodes, destroyed in order: "
                + "Procedural Gate, Object Model, two Functional Registers, two Logic Shrines, then the "
                + "two-phase Paradigm Core (Syntax, then Semantics). Questions get harder the deeper you go. "
                + "The minimap at the top-right shows which nodes still stand."),
            InfoBlock.subheading("Options"),
            InfoBlock.body("Use the option wheel at the bottom-left: Answer, Contact Declarative Dynamo "
                + "(a hint, " + GameConstants.DYNAMO_HINT_USES + " function calls per game), Abandon Question "
                + "(counts as a wrong answer), or use a Consumable."),
            InfoBlock.image("UI_HINT_DECLARATIVE_DYNAMO", 150, "Declarative Dynamo - your mentor"),

            InfoBlock.heading("Consumables"),
            InfoBlock.body("Consumables drop randomly after correct answers (always when you destroy a node). "
                + "Rarer consumables are stronger. Click one to select it, click it again to use it."),
            InfoBlock.image("CONSUMABLE_NULLSLINGER", 90, "NullSlinger (common): nullifies one wrong choice."),
            InfoBlock.image("CONSUMABLE_SCRIPTERS_SACRIFICE", 90, "Scripter's Sacrifice (rare): your turn deals "
                + "double damage. It has " + GameConstants.SCRIPTERS_SACRIFICE_SAFE_USES + " safe uses - overuse it "
                + "and it hurts you every turn until you discard it."),
            InfoBlock.image("CONSUMABLE_REFRESHER", 90, "Refresher (rarest): resets Declarative Dynamo's "
                + "function calls."),

            InfoBlock.heading("Score"),
            InfoBlock.body("Correct answers earn " + GameConstants.SCORE_CORRECT + " points plus a bonus for "
                + "every second left on the timer. Wrong answers, timeouts and abandoned questions cost "
                + GameConstants.SCORE_WRONG_PENALTY + " points."),
            InfoBlock.subheading("Win Condition"),
            InfoBlock.body("Destroy every opposing node, including both phases of the enemy Core."),
            InfoBlock.subheading("Lose Condition"),
            InfoBlock.body("If the enemy destroys the Core of your allied faction, you lose."),

            InfoBlock.heading("Controls"),
            InfoBlock.body("Mouse: pick options and answers. Enter / Space: confirm or continue. "
                + "Esc / P / pause button: pause. F11: fullscreen. Mouse wheel or arrow keys: scroll."),
            InfoBlock.gap(10)
        ), "HOW_TO_PLAY_MENU_ONSELECT");
    }
}
