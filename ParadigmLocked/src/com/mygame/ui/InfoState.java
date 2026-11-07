package com.mygame.ui;

import com.mygame.engine.core.GameState;
import com.mygame.engine.core.GameStateManager;
import com.mygame.engine.core.Sfx;
import com.mygame.engine.core.SoundManager;
import com.mygame.engine.core.StateMethods;
import com.mygame.engine.helpers.constants.GameConstants;
import com.mygame.engine.hud.Hover;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.util.List;
import java.util.function.Consumer;

// Scrollable overlay; BACK returns to the menu or pause menu it was opened from.
public abstract class InfoState implements StateMethods {
    private final GameStateManager stateManager;
    private final String title;
    private final List<InfoBlock> blocks;
    private final Sfx openSound;
    private Consumer<Graphics2D> menuBackdrop;
    private Consumer<Graphics2D> gameBackdrop;
    private GameState returnState = GameState.MENU;
    private double scroll;
    private final Hover backHover = new Hover();

    protected InfoState(GameStateManager stateManager, String title, List<InfoBlock> blocks, Sfx openSound) {
        this.stateManager = stateManager;
        this.title = title;
        this.blocks = blocks;
        this.openSound = openSound;
    }

    public void setBackdrops(Consumer<Graphics2D> menuBackdrop, Consumer<Graphics2D> gameBackdrop) {
        this.menuBackdrop = menuBackdrop;
        this.gameBackdrop = gameBackdrop;
    }

    @Override
    public void draw(Graphics2D g) {
        Consumer<Graphics2D> backdrop = returnState == GameState.PAUSED ? gameBackdrop : menuBackdrop;
        if (backdrop != null) {
            backdrop.accept(g);
            g.setColor(new Color(0, 0, 0, 175));
            g.fillRect(0, 0, GameConstants.LOGICAL_WIDTH, GameConstants.LOGICAL_HEIGHT);
        }
        OverlayMenuRenderer.drawInfoOverlay(g, title, blocks, scroll, backHover.isHovered());
    }

    @Override public void update(double dt) { }

    @Override
    public void onEnter() {
        GameState previous = stateManager.getPreviousState();
        returnState = previous == GameState.PAUSED ? GameState.PAUSED : GameState.MENU;
        scroll = 0;
        backHover.clear();
        if (openSound != null) {
            SoundManager.playSFX(openSound);
        }
    }

    @Override public void onExit() { }

    @Override
    public void mousePressed(MouseEvent event) {
        if (OverlayMenuRenderer.getBackBounds().contains(event.getPoint())) {
            stateManager.setState(returnState);
        }
    }

    @Override public void mouseReleased(MouseEvent event) { }

    @Override
    public void mouseMoved(MouseEvent event) {
        backHover.update(OverlayMenuRenderer.getBackBounds().contains(event.getPoint()));
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent event) {
        scroll = OverlayMenuRenderer.scroll(scroll, event.getPreciseWheelRotation(), blocks);
    }

    @Override
    public void keyPressed(KeyEvent event) {
        switch (event.getKeyCode()) {
            case KeyEvent.VK_ESCAPE:
                stateManager.setState(returnState);
                break;
            case KeyEvent.VK_DOWN:
                scroll = OverlayMenuRenderer.scroll(scroll, 1, blocks);
                break;
            case KeyEvent.VK_UP:
                scroll = OverlayMenuRenderer.scroll(scroll, -1, blocks);
                break;
            default:
                break;
        }
    }

    @Override public void keyReleased(KeyEvent event) { }
}
