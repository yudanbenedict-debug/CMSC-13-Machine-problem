package com.mygame.engine.helpers;


import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class FileLoader {
    //assume we create a csv/txt file for each questions.
    private Map<Difficulty, List<Questions>> ques = new EnumMap<>(Difficulty.class);
    //probably so it's easier to group them by "difficulty"; not finalized
    public FileLoader(){

    }
    public void init() throws Exception{
        BufferedReader read = new BufferedReader(new FileReader("from where the csv file will come from"));
        //read line immediately as this shit is the top of the shit idfk.
        read.readLine();
        String lns;
        while((lns = read.readLine()) != null){
            String[] lst = lns.split(",");
            String quest = lst[0];
            String a_ans = lst[1];
            String b_ans = lst[2];
            String c_ans = lst[3];
            String d_ans = lst[4];
            String cor_ans = lst[5];
            Questions q = new Questions(quest, a_ans, b_ans, c_ans, d_ans, cor_ans);
            int nm = Integer.parseInt(lst[6]);
            //hard coded, will change later to Difficulty itself.
            if(nm == 1){
                ques.computeIfAbsent(Difficulty.EASY, k -> new ArrayList<>()).add(q);
            }else if(nm == 2){
                ques.computeIfAbsent(Difficulty.MEDIUM, k -> new ArrayList<>()).add(q);
            }
            else if(nm == 3){
                ques.computeIfAbsent(Difficulty.HARD, k -> new ArrayList<>()).add(q);
            }
        }
        read.close();
    }
}
