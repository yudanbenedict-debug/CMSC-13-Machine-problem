package com.mygame.game;
import com.mygame.engine.helpers.gamequestions.FileLoader;
import com.mygame.engine.helpers.gamequestions.Question;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

//TODO: not sure if this should be added to the cache or not, just immediately read when starting game.
public class QuestionType {
    private final List<Question> questions;
    private final Map<String, List<Question>> questionsByCategory;
    private final Map<String, List<Question>> questionsByType;

    public QuestionType() {
        questions = new FileLoader().getQuestions();
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