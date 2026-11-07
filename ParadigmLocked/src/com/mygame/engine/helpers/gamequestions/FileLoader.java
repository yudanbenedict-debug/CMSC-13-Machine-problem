package com.mygame.engine.helpers.gamequestions;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

public class FileLoader {
        private static final List<Question> LOADED_QUESTIONS = new ArrayList<>();
        private final List<Question> questions = LOADED_QUESTIONS;
        private static final String[] SEARCH_DIRECTORIES = {
            ".",
            "CMSC-13-Machine-problem",
            "..",
            "../.."
        };
    
    public FileLoader() {
        synchronized (LOADED_QUESTIONS) {
            if (LOADED_QUESTIONS.isEmpty()) {
                initializeQuestions("assets/MCQ/programming_questions.csv");
                initializeQuestions("assets/MCQ/theoretical_questions.csv");
            }
        }
    }

    private void initializeQuestions(String filePath){
        
        //gets the entire String, top to bottom, skipping the header of a CSV
        CSVFormat format = CSVFormat.DEFAULT.builder().setHeader().setSkipHeaderRecord(true).get();

        try (Reader reader = openReader(filePath); CSVParser parser = format.parse(reader)) {
            for (CSVRecord record : parser) {
                questions.add(new Question(record.get("id"), record.get("category"),
                    record.get("module_or_language"), record.get("question_type"),
                    record.get("label"), record.get("question"), record.get("option_A"),
                    record.get("option_B"), record.get("option_C"), record.get("option_D"),
                    record.get("answer")));
            }
        } catch (IOException | IllegalArgumentException exception) {
            System.err.println("[FileLoader] FAILED to load " + filePath + ": " + exception);
        }
    }
        
    
    private Reader openReader(String path) throws IOException{
        InputStream inputStream = com.mygame.engine.core.Resources.open(path);
        if (inputStream != null) {
            return new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
        }
        for (String directory : SEARCH_DIRECTORIES) {
            Path candidatePath = Paths.get(directory, path);
            if (Files.exists(candidatePath)) {
                return Files.newBufferedReader(candidatePath, StandardCharsets.UTF_8);
            }
        }
        throw new IOException("Could not find " + path + " on the classpath or relative to "
                + Paths.get("").toAbsolutePath());
    }

    public List<Question> getQuestions() {
        return List.copyOf(questions);
    }
}
