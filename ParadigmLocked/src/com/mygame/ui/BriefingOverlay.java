package com.mygame.ui;

import com.mygame.engine.helpers.algorithms.EasingMethods;
import com.mygame.engine.helpers.constants.GameConstants;
import com.mygame.engine.hud.UiFontManager;
import com.mygame.engine.hud.UiText;
import com.mygame.game.Faction;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.font.TextLayout;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

// "Get ready" screen shown before the first question: team, node path and two random tips.
final class BriefingOverlay {
    private static final Color TEXT = new Color(222, 235, 230);
    private static final Color GOLD = new Color(229, 190, 94);
    private static final Color MUTED = new Color(190, 190, 190);
    private final Random random = new Random();
    private double timeLeft;
    private double shown;
    private List<String> tips = List.of();

    void start() {
        timeLeft = GameConstants.BRIEFING_SECONDS;
        shown = 0;
        List<String> all = new ArrayList<>(Arrays.asList(HowToPlayState.TIPS));
        Collections.shuffle(all, random);
        tips = all.subList(0, Math.min(2, all.size()));
    }

    boolean isActive() {
        return timeLeft > 0;
    }

    void update(double dt) {
        shown += dt;
        timeLeft -= dt;
    }

    // Skips after the minimum time, keeping the fade-out.
    void skip() {
        if (shown >= GameConstants.BRIEFING_MIN_SECONDS) {
            timeLeft = Math.min(timeLeft, GameConstants.BRIEFING_FADE_SECONDS);
        }
    }

    void draw(Graphics2D g) {
        double fadeIn = EasingMethods.smoothStep(shown / GameConstants.BRIEFING_FADE_SECONDS);
        double fadeOut = EasingMethods.easeIn(Math.min(1, timeLeft / GameConstants.BRIEFING_FADE_SECONDS));
        float alpha = (float) Math.max(0, Math.min(1, fadeIn * fadeOut));
        g.setColor(new Color(0, 0, 0, (int) (215 * alpha)));
        g.fillRect(0, 0, GameConstants.LOGICAL_WIDTH, GameConstants.LOGICAL_HEIGHT);
        Composite old = g.getComposite();
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));

        float centerX = GameConstants.LOGICAL_WIDTH / 2f;
        UiText.drawCentered(g, "GET READY, PROGRAMMER", UiFontManager.luckiestGuy(46, Font.PLAIN),
            centerX, 175, GOLD, new Color(229, 190, 94, 110));
        drawTeamLine(g, centerX, 222);
        UiText.drawCentered(g, "Break through: Procedural Gate > Object Model > Functional Registers"
            + " > Logic Shrines > Core", UiFontManager.dorris(17, Font.BOLD), centerX, 256, MUTED, null);

        int tipY = 315;
        for (String tip : tips) {
            UiText.drawCentered(g, "TIP", UiFontManager.luckiestGuy(20, Font.PLAIN), centerX, tipY, GOLD, null);
            UiText.Block block = UiText.wrap(tip, UiFontManager.notoSerif(21, Font.PLAIN), 760, 1.15f, 3);
            float baseline = tipY + 12 + block.ascent;
            for (TextLayout line : block.lines) {
                UiText.drawLayout(g, line, centerX - line.getAdvance() / 2f, baseline, TEXT, null);
                baseline += block.lineHeight;
            }
            tipY = (int) baseline + 34;
        }

        UiText.drawCentered(g, "Starting in " + (int) Math.ceil(Math.max(0, timeLeft)),
            UiFontManager.luckiestGuy(28, Font.PLAIN), centerX, 600, TEXT, null);
        if (shown >= GameConstants.BRIEFING_MIN_SECONDS) {
            int blink = (int) (150 + 105 * (0.5 + 0.5 * Math.sin(shown * 3)));
            UiText.drawCentered(g, "Click or press Enter to begin", UiFontManager.dorris(18, Font.BOLD),
                centerX, 636, new Color(246, 242, 229, blink), null);
        }
        g.setComposite(old);
    }

    private static void drawTeamLine(Graphics2D g, float centerX, float baseline) {
        Faction team = Faction.player();
        Font font = UiFontManager.dorris(24, Font.BOLD);
        String before = "You fight for the ";
        float x = centerX - (UiText.width(before, font) + UiText.width(team.displayName, font)) / 2f;
        UiText.draw(g, before, font, x, baseline, TEXT);
        UiText.draw(g, team.displayName, font, x + UiText.width(before, font), baseline, team.color,
            new Color(team.color.getRed(), team.color.getGreen(), team.color.getBlue(), 120));
    }
}
