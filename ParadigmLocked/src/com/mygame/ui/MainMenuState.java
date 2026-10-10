package com.mygame.ui;

import com.mygame.engine.core.GameState;
import com.mygame.engine.core.GameStateManager;
import com.mygame.engine.core.StateMethods;
import com.mygame.engine.helpers.GameQuestions.GameConstants;
import com.mygame.engine.hud.Button;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

public class MainMenuState implements StateMethods {
    private final GameStateManager stateManager;
    private final Button startButton;
    private final Button howToPlayButton;
    private final Button creditsButton;
    private final Button quitButton;

    public MainMenuState(GameStateManager stateManager) {
        this.stateManager = stateManager;
        int x = 100;
        int width = 330;
        int height = 64;
        startButton = new Button(x, 320, width, height, "START GAME", "UI_MENU_STARTGAME", () -> stateManager.setState(GameState.PLAYING));
        howToPlayButton = new Button(x, 400, width, height, "HOW TO PLAY", "UI_MENU_HOWTOPLAY", () -> stateManager.setState(GameState.HOWTOPLAY));
        creditsButton = new Button(x, 480, width, height, "CREDITS", "UI_MENU_CREDITS", () -> stateManager.setState(GameState.CREDITS));
        quitButton = new Button(x, 560, width, height, "QUIT", "UI_MENU_QUIT", () -> System.exit(0));
    }

    @Override
    public void draw(Graphics2D g) {
        g.setColor(new Color(12, 19, 28));
        g.fillRect(0, 0, GameConstants.LOGICAL_WIDTH, GameConstants.LOGICAL_HEIGHT);
        g.setColor(new Color(28, 63, 78));
        g.fillRect(0, 0, GameConstants.LOGICAL_WIDTH, 18);
        g.setColor(new Color(228, 241, 235));
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 54));
        g.drawString("BREAK THE PARADIGMLOCK.", 100, 150);
        g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 25));
        g.setColor(new Color(154, 201, 193));
        g.drawString("ASCEND, PROGRAMMER!", 104, 195);
        g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 18));
        g.setColor(new Color(146, 160, 171));
        g.drawString("F11  Fullscreen", 100, 675);
        startButton.draw(g);
        howToPlayButton.draw(g);
        creditsButton.draw(g);
        quitButton.draw(g);
    }

    @Override public void update(double dt) { }
    @Override public void onEnter() { }
    @Override public void onExit() { }
    @Override public void mousePressed(MouseEvent event) { updatePressed(event, true); }
    @Override public void mouseReleased(MouseEvent event) { updatePressed(event, false); }
    @Override public void mouseMoved(MouseEvent event) { updateHover(event); }
    @Override public void keyPressed(KeyEvent event) { }
    @Override public void keyReleased(KeyEvent event) { }

    private void updateHover(MouseEvent event) {
        Point point = event.getPoint();
        startButton.updateHover(point);
        howToPlayButton.updateHover(point);
        creditsButton.updateHover(point);
        quitButton.updateHover(point);
    }

    private void updatePressed(MouseEvent event, boolean pressed) {
        Point point = event.getPoint();
        if (pressed) {
            startButton.press(point);
            howToPlayButton.press(point);
            creditsButton.press(point);
            quitButton.press(point);
        } else {
            startButton.release(point);
            howToPlayButton.release(point);
            creditsButton.release(point);
            quitButton.release(point);
        }
    }
}
