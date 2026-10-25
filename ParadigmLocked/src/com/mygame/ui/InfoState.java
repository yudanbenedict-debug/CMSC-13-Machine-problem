package com.mygame.ui;

import com.mygame.engine.core.GameState;
import com.mygame.engine.core.GameStateManager;
import com.mygame.engine.core.StateMethods;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;

public abstract class InfoState implements StateMethods {
    private final GameStateManager stateManager;
    private final String title;
    private final String[] lines;
    private double scroll;
    private boolean backHovered;

    protected InfoState(GameStateManager stateManager, String title, String[] lines) {
        this.stateManager = stateManager;
        this.title = title;
        this.lines = lines;
    }

    @Override
    public void draw(Graphics2D g) {
        OverlayMenuRenderer.drawInfoOverlay(g, title, lines, scroll, backHovered);
    }

    @Override public void update(double dt) { }
    @Override public void onEnter() { }
    @Override public void onExit() { }
    @Override public void mousePressed(MouseEvent event) {
        if (OverlayMenuRenderer.getBackBounds().contains(event.getPoint())) {
            stateManager.setState(GameState.MENU);
        }
    }
    @Override public void mouseReleased(MouseEvent event) { }
    @Override public void mouseMoved(MouseEvent event) {
        backHovered = OverlayMenuRenderer.getBackBounds().contains(event.getPoint());
    }
    @Override public void mouseWheelMoved(MouseWheelEvent event) {
        scroll = OverlayMenuRenderer.scroll(scroll, event.getWheelRotation(), lines);
    }
    @Override public void keyPressed(KeyEvent event) { }
    @Override public void keyReleased(KeyEvent event) { }
}
