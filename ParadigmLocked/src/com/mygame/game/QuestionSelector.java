package com.mygame.game;

import com.mygame.engine.helpers.algorithms.WeightedKeySelector;
import com.mygame.engine.helpers.constants.GameConstants;
import com.mygame.engine.helpers.gamequestions.Question;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

// Hands out unused questions; missed ones are deferred and return after QUESTION_RETRY_DELAY questions.
public class QuestionSelector {
    private final Map<String, List<Question>> remaining = new HashMap<>();
    private final WeightedKeySelector<String, Question> categoryPicker;
    private final List<Retry> retries = new ArrayList<>();
    private final Random random = new Random();
    private final int total;

    private static final class Retry {
        final Question question;
        int wait = GameConstants.QUESTION_RETRY_DELAY;

        Retry(Question question) {
            this.question = question;
        }
    }

    public QuestionSelector() {
        Map<String, List<Question>> byCategory = new QuestionBank().getQuestionsByCategory();
        int count = 0;
        for (Map.Entry<String, List<Question>> entry : byCategory.entrySet()) {
            remaining.put(entry.getKey(), new ArrayList<>(entry.getValue()));
            count += entry.getValue().size();
        }
        total = count;
        categoryPicker = new WeightedKeySelector<>(byCategory);
    }

    // Prefers the given category (null = weighted pick); null when every question is cleared.
    public Question next(String preferredCategory) {
        for (Iterator<Retry> it = retries.iterator(); it.hasNext();) {
            Retry retry = it.next();
            if (--retry.wait <= 0) {
                release(retry.question);
                it.remove();
            }
        }
        Question question = pick(preferredCategory);
        if (question == null && !retries.isEmpty()) {
            retries.forEach(retry -> release(retry.question)); // only missed questions left
            retries.clear();
            question = pick(preferredCategory);
        }
        return question;
    }

    public void defer(Question question) {
        retries.add(new Retry(question));
    }

    // Scripter's Sacrifice: clears one extra question; false if none left.
    public boolean clearExtraQuestion() {
        if (pick(null) != null) {
            return true;
        }
        return !retries.isEmpty() && retries.remove(retries.size() - 1) != null;
    }

    public int totalQuestions() {
        return total;
    }

    // Questions not answered correctly yet, including ones waiting to come back.
    public int remainingQuestions() {
        int count = retries.size();
        for (List<Question> list : remaining.values()) {
            count += list.size();
        }
        return count;
    }

    private Question pick(String preferredCategory) {
        String category = hasAny(preferredCategory) ? preferredCategory : null;
        if (category != null) {
            categoryPicker.markUsed(category);
        } else {
            category = categoryPicker.selectNextKey();
        }
        if (!hasAny(category)) {
            category = remaining.keySet().stream().filter(this::hasAny).findFirst().orElse(null);
            if (category == null) {
                return null;
            }
            categoryPicker.markUsed(category);
        }
        List<Question> list = remaining.get(category);
        return list.remove(random.nextInt(list.size()));
    }

    private void release(Question question) {
        remaining.get(question.category).add(question);
        categoryPicker.unmarkUsed(question.category);
    }

    private boolean hasAny(String category) {
        return category != null && remaining.containsKey(category) && !remaining.get(category).isEmpty();
    }
}
