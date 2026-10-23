package com.mygame.inputs;

import com.mygame.engine.core.GameStateManager;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.function.Function;
import javax.swing.JComponent;

/*
    Streamlined Universal Input Handling:
    How we got here:
    Idea 1: reuse keyboard and mouseinput handling per state
    Problem 1: We moved away from state handling to displaySurface
    Display Surface is a heirarchy above states (it handles scaling) before
    being sent to statemanager
    Solution: Streamline inputs into inputhandler with the same MouseInput logic
    Now looks like this:
    Display Surface ->
        -> InputHandler
            -> Coordinate Conversions
                -> GameState Manager -> 
                    -> Running Gamestate and Gameloop
    */
public class InputHandler extends MouseAdapter implements KeyListener {
    private final GameStateManager stateManager;
    private final Function<MouseEvent, MouseEvent> mouseEventTranslator;
    private final Runnable toggleFullscreen;

    public InputHandler(JComponent component, GameStateManager stateManager,
                        Function<MouseEvent, MouseEvent> mouseEventTranslator,
                        Runnable toggleFullscreen) {
        this.stateManager = stateManager;
        this.mouseEventTranslator = mouseEventTranslator;
        this.toggleFullscreen = toggleFullscreen;
        component.addMouseListener(this);
        component.addMouseMotionListener(this);
        component.addKeyListener(this);
    }

    @Override
    public void mousePressed(MouseEvent event) {
        stateManager.mousePressed(mouseEventTranslator.apply(event));
    }

    @Override
    public void mouseReleased(MouseEvent event) {
        stateManager.mouseReleased(mouseEventTranslator.apply(event));
    }

    @Override
    public void mouseMoved(MouseEvent event) {
        stateManager.mouseMoved(mouseEventTranslator.apply(event));
    }

    @Override
    public void mouseDragged(MouseEvent event) {
        stateManager.mouseMoved(mouseEventTranslator.apply(event));
    }

    @Override
    public void mouseEntered(MouseEvent event) {
        event.getComponent().requestFocusInWindow();
    }

    @Override
    public void keyPressed(KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.VK_F11) {
            toggleFullscreen.run();
            return;
        }
        stateManager.keyPressed(event);
    }

    @Override
    public void keyReleased(KeyEvent event) {
        stateManager.keyReleased(event);
    }

    @Override
    public void keyTyped(KeyEvent event) {
    }
}
