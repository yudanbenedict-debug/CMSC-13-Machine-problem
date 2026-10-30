package com.mygame.game;
import com.mygame.engine.helpers.gamequestions.FileLoader;
import com.mygame.engine.helpers.gamequestions.Question;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

//TODO: not sure if this should be added to the cache or not, just immediately read when starting game.
public class QuestionBank {
    private final List<Question> questions;
    private final Map<String, List<Question>> questionsByCategory;
    private final Map<String, List<Question>> questionsByType;

    public QuestionBank() {
        // FileLoader already loads both MCQ files once (shared static list). Loading them again
        // here used to add every question 2-3 times, so the same question could repeat.
        FileLoader loader = new FileLoader();
        questions = loader.getQuestions();
        questionsByCategory = questions.stream()
                .collect(Collectors.groupingBy(question -> question.category));
        questionsByType = questions.stream()
                .collect(Collectors.groupingBy(question -> question.questionType));
    }

    public Map<String, List<Question>> getQuestionsByCategory() {
        return questionsByCategory;
    }

    public Map<String, List<Question>> getQuestionsByType() {
        return questionsByType;
    }
}