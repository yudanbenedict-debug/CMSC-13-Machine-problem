package com.mygame.engine.helpers.GameQuestions;

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
        //TODO: AFTER COMPILING EVERYTHING, REMNOVE SETTINGS.JSON AND TRANSFER THE JAR DEPENDECY TO ITS PROPER PLACE.
        //assume we create a csv/txt file for each questions.
        public List<Questions> QUES = new ArrayList<>();
        private static final String[] SEARCH_DIRS = {
            ".",
            "CMSC-13-Machine-problem",
            "..",
            "../.."
        };
    
        //probably so it's easier to group them by "difficulty"; not finalized
    public void init_ques(String filePath){
        
        //gets the entire String, top to bottom, skipping the header of a CSV
        CSVFormat format = CSVFormat.DEFAULT.builder().setHeader().setSkipHeaderRecord(true).get();

        try(Reader read = openReader(filePath); CSVParser parser = format.parse(read)){
            int count = 0;
            for(CSVRecord r: parser){
                QUES.add(new Questions(r.get("id"), r.get("catgeory"), r.get("module_or_language"), r.get("question_type"), r.get("label"), r.get("question"), r.get("option_A"), r.get("option_B"), r.get("option_C"), r.get("option_D"), r.get("answer")));
                count++;
            }
        }
        catch(IOException | IllegalArgumentException e){
            System.err.println("[FileLoader] FAILED to load " + filePath + ": " + e);
            e.printStackTrace();
        }
        }
        
    
    private Reader openReader(String path) throws IOException{
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
    public List<Questions> getQues(){
        return List.copyOf(QUES);
    }
}
