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
    private final QuestionType questionType = new QuestionType();

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
