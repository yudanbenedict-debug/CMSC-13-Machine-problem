package com.mygame.ui;

import com.mygame.engine.core.AssetCache;
import com.mygame.engine.core.Img;
import com.mygame.engine.helpers.constants.GameConstants;
import com.mygame.engine.helpers.constants.UiLayout;
import com.mygame.engine.hud.Button;
import com.mygame.engine.hud.UiFontManager;
import com.mygame.engine.hud.UiText;
import com.mygame.game.Match;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.image.BufferedImage;

// "Contact Declarative Dynamo" panel: portrait, speech text and the ASK FOR A HINT button.
final class HintPanel {
    private static final Color TEXT = new Color(222, 235, 230);
    private static final Color GOLD = new Color(229, 190, 94);
    private static final Color BAD = new Color(0xD05A5A);
    private static final int PORTRAIT_SIZE = 250;
    private static final int TEXT_X = UiLayout.PANEL_X + 300;
    private static final int TEXT_WIDTH = UiLayout.PANEL_WIDTH - 320;
    private final Button askButton;
    private boolean canAsk;

    HintPanel(Runnable onAsk) {
        askButton = new Button(TEXT_X, UiLayout.PANEL_Y + 150, 280, 64, "ASK FOR A HINT", Img.FANCY_BORDER, onAsk);
    }

    void draw(Graphics2D g, Match match, boolean answerLocked) {
        canAsk = match.canAskHint() && !answerLocked;
        BufferedImage dynamo = AssetCache.get(Img.HINT_DECLARATIVE_DYNAMO);
        if (dynamo != null) {
            g.drawImage(dynamo, UiLayout.PANEL_X + 20, UiLayout.PANEL_Y + 15, PORTRAIT_SIZE, PORTRAIT_SIZE, null);
        }
        UiText.draw(g, "DECLARATIVE DYNAMO", UiFontManager.luckiestGuy(26, Font.PLAIN), TEXT_X,
            UiLayout.PANEL_Y + 50, GOLD, null);
        UiText.Block block = UiText.fit("\"" + speech(match, answerLocked) + "\"",
            UiFontManager.choices(24, Font.PLAIN), 24, 13, TEXT_WIDTH, 150, 1.05f);
        UiText.drawBlock(g, block, TEXT_X, UiLayout.PANEL_Y + 68, TEXT, null);
        if (canAsk) {
            askButton.draw(g);
        }
        UiText.draw(g, "Function calls left: " + match.hintsLeft() + "/" + GameConstants.DYNAMO_HINT_USES,
            UiFontManager.dorris(18, Font.BOLD), TEXT_X, UiLayout.PANEL_Y + 255,
            match.hintsLeft() > 0 ? TEXT : BAD);
    }

    private static String speech(Match match, boolean answerLocked) {
        if (match.hint() != null) {
            return match.hint();
        }
        if (match.round() == null || answerLocked) {
            return "Answer first, programmer. I'll be here for the next one.";
        }
        return match.hintsLeft() > 0 ? "Need a hint? It will cost you one function call."
            : "I'm out of function calls. A Refresher would reset them.";
    }

    void mouseMoved(Point point) {
        if (canAsk) {
            askButton.updateHover(point);
        }
    }

    void mouse(Point point, boolean pressed) {
        if (!canAsk) {
            return;
        }
        if (pressed) {
            askButton.press(point);
        } else {
            askButton.release(point);
        }
    }
}
