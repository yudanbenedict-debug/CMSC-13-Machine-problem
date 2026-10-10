package com.mygame.engine.helpers.QuestionAlgorithms;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Random;
import java.util.TreeMap;
//TODO: i'll be adding other randomization for the question selection.
/*
    Inverse-Weighted Random Selection with Dynamic Branching algorithm
    READ ts if u wanna know abt the algorithm:

    https://stackoverflow.com/questions/17250568/randomly-choosing-from-a-list-with-weighted-probabilities

    TLDR: basically a randomization algorithm that is based on frequency.
*/
public class FQAlgorithm<K, V> {
    //copy of the original map, will be used in questionselector.
    private final Map<K, List<V>> originalMap;
    //tracks the count per key.
    private final Map<K, Integer> usedCounts;
    private final Random rand = new Random();

    public FQAlgorithm(Map<K, List<V>> map){
        this.originalMap = map;
        this.usedCounts = new HashMap<>();

        for(K key : originalMap.keySet()){
            usedCounts.put(key, 0);
        }

    }
    //returns the selected key needed.
    public K selectNextKey(){
        NavigableMap<Double, K> rouletteWheel = new TreeMap<>();
        //Starting weight for every key.
        double totalWeight = 0.0;

        for(K key : originalMap.keySet()){
            int totalSize = originalMap.get(key).size();
            int used = usedCounts.get(key);
            int remaining = totalSize - used;

            if(remaining <= 0){
                continue;

            }
            //calculate the weight per key frequency (how frequent they appear and the data within a key)

            double usageRatio = (double) used/totalSize;
            double weight = remaining * (1.0 + usageRatio * 2.0); 
            //weight calculation, basic percentage.
            totalWeight += weight;
            rouletteWheel.put(totalWeight, key);
        }

        if(rouletteWheel.isEmpty()){
            return null; // every element within the map has been used
        }
    
        double randVal = rand.nextDouble() * totalWeight;
        K selectedKey = rouletteWheel.higherEntry(randVal).getValue();

        //increment the count per key;
        usedCounts.put(selectedKey, usedCounts.get(selectedKey) + 1);
        return selectedKey;
    }

}
