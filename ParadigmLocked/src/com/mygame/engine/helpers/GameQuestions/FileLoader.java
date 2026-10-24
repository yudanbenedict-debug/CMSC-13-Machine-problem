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

public class FileLoader {

    // CSVs live in ParadigmLocked/assets/MCQ/
    private static final String[] CSV_FILES = {
        "assets/MCQ/programming_questions.csv",
        "assets/MCQ/theoretical_questions.csv"
    };

    // Folders to look in, so it works no matter which folder the game is launched from.
    private static final String[] SEARCH_DIRS = {
        ".",
        "ParadigmLocked",
        "CMSC-13-Machine-problem/ParadigmLocked",
        "..",
        "../ParadigmLocked"
    };

    public List<Question> QUES = new ArrayList<>();

    public FileLoader() {
        // FIX 1: the constructor must actually load the files.
        for (String file : CSV_FILES) {
            init_ques(file);
        }
        System.out.println("[FileLoader] Loaded " + QUES.size() + " questions total.");
    }

    public void init_ques(String filePath) {
        CSVFormat format = CSVFormat.DEFAULT.builder()
                .setHeader()
                .setSkipHeaderRecord(true)
                .get();

        try (Reader read = openReader(filePath);
             CSVParser parser = format.parse(read)) {
            int count = 0;
            for (CSVRecord r : parser) {
                QUES.add(new Question(
                        r.get("id"),
                        r.get("category"),            // FIX 2: was "catgeory" (typo)
                        r.get("module_or_language"),
                        r.get("question_type"),
                        r.get("label"),
                        r.get("question"),
                        r.get("option_A"),
                        r.get("option_B"),
                        r.get("option_C"),
                        r.get("option_D"),
                        r.get("answer")));
                count++;
            }
            System.out.println("[FileLoader] " + filePath + ": " + count + " questions");
        } catch (IOException | IllegalArgumentException e) {
            System.err.println("[FileLoader] FAILED to load " + filePath + ": " + e);
            e.printStackTrace();
        }
    }

    private Reader openReader(String path) throws IOException {
        InputStream in = FileLoader.class.getResourceAsStream("/" + path);
        if (in != null) {
            return new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
        }
        for (String dir : SEARCH_DIRS) {
            Path p = Paths.get(dir, path);
            if (Files.exists(p)) {
                return Files.newBufferedReader(p, StandardCharsets.UTF_8);
            }
        }
        throw new IOException("Could not find " + path + " on the classpath or relative to "
                + Paths.get("").toAbsolutePath());
    }

    public List<Question> getQuestions() {
        return List.copyOf(QUES);
    }
}