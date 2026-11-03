# ParadigmLocked: Change Log

This file lists what changed in each revision round, which files were touched, and where to tweak the values.
Unless noted otherwise, every tunable number is in `engine/helpers/constants/GameConstants.java`.

---

## Revision 2 (Nov 2, 2026): requested changes

| # | Request | What was done | Main files |
|---|---------|---------------|------------|
| 1 | Title image looked stretched | The logo PNG is 1000x300, but it was drawn at 620x124. `TEMPORARY_MENU_LOGO_HEIGHT` is now computed from the width (620x186), so the 10:3 ratio holds on the title screen and the main menu. | `GameConstants`, `TitleState`, `MainMenuState` |
| 2 | Quieter menu music after "press any key" | Pressing a key on the title screen sets the menu theme to `MENU_MUSIC_DUCKED_LEVEL` (0.18, was 0.35), so the menu button sounds stand out. This uses the new `SoundManager.setLevel(name, level)`. | `TitleState`, `SoundManager` |
| 3 | Remove the title in Credits | Removed the ParadigmLocked logo image from the Credits body. How to Play and Credits now draw the menu background **without** the logo behind the overlay, so the logo no longer pokes out above the frame. | `CreditsState`, `MainMenuState.drawBackdrop`, `ParadigmLocked` |
| 4 | Start with no option selected | Each question now starts with nothing open on the option wheel, and the prompt says **"Select a choice."** Clicking an open option again closes it. When a question resolves (answer, timeout or abandon), the Answer view opens automatically so the correct answer is visible. | `GameplayPreviewState` |
| 5 | Team colors and overlays from the loading screen | The loading backgrounds were renamed to `LOADING_SCREEN_BACKGROUND_DARKWEBKING.png` (was VARIANT_1, orange) and `LOADING_SCREEN_BACKGROUND_IMMUTABLEMOTHER.png` (was VARIANT_2, blue). The variant that shows up picks your team for the session (new `Faction` enum). **Dark Web King art means you are the King's team**: you are orange `#F08A3C` and the enemy (Immutable Mother) is blue `#8FA3FF`. The other art gives the reverse. The colors apply to the HUD triangles, HP bars and minimap nodes. The win/lose screen uses `OVERLAY_<YOUR TEAM>_WIN/LOSE`. | `game/Faction.java` (new), `LoadingState`, `AssetLoader`, `GameHud`, `EndScreenState` |
| 6 | Consumable panel: smaller contents, sharp border | `CONSUMABLE_BORDER` is square (390x390), and it used to be stretched to the wide panel, which made it blurry and squashed. Now each half (left ornament, right ornament) is drawn at the **same** x/y scale and pinned to the panel edges. It is only downscaled, so it stays sharp, and it uses bicubic filtering. Rows sit inside the ornaments. Icon size went from 56 to 40, the name from 28 to 21 and the amount from 24 to 20. The panel is now 460x270 with 72px rows. | `ConsumablePanel`, `GameConstants` (`CONSUMABLE_*`) |
| 7 | SFX -20%, hint needs confirmation, hint sound -40% | All sound effects play at `SFX_VOLUME_LEVEL` = 0.8. `HINT_PROMPT` plays at `HINT_PROMPT_VOLUME_LEVEL` = 0.6. Opening "Contact Declarative Dynamo" **no longer spends a hint**: the panel shows an **ASK FOR A HINT** button, and only clicking it uses a function call. | `SoundManager`, `GameplayPreviewState` (`canAskHint`, `requestHint`, `hintButton`) |
| 8 | Quieter radial hover | `RADIAL_CHOICES_HOVER_01` plays at `RADIAL_HOVER_VOLUME_LEVEL` = 0.4. It still stacks when you sweep across the options. | `SoundManager`, `GameConstants` |
| 9 | Fancy font for consumables and the abandon prompt | Consumable names, DISCARD, "Do you really wish to Abandon this question?" and "Abandoning counts as a wrong answer." now use **Dorris**, the radial-menu font. "Abandon" is still `#fa5555`. | `ConsumablePanel`, `TemporaryUiRenderer.drawAbandonPrompt` |
| 10 | Right-click deselects | Right-clicking during a question clears the selected answer and any selected consumable. It never confirms anything. The confirm prompt now mentions it. | `GameplayPreviewState` (`deselectChoice`) |

### Volume levels at a glance (multiplied by the pause-menu master volume)

| Sound | Level |
|-------|-------|
| Sound effects (default) | 0.8 |
| `HINT_PROMPT` | 0.6 |
| `RADIAL_CHOICES_HOVER_01` | 0.4 |
| `MAIN_GAME_THEME` | 0.35 |
| `THEME_MAIN_MENU` (title screen) | 0.35 |
| `THEME_MAIN_MENU` (after "press any key") | 0.18 |

---

## Revision 1 (Oct 30, 2026): first instruction set (summary)

- **Abandon prompt**: reads "Do you really wish to **Abandon** this question?", with Abandon in `#fa5555`.
- **Fonts**: the question uses `QUESTION_FONT` and the choices use `CHOICES_FONT`, both auto-fitted so text never bleeds out of its box. These fonts are Arabic (Kobani), so digits and `, ; % * "` fall back to a plain sans font to keep code correct (`UiText`).
- **Glow**: all glowing text (hover and selected) goes through `UiText.draw(..., glowColor)` (`engine/hud/UiText.java`).
- **Long code choices**: they are cut off with "..." in their box and shown in full in a preview panel on hover or select.
- **Top bar**: removed the blue top bar and the MENU button. A pause button sits in the top-left, and Esc or P also pauses.
- **HUD**: score and per-question timer (60s) at the top center, foremost-node status for both sides at the top left, and a Paradigm Space minimap at the top right (`GameHud`).
- **Paradigm Space**: 8 nodes per side, 24 HP in total: Procedural Gate, Object Model, Functional Register x2, Logic Shrine x2, then Core (Syntax, Semantics). A correct answer damages the enemy and a wrong answer, timeout or abandon damages you. Early nodes ask theory questions and the Core asks programming questions (`game/ParadigmSpace.java`).
- **Consumables**: NullSlinger, Scripter's Sacrifice and Refresher, with weighted drops (60/28/12). Drop chance is 35% per correct answer and guaranteed when a node is destroyed (`game/Consumable*.java`, `ui/ConsumablePanel.java`).
- **Declarative Dynamo**: 3 hints per game. A hint gives the topic and narrows the answer to two choices.
- **Pause menu**: larger title, tighter options, more padding, and a volume fill that matches the bar's border. The frozen game is drawn behind it.
- **How to Play and Credits**: scrollable rich content with headings, colors and pictures. They return to the menu or the pause menu depending on where they were opened. Mouse wheel scrolling was fixed.
- **Win/lose**: a timed overlay with the final score, which then returns to the menu (`EndScreenState`).
- **Audio**: MP3s converted to WAV, since Java can't play MP3. Added hover sounds, stacking radial clicks, music per screen, low-HP alert, damage tick, consumable spawn and use, hint, win and lose sounds.
- **Title intro**: "PRESS ANY KEY TO START" plays `UI_MENU_ONTITLESTART` and leads into the menu (`TitleState`).
- **Bug fix**: every question was being loaded 2-3 times, so questions could repeat (`QuestionBank`).
