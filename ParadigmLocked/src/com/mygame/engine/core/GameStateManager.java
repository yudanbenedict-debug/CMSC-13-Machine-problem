package com.mygame.engine.core;

import java.awt.Graphics2D;
import java.util.EnumMap;
import java.util.Map;

/*
    Small manager that delegates update/draw/input events to the current GameState handler.
    Once a handler implements all StateMethods, it can be called to hold a certain State
    It connects both logics, allowing a certain class to control a certain state.
 */

public class GameStateManager {
    private GameState current;
    private final Map<GameState, StateMethods> handlers = new EnumMap<>(GameState.class);

    public GameStateManager(GameState initial) {
        this.current = initial;
    }

    public void register(GameState state, StateMethods handler) {
        handlers.put(state, handler);
    }

    public void setState(GameState newState) {
        if (newState == current) return;
        StateMethods oldHandler = handlers.get(current);
        if (oldHandler != null) oldHandler.onExit();
        current = newState;
        StateMethods newHandler = handlers.get(current);
        if (newHandler != null) newHandler.onEnter();
    }

    public GameState getState() { return current; }

    public void update(double dt) { StateMethods h = handlers.get(current); if (h != null) h.update(dt); }
    public void draw(Graphics2D g) { StateMethods h = handlers.get(current); if (h != null) h.draw(g); }

    public void mousePressed(java.awt.event.MouseEvent e) { StateMethods h = handlers.get(current); if (h != null) h.mousePressed(e); }
    public void mouseReleased(java.awt.event.MouseEvent e) { StateMethods h = handlers.get(current); if (h != null) h.mouseReleased(e); }
    public void mouseMoved(java.awt.event.MouseEvent e) { StateMethods h = handlers.get(current); if (h != null) h.mouseMoved(e); }
    public void keyPressed(java.awt.event.KeyEvent e) { StateMethods h = handlers.get(current); if (h != null) h.keyPressed(e); }
    public void keyReleased(java.awt.event.KeyEvent e) { StateMethods h = handlers.get(current); if (h != null) h.keyReleased(e); }
}

