package com.mygame.engine.helpers;

public class Questions {
    public String Question;
    public String A_answer;
    public String B_answer;
    public String C_answer;
    public String D_answer;
    public String Correct_answer;
    public Questions(String q, String a, String b, String c, String d, String cor){
        this.Question = q;
        this.A_answer = a;
        this.B_answer = b;
        this.C_answer = c;
        this.D_answer = d;
        this.Correct_answer = cor;
    }
}
