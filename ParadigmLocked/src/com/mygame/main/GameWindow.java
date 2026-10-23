package com.mygame.main;

import com.mygame.engine.core.DisplaySurface;
import com.mygame.engine.core.GameStateManager;
import com.mygame.engine.core.ObjectManager;
import com.mygame.engine.display.DisplayController;
import com.mygame.engine.helpers.constants.GameConstants;
import java.awt.Dimension;
import javax.swing.JFrame;

public class GameWindow extends JFrame {
    private final DisplaySurface surface;
    private final DisplayController displayController;

    public GameWindow(GameStateManager stateManager, ObjectManager objectManager) {
        super("ParadigmLocked");
        displayController = new DisplayController(this);
        surface = new DisplaySurface(stateManager, objectManager, displayController::toggleFullscreen);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(640, 360));
        setContentPane(surface);
        setSize(GameConstants.LOGICAL_WIDTH, GameConstants.LOGICAL_HEIGHT);
        setLocationRelativeTo(null);
        displayController.setDisplayChangedListener(surface::refreshViewport);
    }

    public DisplaySurface getSurface() {
        return surface;
    }

    public DisplayController getDisplayController() {
        return displayController;
    }
}
