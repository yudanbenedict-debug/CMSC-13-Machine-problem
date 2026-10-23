package com.mygame.engine.helpers.gamequestions;
//TODO: protected only so other class inside the same package can access it immediately.
public class Question {
    public String id;
    public String category;
    public String moduleOrLanguage;
    public String questionType;
    public String label;
    
    public String question;
   
    public String optionA;
    public String optionB;
    public String optionC;
    public String optionD;
  

    public String correctAnswer;
    public Question(String id, String category, String moduleOrLanguage, String questionType,
                    String label, String question, String optionA, String optionB,
                    String optionC, String optionD, String correctAnswer) {
        this.id = id;
        this.category = category;
        this.label = label;
        this.moduleOrLanguage = moduleOrLanguage;
        this.questionType = questionType;
        this.question = question;
        this.optionA = optionA;
        this.optionB = optionB;
        this.optionC = optionC;
        this.optionD = optionD;
        this.correctAnswer = correctAnswer;
    }
}
