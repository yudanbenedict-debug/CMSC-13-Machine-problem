package com.mygame.ui;

import com.mygame.engine.core.GameState;
import com.mygame.engine.core.GameStateManager;
import com.mygame.engine.core.StateMethods;
import com.mygame.engine.helpers.GameConstants;
import com.mygame.engine.hud.Button;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

public class GameplayPreviewState implements StateMethods {
    private final GameStateManager stateManager;
    private final Button answerButton;
    private final Button helpButton;
    private final Button skipButton;
    private final Button consumableButton;
    private final Button menuButton;

    public GameplayPreviewState(GameStateManager stateManager) {
        this.stateManager = stateManager;
        answerButton = new Button(80, 550, 250, 64, "ANSWER", null, () -> { });
        helpButton = new Button(350, 550, 250, 64, "ASK FOR HELP", null, () -> { });
        skipButton = new Button(620, 550, 250, 64, "SKIP", null, () -> { });
        consumableButton = new Button(890, 550, 300, 64, "CONSUMABLE", null, () -> { });
        menuButton = new Button(1000, 40, 180, 48, "MENU", null, () -> stateManager.setState(GameState.MENU));
    }

    @Override
    public void draw(Graphics2D g) {
        g.setColor(new Color(15, 22, 30));
        g.fillRect(0, 0, GameConstants.LOGICAL_WIDTH, GameConstants.LOGICAL_HEIGHT);
        g.setColor(new Color(28, 63, 78));
        g.fillRect(0, 0, GameConstants.LOGICAL_WIDTH, 18);
        g.setColor(new Color(226, 240, 235));
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 34));
        g.drawString("PARADIGM SPACE", 80, 76);
        g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 20));
        g.setColor(new Color(171, 191, 194));
        g.drawString("OBJECT MODEL", 80, 118);
        g.drawString("TIME LEFT: --", 890, 76);
        g.drawString("0 POINTS", 890, 108);
        g.setColor(new Color(34, 47, 61));
        g.fillRoundRect(80, 155, 1110, 275, 16, 16);
        g.setColor(new Color(222, 235, 230));
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 30));
        g.drawString("QUESTION HERE", 125, 225);
        g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 21));
        g.setColor(new Color(171, 191, 194));
        g.drawString("The question and answer presentation will be connected here.", 125, 275);
        g.drawString("This preview intentionally does not resolve answers yet.", 125, 315);
        answerButton.draw(g);
        helpButton.draw(g);
        skipButton.draw(g);
        consumableButton.draw(g);
        menuButton.draw(g);
    }

    @Override public void update(double dt) { }
    @Override public void onEnter() { }
    @Override public void onExit() { }
    @Override public void mousePressed(MouseEvent event) { updateButtons(event.getPoint(), true); }
    @Override public void mouseReleased(MouseEvent event) { updateButtons(event.getPoint(), false); }
    @Override public void mouseMoved(MouseEvent event) { updateHover(event.getPoint()); }
    @Override public void keyPressed(KeyEvent event) { }
    @Override public void keyReleased(KeyEvent event) { }

    private void updateHover(Point point) {
        answerButton.updateHover(point);
        helpButton.updateHover(point);
        skipButton.updateHover(point);
        consumableButton.updateHover(point);
        menuButton.updateHover(point);
    }

    private void updateButtons(Point point, boolean pressed) {
        Button[] buttons = { answerButton, helpButton, skipButton, consumableButton, menuButton };
        for (Button button : buttons) {
            if (pressed) {
                button.press(point);
            } else {
                button.release(point);
            }
        }
    }
}
