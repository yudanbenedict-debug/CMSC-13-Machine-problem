package com.mygame.ui;

import com.mygame.engine.core.GameState;
import com.mygame.engine.core.GameStateManager;
import com.mygame.engine.core.SoundManager;
import com.mygame.engine.core.StateMethods;
import com.mygame.engine.helpers.constants.GameConstants;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.function.Consumer;

public final class PauseState implements StateMethods {
    private final GameStateManager stateManager;
    private final Consumer<Graphics2D> backdrop;
    private double volume = SoundManager.getVolumeMaster();
    private double lastAudibleVolume = volume;
    private boolean muted;
    private boolean draggingVolume;
    private boolean muteHovered;
    private int hoveredOption = -1;

    /** backdrop draws the frozen gameplay behind the pause overlay (can be null). */
    public PauseState(GameStateManager stateManager, Consumer<Graphics2D> backdrop) {
        this.stateManager = stateManager;
        this.backdrop = backdrop;
    }

    @Override
    public void draw(Graphics2D graphics) {
        if (backdrop != null) {
            backdrop.accept(graphics);
            graphics.setColor(new Color(0, 0, 0, 175));
            graphics.fillRect(0, 0, GameConstants.LOGICAL_WIDTH, GameConstants.LOGICAL_HEIGHT);
        }
        OverlayMenuRenderer.drawPauseOverlay(graphics, hoveredOption, volume, muted, muteHovered);
    }

    @Override public void update(double deltaTime) { }
    @Override public void onEnter() { hoveredOption = -1; draggingVolume = false; }
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
        int now = -1;
        for (int index = 0; index < 3; index++) {
            if (OverlayMenuRenderer.getPauseOptionBounds(index).contains(point)) {
                now = index;
                break;
            }
        }
        if (now >= 0 && now != hoveredOption) {
            SoundManager.playMenuHover();
        }
        hoveredOption = now;
        muteHovered = OverlayMenuRenderer.getPauseMuteBounds().contains(point);
        if (draggingVolume) {
            updateVolume(point.x);
        }
    }

    private void updateVolume(int mouseX) {
        int start = OverlayMenuRenderer.volumeTrackStart();
        int end = OverlayMenuRenderer.volumeTrackEnd();
        volume = Math.max(0, Math.min(1, (mouseX - start) / (double) (end - start)));
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
