package com.mygame.engine.helpers.gamequestions;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/*
    Changes:
    fixed the import and addded the missing libraries, also added a vscode settings.json to read where the libraries are
    Gincentralize ko an naming conventions, stick to it stop using single letters holy code smell
    pretty much the same changes han imo part, kind of just cleaned it up, an imo duha nga test method waray ko la ginlabot
    gonna start working on my shi
    "The loader keeps the pair's CSV record mapping,
    but centralizes file loading and path resolution so both question sets use the same code."
 */
public class FileLoader {
    private final List<Question> questions = new ArrayList<>();

    public FileLoader() {
        loadQuestions("programming_questions.csv");
        loadQuestions("theoretical_questions.csv");
    }

    private void loadQuestions(String fileName) {
        Path filePath = resolveFile(fileName);
        try (BufferedReader reader = Files.newBufferedReader(filePath, StandardCharsets.UTF_8);
             CSVParser parser = CSVFormat.DEFAULT.builder().setHeader().setSkipHeaderRecord(true).get().parse(reader)){
            for (CSVRecord record : parser) {
                questions.add(new Question(
                        record.get("id"),
                        record.get("category"),
                        record.get("module_or_language"),
                        record.get("question_type"),
                        record.get("label"),
                        record.get("question"),
                        record.get("option_A"),
                        record.get("option_B"),
                        record.get("option_C"),
                        record.get("option_D"),
                        record.get("answer")));
            }
        } catch (IOException | IllegalArgumentException exception){
            throw new IllegalStateException("Unable to load question file: " + filePath, exception);
        }
    }

    private Path resolveFile(String fileName) {
        List<Path> candidates = List.of(
                Paths.get("assets", "MCQ", fileName),
                Paths.get("ParadigmLocked", "assets", "MCQ", fileName),
                Paths.get("..", "assets", "MCQ", fileName),
                Paths.get("..", "ParadigmLocked", "assets", "MCQ", fileName));

        return candidates.stream().filter(Files::isRegularFile).findFirst().orElseThrow(() -> new IllegalStateException("Question file not found: " + fileName));
    }

    public List<Question> getQuestions() {
        return List.copyOf(questions);
    }
}
