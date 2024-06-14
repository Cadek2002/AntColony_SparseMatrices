package HelperFunctions;

import java.util.ArrayList;

public class AlgoResult {
    public int cost;
    public ArrayList<Integer> path;
    public long Seconds;
    public long NanoSeconds;
    public int batchID;
    public String alias;
    public int iteration;
    public int successIteration;


    public AlgoResult(int cost, ArrayList<Integer> path, int successIteration) {
        this.cost = cost;
        this.path = path;
        this.successIteration = successIteration;
    }

    public AlgoResult(int batchID, int cost, ArrayList<Integer> path, long seconds, long nanoSeconds) {
        this.cost = cost;
        this.path = path;
        Seconds = seconds;
        NanoSeconds = nanoSeconds;
        this.batchID = batchID;
        alias = Integer.toString(batchID);
        successIteration = -1;
        this.iteration = -1;
    }

    public AlgoResult(String alias, int batchID, int cost, ArrayList<Integer> path, long seconds, long nanoSeconds) {
        this.cost = cost;
        this.path = path;
        Seconds = seconds;
        NanoSeconds = nanoSeconds;
        this.batchID = batchID;
        this.alias = alias;
        successIteration = -1;
        this.iteration = -1;
    }

    public AlgoResult(String alias,int iteration, int batchID, int cost, ArrayList<Integer> path, long seconds, long nanoSeconds) {
        this.cost = cost;
        this.path = path;
        Seconds = seconds;
        NanoSeconds = nanoSeconds;
        this.batchID = batchID;
        this.alias = alias;
        this.iteration = 0;
        this.iteration = iteration;
    }
}
