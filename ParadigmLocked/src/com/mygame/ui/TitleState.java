package com.mygame.ui;

import com.mygame.engine.core.AssetCache;
import com.mygame.engine.core.GameState;
import com.mygame.engine.core.GameStateManager;
import com.mygame.engine.core.Img;
import com.mygame.engine.core.Sfx;
import com.mygame.engine.core.SoundManager;
import com.mygame.engine.core.StateMethods;
import com.mygame.engine.helpers.algorithms.EasingMethods;
import com.mygame.engine.helpers.constants.GameConstants;
import com.mygame.engine.helpers.constants.UiLayout;
import com.mygame.engine.hud.UiFontManager;
import com.mygame.engine.hud.UiText;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

// Title intro: logo fades in, any key starts and slides into the menu.
public final class TitleState implements StateMethods {
    private static final double LOGO_FADE_SECONDS = 1.2;
    private static final double START_TRANSITION_SECONDS = 1.1;
    private static final double PROMPT_FADE_SECONDS = 1.0;
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
        SoundManager.playMusic(Sfx.THEME_MAIN_MENU);
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
        MainMenuState.drawCircuit(g);
        double transition = startedAt < 0 ? 0 : Math.min(1, (elapsed - startedAt) / START_TRANSITION_SECONDS);
        double eased = EasingMethods.smoothStep(transition);
        g.setColor(new Color(0, 0, 0, (int) (150 * (1 - eased))));
        g.fillRect(0, 0, GameConstants.LOGICAL_WIDTH, GameConstants.LOGICAL_HEIGHT);

        BufferedImage logo = AssetCache.get(Img.MAIN_MENU_LOGO);
        if (logo != null) {
            float alpha = (float) EasingMethods.smoothStep(elapsed / LOGO_FADE_SECONDS);
            double scale = 1.35 - 0.35 * eased;
            int width = (int) (UiLayout.MENU_LOGO_WIDTH * scale);
            int height = (int) (UiLayout.MENU_LOGO_HEIGHT * scale);
            int startY = 230;
            int y = (int) (startY + (UiLayout.MENU_LOGO_Y - startY) * eased);
            java.awt.Composite old = g.getComposite();
            g.setComposite(java.awt.AlphaComposite.getInstance(java.awt.AlphaComposite.SRC_OVER, alpha));
            g.drawImage(logo, (GameConstants.LOGICAL_WIDTH - width) / 2, y, width, height, null);
            g.setComposite(old);
        }

        drawPrompt(g);
    }

    // Prompt opacity: ease-in on appear, gentle pulse, ease-out after a key press.
    private void drawPrompt(Graphics2D g) {
        double appear = EasingMethods.easeIn(clamp((elapsed - LOGO_FADE_SECONDS * 0.6) / PROMPT_FADE_SECONDS));
        double pulse = 0.75 + 0.25 * (0.5 + 0.5 * Math.sin(elapsed * 2.6));
        double out = startedAt < 0 ? 1
            : 1 - EasingMethods.easeIn(clamp((elapsed - startedAt) / (START_TRANSITION_SECONDS * 0.6)));
        int alpha = (int) Math.round(255 * appear * pulse * out);
        if (alpha <= 0) {
            return;
        }
        UiText.drawCentered(g, "PRESS ANY KEY TO START", UiFontManager.luckiestGuy(30, Font.PLAIN),
            GameConstants.LOGICAL_WIDTH / 2f, 560, new Color(246, 242, 229, alpha),
            new Color(255, 236, 170, alpha / 2));
    }

    private static double clamp(double value) {
        return Math.max(0, Math.min(1, value));
    }

    private void start() {
        if (startedAt >= 0) {
            return;
        }
        startedAt = elapsed;
        SoundManager.playSFX(Sfx.UI_MENU_ONTITLESTART);
        // quieter menu theme from here on
        SoundManager.setLevel(Sfx.THEME_MAIN_MENU, GameConstants.MENU_MUSIC_DUCKED_LEVEL);
    }

    @Override public void mousePressed(MouseEvent event) { start(); }
    @Override public void mouseReleased(MouseEvent event) { }
    @Override public void mouseMoved(MouseEvent event) { }
    @Override public void keyPressed(KeyEvent event) { start(); }
    @Override public void keyReleased(KeyEvent event) { }
}
