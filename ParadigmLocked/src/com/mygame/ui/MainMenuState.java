package com.mygame.ui;

import com.mygame.engine.core.AssetCache;
import com.mygame.engine.core.GameState;
import com.mygame.engine.core.GameStateManager;
import com.mygame.engine.core.StateMethods;
import com.mygame.engine.helpers.constants.GameConstants;
import com.mygame.engine.hud.Button;
import com.mygame.engine.hud.TemporaryUiRenderer;
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

    public MainMenuState(GameStateManager stateManager) {
        this.stateManager = stateManager;
        AssetCache.load("UI_MAIN_MENU_BACKGROUND", "images/mainmenu/MAIN_MENU_STATIC.png");
        AssetCache.load("UI_MAIN_MENU_LOGO", "images/mainmenu/MAIN_MENU_LOGO_STATIC.png");
        AssetCache.load("UI_MENU_FANCY_BORDER", "images/buttons/FANCY_BORDER.png");
        int x = GameConstants.TEMPORARY_MENU_BUTTON_X;
        int width = GameConstants.TEMPORARY_MENU_BUTTON_WIDTH;
        int height = GameConstants.TEMPORARY_MENU_BUTTON_HEIGHT;
        int startY = GameConstants.TEMPORARY_MENU_BUTTON_START_Y;
        int spacing = GameConstants.TEMPORARY_MENU_BUTTON_SPACING;
        startButton = new Button(x, startY, width, height, "START GAME", "UI_MENU_FANCY_BORDER", () -> stateManager.setState(GameState.PLAYING));
        howToPlayButton = new Button(x, startY + spacing, width, height, "HOW TO PLAY", "UI_MENU_FANCY_BORDER", () -> stateManager.setState(GameState.HOWTOPLAY));
        creditsButton = new Button(x, startY + spacing * 2, width, height, "CREDITS", "UI_MENU_FANCY_BORDER", () -> stateManager.setState(GameState.CREDITS));
        quitButton = new Button(x, startY + spacing * 3, width, height, "QUIT", "UI_MENU_FANCY_BORDER", () -> System.exit(0));
    }

    @Override
    public void draw(Graphics2D g) {
        BufferedImage background = AssetCache.get("UI_MAIN_MENU_BACKGROUND");
        if (background != null) {
            g.drawImage(background, 0, 0, GameConstants.LOGICAL_WIDTH, GameConstants.LOGICAL_HEIGHT, null);
        } else {
            TemporaryUiRenderer.drawScreenBackground(g);
        }
        BufferedImage logo = AssetCache.get("UI_MAIN_MENU_LOGO");
        if (logo != null) {
            g.drawImage(logo, GameConstants.TEMPORARY_MENU_LOGO_X,
                    GameConstants.TEMPORARY_MENU_LOGO_Y,
                    GameConstants.TEMPORARY_MENU_LOGO_WIDTH,
                    GameConstants.TEMPORARY_MENU_LOGO_HEIGHT, null);
        }
        // if (logo == null) {
        //     g.setColor(new Color(228, 241, 235));
        //     g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 54));
        //     g.drawString("BREAK THE PARADIGMLOCK.", 100, 150);
        //     g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 25));
        //     g.setColor(new Color(154, 201, 193));
        //     g.drawString("ASCEND, PROGRAMMER!", 104, 195);
        // }
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
