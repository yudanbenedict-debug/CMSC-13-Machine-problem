package com.mygame;

import com.mygame.engine.core.AssetLoader;
import com.mygame.engine.core.GameState;
import com.mygame.engine.core.GameStateManager;
import com.mygame.engine.helpers.constants.GameConstants;
import com.mygame.game.Consumable;
import com.mygame.game.Match;
import com.mygame.game.QuestionRound;
import com.mygame.ui.CreditsState;
import com.mygame.ui.EndScreenState;
import com.mygame.ui.GameplayState;
import com.mygame.ui.HowToPlayState;
import com.mygame.ui.MainMenuState;
import com.mygame.ui.PauseState;
import com.mygame.ui.SideSelectState;
import com.mygame.ui.TitleState;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

// Plain-Java checks (no JUnit needed): run with ./run-tests.sh from the ParadigmLocked folder.
public final class SelfTest {
    private static int failures;

    public static void main(String[] args) {
        System.setProperty("java.awt.headless", "true");
        AssetLoader loader = new AssetLoader();
        while (loader.loadNext()) {
            // load everything (sounds may fail without an audio device; that's fine here)
        }
        questionRoundsKeepTheRightAnswer();
        perfectPlayClearsEveryQuestion();
        missedQuestionsComeBack();
        badPlayLoses();
        consumablesWork();
        everyScreenDraws();
        System.out.println(failures == 0 ? "ALL TESTS PASSED" : failures + " TEST(S) FAILED");
        System.exit(failures == 0 ? 0 : 1);
    }

    private static void questionRoundsKeepTheRightAnswer() {
        Match match = new Match();
        for (int i = 0; i < 50 && match.nextQuestion(); i++) {
            QuestionRound round = match.round();
            String expected = optionText(round);
            check(round.answers()[round.correctIndex()].equals(expected), "shuffled answer still correct");
            match.resolve(true, 0);
        }
    }

    private static String optionText(QuestionRound round) {
        switch (round.question().correctAnswer.trim().toUpperCase()) {
            case "A": return round.question().optionA;
            case "B": return round.question().optionB;
            case "C": return round.question().optionC;
            default: return round.question().optionD;
        }
    }

    private static void perfectPlayClearsEveryQuestion() {
        Match match = new Match();
        Set<String> seen = new HashSet<>();
        int turns = 0;
        while (match.nextQuestion()) {
            check(match.space().enemy().totalHp() == match.questionsLeft() + 1, "enemy HP == questions left");
            seen.add(match.round().question().category + match.round().question().id);
            match.resolve(true, GameConstants.QUESTION_TIME_SECONDS);
            turns++;
        }
        check(match.outcome() == Match.Outcome.WON, "perfect play wins");
        check(turns == match.totalQuestions() && seen.size() == turns, "win takes every question exactly once");
    }

    private static void missedQuestionsComeBack() {
        Match match = new Match();
        Random random = new Random(1);
        Set<String> cleared = new HashSet<>();
        while (match.nextQuestion()) {
            boolean correct = random.nextDouble() < 0.9;
            if (correct) {
                cleared.add(match.round().question().category + match.round().question().id);
            }
            match.resolve(correct, 30);
        }
        check(match.outcome() == Match.Outcome.WON, "90% accuracy still wins");
        check(cleared.size() == match.totalQuestions(), "missed questions were asked again");
    }

    private static void badPlayLoses() {
        Match match = new Match();
        while (match.nextQuestion()) {
            match.resolve(false, 0);
        }
        check(match.outcome() == Match.Outcome.LOST, "always wrong loses");
        check(match.space().ally().defeated(), "ally core destroyed");
    }

    private static void consumablesWork() {
        Match match = new Match();
        match.nextQuestion();
        match.inventory().add(Consumable.NULLSLINGER);
        Match.Use use = match.useConsumable(Consumable.NULLSLINGER);
        check(use.spent && use.nullifiedChoice != match.round().correctIndex(), "NullSlinger hits a wrong choice");
        check(match.askHint() != null && match.hintsLeft() == GameConstants.DYNAMO_HINT_USES - 1, "hint costs one call");
        match.inventory().add(Consumable.REFRESHER);
        check(match.useConsumable(Consumable.REFRESHER).spent && match.hintsLeft() == GameConstants.DYNAMO_HINT_USES,
            "Refresher resets hints");
        match.inventory().add(Consumable.SCRIPTERS_SACRIFICE);
        int before = match.questionsLeft();
        match.useConsumable(Consumable.SCRIPTERS_SACRIFICE);
        match.resolve(true, 0);
        check(match.questionsLeft() == before - 1, "Scripter's double damage clears an extra question");
    }

    private static void everyScreenDraws() {
        GameStateManager manager = new GameStateManager(GameState.LOADING);
        MainMenuState menu = new MainMenuState(manager);
        EndScreenState end = new EndScreenState(manager);
        GameplayState gameplay = new GameplayState(manager, end);
        HowToPlayState howToPlay = new HowToPlayState(manager);
        CreditsState credits = new CreditsState(manager);
        howToPlay.setBackdrops(menu::drawBackdrop, gameplay::drawScene);
        credits.setBackdrops(menu::drawBackdrop, gameplay::drawScene);
        manager.register(GameState.TITLE, new TitleState(manager));
        manager.register(GameState.MENU, menu);
        manager.register(GameState.SIDE_SELECT, new SideSelectState(manager, menu::drawBackdrop));
        manager.register(GameState.PLAYING, gameplay);
        manager.register(GameState.PAUSED, new PauseState(manager, gameplay::drawScene));
        manager.register(GameState.HOWTOPLAY, howToPlay);
        manager.register(GameState.CREDITS, credits);
        manager.register(GameState.WON, end);
        BufferedImage frame = new BufferedImage(GameConstants.LOGICAL_WIDTH, GameConstants.LOGICAL_HEIGHT,
            BufferedImage.TYPE_INT_ARGB);
        for (GameState state : new GameState[] {GameState.TITLE, GameState.MENU, GameState.SIDE_SELECT,
                GameState.PLAYING, GameState.PAUSED, GameState.HOWTOPLAY, GameState.CREDITS, GameState.WON}) {
            try {
                manager.setState(state);
                for (int i = 0; i < 30; i++) {
                    manager.update(1 / 60.0);
                    Graphics2D g = frame.createGraphics();
                    manager.draw(g);
                    g.dispose();
                }
                check(true, state + " draws");
            } catch (RuntimeException e) {
                e.printStackTrace();
                check(false, state + " draws");
            }
        }
    }

    private static void check(boolean ok, String name) {
        if (!ok) {
            failures++;
            System.out.println("FAIL: " + name);
        }
    }
}
