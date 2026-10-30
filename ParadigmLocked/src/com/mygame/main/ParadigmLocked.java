package com.mygame.main;

import com.mygame.engine.core.GameLoop;
import com.mygame.engine.core.GameState;
import com.mygame.engine.core.GameStateManager;
import com.mygame.engine.core.ObjectManager;
import com.mygame.ui.CreditsState;
import com.mygame.ui.EndScreenState;
import com.mygame.ui.GameplayPreviewState;
import com.mygame.ui.HowToPlayState;
import com.mygame.ui.LoadingState;
import com.mygame.ui.MainMenuState;
import com.mygame.ui.PauseState;
import com.mygame.ui.TitleState;
import javax.swing.SwingUtilities;

public final class ParadigmLocked {
    private ParadigmLocked() {
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            GameStateManager stateManager = new GameStateManager(GameState.LOADING);
            ObjectManager objectManager = new ObjectManager();
            GameWindow window = new GameWindow(stateManager, objectManager);

            stateManager.register(GameState.LOADING, new LoadingState(() -> {
                MainMenuState menu = new MainMenuState(stateManager);
                EndScreenState endScreen = new EndScreenState(stateManager);
                GameplayPreviewState gameplay = new GameplayPreviewState(stateManager, endScreen);
                HowToPlayState howToPlay = new HowToPlayState(stateManager);
                CreditsState credits = new CreditsState(stateManager);
                howToPlay.setBackdrops(menu::drawBackground, gameplay::drawScene);
                credits.setBackdrops(menu::drawBackground, gameplay::drawScene);
                stateManager.register(GameState.TITLE, new TitleState(stateManager));
                stateManager.register(GameState.MENU, menu);
                stateManager.register(GameState.PLAYING, gameplay);
                stateManager.register(GameState.PAUSED, new PauseState(stateManager, gameplay::drawScene));
                stateManager.register(GameState.HOWTOPLAY, howToPlay);
                stateManager.register(GameState.CREDITS, credits);
                stateManager.register(GameState.WON, endScreen);
                stateManager.register(GameState.LOST, endScreen);
                stateManager.setState(GameState.TITLE);
            }));

            window.setVisible(true);
            window.getSurface().requestFocusInWindow();
            GameLoop gameLoop = new GameLoop(objectManager, window.getSurface()::updateGame);
            gameLoop.start();
            window.addWindowListener(new java.awt.event.WindowAdapter() {
                @Override
                public void windowClosing(java.awt.event.WindowEvent event) {
                    gameLoop.stop();
                }
            });
        });
    }
}
