package com.mygame.ui;

import com.mygame.engine.core.AssetCache;
import com.mygame.engine.core.GameState;
import com.mygame.engine.core.GameStateManager;
import com.mygame.engine.core.StateMethods;
import com.mygame.engine.helpers.constants.GameConstants;
import com.mygame.engine.helpers.gamequestions.Question;
import com.mygame.engine.hud.Button;
import com.mygame.engine.hud.ChoiceButton;
import com.mygame.engine.hud.TemporaryRadialMenu;
import com.mygame.engine.hud.TemporaryUiRenderer;
import com.mygame.game.QuestionSelector;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

public class GameplayPreviewState implements StateMethods {
    private final GameStateManager stateManager;
    private final QuestionSelector questionSelector;
    private final TemporaryRadialMenu radialMenu;
    private final Button menuButton;
    private final ChoiceButton[] choiceButtons;
    private Question currentQuestion;
    private int selectedChoiceIndex = -1;
    private boolean answerLocked;
    private TemporaryRadialMenu.Option activeOption;

    public GameplayPreviewState(GameStateManager stateManager) {
        this.stateManager = stateManager;
        AssetCache.load("UI_MAIN_GAME_BACKGROUND", "images/backgrounds/MAIN_GAME_BACKGROUND_STATIC.png");
        questionSelector = new QuestionSelector();
        currentQuestion = questionSelector.getNextQuestion();
        radialMenu = new TemporaryRadialMenu();
        radialMenu.setSelectionListener(this::activateOption);
        activeOption = TemporaryRadialMenu.Option.ANSWER;
        menuButton = new Button(GameConstants.TEMPORARY_GAMEPLAY_MENU_X,
            GameConstants.TEMPORARY_GAMEPLAY_MENU_Y,
            GameConstants.TEMPORARY_GAMEPLAY_MENU_WIDTH,
            GameConstants.TEMPORARY_GAMEPLAY_MENU_HEIGHT, "MENU", null,
            () -> stateManager.setState(GameState.MENU));
        choiceButtons = new ChoiceButton[] {
            createChoiceButton("A", GameConstants.TEMPORARY_GAMEPLAY_CHOICE_LEFT_X,
                GameConstants.TEMPORARY_GAMEPLAY_CHOICE_TOP_Y, 0),
            createChoiceButton("B", GameConstants.TEMPORARY_GAMEPLAY_CHOICE_RIGHT_X,
                GameConstants.TEMPORARY_GAMEPLAY_CHOICE_TOP_Y, 1),
            createChoiceButton("C", GameConstants.TEMPORARY_GAMEPLAY_CHOICE_LEFT_X,
                GameConstants.TEMPORARY_GAMEPLAY_CHOICE_BOTTOM_Y, 2),
            createChoiceButton("D", GameConstants.TEMPORARY_GAMEPLAY_CHOICE_RIGHT_X,
                GameConstants.TEMPORARY_GAMEPLAY_CHOICE_BOTTOM_Y, 3)
        };
    }

    @Override
    public void draw(Graphics2D g) {
        BufferedImage background = AssetCache.get("UI_MAIN_GAME_BACKGROUND");
        if (background != null) {
            g.drawImage(background, 0, 0, GameConstants.LOGICAL_WIDTH, GameConstants.LOGICAL_HEIGHT, null);
        } else {
            TemporaryUiRenderer.drawScreenBackground(g);
        }
        TemporaryUiRenderer.drawGameplayTopPanel(g);
        if (currentQuestion == null) {
            TemporaryUiRenderer.drawGameplayQuestion(g, "No questions available.", "", "", "", "");
        } else {
            TemporaryUiRenderer.drawGameplayQuestion(g, currentQuestion.question, "", "", "", "");
        }
        if (activeOption == TemporaryRadialMenu.Option.ANSWER) {
            TemporaryUiRenderer.drawGameplayChoicesPanel(g);
            for (ChoiceButton choiceButton : choiceButtons) {
                choiceButton.draw(g);
            }
        } else if (activeOption != null) {
            TemporaryUiRenderer.drawOptionState(g, activeOption.label(), activeOption.description());
        }
        radialMenu.draw(g);
        menuButton.draw(g);
    }

    @Override public void update(double dt) { radialMenu.update(dt); }
    @Override public void onEnter() { }
    @Override public void onExit() { }
    @Override public void mousePressed(MouseEvent event) { updateButtons(event.getPoint(), true); }
    @Override public void mouseReleased(MouseEvent event) { updateButtons(event.getPoint(), false); }
    @Override public void mouseMoved(MouseEvent event) { updateHover(event.getPoint()); }
    @Override public void keyPressed(KeyEvent event) { }
    @Override public void keyReleased(KeyEvent event) { }

    private void loadNextQuestion() {
        Question nextQuestion = questionSelector.getNextQuestion();
        if (nextQuestion != null) {
            currentQuestion = nextQuestion;
            selectedChoiceIndex = -1;
            answerLocked = false;
            choiceButtons[0].setText(currentQuestion.optionA);
            choiceButtons[1].setText(currentQuestion.optionB);
            choiceButtons[2].setText(currentQuestion.optionC);
            choiceButtons[3].setText(currentQuestion.optionD);
            for (ChoiceButton choiceButton : choiceButtons) {
                choiceButton.setSelected(false);
                choiceButton.setEnabled(true);
            }
        }
    }

    private void activateOption(TemporaryRadialMenu.Option option) {
        activeOption = option;
        if (option != TemporaryRadialMenu.Option.ANSWER) {
            selectedChoiceIndex = -1;
            answerLocked = false;
            for (ChoiceButton choiceButton : choiceButtons) {
                choiceButton.setSelected(false);
            }
        }
    }

    private ChoiceButton createChoiceButton(String label, int x, int y, int index) {
        String text = getChoiceText(index);
        return new ChoiceButton(x, y, GameConstants.TEMPORARY_GAMEPLAY_CHOICE_WIDTH,
                GameConstants.TEMPORARY_GAMEPLAY_CHOICE_HEIGHT, label, text,
                () -> selectChoice(index));
    }

    private String getChoiceText(int index) {
        if (currentQuestion == null) {
            return "";
        }
        return new String[] {
                currentQuestion.optionA,
                currentQuestion.optionB,
                currentQuestion.optionC,
                currentQuestion.optionD
        }[index];
    }

    private void selectChoice(int selectedIndex) {
        if (answerLocked) {
            return;
        }
        selectedChoiceIndex = selectedIndex;
        for (int index = 0; index < choiceButtons.length; index++) {
            choiceButtons[index].setSelected(index == selectedIndex);
        }
    }

    private void updateHover(Point point) {
        radialMenu.mouseMoved(point);
        menuButton.updateHover(point);
        if (activeOption == TemporaryRadialMenu.Option.ANSWER) {
            for (ChoiceButton choiceButton : choiceButtons) {
                choiceButton.updateHover(point);
            }
        }
    }

    private void updateButtons(Point point, boolean pressed) {
        if (pressed) {
            radialMenu.mousePressed(point);
            menuButton.press(point);
        } else {
            menuButton.release(point);
        }
        if (activeOption == TemporaryRadialMenu.Option.ANSWER) {
            for (ChoiceButton choiceButton : choiceButtons) {
                if (pressed) {
                    choiceButton.press(point);
                } else {
                    choiceButton.release(point);
                }
            }
        }
    }
}
