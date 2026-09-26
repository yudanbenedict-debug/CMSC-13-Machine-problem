package com.mygame.engine.core;

public class ThreadManager {
    private final ThreadGroup gameThreadGroup;

    public ThreadManager(){
        this.gameThreadGroup = new ThreadGroup("MachineProblem");
    }

    public Thread startThread(String threadName, Runnable task){
        Thread t = new Thread(gameThreadGroup, task, threadName);
        t.start();
        return t;
    }

    public void stopAll(){
        gameThreadGroup.interrupt();
    }
}