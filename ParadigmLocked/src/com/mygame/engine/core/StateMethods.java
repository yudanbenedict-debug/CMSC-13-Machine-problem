package com.mygame.engine.core;

import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;

// every game state (menu, playing, paused, etc.) implements these methods

public interface StateMethods {
    //draw components
    void update(double dt);
    void draw(Graphics2D g);
    //exit/enter logic components
    void onEnter();
    void onExit();
    //keyboard and mouse input class components
    void mousePressed(MouseEvent e);
    void mouseReleased(MouseEvent e);
    void mouseMoved(MouseEvent e);
    default void mouseWheelMoved(MouseWheelEvent e) { }
    void keyPressed(KeyEvent e);
    void keyReleased(KeyEvent e);
}

