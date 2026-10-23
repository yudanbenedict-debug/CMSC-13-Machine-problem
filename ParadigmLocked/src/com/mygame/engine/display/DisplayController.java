package com.mygame.engine.display;

import java.awt.GraphicsDevice;
import java.awt.Rectangle;
import javax.swing.JFrame;

/*
    Display controller made to offhand the methods building up in GameWindow
    The fullscreen logic, boundaries and window boundaries and display logic
    are all ran in displaycontroller before being sent to GameWindow
*/
public class DisplayController {
    private final JFrame window;
    private boolean fullscreen;
    private Rectangle windowedBounds;
    private Runnable displayChanged = () -> { };

    public DisplayController(JFrame window) {
        this.window = window;
    }

    public void setDisplayChangedListener(Runnable displayChanged) {
        this.displayChanged = displayChanged == null ? () -> { } : displayChanged;
    }

    public void toggleFullscreen() {
        GraphicsDevice device = window.getGraphicsConfiguration().getDevice();
        if (!fullscreen) {
            windowedBounds = new Rectangle(window.getBounds());
            window.dispose();
            window.setUndecorated(true);
            window.setResizable(false);
            device.setFullScreenWindow(window);
            window.setVisible(true);
            fullscreen = true;
        } else {
            device.setFullScreenWindow(null);
            window.dispose();
            window.setUndecorated(false);
            window.setResizable(true);
            if (windowedBounds != null) {
                window.setBounds(windowedBounds);
            }
            window.setVisible(true);
            fullscreen = false;
        }
        window.revalidate();
        window.repaint();
        displayChanged.run();
    }

    public boolean isFullscreen() {
        return fullscreen;
    }
}
