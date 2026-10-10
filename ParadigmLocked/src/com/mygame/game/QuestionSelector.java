package com.mygame.game;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import com.mygame.engine.helpers.GameQuestions.Questions;
import com.mygame.engine.helpers.QuestionAlgorithms.FQAlgorithm;


//TODO: Improve the methods (they might be reducable, i just winged ts).
public class QuestionSelector {
    //algorithm for randomization of questions based on the frequency(type/category);
    private QuestionsKind questions = new QuestionsKind();

    //randomize by category
    private FQAlgorithm<String, Questions> keySelectorForCategory = new FQAlgorithm<>(questions.getQuestionCategory());
    private FQAlgorithm<String, Questions> keySelectorForType = new FQAlgorithm<>(questions.getQuestionType());
    private Map<String, Set<Integer>> usedIndexes = new HashMap<>();


    private String getQuestionByCategory(){
        String key = keySelectorForCategory.selectNextKey();
        //randomization
        if(key == null){
            return null;
        }
        List<Questions> list = questions.getQuestionCategory().get(key);
        //used Questions from the map
        Set<Integer> used = usedIndexes.computeIfAbsent(key, k -> new HashSet<>());

        //pick a random unsed index
        //stil usable
        List<Integer> available = new ArrayList<>();

        for(int i = 0; i < list.size(); i++){
            if(!used.contains(i)) {
                available.add(i);
            }
        }
        if(available.isEmpty()) {
            return null;
        }

        int count = available.get(new Random().nextInt(available.size()));
        used.add(count);

        //Must return a random String from the same key
        return list.get(count).question;
    }

    private String getQuestionByType(){
        String key = keySelectorForType.selectNextKey();
        if(key == null){
            return null;
        }
        List<Questions> list = questions.getQuestionType().get(key);
        //use same "used" index 
        Set<Integer> used = usedIndexes.computeIfAbsent(key, k -> new HashSet<>());

        List<Integer> available = new ArrayList<>();
        for(int i = 0; i < list.size(); i++){
            if(!used.contains(i)){
                available.add(i);
            }
        }
        //no more available elements in that key; same as before.
        if(available.isEmpty()){
            return null;
        }
        int idx = available.get(new Random().nextInt(available.size()));
        used.add(idx);
        return list.get(idx).question;
    }

}
