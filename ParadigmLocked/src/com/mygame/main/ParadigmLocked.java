package com.mygame.main;

import com.mygame.engine.core.GameLoop;
import com.mygame.engine.core.GameState;
import com.mygame.engine.core.GameStateManager;
import com.mygame.engine.core.ObjectManager;
import com.mygame.ui.CreditsState;
import com.mygame.ui.GameplayPreviewState;
import com.mygame.ui.HowToPlayState;
import com.mygame.ui.LoadingState;
import com.mygame.ui.MainMenuState;
import com.mygame.ui.PauseState;
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
                stateManager.register(GameState.MENU, new MainMenuState(stateManager));
                stateManager.register(GameState.PLAYING, new GameplayPreviewState(stateManager));
                stateManager.register(GameState.PAUSED, new PauseState(stateManager));
                stateManager.register(GameState.HOWTOPLAY, new HowToPlayState(stateManager));
                stateManager.register(GameState.CREDITS, new CreditsState(stateManager));
                stateManager.setState(GameState.MENU);
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
