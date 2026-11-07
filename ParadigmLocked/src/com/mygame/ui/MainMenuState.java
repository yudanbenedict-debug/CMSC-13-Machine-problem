package com.mygame.ui;

import com.mygame.engine.core.AssetCache;
import com.mygame.engine.core.GameState;
import com.mygame.engine.core.GameStateManager;
import com.mygame.engine.core.Img;
import com.mygame.engine.core.Sfx;
import com.mygame.engine.core.SoundManager;
import com.mygame.engine.core.StateMethods;
import com.mygame.engine.helpers.algorithms.EasingMethods;
import com.mygame.engine.helpers.constants.UiLayout;
import com.mygame.engine.hud.Button;
import com.mygame.engine.hud.CircuitBackground;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

public class MainMenuState implements StateMethods {
    private final GameStateManager stateManager;
    private final Button startButton;
    private final Button howToPlayButton;
    private final Button creditsButton;
    private final Button quitButton;
    private double buttonsFade = 1;

    public MainMenuState(GameStateManager stateManager) {
        this.stateManager = stateManager;
        int x = UiLayout.MENU_BUTTON_X;
        int width = UiLayout.MENU_BUTTON_WIDTH;
        int height = UiLayout.MENU_BUTTON_HEIGHT;
        int startY = UiLayout.MENU_BUTTON_START_Y;
        int spacing = UiLayout.MENU_BUTTON_SPACING;
        startButton = new Button(x, startY, width, height, "START GAME", Img.FANCY_BORDER, () -> stateManager.setState(GameState.SIDE_SELECT));
        howToPlayButton = new Button(x, startY + spacing, width, height, "HOW TO PLAY", Img.FANCY_BORDER, () -> stateManager.setState(GameState.HOWTOPLAY));
        creditsButton = new Button(x, startY + spacing * 2, width, height, "CREDITS", Img.FANCY_BORDER, () -> stateManager.setState(GameState.CREDITS));
        quitButton = new Button(x, startY + spacing * 3, width, height, "QUIT", Img.FANCY_BORDER, () -> System.exit(0));
    }

    @Override
    public void draw(Graphics2D g) {
        drawBackground(g);
        // buttons fade in when coming from the title screen
        java.awt.Composite old = g.getComposite();
        float alpha = (float) EasingMethods.easeIn(Math.min(1, buttonsFade));
        g.setComposite(java.awt.AlphaComposite.getInstance(java.awt.AlphaComposite.SRC_OVER, alpha));
        startButton.draw(g);
        howToPlayButton.draw(g);
        creditsButton.draw(g);
        quitButton.draw(g);
        g.setComposite(old);
    }

    // Menu background without the logo (behind How to Play / Credits).
    public void drawBackdrop(Graphics2D g) {
        drawCircuit(g);
    }

    // shared menu background + circuit cycling green -> blue -> red
    public static void drawCircuit(Graphics2D g) {
        CircuitBackground.menu().setMenuCycle();
        CircuitBackground.menu().draw(g, AssetCache.get(Img.MAIN_MENU_BACKGROUND));
    }

    public void drawBackground(Graphics2D g) {
        drawBackdrop(g);
        BufferedImage logo = AssetCache.get(Img.MAIN_MENU_LOGO);
        if (logo != null) {
            g.drawImage(logo, UiLayout.MENU_LOGO_X,
                    UiLayout.MENU_LOGO_Y,
                    UiLayout.MENU_LOGO_WIDTH,
                    UiLayout.MENU_LOGO_HEIGHT, null);
        }
    }

    @Override public void update(double dt) { buttonsFade = Math.min(1, buttonsFade + dt / 0.7); }

    @Override
    public void onEnter() {
        SoundManager.playMusic(Sfx.THEME_MAIN_MENU);
        buttonsFade = stateManager.getPreviousState() == GameState.TITLE ? 0 : 1;
    }
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
