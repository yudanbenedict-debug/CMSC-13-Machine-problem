package com.mygame.game;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import com.mygame.engine.helpers.GameQuestions.FileLoader;
import com.mygame.engine.helpers.GameQuestions.Questions;

//TODO: not sure if this should be added to the cache or not, just immediately read when starting game.
public class QuestionsKind{
    private FileLoader fl = new FileLoader();
    //Store per category, then we'll store by difficulty after
    private Map<String, List<Questions>> ques_kind = new HashMap<>();
    

    private void init_cat(){
        
    }




}