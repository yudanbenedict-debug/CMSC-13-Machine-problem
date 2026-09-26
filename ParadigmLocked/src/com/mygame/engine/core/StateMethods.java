package com.mygame.engine.core;

import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

/*
 * Interface for state-specific behavior (menu, playing, paused, etc.).
 */

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
    void keyPressed(KeyEvent e);
    void keyReleased(KeyEvent e);
}

