package com.mygame.ui;

import com.mygame.engine.core.AssetCache;
import com.mygame.engine.core.GameState;
import com.mygame.engine.core.GameStateManager;
import com.mygame.engine.core.SoundManager;
import com.mygame.engine.core.StateMethods;
import com.mygame.engine.helpers.constants.GameConstants;
import com.mygame.engine.helpers.gamequestions.Question;
import com.mygame.engine.hud.Button;
import com.mygame.engine.hud.ChoiceButton;
import com.mygame.engine.hud.TemporaryRadialMenu;
import com.mygame.engine.hud.TemporaryUiRenderer;
import com.mygame.engine.hud.UiFontManager;
import com.mygame.engine.hud.UiText;
import com.mygame.engine.helpers.algorithms.EasingMethods;
import com.mygame.game.Consumable;
import com.mygame.game.ConsumableInventory;
import com.mygame.game.Faction;
import com.mygame.game.ParadigmSpace;
import com.mygame.game.QuestionRound;
import com.mygame.game.QuestionSelector;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import javax.swing.SwingUtilities;

// Main gameplay screen: question -> answer/timeout -> resolve turn -> next question.
public class GameplayPreviewState implements StateMethods {
    private static final Color TEXT = new Color(222, 235, 230);
    private static final Color GOOD = new Color(0x7FB05A);
    private static final Color BAD = new Color(0xD05A5A);
    private static final Color GOLD = new Color(229, 190, 94);

    private final GameStateManager stateManager;
    private final EndScreenState endScreen;
    private final TemporaryRadialMenu radialMenu;
    private final ChoiceButton[] choiceButtons;
    private final ChoiceButton[] abandonChoiceButtons;
    private final Button hintButton;
    private final Random random = new Random();

    private QuestionSelector questionSelector;
    private ParadigmSpace space;
    private ConsumableInventory inventory;
    private ConsumablePanel consumablePanel;
    private Question currentQuestion;
    private QuestionRound currentRound;
    private TemporaryRadialMenu.Option activeOption;
    private int selectedChoiceIndex = -1;
    private boolean answerLocked;
    private boolean lastAnswerCorrect;
    private String resultMessage = "";
    private String noticeMessage = "";
    private boolean confirmKeyHeld;
    private boolean pauseHovered;

    private double timeLeft;
    private double clock;
    private int score;
    private int hintsLeft;
    private String hintText;
    private boolean scriptersActiveThisTurn;
    private boolean lowHpAlertPlayed;
    private double briefingLeft;       // > 0 while the "get ready" briefing is showing
    private double briefingShown;
    private String[] briefingTips = new String[0];
    private Boolean gameResult;      // null = still playing, true = won, false = lost
    private double endDelay;

    public GameplayPreviewState(GameStateManager stateManager, EndScreenState endScreen) {
        this.stateManager = stateManager;
        this.endScreen = endScreen;
        radialMenu = new TemporaryRadialMenu();
        radialMenu.setSelectionListener(this::activateOption);
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
        abandonChoiceButtons = new ChoiceButton[] {
            new ChoiceButton(GameConstants.TEMPORARY_ABANDON_CHOICE_LEFT_X, GameConstants.TEMPORARY_ABANDON_CHOICE_Y,
                GameConstants.TEMPORARY_ABANDON_CHOICE_WIDTH, GameConstants.TEMPORARY_ABANDON_CHOICE_HEIGHT,
                "YES", "", this::confirmAbandonQuestion),
            new ChoiceButton(GameConstants.TEMPORARY_ABANDON_CHOICE_RIGHT_X, GameConstants.TEMPORARY_ABANDON_CHOICE_Y,
                GameConstants.TEMPORARY_ABANDON_CHOICE_WIDTH, GameConstants.TEMPORARY_ABANDON_CHOICE_HEIGHT,
                "NO", "", this::cancelAbandonQuestion)
        };
        hintButton = new Button(GameConstants.TEMPORARY_GAMEPLAY_CHOICES_PANEL_X + 300,
            GameConstants.TEMPORARY_GAMEPLAY_CHOICES_PANEL_Y + 150, 280, 64, "ASK FOR A HINT",
            "UI_MENU_FANCY_BORDER", this::requestHint);
        startNewGame();
    }

    public void startNewGame() {
        questionSelector = new QuestionSelector();
        space = new ParadigmSpace(questionSelector.totalQuestions());
        inventory = new ConsumableInventory();
        consumablePanel = new ConsumablePanel(inventory, this::useConsumable, this::discardScripters);
        score = 0;
        hintsLeft = GameConstants.DYNAMO_HINT_USES;
        lowHpAlertPlayed = false;
        gameResult = null;
        endDelay = 0;
        noticeMessage = "";
        radialMenu.clearSelection();
        loadNextQuestion();
        startBriefing();
    }

    @Override
    public void onEnter() {
        GameState previous = stateManager.getPreviousState();
        // fresh run from the menu; keep going when returning from pause
        if (previous == GameState.MENU || previous == GameState.TITLE || previous == GameState.WON
                || previous == GameState.LOST) {
            startNewGame();
        }
        SoundManager.playMusic("MAIN_GAME_THEME");
        confirmKeyHeld = false;
    }

    @Override public void onExit() { }

    @Override
    public void draw(Graphics2D g) {
        drawScene(g);
        if (briefingLeft > 0) {
            drawBriefing(g);
        }
        GameHud.drawPauseButton(g, pauseHovered);
    }

    // Full gameplay frame; also drawn behind the pause and end overlays.
    public void drawScene(Graphics2D g) {
        BufferedImage background = AssetCache.get("UI_MAIN_GAME_BACKGROUND");
        if (background != null) {
            g.drawImage(background, 0, 0, GameConstants.LOGICAL_WIDTH, GameConstants.LOGICAL_HEIGHT, null);
        } else {
            TemporaryUiRenderer.drawScreenBackground(g);
        }
        GameHud.drawPauseButton(g, false);
        GameHud.drawNodeStatus(g, space, clock);
        GameHud.drawTimerAndScore(g, timeLeft, score, currentRound != null && !answerLocked);
        GameHud.drawMinimap(g, space, clock);
        int left = questionSelector.remainingQuestions() + (currentRound != null && !answerLocked ? 1 : 0);
        GameHud.drawQuestionsLeft(g, left, questionSelector.totalQuestions());

        if (briefingLeft > 0) {
            radialMenu.draw(g);
            return;
        }
        TemporaryUiRenderer.drawGameplayQuestion(g,
            currentQuestion == null ? "No questions available." : currentQuestion.question);
        drawStatusMessage(g);

        if (activeOption == TemporaryRadialMenu.Option.ANSWER) {
            for (ChoiceButton choiceButton : choiceButtons) {
                choiceButton.draw(g);
            }
            drawChoicePreview(g);
        } else if (activeOption == TemporaryRadialMenu.Option.ABANDON) {
            TemporaryUiRenderer.drawAbandonPrompt(g);
            for (ChoiceButton choiceButton : abandonChoiceButtons) {
                choiceButton.draw(g);
            }
        } else if (activeOption == TemporaryRadialMenu.Option.CONTACT_HELP) {
            drawHintPanel(g);
        } else if (activeOption == TemporaryRadialMenu.Option.CONSUMABLE) {
            consumablePanel.draw(g);
        }
        radialMenu.draw(g);
    }

    // Shows a truncated (long code) choice in full while hovered or selected.
    private void drawChoicePreview(Graphics2D g) {
        ChoiceButton shown = null;
        for (ChoiceButton choiceButton : choiceButtons) {
            if (choiceButton.isHovered()) {
                shown = choiceButton;
            }
        }
        if (shown == null && !answerLocked && selectedChoiceIndex >= 0) {
            shown = choiceButtons[selectedChoiceIndex];
        }
        if (shown != null && shown.needsPreview()) {
            TemporaryUiRenderer.drawChoicePreview(g, shown.getLabel(), shown.getText());
        }
    }

    private void drawStatusMessage(Graphics2D g) {
        String text = statusText();
        if (text.isEmpty()) {
            return;
        }
        Color color = answerLocked ? (lastAnswerCorrect ? GOOD : BAD) : TEXT;
        Font font = UiFontManager.dorris(18, Font.BOLD);
        float centerX = GameConstants.TEMPORARY_GAMEPLAY_CHOICES_PANEL_X
            + GameConstants.TEMPORARY_GAMEPLAY_CHOICES_PANEL_WIDTH / 2f;
        UiText.Block block = UiText.fit(text, font, 18, 12,
            GameConstants.TEMPORARY_GAMEPLAY_CHOICES_PANEL_WIDTH, 26, 1f);
        if (!block.lines.isEmpty()) {
            UiText.drawLayout(g, block.lines.get(0), centerX - block.lines.get(0).getAdvance() / 2f,
                GameConstants.STATUS_MESSAGE_Y, color, null);
        }
        if (!noticeMessage.isEmpty() && answerLocked) {
            UiText.drawCentered(g, noticeMessage, UiFontManager.dorris(16, Font.BOLD), centerX,
                GameConstants.STATUS_MESSAGE_Y - 24, GOLD, null);
        }
    }

    private String statusText() {
        if (gameResult != null) {
            return gameResult ? "The enemy Core has fallen!" : "Your Core has been corrupted...";
        }
        if (currentRound == null) {
            return "";
        }
        if (answerLocked) {
            return resultMessage;
        }
        if (activeOption == TemporaryRadialMenu.Option.CONSUMABLE) {
            return noticeMessage.isEmpty() ? consumablePanel.statusText() : noticeMessage;
        }
        if (activeOption != null && activeOption != TemporaryRadialMenu.Option.ANSWER) {
            return "";
        }
        if (!noticeMessage.isEmpty()) {
            return noticeMessage;
        }
        String prefix = scriptersActiveThisTurn ? "[x2 DAMAGE] " : "";
        return prefix + (selectedChoiceIndex >= 0 && activeOption != null
            ? "Click it again or press Enter to confirm. Right-click to deselect." : "Select a choice.");
    }

    private void drawHintPanel(Graphics2D g) {
        int panelX = GameConstants.TEMPORARY_GAMEPLAY_CHOICES_PANEL_X;
        int panelY = GameConstants.TEMPORARY_GAMEPLAY_CHOICES_PANEL_Y;
        BufferedImage dynamo = AssetCache.get("UI_HINT_DECLARATIVE_DYNAMO");
        if (dynamo != null) {
            g.drawImage(dynamo, panelX + 20, panelY + 15, 250, 250, null);
        }
        int textX = panelX + 300;
        int textWidth = GameConstants.TEMPORARY_GAMEPLAY_CHOICES_PANEL_WIDTH - 320;
        UiText.draw(g, "DECLARATIVE DYNAMO", UiFontManager.luckiestGuy(26, Font.PLAIN), textX, panelY + 50,
            GOLD, null);
        String body;
        if (hintText != null) {
            body = hintText;
        } else if (currentRound == null || answerLocked) {
            body = "Answer first, programmer. I'll be here for the next one.";
        } else if (hintsLeft > 0) {
            body = "Need a hint? It will cost you one function call.";
        } else {
            body = "I'm out of function calls. A Refresher would reset them.";
        }
        UiText.Block block = UiText.fit("\"" + body + "\"", UiFontManager.choices(24, Font.PLAIN), 24, 13,
            textWidth, 150, 1.05f);
        UiText.drawBlock(g, block, textX, panelY + 68, TEXT, null);
        if (canAskHint()) {
            hintButton.draw(g);
        }
        UiText.draw(g, "Function calls left: " + hintsLeft + "/" + GameConstants.DYNAMO_HINT_USES,
            UiFontManager.dorris(18, Font.BOLD), textX, panelY + 255, hintsLeft > 0 ? TEXT : BAD);
    }

    @Override
    public void update(double dt) {
        clock += dt;
        radialMenu.update(dt);
        if (briefingLeft > 0) {
            briefingShown += dt;
            briefingLeft -= dt;
            return;
        }
        if (gameResult != null) {
            endDelay -= dt;
            if (endDelay <= 0) {
                showEndScreen();
            }
            return;
        }
        if (currentRound != null && !answerLocked) {
            timeLeft -= dt;
            if (timeLeft <= 0) {
                timeLeft = 0;
                lockWithResult(false, "Time's up! ");
            }
        }
    }

    @Override
    public void mousePressed(MouseEvent event) {
        Point point = event.getPoint();
        if (GameHud.pauseBounds().contains(point)) {
            stateManager.setState(GameState.PAUSED);
            return;
        }
        if (briefingLeft > 0) {
            skipBriefing();
            return;
        }
        if (gameResult != null) {
            return;
        }
        if (SwingUtilities.isRightMouseButton(event)) {
            deselectChoice();
            return;
        }
        if (answerLocked && !radialHit(point)) {
            loadNextQuestion();
            return;
        }
        updateButtons(point, true);
    }

    private boolean radialHit(Point point) {
        double distance = Math.hypot(point.x - GameConstants.TEMPORARY_RADIAL_CENTER_X,
            point.y - GameConstants.TEMPORARY_RADIAL_CENTER_Y);
        return distance <= GameConstants.TEMPORARY_RADIAL_ITEM_RADIUS + GameConstants.TEMPORARY_RADIAL_LABEL_WIDTH / 2.0 + 40;
    }

    @Override
    public void mouseReleased(MouseEvent event) {
        if (!SwingUtilities.isRightMouseButton(event)) {
            updateButtons(event.getPoint(), false);
        }
    }

    // Right-click: deselect the answer and consumable.
    private void deselectChoice() {
        if (answerLocked) {
            return;
        }
        selectedChoiceIndex = -1;
        for (ChoiceButton choiceButton : choiceButtons) {
            choiceButton.setSelected(false);
        }
        consumablePanel.clearSelection();
    }

    @Override
    public void mouseMoved(MouseEvent event) {
        Point point = event.getPoint();
        boolean nowHovered = GameHud.pauseBounds().contains(point);
        if (nowHovered && !pauseHovered) {
            SoundManager.playMenuHover();
        }
        pauseHovered = nowHovered;
        if (briefingLeft > 0) {
            return;
        }
        radialMenu.mouseMoved(point);
        if (activeOption == TemporaryRadialMenu.Option.ANSWER) {
            for (ChoiceButton choiceButton : choiceButtons) {
                choiceButton.updateHover(point);
            }
        } else if (activeOption == TemporaryRadialMenu.Option.ABANDON) {
            for (ChoiceButton choiceButton : abandonChoiceButtons) {
                choiceButton.updateHover(point);
            }
        } else if (activeOption == TemporaryRadialMenu.Option.CONSUMABLE) {
            consumablePanel.mouseMoved(point);
        } else if (activeOption == TemporaryRadialMenu.Option.CONTACT_HELP && canAskHint()) {
            hintButton.updateHover(point);
        }
    }

    @Override
    public void keyPressed(KeyEvent event) {
        int code = event.getKeyCode();
        if (code == KeyEvent.VK_ESCAPE || code == KeyEvent.VK_P) {
            stateManager.setState(GameState.PAUSED);
            return;
        }
        if (code != KeyEvent.VK_ENTER && code != KeyEvent.VK_SPACE) {
            return;
        }
        if (briefingLeft > 0) {
            skipBriefing();
            confirmKeyHeld = true;  // same press must not confirm an answer
            return;
        }
        // Ignore key auto-repeat so holding Enter can't confirm and skip the feedback in one go.
        if (confirmKeyHeld || gameResult != null) {
            return;
        }
        confirmKeyHeld = true;
        if (answerLocked) {
            loadNextQuestion();
        } else if (activeOption == TemporaryRadialMenu.Option.ANSWER) {
            confirmAnswer();
        }
    }

    @Override
    public void keyReleased(KeyEvent event) {
        int code = event.getKeyCode();
        if (code == KeyEvent.VK_ENTER || code == KeyEvent.VK_SPACE) {
            confirmKeyHeld = false;
        }
    }

    private void updateButtons(Point point, boolean pressed) {
        if (pressed) {
            radialMenu.mousePressed(point);
        }
        if (activeOption == TemporaryRadialMenu.Option.ANSWER) {
            for (ChoiceButton choiceButton : choiceButtons) {
                if (pressed) {
                    choiceButton.press(point);
                } else {
                    choiceButton.release(point);
                }
            }
        } else if (activeOption == TemporaryRadialMenu.Option.ABANDON) {
            for (ChoiceButton choiceButton : abandonChoiceButtons) {
                if (pressed) {
                    choiceButton.press(point);
                } else {
                    choiceButton.release(point);
                }
            }
        } else if (activeOption == TemporaryRadialMenu.Option.CONSUMABLE && pressed) {
            consumablePanel.mousePressed(point);
        } else if (activeOption == TemporaryRadialMenu.Option.CONTACT_HELP && canAskHint()) {
            if (pressed) {
                hintButton.press(point);
            } else {
                hintButton.release(point);
            }
        }
    }

    private void startBriefing() {
        briefingLeft = GameConstants.BRIEFING_SECONDS;
        briefingShown = 0;
        java.util.List<String> tips = new ArrayList<>(java.util.Arrays.asList(HowToPlayState.TIPS));
        java.util.Collections.shuffle(tips, random);
        briefingTips = tips.subList(0, Math.min(2, tips.size())).toArray(new String[0]);
    }

    // Skips the briefing after the minimum time, keeping the fade-out.
    private void skipBriefing() {
        if (briefingShown >= GameConstants.BRIEFING_MIN_SECONDS) {
            briefingLeft = Math.min(briefingLeft, GameConstants.BRIEFING_FADE_SECONDS);
        }
    }

    private void drawBriefing(Graphics2D g) {
        double in = EasingMethods.smoothStep(briefingShown / GameConstants.BRIEFING_FADE_SECONDS);
        double out = EasingMethods.easeIn(Math.min(1, briefingLeft / GameConstants.BRIEFING_FADE_SECONDS));
        float alpha = (float) Math.max(0, Math.min(1, in * out));
        g.setColor(new Color(0, 0, 0, (int) (215 * alpha)));
        g.fillRect(0, 0, GameConstants.LOGICAL_WIDTH, GameConstants.LOGICAL_HEIGHT);
        java.awt.Composite old = g.getComposite();
        g.setComposite(java.awt.AlphaComposite.getInstance(java.awt.AlphaComposite.SRC_OVER, alpha));

        float centerX = GameConstants.LOGICAL_WIDTH / 2f;
        UiText.drawCentered(g, "GET READY, PROGRAMMER", UiFontManager.luckiestGuy(46, Font.PLAIN),
            centerX, 175, GOLD, new Color(229, 190, 94, 110));
        Faction team = Faction.player();
        Font teamFont = UiFontManager.dorris(24, Font.BOLD);
        String before = "You fight for the ";
        float teamX = centerX - (UiText.width(before, teamFont) + UiText.width(team.displayName, teamFont)) / 2f;
        UiText.draw(g, before, teamFont, teamX, 222, TEXT);
        UiText.draw(g, team.displayName, teamFont, teamX + UiText.width(before, teamFont), 222, team.color,
            new Color(team.color.getRed(), team.color.getGreen(), team.color.getBlue(), 120));
        UiText.drawCentered(g, "Break through: Procedural Gate > Object Model > Functional Registers"
            + " > Logic Shrines > Core", UiFontManager.dorris(17, Font.BOLD), centerX, 256,
            new Color(190, 190, 190), null);

        int tipY = 315;
        for (String tip : briefingTips) {
            UiText.drawCentered(g, "TIP", UiFontManager.luckiestGuy(20, Font.PLAIN), centerX, tipY, GOLD, null);
            UiText.Block block = UiText.wrap(tip, UiFontManager.notoSerif(21, Font.PLAIN), 760, 1.15f, 3);
            float baseline = tipY + 12 + block.ascent;
            for (java.awt.font.TextLayout line : block.lines) {
                UiText.drawLayout(g, line, centerX - line.getAdvance() / 2f, baseline, TEXT, null);
                baseline += block.lineHeight;
            }
            tipY = (int) baseline + 34;
        }

        int seconds = (int) Math.ceil(Math.max(0, briefingLeft));
        UiText.drawCentered(g, "Starting in " + seconds, UiFontManager.luckiestGuy(28, Font.PLAIN),
            centerX, 600, TEXT, null);
        if (briefingShown >= GameConstants.BRIEFING_MIN_SECONDS) {
            int blink = (int) (150 + 105 * (0.5 + 0.5 * Math.sin(briefingShown * 3)));
            UiText.drawCentered(g, "Click or press Enter to begin", UiFontManager.dorris(18, Font.BOLD),
                centerX, 636, new Color(246, 242, 229, blink), null);
        }
        g.setComposite(old);
    }

    private String preferredCategory() {
        switch (space.currentTier()) {
            case EARLY:
                return "Theoretical";
            case CORE:
                return "Programming";
            default:
                return null;
        }
    }

    private void loadNextQuestion() {
        if (gameResult != null) {
            return;
        }
        selectedChoiceIndex = -1;
        answerLocked = false;
        resultMessage = "";
        noticeMessage = "";
        hintText = null;
        scriptersActiveThisTurn = false;
        timeLeft = GameConstants.QUESTION_TIME_SECONDS;
        consumablePanel.clearSelection();

        Question nextQuestion = questionSelector.getNextQuestion(preferredCategory());
        if (nextQuestion == null) {
            currentQuestion = null;
            currentRound = null;
            for (ChoiceButton choiceButton : choiceButtons) {
                choiceButton.reset("");
                choiceButton.setEnabled(false);
            }
            // out of questions: whoever kept more HP (in %) wins
            double allyLeft = space.ally().totalHp() / (double) space.ally().maxTotalHp();
            double enemyLeft = space.enemy().totalHp() / (double) space.enemy().maxTotalHp();
            endGame(allyLeft > enemyLeft);
            return;
        }
        currentQuestion = nextQuestion;
        currentRound = new QuestionRound(currentQuestion);
        String[] answers = currentRound.getShuffledQuestions();
        for (int index = 0; index < choiceButtons.length; index++) {
            choiceButtons[index].reset(answers[index]);
        }
        radialMenu.clearSelection();
    }

    private int correctIndex() {
        String[] answers = currentRound.getShuffledQuestions();
        for (int index = 0; index < answers.length; index++) {
            if (currentRound.isCorrect(answers[index])) {
                return index;
            }
        }
        return 0;
    }

    private void confirmAnswer() {
        if (currentRound == null || answerLocked || selectedChoiceIndex < 0) {
            return;
        }
        boolean correct = selectedChoiceIndex == correctIndex();
        lockWithResult(correct, "");
    }

    private void lockWithResult(boolean correct, String reasonPrefix) {
        int correctIndex = correctIndex();
        answerLocked = true;
        lastAnswerCorrect = correct;
        for (int index = 0; index < choiceButtons.length; index++) {
            choiceButtons[index].setSelected(false);
            choiceButtons[index].setResult(index == correctIndex ? ChoiceButton.Result.CORRECT
                : index == selectedChoiceIndex ? ChoiceButton.Result.WRONG : ChoiceButton.Result.NONE);
            choiceButtons[index].setEnabled(false);
        }
        String answerLetter = String.valueOf("ABCD".charAt(correctIndex));
        if (activeOption != TemporaryRadialMenu.Option.ANSWER) {
            radialMenu.select(TemporaryRadialMenu.Option.ANSWER);
        }
        resultMessage = reasonPrefix + (correct ? "Correct! " : "Wrong. Answer: " + answerLetter + ". ")
            + resolveTurn(correct) + " Click or press Enter to continue.";
    }

    // Applies damage, score and drops; returns the status summary.
    private String resolveTurn(boolean correct) {
        StringBuilder summary = new StringBuilder();
        if (correct) {
            // 1 damage = 1 question cleared (Scripter's extra damage clears one more)
            int damage = 1;
            if (scriptersActiveThisTurn && questionSelector.clearExtraQuestion()) {
                damage = 2;
            }
            boolean destroyed = space.enemy().damage(damage);
            int gained = GameConstants.SCORE_CORRECT
                + (int) Math.ceil(timeLeft) * GameConstants.SCORE_TIME_BONUS_PER_SECOND;
            score += gained;
            summary.append('+').append(gained).append(" pts, ").append(damage).append(" dmg.");
            if (destroyed && !space.enemy().defeated()) {
                summary.append(" Node destroyed!");
            }
            if ((destroyed || inventory.shouldDrop()) && !space.enemy().defeated()) {
                Consumable drop = inventory.rollDrop();
                inventory.add(drop);
                SoundManager.playSFX("CONSUMABLE_SPAWN");
                noticeMessage = "You received " + drop.displayName + "!";
            }
        } else {
            // missed questions return to the pool later
            questionSelector.defer(currentQuestion);
            space.ally().damage(1);
            score = Math.max(0, score - GameConstants.SCORE_WRONG_PENALTY);
            SoundManager.playSFX("DAMAGE_TICK");
            summary.append('-').append(GameConstants.SCORE_WRONG_PENALTY).append(" pts.");
        }
        if (inventory.scriptersPunishing() && !space.ally().defeated()) {
            space.ally().damage(1);
            SoundManager.playSFX("DAMAGE_TICK");
            summary.append(" Scripter's Sacrifice hurts you!");
        }
        scriptersActiveThisTurn = false;

        if (!lowHpAlertPlayed && space.ally().totalHp() == 1) {
            lowHpAlertPlayed = true;
            SoundManager.playSFX("LOW_HP_ALERT");
        }
        if (space.enemy().defeated()) {
            endGame(true);
        } else if (space.ally().defeated()) {
            endGame(false);
        }
        return summary.toString();
    }

    private void endGame(boolean won) {
        gameResult = won;
        endDelay = GameConstants.END_SCREEN_DELAY_SECONDS;
    }

    private void showEndScreen() {
        endScreen.show(gameResult, score, this::drawScene);
        stateManager.setState(gameResult ? GameState.WON : GameState.LOST);
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
        if (option != TemporaryRadialMenu.Option.CONSUMABLE) {
            consumablePanel.clearSelection();
        }
        if (!answerLocked) {
            noticeMessage = "";
        }
    }

    private boolean canAskHint() {
        return currentRound != null && !answerLocked && hintText == null && hintsLeft > 0 && gameResult == null;
    }

    // Only called from the ASK FOR A HINT button.
    private void requestHint() {
        if (!canAskHint()) {
            return;
        }
        hintsLeft--;
        hintText = buildHint();
        SoundManager.playSFX("HINT_PROMPT");
    }

    // Hint: the question's topic plus a 50/50 between two choices.
    private String buildHint() {
        int correct = correctIndex();
        List<Integer> wrong = new ArrayList<>();
        for (int index = 0; index < choiceButtons.length; index++) {
            if (index != correct && !choiceButtons[index].isNullified()) {
                wrong.add(index);
            }
        }
        String topic = "Theoretical".equalsIgnoreCase(currentQuestion.category)
            ? "This one comes from " + currentQuestion.moduleOrLanguage + "."
            : "Read that " + currentQuestion.moduleOrLanguage + " carefully, line by line.";
        if (wrong.isEmpty()) {
            return topic;
        }
        int other = wrong.get(random.nextInt(wrong.size()));
        char first = "ABCD".charAt(Math.min(correct, other));
        char second = "ABCD".charAt(Math.max(correct, other));
        return topic + " Between you and me... it's either " + first + " or " + second + ".";
    }

    private void confirmAbandonQuestion() {
        if (currentRound == null || answerLocked) {
            radialMenu.select(TemporaryRadialMenu.Option.ANSWER);
            return;
        }
        selectedChoiceIndex = -1;
        lockWithResult(false, "Question abandoned. ");
        radialMenu.select(TemporaryRadialMenu.Option.ANSWER);
    }

    private void cancelAbandonQuestion() {
        radialMenu.clearSelection();
    }

    // Returns true if the consumable was spent.
    private boolean useConsumable(Consumable consumable) {
        if (currentRound == null || answerLocked || gameResult != null) {
            noticeMessage = "Consumables can only be used during a question.";
            return false;
        }
        switch (consumable) {
            case NULLSLINGER: {
                int correct = correctIndex();
                List<Integer> targets = new ArrayList<>();
                for (int index = 0; index < choiceButtons.length; index++) {
                    if (index != correct && !choiceButtons[index].isNullified()) {
                        targets.add(index);
                    }
                }
                if (targets.size() <= 1) {
                    noticeMessage = "Nothing left to nullify.";
                    return false;
                }
                if (!inventory.consume(consumable)) {
                    return false;
                }
                int target = targets.get(random.nextInt(targets.size()));
                choiceButtons[target].setNullified(true);
                if (selectedChoiceIndex == target) {
                    selectedChoiceIndex = -1;
                }
                SoundManager.playSFX(consumable.usedSound);
                radialMenu.select(TemporaryRadialMenu.Option.ANSWER);
                noticeMessage = "NullSlinger nullified choice " + "ABCD".charAt(target) + ".";
                return true;
            }
            case SCRIPTERS_SACRIFICE: {
                if (scriptersActiveThisTurn) {
                    noticeMessage = "Already active for this turn.";
                    return false;
                }
                if (!inventory.consume(consumable)) {
                    return false;
                }
                scriptersActiveThisTurn = true;
                SoundManager.playSFX(consumable.usedSound);
                radialMenu.select(TemporaryRadialMenu.Option.ANSWER);
                noticeMessage = "Scripter's Sacrifice: this turn deals double damage!";
                return true;
            }
            case REFRESHER: {
                if (!inventory.consume(consumable)) {
                    return false;
                }
                hintsLeft = GameConstants.DYNAMO_HINT_USES;
                SoundManager.playSFX(consumable.usedSound);
                noticeMessage = "Refresher: Dynamo's function calls reset to " + hintsLeft + ".";
                return true;
            }
            default:
                return false;
        }
    }

    private void discardScripters() {
        inventory.discardScripters();
        noticeMessage = "Scripter's Sacrifice discarded.";
    }

    private ChoiceButton createChoiceButton(String label, int x, int y, int index) {
        return new ChoiceButton(x, y, GameConstants.TEMPORARY_GAMEPLAY_CHOICE_WIDTH,
                GameConstants.TEMPORARY_GAMEPLAY_CHOICE_HEIGHT, label, "", () -> selectChoice(index));
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
        noticeMessage = "";
        for (int index = 0; index < choiceButtons.length; index++) {
            choiceButtons[index].setSelected(index == selectedIndex);
        }
    }

    public int getScore() {
        return score;
    }

    public ParadigmSpace getSpace() {
        return space;
    }
}
