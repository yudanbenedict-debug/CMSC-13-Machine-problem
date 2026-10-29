package com.mygame.game;
import com.mygame.engine.helpers.gamequestions.FileLoader;
import com.mygame.engine.helpers.gamequestions.Question;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

//TODO: not sure if this should be added to the cache or not, just immediately read when starting game.
public class QuestionBank {
    private final List<Question> questions;
    private final Map<String, List<Question>> questionsByCategory;
    private final Map<String, List<Question>> questionsByType;

    public QuestionBank() {
        FileLoader loader = new FileLoader();
        loadMcqFile(loader, "assets/MCQ/programming_questions.csv");
        loadMcqFile(loader, "assets/MCQ/theoretical_questions.csv");
        loadMcqFile(loader, "ParadigmLocked/assets/MCQ/programming_questions.csv");
        loadMcqFile(loader, "ParadigmLocked/assets/MCQ/theoretical_questions.csv");
        questions = loader.getQuestions();
        questionsByCategory = questions.stream()
                .collect(Collectors.groupingBy(question -> question.category));
        questionsByType = questions.stream()
                .collect(Collectors.groupingBy(question -> question.questionType));
    }

    private void loadMcqFile(FileLoader loader, String filePath) {
        Path path = Paths.get(filePath);
        if (Files.exists(path)) {
            loader.initializeQuestions(filePath);
        }
    }

    public Map<String, List<Question>> getQuestionsByCategory() {
        return questionsByCategory;
    }

    public Map<String, List<Question>> getQuestionsByType() {
        return questionsByType;
    }
}