package com.mygame.engine.helpers.gamequestions;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

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

/*
    Changes:
    fixed the import and addded the missing libraries, also added a vscode settings.json to read where the libraries are
 */
public class FileLoader {
        //TODO: AFTER COMPILING EVERYTHING, REMNOVE SETTINGS.JSON AND TRANSFER THE JAR DEPENDECY TO ITS PROPER PLACE.
        //assume we create a csv/txt file for each questions.
        private static final List<Question> LOADED_QUESTIONS = new ArrayList<>();
        public final List<Question> questions = LOADED_QUESTIONS;
        private static final String[] SEARCH_DIRECTORIES = {
            ".",
            "CMSC-13-Machine-problem",
            "..",
            "../.."
        };
    
        //probably so it's easier to group them by "difficulty"; not finalized
    public FileLoader() {
        synchronized (LOADED_QUESTIONS) {
            if (LOADED_QUESTIONS.isEmpty()) {
                initializeQuestions("assets/MCQ/programming_questions.csv");
                initializeQuestions("assets/MCQ/theoretical_questions.csv");
            }
        }
    }

    public final void initializeQuestions(String filePath){
        
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
        InputStream inputStream = FileLoader.class.getResourceAsStream("/" + path);
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
