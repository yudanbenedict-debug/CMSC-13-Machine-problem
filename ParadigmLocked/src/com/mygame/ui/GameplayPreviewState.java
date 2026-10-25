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
import com.mygame.engine.hud.UiFontManager;
import com.mygame.game.QuestionRound;
import com.mygame.game.QuestionSelector;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
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
    private QuestionRound currentRound;
    private String resultMessage = "";
    private boolean lastAnswerCorrect;
    private boolean confirmKeyHeld;

    // Clicking these areas while an answer is locked moves on to the next question.
    // (Choice buttons start at y=485 and the MENU button at x=1000, so neither overlaps.)
    private static final Rectangle CONTINUE_QUESTION_AREA = new Rectangle(
            GameConstants.TEMPORARY_GAMEPLAY_PANEL_X, GameConstants.TEMPORARY_GAMEPLAY_PANEL_Y,
            GameConstants.TEMPORARY_GAMEPLAY_PANEL_WIDTH, GameConstants.TEMPORARY_GAMEPLAY_PANEL_HEIGHT);
    private static final Rectangle CONTINUE_STATUS_AREA = new Rectangle(
            GameConstants.TEMPORARY_GAMEPLAY_TOP_PANEL_X, GameConstants.TEMPORARY_GAMEPLAY_TOP_PANEL_Y,
            GameConstants.TEMPORARY_GAMEPLAY_MENU_X - GameConstants.TEMPORARY_GAMEPLAY_TOP_PANEL_X - 10,
            GameConstants.TEMPORARY_GAMEPLAY_TOP_PANEL_HEIGHT);

    public GameplayPreviewState(GameStateManager stateManager) {
        this.stateManager = stateManager;
        AssetCache.load("UI_MAIN_GAME_BACKGROUND", "images/backgrounds/MAIN_GAME_BACKGROUND_STATIC.png");
        questionSelector = new QuestionSelector();
        currentQuestion = questionSelector.getNextQuestion();
        // Only build a round when there is a question, otherwise the app would crash at launch.
        currentRound = currentQuestion == null ? null : new QuestionRound(currentQuestion);
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
        drawStatusMessage(g);
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
    @Override public void mousePressed(MouseEvent event) {
        if (answerLocked && isContinueClick(event.getPoint())) {
            loadNextQuestion();
            return;
        }
        updateButtons(event.getPoint(), true);
    }
    @Override public void mouseReleased(MouseEvent event) { updateButtons(event.getPoint(), false); }
    @Override public void mouseMoved(MouseEvent event) { updateHover(event.getPoint()); }
    @Override public void keyPressed(KeyEvent event) {
        int code = event.getKeyCode();
        if (code != KeyEvent.VK_ENTER && code != KeyEvent.VK_SPACE) {
            return;
        }
        // Ignore key auto-repeat so holding Enter can't confirm and skip the feedback in one go.
        if (confirmKeyHeld) {
            return;
        }
        confirmKeyHeld = true;
        if (answerLocked) {
            loadNextQuestion();
        } else if (activeOption == TemporaryRadialMenu.Option.ANSWER) {
            confirmAnswer();
        }
    }
    @Override public void keyReleased(KeyEvent event) {
        int code = event.getKeyCode();
        if (code == KeyEvent.VK_ENTER || code == KeyEvent.VK_SPACE) {
            confirmKeyHeld = false;
        }
    }

    private void loadNextQuestion() {
        Question nextQuestion = questionSelector.getNextQuestion();
        selectedChoiceIndex = -1;
        answerLocked = false;
        resultMessage = "";
        if (nextQuestion == null) {
            currentQuestion = null;
            currentRound = null;
            for (ChoiceButton choiceButton : choiceButtons) {
                choiceButton.setText("");
                choiceButton.setSelected(false);
                choiceButton.setEnabled(false);
            }
            return;
        }
        currentQuestion = nextQuestion;
        currentRound = new QuestionRound(currentQuestion);
        String[] answers = currentRound.getShuffledQuestions();
        for (int index = 0; index < choiceButtons.length; index++) {
            choiceButtons[index].setText(answers[index]);
            choiceButtons[index].setSelected(false);
            choiceButtons[index].setEnabled(true);
        }
    }
    //TODO: CONFIRMATION OF UI CHECKER (TOP OF SCREEN CURRENTLY)
    //Confirmation since u didnt add
    private void confirmAnswer() {
        if (currentRound == null || answerLocked || selectedChoiceIndex < 0) {
            return;
        }
        String[] answers = currentRound.getShuffledQuestions();
        boolean correct = currentRound.isCorrect(answers[selectedChoiceIndex]);

        // Find which on-screen slot holds the right answer so it can be highlighted and named.
        int correctIndex = selectedChoiceIndex;
        for (int index = 0; index < answers.length; index++) {
            if (currentRound.isCorrect(answers[index])) {
                correctIndex = index;
                break;
            }
        }

        answerLocked = true;
        lastAnswerCorrect = correct;
        for (int index = 0; index < choiceButtons.length; index++) {
            choiceButtons[index].setSelected(index == correctIndex);
            choiceButtons[index].setEnabled(false);
        }
        resultMessage = correct
                ? "Correct! Click or press Enter to continue.": "Wrong. Answer: " + "ABCD".charAt(correctIndex) + ". Click or press Enter to continue.";
        // TODO: apply the consequences here... made by claude.
    }

    private boolean isContinueClick(Point point) {
        return CONTINUE_QUESTION_AREA.contains(point) || CONTINUE_STATUS_AREA.contains(point);
    }

    private String statusText() {
        if (currentRound == null) {
            return "";
        }
        if (answerLocked) {
            return resultMessage;
        }
        if (activeOption != TemporaryRadialMenu.Option.ANSWER) {
            return "";
        }
        return selectedChoiceIndex >= 0 ? "Click it again or press Enter to confirm." : "Pick an answer.";
    }

    private void drawStatusMessage(Graphics2D g) {
        String text = statusText();
        if (text.isEmpty()) {
            return;
        }
        g.setFont(UiFontManager.dorris(18, Font.BOLD));
        g.setColor(!answerLocked ? new Color(222, 235, 230)
                : lastAnswerCorrect ? new Color(0x7FB05A) : new Color(0xD05A5A));
        FontMetrics metrics = g.getFontMetrics();
        int x = CONTINUE_STATUS_AREA.x + 20;
        int y = CONTINUE_STATUS_AREA.y + (CONTINUE_STATUS_AREA.height - metrics.getHeight()) / 2
                + metrics.getAscent();
        g.drawString(text, x, y);
    }

    private void activateOption(TemporaryRadialMenu.Option option) {
        activeOption = option;
        // Switching menu options clears a pending pick, but must never unlock an answered question
        // (otherwise the player could lock a wrong answer, switch tabs, and answer again).
        if (option != TemporaryRadialMenu.Option.ANSWER && !answerLocked) {
            selectedChoiceIndex = -1;
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
        if (currentRound == null) {
            return "";
        }
        return currentRound.getShuffledQuestions()[index];
    }

    private void selectChoice(int selectedIndex) {
        if (answerLocked || currentRound == null) {
            return;
        }
        if (selectedIndex == selectedChoiceIndex) {
            confirmAnswer(); // clicking the already-selected answer confirms it
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