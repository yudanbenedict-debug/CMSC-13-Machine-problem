package com.mygame.game;
import com.mygame.engine.helpers.GameQuestions.Questions;
import com.mygame.engine.helpers.QuestionAlgorithms.FQAlgorithm;

public class QuestionSelector {
    //algorithm for randomization of questions based on the frequency(type/category);
    private QuestionsKind questions = new QuestionsKind();
    //randomize by category
    private FQAlgorithm<String, Questions> keySelector = new FQAlgorithm<>(questions.getQuestionCategory());

    private String getQuestionByCategory(){
        String key = keySelector.selectNextKey();
        //randomization

        return questions.getQuestionCategory().get(key).get(0).question;
    }

}
