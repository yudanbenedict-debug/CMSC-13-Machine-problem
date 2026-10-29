package com.mygame.ui;

import com.mygame.engine.core.AssetCache;
import com.mygame.engine.core.AssetLoader;
import com.mygame.engine.core.StateMethods;
import com.mygame.engine.helpers.constants.GameConstants;
import com.mygame.engine.hud.UiFontManager;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.concurrent.ThreadLocalRandom;

public final class LoadingState implements StateMethods {
    private final AssetLoader assetLoader;
    private final Runnable completionAction;
    private final String backgroundKey;
    private double elapsed;

    public LoadingState(Runnable completionAction) {
        this.assetLoader = new AssetLoader();
        this.completionAction = completionAction;
        this.backgroundKey = ThreadLocalRandom.current().nextBoolean()
            ? "UI_LOADING_BACKGROUND_1" : "UI_LOADING_BACKGROUND_2";
        AssetLoader.prepareLoadingScreenAssets();
    }

    @Override
    public void update(double deltaTime) {
        elapsed += Math.max(0, deltaTime);
        assetLoader.loadNext();
        if (assetLoader.isComplete()
                && elapsed >= GameConstants.LOADING_SCREEN_MINIMUM_SECONDS) {
            completionAction.run();
        }
    }

    @Override
    public void draw(Graphics2D graphics) {
        var background = AssetCache.get(backgroundKey);
        if (background != null) {
            graphics.drawImage(background, 0, 0,
                GameConstants.LOGICAL_WIDTH, GameConstants.LOGICAL_HEIGHT, null);
        } else {
            graphics.setColor(Color.BLACK);
            graphics.fillRect(0, 0, GameConstants.LOGICAL_WIDTH, GameConstants.LOGICAL_HEIGHT);
        }

        double progress = assetLoader.progress();
        int barX = GameConstants.LOADING_SCREEN_BAR_INSET;
        int barY = GameConstants.LOADING_SCREEN_BAR_Y;
        int barWidth = GameConstants.LOGICAL_WIDTH - barX * 2;
        int barHeight = GameConstants.LOADING_SCREEN_BAR_HEIGHT;

        graphics.setColor(Color.WHITE);
        graphics.setFont(UiFontManager.openSauceSansLight(20, Font.PLAIN));
        String label = "loading...";
        int labelX = barX;
        graphics.drawString(label, labelX, barY - 5);
        graphics.fillRect(barX, barY, (int) (barWidth * progress), barHeight);
    }

    @Override public void onEnter() { }
    @Override public void onExit() { }
    @Override public void mousePressed(MouseEvent event) { }
    @Override public void mouseReleased(MouseEvent event) { }
    @Override public void mouseMoved(MouseEvent event) { }
    @Override public void keyPressed(KeyEvent event) { }
    @Override public void keyReleased(KeyEvent event) { }
}
