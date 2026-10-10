package com.mygame.main;

import com.mygame.engine.core.DisplaySurface;
import com.mygame.engine.core.GameStateManager;
import com.mygame.engine.core.ObjectManager;
import com.mygame.engine.helpers.GameQuestions.GameConstants;
import java.awt.Dimension;
import java.awt.GraphicsDevice;
import java.awt.Rectangle;
import javax.swing.JFrame;

public class GameWindow extends JFrame {
    private final DisplaySurface surface;
    private boolean fullscreen;
    private Rectangle windowedBounds;

    public GameWindow(GameStateManager stateManager, ObjectManager objectManager) {
        super("ParadigmLocked");
        surface = new DisplaySurface(stateManager, objectManager, this::toggleFullscreen);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(640, 360));
        setContentPane(surface);
        setSize(GameConstants.LOGICAL_WIDTH, GameConstants.LOGICAL_HEIGHT);
        setLocationRelativeTo(null);
    }

    public DisplaySurface getSurface() {
        return surface;
    }

    public void toggleFullscreen() {
        GraphicsDevice device = getGraphicsConfiguration().getDevice();
        if (!fullscreen) {
            windowedBounds = getBounds();
            dispose();
            setUndecorated(true);
            setResizable(false);
            device.setFullScreenWindow(this);
            setVisible(true);
            fullscreen = true;
        } else {
            device.setFullScreenWindow(null);
            dispose();
            setUndecorated(false);
            setResizable(true);
            if (windowedBounds != null) {
                setBounds(windowedBounds);
            }
            setVisible(true);
            fullscreen = false;
        }
        surface.requestFocusInWindow();
    }
}
