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
    public FileLoader(){
        init_programming_ques();
        init_theoretical_ques();
       
    }
    /*
        //NOTE: DON'T MIND THAT THERE ARE 2 METHODS, I'M TESTING SMTHNG.
    */
   //TODO: COMPLETE THE INSERTION TO THE MAP
    private void init_theoretical_ques(){

        Path ph = Paths.get("theoretical_questions.csv");
        //implement the shit later.
        try(BufferedReader r = Files.newBufferedReader(ph, StandardCharsets.UTF_8);
            CSVParser pr = CSVFormat.DEFAULT.builder().setHeader().setSkipHeaderRecord(true).get().parse(r)){
            //using CSV
                for(CSVRecord record : pr){
                    String id = record.get("id");
                    String category = record.get("category");
                    String module = record.get("module_or_language");
                    String ques_type = record.get("question_type");
                    String label = record.get("label");
                    String ques = record.get("question");
                    String op_A = record.get("option_A");
                    String op_B = record.get("option_B"); 
                    String op_C = record.get("option_C");
                    String op_D = record.get("option_D");
                    String ans = record.get("answer");
                    Questions q = new Questions(id, category, module, ques_type, label, ques, op_A, op_B, op_C, op_D, ans);
                    QUES.add(q);
                }
        }catch(IOException e){
            //show system err
            System.err.println("Unable to read file path");
        }
    }
    private void init_programming_ques(){
        Path ph = Paths.get("programming_questions.csv");
        try(BufferedReader br = Files.newBufferedReader(ph, StandardCharsets.UTF_8); 
            CSVParser cw = CSVFormat.DEFAULT.builder().setHeader().setSkipHeaderRecord(true).get().parse(br)){

                for(CSVRecord record : cw){
                    String id = record.get("id");
                    String category = record.get("category");
                    String module = record.get("module_or_language");
                    String ques_type = record.get("question_type");
                    String label = record.get("label");
                    String ques = record.get("question");

                    String op_A = record.get("option_A");
                    String op_B = record.get("option_B"); 
                    String op_C = record.get("option_C");
                    String op_D = record.get("option_D");
                    String ans = record.get("answer");
                    Questions q = new Questions(id, category, module, ques_type, label, ques, op_A, op_B, op_C, op_D, ans);
                    QUES.add(q);
                }
            }catch(IOException e){
                e.getLocalizedMessage();
            }
    }
    public List<Questions> getQues(){
        return QUES;
    }
}
