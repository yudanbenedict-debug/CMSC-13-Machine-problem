package com.mygame.game;
import com.mygame.engine.helpers.algorithms.WeightedKeySelector;
import com.mygame.engine.helpers.gamequestions.Question;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

/* yo i reduced it, first of all i added a get next question, get nextquestionbytype
    they return the quesiton objects so didi ginstreamline an question generation
    I just used ur algorithm for it tho chat me kun basahon mo na ini
    cus maybe mas worth it igmove an FQAlgo ha gamequestions folder nala
    unless mahimo kapan algo which prolly diri
*/ 

public class QuestionSelector {
    // Select a question by weighted category or question type. (removed old randomize by category)
    private final QuestionBank questionType = new QuestionBank();

    private final WeightedKeySelector<String, Question> keySelectorForCategory = new WeightedKeySelector<>(questionType.getQuestionsByCategory());
    private final WeightedKeySelector<String, Question> keySelectorForType = new WeightedKeySelector<>(questionType.getQuestionsByType());
    private final Map<String, Set<Integer>> usedCategoryIndexes = new HashMap<>();
    private final Map<String, Set<Integer>> usedTypeIndexes = new HashMap<>();
    private final Random random = new Random();

    /*
        Return the next unused question selected by weighted category
        Return NULL if all categories have been used
    */
    public Question getNextQuestion(){
        return getQuestionByCategory();
    }

    // Prefers the given category, then falls back to the weighted pick.
    public Question getNextQuestion(String preferredCategory) {
        tickRetries();
        Question question = pickQuestion(preferredCategory);
        if (question == null && !retries.isEmpty()) {
            // only missed questions left: release them all
            for (Retry retry : new ArrayList<>(retries)) {
                release(retry);
            }
            retries.clear();
            question = pickQuestion(preferredCategory);
        }
        return question;
    }

    // Missed questions are deferred and return to the pool after RETRY_DELAY questions.
    private static final int RETRY_DELAY = 5;

    private static final class Retry {
        final String key;
        final int index;
        int wait;

        Retry(String key, int index, int wait) {
            this.key = key;
            this.index = index;
            this.wait = wait;
        }
    }

    private final List<Retry> retries = new ArrayList<>();

    public void defer(Question question) {
        for (Map.Entry<String, List<Question>> entry : questionType.getQuestionsByCategory().entrySet()) {
            int index = entry.getValue().indexOf(question);
            if (index >= 0) {
                retries.add(new Retry(entry.getKey(), index, RETRY_DELAY));
                return;
            }
        }
    }

    private void tickRetries() {
        for (Retry retry : new ArrayList<>(retries)) {
            if (--retry.wait <= 0) {
                release(retry);
                retries.remove(retry);
            }
        }
    }

    private void release(Retry retry) {
        Set<Integer> used = usedCategoryIndexes.get(retry.key);
        if (used != null && used.remove(retry.index)) {
            keySelectorForCategory.unmarkUsed(retry.key);
        }
    }

    public int totalQuestions() {
        int total = 0;
        for (List<Question> list : questionType.getQuestionsByCategory().values()) {
            total += list.size();
        }
        return total;
    }

    // Includes questions waiting to come back.
    public int remainingQuestions() {
        int used = 0;
        for (Set<Integer> set : usedCategoryIndexes.values()) {
            used += set.size();
        }
        return totalQuestions() - used + retries.size();
    }

    // Scripter's Sacrifice: clears one extra question; false if none left.
    public boolean clearExtraQuestion() {
        if (pickQuestion(null) != null) {
            return true;
        }
        if (!retries.isEmpty()) {
            retries.remove(retries.size() - 1);  // stays used = cleared
            return true;
        }
        return false;
    }

    private Question pickQuestion(String preferredCategory) {
        if (preferredCategory != null) {
            Question preferred = pickFromCategory(preferredCategory);
            if (preferred != null) {
                return preferred;
            }
        }
        Question question = getQuestionByCategory();
        if (question != null) {
            return question;
        }
        // fallback: any category with questions left
        for (String key : questionType.getQuestionsByCategory().keySet()) {
            Question fallback = pickFromCategory(key);
            if (fallback != null) {
                return fallback;
            }
        }
        return null;
    }

    private Question pickFromCategory(String category) {
        List<Question> list = questionType.getQuestionsByCategory().get(category);
        if (list == null) {
            return null;
        }
        Set<Integer> used = usedCategoryIndexes.computeIfAbsent(category, ignored -> new HashSet<>());
        List<Integer> available = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            if (!used.contains(i)) {
                available.add(i);
            }
        }
        if (available.isEmpty()) {
            return null;
        }
        int index = available.get(random.nextInt(available.size()));
        used.add(index);
        keySelectorForCategory.markUsed(category);
        return list.get(index);
    }

    /*
        Returns the next unused question selected by weighted question type
        Return NULL after every question type has been used
    */
    public Question getNextQuestionByType() {
        return getQuestionByType();
    }

    private Question getQuestionByCategory() {
        String key = keySelectorForCategory.selectNextKey();
        if(key == null){
            return null;
        }
        List<Question> list = questionType.getQuestionsByCategory().get(key);
        Set<Integer> used = usedCategoryIndexes.computeIfAbsent(key, ignored -> new HashSet<>());

        List<Integer> available = new ArrayList<>();

        for(int i = 0; i < list.size(); i++){
            if(!used.contains(i)) {
                available.add(i);
            }
        }
        if(available.isEmpty()) {
            return null;
        }

        int count = available.get(random.nextInt(available.size())); //dont need a NEW random lol
        used.add(count);

        return list.get(count);
    }

    private Question getQuestionByType() {
        String key = keySelectorForType.selectNextKey();
        if(key == null){
            return null;
        }
        List<Question> list = questionType.getQuestionsByType().get(key);
        Set<Integer> used = usedTypeIndexes.computeIfAbsent(key, ignored -> new HashSet<>());

        List<Integer> available = new ArrayList<>();
        for(int i = 0; i < list.size(); i++){
            if(!used.contains(i)){
                available.add(i);
            }
        }
        if(available.isEmpty()){
            return null;
        }
        int idx = available.get(random.nextInt(available.size()));
        used.add(idx);
        return list.get(idx);
    }

}
