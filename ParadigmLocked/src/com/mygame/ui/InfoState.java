package com.mygame.ui;

import com.mygame.engine.core.GameStateManager;
import com.mygame.engine.core.StateMethods;
import com.mygame.engine.helpers.GameQuestions.GameConstants;
import com.mygame.engine.hud.Button;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

public abstract class InfoState implements StateMethods {
    private final GameStateManager stateManager;
    private final String title;
    private final String[] lines;
    private final Button backButton;

    protected InfoState(GameStateManager stateManager, String title, String[] lines) {
        this.stateManager = stateManager;
        this.title = title;
        this.lines = lines;
        backButton = new Button(100, 590, 250, 64, "BACK TO MENU", null, () -> stateManager.setState(com.mygame.engine.core.GameState.MENU));
    }

    @Override
    public void draw(Graphics2D g) {
        g.setColor(new Color(12, 19, 28));
        g.fillRect(0, 0, GameConstants.LOGICAL_WIDTH, GameConstants.LOGICAL_HEIGHT);
        g.setColor(new Color(228, 241, 235));
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 48));
        g.drawString(title, 100, 120);
        g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 24));
        g.setColor(new Color(190, 208, 207));
        int y = 205;
        for (String line : lines) {
            g.drawString(line, 100, y);
            y += 48;
        }
        backButton.draw(g);
    }

    @Override public void update(double dt) { }
    @Override public void onEnter() { }
    @Override public void onExit() { }
    @Override public void mousePressed(MouseEvent event) { backButton.press(event.getPoint()); }
    @Override public void mouseReleased(MouseEvent event) { backButton.release(event.getPoint()); }
    @Override public void mouseMoved(MouseEvent event) { backButton.updateHover(event.getPoint()); }
    @Override public void keyPressed(KeyEvent event) { }
    @Override public void keyReleased(KeyEvent event) { }
}
