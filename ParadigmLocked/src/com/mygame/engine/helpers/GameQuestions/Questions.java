package com.mygame.engine.helpers.GameQuestions;
//TODO: protected only so other class inside the same package can access it immediately.
public class Questions {
    public String id;
    public String category;
    public String module_or_language;
    public String question_type;
    public String label;
    
    public String question;
   
    public String A_answer;
    public String B_answer;
    public String C_answer;
    public String D_answer;
  

    public String correct_answer;
    public Questions(String id, String cat, String m_or_l, String q_t, String l, String q, String a, String b, String c, String d, String cor){
        this.id = id;
        this.category = cat;
        this.label = l;
        this.module_or_language = m_or_l;
        this.question_type = q_t;
        this.question = q;
        this.A_answer = a;
        this.B_answer = b;
        this.C_answer = c;
        this.D_answer = d;
        this.correct_answer = cor;
    }
}
