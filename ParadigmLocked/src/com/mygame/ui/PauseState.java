package com.mygame.ui;

import com.mygame.engine.core.GameState;
import com.mygame.engine.core.GameStateManager;
import com.mygame.engine.core.SoundManager;
import com.mygame.engine.core.StateMethods;
import com.mygame.engine.helpers.constants.GameConstants;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

public final class PauseState implements StateMethods {
    private final GameStateManager stateManager;
    private double volume = 0.6;
    private double lastAudibleVolume = volume;
    private boolean muted;
    private boolean draggingVolume;
    private int hoveredOption = -1;

    public PauseState(GameStateManager stateManager) {
        this.stateManager = stateManager;
    }

    @Override
    public void draw(Graphics2D graphics) {
        OverlayMenuRenderer.drawPauseOverlay(graphics,
            hoveredOption == 0, hoveredOption == 1, hoveredOption == 2, volume, muted);
    }

    @Override public void update(double deltaTime) { }
    @Override public void onEnter() { }
    @Override public void onExit() { }

    @Override
    public void mousePressed(MouseEvent event) {
        Point point = event.getPoint();
        if (OverlayMenuRenderer.getPauseMuteBounds().contains(point)) {
            toggleMute();
            return;
        }
        if (OverlayMenuRenderer.getPauseVolumeBounds().contains(point)) {
            draggingVolume = true;
            updateVolume(point.x);
            return;
        }
        if (OverlayMenuRenderer.getPauseOptionBounds(0).contains(point)) {
            stateManager.setState(GameState.PLAYING);
        } else if (OverlayMenuRenderer.getPauseOptionBounds(1).contains(point)) {
            stateManager.setState(GameState.HOWTOPLAY);
        } else if (OverlayMenuRenderer.getPauseOptionBounds(2).contains(point)) {
            stateManager.setState(GameState.MENU);
        }
    }

    @Override
    public void mouseReleased(MouseEvent event) {
        draggingVolume = false;
    }

    @Override
    public void mouseMoved(MouseEvent event) {
        updateHover(event.getPoint());
    }

    @Override
    public void keyPressed(KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.VK_ESCAPE || event.getKeyCode() == KeyEvent.VK_P) {
            stateManager.setState(GameState.PLAYING);
        }
    }

    @Override public void keyReleased(KeyEvent event) { }

    private void updateHover(Point point) {
        hoveredOption = -1;
        for (int index = 0; index < 3; index++) {
            if (OverlayMenuRenderer.getPauseOptionBounds(index).contains(point)) {
                hoveredOption = index;
                break;
            }
        }
        if (draggingVolume) {
            updateVolume(point.x);
        }
    }

    private void updateVolume(int mouseX) {
        var bounds = OverlayMenuRenderer.getPauseVolumeBounds();
        int start = bounds.x + GameConstants.PAUSE_VOLUME_ICON_WIDTH + 8;
        int width = bounds.width - GameConstants.PAUSE_VOLUME_ICON_WIDTH - 16;
        volume = Math.max(0, Math.min(1, (mouseX - start) / (double) width));
        if (volume > 0) {
            lastAudibleVolume = volume;
            muted = false;
        }
        SoundManager.setVolumeMaster((float) (muted ? 0 : volume));
    }

    private void toggleMute() {
        if (muted) {
            volume = lastAudibleVolume;
            muted = false;
        } else {
            if (volume > 0) {
                lastAudibleVolume = volume;
            }
            muted = true;
        }
        SoundManager.setVolumeMaster((float) (muted ? 0 : volume));
    }
}
