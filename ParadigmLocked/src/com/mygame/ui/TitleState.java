package com.mygame.ui;

import com.mygame.engine.core.AssetCache;
import com.mygame.engine.core.GameState;
import com.mygame.engine.core.GameStateManager;
import com.mygame.engine.core.SoundManager;
import com.mygame.engine.core.StateMethods;
import com.mygame.engine.helpers.constants.GameConstants;
import com.mygame.engine.hud.TemporaryUiRenderer;
import com.mygame.engine.hud.UiFontManager;
import com.mygame.engine.hud.UiText;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

/*
    Title introduction (UI_MENU_ONTITLESTART idea):
    after loading, the logo fades in over the menu art with a pulsing "PRESS ANY KEY TO START".
    Any key or click plays UI_MENU_ONTITLESTART, the logo slides up into its menu position
    while the screen flashes, then the main menu appears.
 */
public final class TitleState implements StateMethods {
    private static final double LOGO_FADE_SECONDS = 1.2;
    private static final double START_TRANSITION_SECONDS = 0.9;
    private final GameStateManager stateManager;
    private double elapsed;
    private double startedAt = -1;

    public TitleState(GameStateManager stateManager) {
        this.stateManager = stateManager;
    }

    @Override
    public void onEnter() {
        elapsed = 0;
        startedAt = -1;
        SoundManager.playMusic("THEME_MAIN_MENU");
    }

    @Override public void onExit() { }

    @Override
    public void update(double dt) {
        elapsed += dt;
        if (startedAt >= 0 && elapsed - startedAt >= START_TRANSITION_SECONDS) {
            stateManager.setState(GameState.MENU);
        }
    }

    @Override
    public void draw(Graphics2D g) {
        BufferedImage background = AssetCache.get("UI_MAIN_MENU_BACKGROUND");
        if (background != null) {
            g.drawImage(background, 0, 0, GameConstants.LOGICAL_WIDTH, GameConstants.LOGICAL_HEIGHT, null);
        } else {
            TemporaryUiRenderer.drawScreenBackground(g);
        }
        double transition = startedAt < 0 ? 0 : Math.min(1, (elapsed - startedAt) / START_TRANSITION_SECONDS);
        double eased = 1 - Math.pow(1 - transition, 3);
        g.setColor(new Color(0, 0, 0, (int) (150 * (1 - eased))));
        g.fillRect(0, 0, GameConstants.LOGICAL_WIDTH, GameConstants.LOGICAL_HEIGHT);

        BufferedImage logo = AssetCache.get("UI_MAIN_MENU_LOGO");
        if (logo != null) {
            float alpha = (float) Math.min(1, elapsed / LOGO_FADE_SECONDS);
            double scale = 1.35 - 0.35 * eased;
            int width = (int) (GameConstants.TEMPORARY_MENU_LOGO_WIDTH * scale);
            int height = (int) (GameConstants.TEMPORARY_MENU_LOGO_HEIGHT * scale);
            int startY = 230;
            int y = (int) (startY + (GameConstants.TEMPORARY_MENU_LOGO_Y - startY) * eased);
            java.awt.Composite old = g.getComposite();
            g.setComposite(java.awt.AlphaComposite.getInstance(java.awt.AlphaComposite.SRC_OVER, alpha));
            g.drawImage(logo, (GameConstants.LOGICAL_WIDTH - width) / 2, y, width, height, null);
            g.setComposite(old);
        }

        if (startedAt < 0 && elapsed > LOGO_FADE_SECONDS * 0.6) {
            int alpha = (int) (140 + 115 * (0.5 + 0.5 * Math.sin(elapsed * 3.2)));
            UiText.drawCentered(g, "PRESS ANY KEY TO START", UiFontManager.luckiestGuy(30, Font.PLAIN),
                GameConstants.LOGICAL_WIDTH / 2f, 560, new Color(246, 242, 229, alpha),
                new Color(255, 236, 170, alpha / 2));
        }
        if (startedAt >= 0) {
            // quick white flash synced with the start sound
            int flash = (int) (200 * Math.max(0, 1 - (elapsed - startedAt) / 0.35));
            g.setColor(new Color(255, 255, 255, flash));
            g.fillRect(0, 0, GameConstants.LOGICAL_WIDTH, GameConstants.LOGICAL_HEIGHT);
        }
    }

    private void start() {
        if (startedAt >= 0) {
            return;
        }
        startedAt = elapsed;
        SoundManager.playSFX("UI_MENU_ONTITLESTART");
    }

    @Override public void mousePressed(MouseEvent event) { start(); }
    @Override public void mouseReleased(MouseEvent event) { }
    @Override public void mouseMoved(MouseEvent event) { }
    @Override public void keyPressed(KeyEvent event) { start(); }
    @Override public void keyReleased(KeyEvent event) { }
}
