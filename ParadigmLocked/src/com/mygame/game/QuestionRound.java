package com.mygame.game;

import com.mygame.engine.helpers.gamequestions.Question;
import java.util.Random;

// One asked question with its four answers shuffled.
public class QuestionRound {
    private static final Random RANDOM = new Random();
    private final Question question;
    private final String[] answers;
    private final int correctIndex;

    public QuestionRound(Question question) {
        this.question = question;
        answers = new String[] {question.optionA, question.optionB, question.optionC, question.optionD};
        String correctText = answers["ABCD".indexOf(question.correctAnswer.trim().toUpperCase())];
        // Fisher-Yates shuffle
        for (int i = answers.length - 1; i > 0; i--) {
            int j = RANDOM.nextInt(i + 1);
            String temp = answers[i];
            answers[i] = answers[j];
            answers[j] = temp;
        }
        int found = 0;
        for (int index = 0; index < answers.length; index++) {
            if (answers[index].equals(correctText)) {
                found = index;
            }
        }
        correctIndex = found;
    }

    public Question question() {
        return question;
    }

    public String[] answers() {
        return answers;
    }

    public int correctIndex() {
        return correctIndex;
    }

    public boolean isCorrect(int index) {
        return index == correctIndex;
    }
}
