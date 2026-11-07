package com.mygame.ui;

import com.mygame.engine.core.AssetCache;
import com.mygame.engine.core.GameState;
import com.mygame.engine.core.GameStateManager;
import com.mygame.engine.core.Img;
import com.mygame.engine.core.Sfx;
import com.mygame.engine.core.SoundManager;
import com.mygame.engine.core.StateMethods;
import com.mygame.engine.helpers.constants.GameConstants;
import com.mygame.engine.helpers.constants.UiLayout;
import com.mygame.engine.hud.ChoiceButton;
import com.mygame.engine.hud.CircuitBackground;
import com.mygame.engine.hud.Hover;
import com.mygame.engine.hud.RadialMenu.Option;
import com.mygame.engine.hud.RadialMenu;
import com.mygame.engine.hud.UiFontManager;
import com.mygame.engine.hud.UiRenderer;
import com.mygame.engine.hud.UiText;
import com.mygame.game.Consumable;
import com.mygame.game.Faction;
import com.mygame.game.Match;
import com.mygame.game.QuestionRound;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import javax.swing.SwingUtilities;

// Gameplay screen: draws the match and turns input into actions; the rules live in Match.
public class GameplayState implements StateMethods {
    private static final Color TEXT = new Color(222, 235, 230);
    private static final Color GOOD = new Color(0x7FB05A);
    private static final Color BAD = new Color(0xD05A5A);
    private static final Color GOLD = new Color(229, 190, 94);

    private final GameStateManager stateManager;
    private final EndScreenState endScreen;
    private final RadialMenu radialMenu = new RadialMenu();
    private final ChoiceButton[] choiceButtons = new ChoiceButton[4];
    private final ChoiceButton[] abandonButtons;
    private final HintPanel hintPanel = new HintPanel(this::requestHint);
    private final BriefingOverlay briefing = new BriefingOverlay();
    private final CircuitBackground circuit = new CircuitBackground(7L, 0.55f);
    private final Hover pauseHover = new Hover();

    private Match match;
    private ConsumablePanel consumablePanel;
    private Option activeOption; // null = nothing open ("Select a choice")
    private Option optionBeforeLock;
    private int selectedChoice = -1;
    private boolean answerLocked;
    private boolean lastAnswerCorrect;
    private String resultMessage = "";
    private String notice = "";
    private boolean confirmKeyHeld;
    private double timeLeft;
    private double clock;
    private double endDelay;

    public GameplayState(GameStateManager stateManager, EndScreenState endScreen) {
        this.stateManager = stateManager;
        this.endScreen = endScreen;
        radialMenu.setSelectionListener(this::openOption);
        int[][] positions = {{UiLayout.CHOICE_LEFT_X, UiLayout.CHOICE_TOP_Y}, {UiLayout.CHOICE_RIGHT_X, UiLayout.CHOICE_TOP_Y},
            {UiLayout.CHOICE_LEFT_X, UiLayout.CHOICE_BOTTOM_Y}, {UiLayout.CHOICE_RIGHT_X, UiLayout.CHOICE_BOTTOM_Y}};
        for (int index = 0; index < choiceButtons.length; index++) {
            int choice = index;
            choiceButtons[index] = new ChoiceButton(positions[index][0], positions[index][1], UiLayout.CHOICE_WIDTH,
                UiLayout.CHOICE_HEIGHT, String.valueOf(Match.letter(index)), "", () -> selectChoice(choice));
        }
        abandonButtons = new ChoiceButton[] {
            new ChoiceButton(UiLayout.ABANDON_CHOICE_LEFT_X, UiLayout.ABANDON_CHOICE_Y, UiLayout.ABANDON_CHOICE_WIDTH,
                UiLayout.ABANDON_CHOICE_HEIGHT, "YES", "", this::confirmAbandon),
            new ChoiceButton(UiLayout.ABANDON_CHOICE_RIGHT_X, UiLayout.ABANDON_CHOICE_Y, UiLayout.ABANDON_CHOICE_WIDTH,
                UiLayout.ABANDON_CHOICE_HEIGHT, "NO", "", radialMenu::clearSelection)
        };
        startNewGame();
    }

    public void startNewGame() {
        match = new Match();
        consumablePanel = new ConsumablePanel(match.inventory(), this::useConsumable, this::discardScripters);
        notice = "";
        endDelay = 0;
        optionBeforeLock = null;
        loadNextQuestion();
        briefing.start();
    }

    public Match match() {
        return match;
    }

    @Override
    public void onEnter() {
        GameState previous = stateManager.getPreviousState();
        // fresh run when coming from the menus; keep going when returning from pause
        if (previous != GameState.PAUSED && previous != GameState.HOWTOPLAY) {
            startNewGame();
        }
        SoundManager.playMusic(Sfx.MAIN_GAME_THEME);
        confirmKeyHeld = false;
    }

    @Override public void onExit() { }

    @Override
    public void draw(Graphics2D g) {
        drawScene(g);
        if (briefing.isActive()) {
            briefing.draw(g);
        }
        GameHud.drawPauseButton(g, pauseHover.isHovered());
    }

    // Full gameplay frame; also drawn behind the pause and end overlays.
    public void drawScene(Graphics2D g) {
        updateCircuitColor();
        circuit.draw(g, AssetCache.get(Img.MAIN_GAME_BACKGROUND));
        GameHud.drawPauseButton(g, false);
        GameHud.drawNodeStatus(g, match.space(), clock);
        GameHud.drawTimerAndScore(g, timeLeft, match.score(), questionActive());
        GameHud.drawMinimap(g, match.space(), clock);
        GameHud.drawQuestionsLeft(g, match.questionsLeft() + (questionActive() ? 1 : 0), match.totalQuestions());
        if (!briefing.isActive()) {
            QuestionRound round = match.round();
            UiRenderer.drawGameplayQuestion(g, round == null ? "No questions available." : round.question().question);
            drawStatusMessage(g);
            drawOpenPanel(g);
        }
        radialMenu.draw(g);
    }

    private void drawOpenPanel(Graphics2D g) {
        if (activeOption == Option.ANSWER) {
            for (ChoiceButton button : choiceButtons) {
                button.draw(g);
            }
            drawChoicePreview(g);
        } else if (activeOption == Option.ABANDON) {
            UiRenderer.drawAbandonPrompt(g);
            for (ChoiceButton button : abandonButtons) {
                button.draw(g);
            }
        } else if (activeOption == Option.CONTACT_HELP) {
            hintPanel.draw(g, match, answerLocked);
        } else if (activeOption == Option.CONSUMABLE) {
            consumablePanel.draw(g);
        }
    }

    // Shows a truncated (long code) choice in full while hovered or selected.
    private void drawChoicePreview(Graphics2D g) {
        ChoiceButton shown = null;
        for (ChoiceButton button : choiceButtons) {
            if (button.isHovered()) {
                shown = button;
            }
        }
        if (shown == null && !answerLocked && selectedChoice >= 0) {
            shown = choiceButtons[selectedChoice];
        }
        if (shown != null && shown.needsPreview()) {
            UiRenderer.drawChoicePreview(g, shown.getLabel(), shown.getText());
        }
    }

    // green when even; the leading team's color once someone has destroyed more nodes, brighter per extra node
    private void updateCircuitColor() {
        int lead = match.nodeLead();
        if (lead == 0) {
            circuit.setTarget(CircuitBackground.NEUTRAL, 0.45);
        } else {
            Faction leader = lead > 0 ? Faction.player() : Faction.enemy();
            circuit.setTarget(leader.circuitColor, Math.min(1, 0.25 + 0.25 * Math.abs(lead)));
        }
    }

    private void drawStatusMessage(Graphics2D g) {
        String text = statusText();
        float centerX = UiLayout.PANEL_X + UiLayout.PANEL_WIDTH / 2f;
        if (!text.isEmpty()) {
            Color color = answerLocked ? (lastAnswerCorrect ? GOOD : BAD) : TEXT;
            UiText.Block block = UiText.fit(text, UiFontManager.dorris(18, Font.BOLD), 18, 12, UiLayout.PANEL_WIDTH, 26, 1f);
            if (!block.lines.isEmpty()) {
                UiText.drawLayout(g, block.lines.get(0), centerX - block.lines.get(0).getAdvance() / 2f,
                    UiLayout.STATUS_MESSAGE_Y, color, null);
            }
        }
        if (!notice.isEmpty() && answerLocked) {
            UiText.drawCentered(g, notice, UiFontManager.dorris(16, Font.BOLD), centerX,
                UiLayout.STATUS_MESSAGE_Y - 24, GOLD, null);
        }
    }

    private String statusText() {
        if (match.outcome() != Match.Outcome.PLAYING) {
            return match.outcome() == Match.Outcome.WON ? "The enemy Core has fallen!" : "Your Core has been corrupted...";
        }
        if (match.round() == null) {
            return "";
        }
        if (answerLocked) {
            return resultMessage;
        }
        if (activeOption == Option.CONSUMABLE) {
            return notice.isEmpty() ? consumablePanel.statusText() : notice;
        }
        if (activeOption != null && activeOption != Option.ANSWER) {
            return "";
        }
        if (!notice.isEmpty()) {
            return notice;
        }
        String prefix = match.doubleDamage() ? "[x2 DAMAGE] " : "";
        if (activeOption == null) {
            return prefix + "Select a choice. (Up / Down, then Enter, or click the wheel)";
        }
        return prefix + (selectedChoice >= 0
            ? "Click it again or press Enter to confirm. Right-click to deselect." : "Select a choice.");
    }

    private boolean questionActive() {
        return match.round() != null && !answerLocked;
    }

    @Override
    public void update(double dt) {
        clock += dt;
        radialMenu.update(dt);
        if (briefing.isActive()) {
            briefing.update(dt);
        } else if (match.outcome() != Match.Outcome.PLAYING) {
            endDelay -= dt;
            if (endDelay <= 0) {
                endScreen.show(match.outcome() == Match.Outcome.WON, match.score(), this::drawScene);
                stateManager.setState(match.outcome() == Match.Outcome.WON ? GameState.WON : GameState.LOST);
            }
        } else if (questionActive()) {
            timeLeft -= dt;
            if (timeLeft <= 0) {
                timeLeft = 0;
                lockAnswer(false, "Time's up! ");
            }
        }
    }

    @Override
    public void mousePressed(MouseEvent event) {
        Point point = event.getPoint();
        if (GameHud.pauseBounds().contains(point)) {
            stateManager.setState(GameState.PAUSED);
        } else if (briefing.isActive()) {
            briefing.skip();
        } else if (match.outcome() != Match.Outcome.PLAYING) {
            return;
        } else if (SwingUtilities.isRightMouseButton(event)) {
            deselectChoice();
        } else if (answerLocked && !radialMenu.contains(point)) {
            loadNextQuestion();
        } else {
            radialMenu.mousePressed(point);
            mouseOnPanel(point, true);
        }
    }

    @Override
    public void mouseReleased(MouseEvent event) {
        if (!SwingUtilities.isRightMouseButton(event) && !briefing.isActive()) {
            mouseOnPanel(event.getPoint(), false);
        }
    }

    @Override
    public void mouseMoved(MouseEvent event) {
        Point point = event.getPoint();
        pauseHover.update(GameHud.pauseBounds().contains(point));
        if (briefing.isActive()) {
            return;
        }
        radialMenu.mouseMoved(point);
        if (activeOption == Option.ANSWER || activeOption == Option.ABANDON) {
            for (ChoiceButton button : activeOption == Option.ANSWER ? choiceButtons : abandonButtons) {
                button.updateHover(point);
            }
        } else if (activeOption == Option.CONSUMABLE) {
            consumablePanel.mouseMoved(point);
        } else if (activeOption == Option.CONTACT_HELP) {
            hintPanel.mouseMoved(point);
        }
    }

    private void mouseOnPanel(Point point, boolean pressed) {
        if (activeOption == Option.ANSWER || activeOption == Option.ABANDON) {
            for (ChoiceButton button : activeOption == Option.ANSWER ? choiceButtons : abandonButtons) {
                if (pressed) {
                    button.press(point);
                } else {
                    button.release(point);
                }
            }
        } else if (activeOption == Option.CONSUMABLE && pressed) {
            consumablePanel.mousePressed(point);
        } else if (activeOption == Option.CONTACT_HELP) {
            hintPanel.mouse(point, pressed);
        }
    }

    @Override
    public void keyPressed(KeyEvent event) {
        int code = event.getKeyCode();
        if (code == KeyEvent.VK_ESCAPE || code == KeyEvent.VK_P) {
            stateManager.setState(GameState.PAUSED);
            return;
        }
        boolean playing = !briefing.isActive() && match.outcome() == Match.Outcome.PLAYING;
        if ((code == KeyEvent.VK_UP || code == KeyEvent.VK_DOWN) && playing) {
            radialMenu.moveFocus(code == KeyEvent.VK_UP ? -1 : 1);
            return;
        }
        if (code != KeyEvent.VK_ENTER && code != KeyEvent.VK_SPACE) {
            return;
        }
        if (briefing.isActive()) {
            briefing.skip();
            confirmKeyHeld = true; // same press must not confirm an answer
            return;
        }
        if (confirmKeyHeld || !playing) {
            return; // ignore key auto-repeat
        }
        confirmKeyHeld = true;
        if (radialMenu.hasKeyboardFocus()) {
            radialMenu.confirmFocus();
        } else if (answerLocked) {
            loadNextQuestion();
        } else if (activeOption == Option.ANSWER) {
            confirmAnswer();
        } else if (activeOption == Option.CONTACT_HELP) {
            requestHint();
        }
    }

    @Override
    public void keyReleased(KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.VK_ENTER || event.getKeyCode() == KeyEvent.VK_SPACE) {
            confirmKeyHeld = false;
        }
    }

    private void loadNextQuestion() {
        if (!match.nextQuestion()) {
            for (ChoiceButton button : choiceButtons) {
                button.reset("");
                button.setEnabled(false);
            }
            endDelay = GameConstants.END_SCREEN_DELAY_SECONDS;
            return;
        }
        selectedChoice = -1;
        answerLocked = false;
        resultMessage = "";
        notice = "";
        timeLeft = GameConstants.QUESTION_TIME_SECONDS;
        consumablePanel.clearSelection();
        String[] answers = match.round().answers();
        for (int index = 0; index < choiceButtons.length; index++) {
            choiceButtons[index].reset(answers[index]);
        }
        // stay in the answer view if the last question was answered from it, otherwise back to "Select a choice"
        if (optionBeforeLock == Option.ANSWER) {
            radialMenu.select(Option.ANSWER);
        } else {
            radialMenu.clearSelection();
        }
        optionBeforeLock = null;
    }

    private void selectChoice(int choice) {
        if (!questionActive()) {
            return;
        }
        if (choice == selectedChoice) {
            confirmAnswer(); // clicking the already-selected answer confirms it
            return;
        }
        selectedChoice = choice;
        notice = "";
        for (int index = 0; index < choiceButtons.length; index++) {
            choiceButtons[index].setSelected(index == choice);
        }
    }

    // Right-click: deselect the answer and consumable.
    private void deselectChoice() {
        if (answerLocked) {
            return;
        }
        selectedChoice = -1;
        for (ChoiceButton button : choiceButtons) {
            button.setSelected(false);
        }
        consumablePanel.clearSelection();
    }

    private void confirmAnswer() {
        if (questionActive() && selectedChoice >= 0) {
            lockAnswer(match.round().isCorrect(selectedChoice), "");
        }
    }

    private void confirmAbandon() {
        if (questionActive()) {
            selectedChoice = -1;
            lockAnswer(false, "Question abandoned. ");
        }
        radialMenu.select(Option.ANSWER);
    }

    // Locks the question, reveals the answer and applies the turn.
    private void lockAnswer(boolean correct, String reasonPrefix) {
        int correctIndex = match.round().correctIndex();
        answerLocked = true;
        lastAnswerCorrect = correct;
        for (int index = 0; index < choiceButtons.length; index++) {
            choiceButtons[index].setSelected(false);
            choiceButtons[index].setResult(index == correctIndex ? ChoiceButton.Result.CORRECT
                : index == selectedChoice ? ChoiceButton.Result.WRONG : ChoiceButton.Result.NONE);
            choiceButtons[index].setEnabled(false);
        }
        optionBeforeLock = activeOption;
        if (activeOption != Option.ANSWER) {
            radialMenu.select(Option.ANSWER);
        }
        Match.Turn turn = match.resolve(correct, timeLeft);
        for (int tick = 0; tick < turn.selfDamageTicks; tick++) {
            SoundManager.playSFX(Sfx.DAMAGE_TICK);
        }
        if (turn.drop != null) {
            SoundManager.playSFX(Sfx.CONSUMABLE_SPAWN);
            notice = "You received " + turn.drop.displayName + "!";
        }
        if (turn.lowHpWarning) {
            SoundManager.playSFX(Sfx.LOW_HP_ALERT);
        }
        if (match.outcome() != Match.Outcome.PLAYING) {
            endDelay = GameConstants.END_SCREEN_DELAY_SECONDS;
        }
        resultMessage = reasonPrefix + (correct ? "Correct! " : "Wrong. Answer: " + Match.letter(correctIndex) + ". ")
            + turn.summary + " Click or press Enter to continue.";
    }

    private void openOption(Option option) {
        activeOption = option;
        // switching panels drops a pending pick, but never unlocks an answered question
        if (option != Option.ANSWER && !answerLocked) {
            selectedChoice = -1;
            for (ChoiceButton button : choiceButtons) {
                button.setSelected(false);
            }
        }
        if (option != Option.CONSUMABLE) {
            consumablePanel.clearSelection();
        }
        if (!answerLocked) {
            notice = "";
        }
    }

    private void requestHint() {
        if (questionActive() && match.canAskHint()) {
            match.askHint();
            SoundManager.playSFX(Sfx.HINT_PROMPT);
        }
    }

    // Returns true if the consumable was spent.
    private boolean useConsumable(Consumable consumable) {
        if (!questionActive() || match.outcome() != Match.Outcome.PLAYING) {
            notice = "Consumables can only be used during a question.";
            return false;
        }
        Match.Use use = match.useConsumable(consumable);
        if (!use.spent) {
            notice = use.message;
            return false;
        }
        SoundManager.playSFX(consumable.usedSound);
        if (use.nullifiedChoice >= 0) {
            choiceButtons[use.nullifiedChoice].setNullified(true);
            if (selectedChoice == use.nullifiedChoice) {
                selectedChoice = -1;
            }
        }
        if (consumable != Consumable.REFRESHER) {
            radialMenu.select(Option.ANSWER);
        }
        notice = use.message; // after select(), which clears the notice
        return true;
    }

    private void discardScripters() {
        match.inventory().discardScripters();
        notice = "Scripter's Sacrifice discarded.";
    }
}
