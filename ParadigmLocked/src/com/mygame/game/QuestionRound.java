package com.mygame.game;

import java.util.Random;

import com.mygame.engine.helpers.gamequestions.Question;

public class QuestionRound {

    private Question currQuestion;
    private String correctText;
    private String[] answers = new String[4];
    public QuestionRound(Question currQuestion){
        this.currQuestion = currQuestion;
        resolveCorrectAnswer();
        shuffleQuestions();
        getQuestionsMaterials();
    }
    private void resolveCorrectAnswer(){
        if(currQuestion.correctAnswer.equals("A")){
            correctText = currQuestion.optionA;
        }else if(currQuestion.correctAnswer.equals("B")){
            correctText = currQuestion.optionB;
        }
        else if(currQuestion.correctAnswer.equals("C")){
            correctText = currQuestion.optionC;
        }
        else if(currQuestion.correctAnswer.equals("D")){
            correctText = currQuestion.optionD;
        }
    }
    private void shuffleQuestions(){
        Random rand = new Random();
        answers[0] = currQuestion.optionA;
        answers[1] = currQuestion.optionB;
        answers[2] = currQuestion.optionC;
        answers[3] = currQuestion.optionD;

        for(int i = 3; i > 0; i--){
            int randomIndex = rand.nextInt(i + 1);
            String temp = answers[i];
            answers[i] = answers[randomIndex];
            answers[randomIndex] = temp;
        }
    }
    public boolean isCorrect(String ans){
        return correctText.equals(ans);
    }
    public String[] getShuffledQuestions(){
        return answers;
    }
    //debugger
    private void getQuestionsMaterials(){
        if (!Boolean.getBoolean("paradigm.debug")) {
            return; // run with -Dparadigm.debug=true to print the questions again
        }
        System.err.println(currQuestion.id);
        System.err.println(currQuestion.question);
        for(int i = 0; i < 4; i++){
            System.err.println(answers[i]);
        }
    }
}
