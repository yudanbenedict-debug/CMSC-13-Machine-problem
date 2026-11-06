package com.mygame.game;

import com.mygame.engine.helpers.gamequestions.FileLoader;
import com.mygame.engine.helpers.gamequestions.Question;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

// All loaded questions grouped by category ("Theoretical" / "Programming").
public class QuestionBank {
    private final Map<String, List<Question>> questionsByCategory;

    public QuestionBank() {
        questionsByCategory = new FileLoader().getQuestions().stream()
                .collect(Collectors.groupingBy(question -> question.category));
    }

    public Map<String, List<Question>> getQuestionsByCategory() {
        return questionsByCategory;
    }
}
