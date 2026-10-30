package com.mygame.ui;

import com.mygame.engine.core.AssetCache;
import com.mygame.engine.core.GameState;
import com.mygame.engine.core.GameStateManager;
import com.mygame.engine.core.SoundManager;
import com.mygame.engine.core.StateMethods;
import com.mygame.engine.helpers.constants.GameConstants;
import com.mygame.engine.hud.UiFontManager;
import com.mygame.engine.hud.UiText;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.function.Consumer;

/*
    WON / LOST overlay. Uses the OVERLAY_<SIDE>_WIN / _LOSE assets of the side you fight for.
    It's a timed showcase: it fades in over the frozen gameplay, ignores input,
    then returns to the main menu after END_SCREEN_SECONDS.
 */
public final class EndScreenState implements StateMethods {
    private final GameStateManager stateManager;
    private boolean won;
    private int finalScore;
    private Consumer<Graphics2D> backdrop;
    private double elapsed;

    public EndScreenState(GameStateManager stateManager) {
        this.stateManager = stateManager;
    }

    public void show(boolean won, int finalScore, Consumer<Graphics2D> backdrop) {
        this.won = won;
        this.finalScore = finalScore;
        this.backdrop = backdrop;
    }

    private String overlayKey() {
        String side = GameConstants.PLAYER_SIDE_IMMUTABLE_MOTHER ? "IMMUTABLEMOTHER" : "DARKWEBKING";
        return "UI_OVERLAY_" + side + (won ? "_WIN" : "_LOSE");
    }

    @Override
    public void onEnter() {
        elapsed = 0;
        SoundManager.stopAllMusic();
        SoundManager.playSFX(won ? "GAME_WON" : "GAME_LOST");
    }

    @Override public void onExit() { }

    @Override
    public void update(double dt) {
        elapsed += dt;
        if (elapsed >= GameConstants.END_SCREEN_SECONDS) {
            stateManager.setState(GameState.MENU);
        }
    }

    @Override
    public void draw(Graphics2D g) {
        if (backdrop != null) {
            backdrop.accept(g);
        }
        float fade = (float) Math.min(1, elapsed / 0.8);
        g.setColor(new Color(0, 0, 0, (int) (225 * fade)));
        g.fillRect(0, 0, GameConstants.LOGICAL_WIDTH, GameConstants.LOGICAL_HEIGHT);
        Composite old = g.getComposite();
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, fade));
        BufferedImage overlay = AssetCache.get(overlayKey());
        if (overlay != null) {
            g.drawImage(overlay, 0, 0, GameConstants.LOGICAL_WIDTH, GameConstants.LOGICAL_HEIGHT, null);
        } else {
            UiText.drawCentered(g, won ? "VICTORY" : "DEFEAT", UiFontManager.luckiestGuy(90, Font.PLAIN),
                GameConstants.LOGICAL_WIDTH / 2f, 380, Color.WHITE, null);
        }
        UiText.drawCentered(g, "FINAL SCORE: " + finalScore, UiFontManager.luckiestGuy(34, Font.PLAIN),
            GameConstants.LOGICAL_WIDTH / 2f, 640, new Color(246, 242, 229), new Color(0, 0, 0, 200));
        g.setComposite(old);
    }

    @Override public void mousePressed(MouseEvent event) { }
    @Override public void mouseReleased(MouseEvent event) { }
    @Override public void mouseMoved(MouseEvent event) { }
    @Override public void keyPressed(KeyEvent event) { }
    @Override public void keyReleased(KeyEvent event) { }
}
