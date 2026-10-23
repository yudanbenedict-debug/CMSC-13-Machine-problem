package com.mygame.engine.helpers.GameQuestions;

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


public class FileLoader {
    //TODO: AFTER COMPILING EVERYTHING, REMNOVE SETTINGS.JSON AND TRANSFER THE JAR DEPENDECY TO ITS PROPER PLACE.
    //assume we create a csv/txt file for each questions.
    public List<Questions> QUES = new ArrayList<>();
   
    //probably so it's easier to group them by "difficulty"; not finalized
   public void init_ques(String filePath){
    int count = 0;
    
    CSVFormat format = CSVFormat.DEFAULT.builder().setHeader().setSkipHeaderRecord(true).get();

   }
    public List<Questions> getQues(){
        return QUES;
    }
}
