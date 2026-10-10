package com.mygame.game;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.mygame.engine.helpers.GameQuestions.FileLoader;
import com.mygame.engine.helpers.GameQuestions.Questions;

//TODO: not sure if this should be added to the cache or not, just immediately read when starting game.
public class QuestionsKind{
    private FileLoader fl = new FileLoader();
    private final List<Questions> q = fl.getQues();
    //Store per category, then we'll store by difficulty after. 
    /*
    NOTE: Just these 2 for now as they're the core.
    */
    private Map<String, List<Questions>> ques_cat = new HashMap<>();
    private Map<String, List<Questions>> ques_type = new HashMap<>();
    
    private void init_cat() {
        ques_cat = q.stream() .collect(Collectors.groupingBy(question -> question.category));
    }
    //stream ADT instead of looping through the entire array.
    private void init_ques_type() {
        ques_type = q.stream().collect(Collectors.groupingBy(question -> question.question_type));
    }

    //getters
    public Map<String, List<Questions>> getQuestionCategory(){
        return ques_cat;
    }
    public Map<String, List<Questions>> getQuestionType(){
        return ques_type;
    }



}