package com.mygame.game;

import com.mygame.engine.helpers.constants.GameConstants;
import com.mygame.engine.helpers.gamequestions.Question;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

// One playthrough's rules and state (questions, nodes, score, hints, consumables); no drawing or input here.
public final class Match {
    public enum Outcome { PLAYING, WON, LOST }

    // What happened when a question was resolved, so the screen can show it and play sounds.
    public static final class Turn {
        public final String summary;
        public final Consumable drop;
        public final int selfDamageTicks;
        public final boolean lowHpWarning;

        Turn(String summary, Consumable drop, int selfDamageTicks, boolean lowHpWarning) {
            this.summary = summary;
            this.drop = drop;
            this.selfDamageTicks = selfDamageTicks;
            this.lowHpWarning = lowHpWarning;
        }
    }

    // Result of trying to use a consumable.
    public static final class Use {
        public final boolean spent;
        public final String message;
        public final int nullifiedChoice;

        Use(boolean spent, String message, int nullifiedChoice) {
            this.spent = spent;
            this.message = message;
            this.nullifiedChoice = nullifiedChoice;
        }
    }

    private final QuestionSelector selector = new QuestionSelector();
    private final ParadigmSpace space = new ParadigmSpace(selector.totalQuestions());
    private final ConsumableInventory inventory = new ConsumableInventory();
    private final Random random = new Random();
    private final boolean[] nullified = new boolean[4];
    private QuestionRound round;
    private Outcome outcome = Outcome.PLAYING;
    private int score;
    private int hintsLeft = GameConstants.DYNAMO_HINT_USES;
    private String hint;
    private boolean doubleDamage;
    private boolean lowHpWarned;

    // Loads the next question; when none are left the match ends (more HP left in % wins).
    public boolean nextQuestion() {
        if (outcome != Outcome.PLAYING) {
            return false;
        }
        Arrays.fill(nullified, false);
        hint = null;
        doubleDamage = false;
        Question question = selector.next(preferredCategory());
        if (question == null) {
            round = null;
            boolean allyAhead = space.ally().totalHp() / (double) space.ally().maxTotalHp()
                > space.enemy().totalHp() / (double) space.enemy().maxTotalHp();
            outcome = allyAhead ? Outcome.WON : Outcome.LOST;
            return false;
        }
        round = new QuestionRound(question);
        return true;
    }

    // early nodes ask theory, the Core asks programming, the middle mixes both
    private String preferredCategory() {
        switch (space.currentTier()) {
            case EARLY:
                return "Theoretical";
            case CORE:
                return "Programming";
            default:
                return null;
        }
    }

    // Applies damage, score, drops and Scripter's self-damage for the current question.
    public Turn resolve(boolean correct, double timeLeft) {
        StringBuilder summary = new StringBuilder();
        Consumable drop = null;
        int selfDamage = 0;
        if (correct) {
            // 1 damage = 1 question cleared (Scripter's extra damage clears one more)
            int damage = doubleDamage && selector.clearExtraQuestion() ? 2 : 1;
            boolean destroyed = space.enemy().damage(damage);
            int gained = GameConstants.SCORE_CORRECT
                + (int) Math.ceil(timeLeft) * GameConstants.SCORE_TIME_BONUS_PER_SECOND;
            score += gained;
            summary.append(gained).append(" points, ").append(damage).append(" damage.");
            if (!space.enemy().defeated()) {
                if (destroyed) {
                    summary.append(" Node destroyed!");
                }
                if (destroyed || inventory.shouldDrop()) {
                    drop = inventory.rollDrop();
                    inventory.add(drop);
                }
            }
        } else {
            selector.defer(round.question()); // missed questions come back later
            space.ally().damage(1);
            selfDamage++;
            score = Math.max(0, score - GameConstants.SCORE_WRONG_PENALTY);
            summary.append("Lost ").append(GameConstants.SCORE_WRONG_PENALTY).append(" points.");
        }
        if (inventory.scriptersPunishing() && !space.ally().defeated()) {
            space.ally().damage(1);
            selfDamage++;
            summary.append(" Scripter's Sacrifice hurts you!");
        }
        doubleDamage = false;
        boolean lowHp = !lowHpWarned && space.ally().totalHp() == 1;
        lowHpWarned |= lowHp;
        if (space.enemy().defeated()) {
            outcome = Outcome.WON;
        } else if (space.ally().defeated()) {
            outcome = Outcome.LOST;
        }
        return new Turn(summary.toString(), drop, selfDamage, lowHp);
    }

    public boolean canAskHint() {
        return round != null && hint == null && hintsLeft > 0 && outcome == Outcome.PLAYING;
    }

    // Hint: the question's topic plus a 50/50 between the right answer and one wrong one.
    public String askHint() {
        if (!canAskHint()) {
            return hint;
        }
        hintsLeft--;
        Question question = round.question();
        String topic = "Theoretical".equalsIgnoreCase(question.category)
            ? "This one comes from " + question.moduleOrLanguage + "."
            : "Read that " + question.moduleOrLanguage + " carefully, line by line.";
        List<Integer> wrong = wrongChoicesLeft();
        if (wrong.isEmpty()) {
            hint = topic;
        } else {
            int correct = round.correctIndex();
            int other = wrong.get(random.nextInt(wrong.size()));
            hint = topic + " Between you and me... it's either " + letter(Math.min(correct, other))
                + " or " + letter(Math.max(correct, other)) + ".";
        }
        return hint;
    }

    public Use useConsumable(Consumable consumable) {
        switch (consumable) {
            case NULLSLINGER: {
                List<Integer> targets = wrongChoicesLeft();
                if (targets.size() <= 1) {
                    return new Use(false, "Nothing left to nullify.", -1);
                }
                if (!inventory.consume(consumable)) {
                    return new Use(false, "", -1);
                }
                int target = targets.get(random.nextInt(targets.size()));
                nullified[target] = true;
                return new Use(true, "NullSlinger nullified choice " + letter(target) + ".", target);
            }
            case SCRIPTERS_SACRIFICE: {
                if (doubleDamage) {
                    return new Use(false, "Already active for this turn.", -1);
                }
                if (!inventory.consume(consumable)) {
                    return new Use(false, "", -1);
                }
                doubleDamage = true;
                return new Use(true, "Scripter's Sacrifice: this turn deals double damage!", -1);
            }
            default: {
                if (!inventory.consume(consumable)) {
                    return new Use(false, "", -1);
                }
                hintsLeft = GameConstants.DYNAMO_HINT_USES;
                return new Use(true, "Refresher: Dynamo's function calls reset to " + hintsLeft + ".", -1);
            }
        }
    }

    private List<Integer> wrongChoicesLeft() {
        List<Integer> wrong = new ArrayList<>();
        for (int index = 0; index < nullified.length; index++) {
            if (index != round.correctIndex() && !nullified[index]) {
                wrong.add(index);
            }
        }
        return wrong;
    }

    public static char letter(int index) {
        return "ABCD".charAt(index);
    }

    // destroyed enemy nodes minus destroyed ally nodes (positive = you're ahead)
    public int nodeLead() {
        return space.enemy().foremostIndex() - space.ally().foremostIndex();
    }

    public QuestionRound round() {
        return round;
    }

    public ParadigmSpace space() {
        return space;
    }

    public ConsumableInventory inventory() {
        return inventory;
    }

    public Outcome outcome() {
        return outcome;
    }

    public int score() {
        return score;
    }

    public int hintsLeft() {
        return hintsLeft;
    }

    public String hint() {
        return hint;
    }

    public boolean doubleDamage() {
        return doubleDamage;
    }

    public int questionsLeft() {
        return selector.remainingQuestions();
    }

    public int totalQuestions() {
        return selector.totalQuestions();
    }
}
