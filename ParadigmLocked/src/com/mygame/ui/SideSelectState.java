package com.mygame.ui;

import com.mygame.engine.core.AssetCache;
import com.mygame.engine.core.GameState;
import com.mygame.engine.core.GameStateManager;
import com.mygame.engine.core.Sfx;
import com.mygame.engine.core.SoundManager;
import com.mygame.engine.core.StateMethods;
import com.mygame.engine.helpers.algorithms.EasingMethods;
import com.mygame.engine.helpers.constants.GameConstants;
import com.mygame.engine.hud.UiFontManager;
import com.mygame.engine.hud.UiText;
import com.mygame.game.Faction;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.util.function.Consumer;

// "Choose your side" screen between START GAME and the match: mouse or Left/Right + Enter, Esc goes back.
public final class SideSelectState implements StateMethods {
    private static final Color TEXT = new Color(246, 242, 229);
    private static final Color MUTED = new Color(190, 190, 190);
    private static final Faction[] SIDES = {Faction.DARKWEBKING, Faction.IMMUTABLEMOTHER};
    private static final String[] TAGLINES = {
        "Destroy the Immutable Mother's nodes and the King promises you power.",
        "Bring down the Dark Web King and the Mother grants you a wish."
    };
    // source crops of the loading art (1920x1080) centered on each boss's face
    private static final Rectangle[] CROPS = {new Rectangle(96, 40, 960, 720), new Rectangle(800, 40, 960, 720)};
    private static final int CARD_WIDTH = 420;
    private static final int CARD_HEIGHT = 315;
    private static final int CARD_Y = 200;
    private static final int CARD_GAP = 60;

    private final GameStateManager stateManager;
    private final Consumer<Graphics2D> backdrop;
    private final double[] lift = new double[SIDES.length];
    private int focused;
    private double elapsed;

    public SideSelectState(GameStateManager stateManager, Consumer<Graphics2D> backdrop) {
        this.stateManager = stateManager;
        this.backdrop = backdrop;
    }

    private static Rectangle cardBounds(int index) {
        int totalWidth = CARD_WIDTH * 2 + CARD_GAP;
        int x = (GameConstants.LOGICAL_WIDTH - totalWidth) / 2 + index * (CARD_WIDTH + CARD_GAP);
        return new Rectangle(x, CARD_Y, CARD_WIDTH, CARD_HEIGHT);
    }

    @Override
    public void onEnter() {
        focused = Faction.player() == Faction.DARKWEBKING ? 0 : 1;
        elapsed = 0;
    }

    @Override public void onExit() { }

    @Override
    public void update(double dt) {
        elapsed += dt;
        for (int index = 0; index < lift.length; index++) {
            lift[index] = EasingMethods.approach(lift[index], index == focused ? 1 : 0, 10, dt);
        }
    }

    @Override
    public void draw(Graphics2D g) {
        if (backdrop != null) {
            backdrop.accept(g);
        }
        float fade = (float) EasingMethods.smoothStep(elapsed / 0.4);
        g.setColor(new Color(0, 0, 0, (int) (150 * fade)));
        g.fillRect(0, 0, GameConstants.LOGICAL_WIDTH, GameConstants.LOGICAL_HEIGHT);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        float centerX = GameConstants.LOGICAL_WIDTH / 2f;
        UiText.drawCentered(g, "CHOOSE YOUR SIDE", UiFontManager.luckiestGuy(52, Font.PLAIN), centerX, 130,
            new Color(229, 190, 94), new Color(229, 190, 94, 110));
        UiText.drawCentered(g, "The boss you fight for decides your colors. The other one is your enemy.",
            UiFontManager.dorris(18, Font.BOLD), centerX, 165, MUTED, null);

        for (int index = 0; index < SIDES.length; index++) {
            drawCard(g, index);
        }
        UiText.drawCentered(g, "Left / Right, then Enter, or click a side.   Esc: back",
            UiFontManager.dorris(16, Font.BOLD), centerX, 680, MUTED, null);
    }

    private void drawCard(Graphics2D g, int index) {
        Faction side = SIDES[index];
        Rectangle bounds = cardBounds(index);
        int raise = (int) Math.round(10 * lift[index]);
        int y = bounds.y - raise;
        Rectangle crop = CROPS[index];
        BufferedImage art = AssetCache.getScaled(side.loadingBackground, crop.x, crop.y, crop.width, crop.height,
            bounds.width, bounds.height);
        RoundRectangle2D.Double shape = new RoundRectangle2D.Double(bounds.x, y, bounds.width, bounds.height, 18, 18);
        if (art != null) {
            Graphics2D clipped = (Graphics2D) g.create();
            clipped.clip(shape);
            clipped.drawImage(art, bounds.x, y, null);
            clipped.setColor(new Color(0, 0, 0, (int) (110 * (1 - lift[index]))));
            clipped.fill(shape);
            clipped.dispose();
        }
        Color color = side.color;
        for (int glow = 3; glow >= 1; glow--) {
            g.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), (int) (45 * lift[index] / glow)));
            g.setStroke(new BasicStroke(4f + glow * 5f));
            g.draw(shape);
        }
        g.setColor(index == focused ? color : new Color(color.getRed(), color.getGreen(), color.getBlue(), 140));
        g.setStroke(new BasicStroke(index == focused ? 3.5f : 2f));
        g.draw(shape);
        g.setStroke(new BasicStroke(1f));

        float cardCenter = bounds.x + bounds.width / 2f;
        UiText.drawCentered(g, side.displayName.toUpperCase(), UiFontManager.luckiestGuy(32, Font.PLAIN),
            cardCenter, y + bounds.height + 44, color, index == focused ? new Color(color.getRed(),
                color.getGreen(), color.getBlue(), 120) : null);
        UiText.Block tagline = UiText.wrap(TAGLINES[index], UiFontManager.notoSerif(17, Font.PLAIN),
            bounds.width, 1.1f, 2);
        float baseline = y + bounds.height + 72;
        for (java.awt.font.TextLayout line : tagline.lines) {
            UiText.drawLayout(g, line, cardCenter - line.getAdvance() / 2f, baseline, TEXT, null);
            baseline += tagline.lineHeight;
        }
    }

    private void focus(int index) {
        if (index != focused) {
            SoundManager.playMenuHover();
        }
        focused = index;
    }

    private void choose(int index) {
        Faction.setPlayer(SIDES[index]);
        SoundManager.playSFX(Sfx.UI_MENU_ONTITLESTART);
        stateManager.setState(GameState.PLAYING);
    }

    @Override
    public void mousePressed(MouseEvent event) {
        for (int index = 0; index < SIDES.length; index++) {
            if (cardBounds(index).contains(event.getPoint())) {
                choose(index);
                return;
            }
        }
    }

    @Override public void mouseReleased(MouseEvent event) { }

    @Override
    public void mouseMoved(MouseEvent event) {
        for (int index = 0; index < SIDES.length; index++) {
            if (cardBounds(index).contains(event.getPoint())) {
                focus(index);
            }
        }
    }

    @Override
    public void keyPressed(KeyEvent event) {
        switch (event.getKeyCode()) {
            case KeyEvent.VK_LEFT:
            case KeyEvent.VK_A:
                focus(0);
                break;
            case KeyEvent.VK_RIGHT:
            case KeyEvent.VK_D:
                focus(1);
                break;
            case KeyEvent.VK_ENTER:
            case KeyEvent.VK_SPACE:
                choose(focused);
                break;
            case KeyEvent.VK_ESCAPE:
                stateManager.setState(GameState.MENU);
                break;
            default:
                break;
        }
    }

    @Override public void keyReleased(KeyEvent event) { }
}
