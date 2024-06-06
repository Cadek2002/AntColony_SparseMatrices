import HelperFunctions.HelperFunctions;

import java.util.ArrayList;
import java.util.Collections;

public class BruteForceAlgorithm {

    int bestCost;
    ArrayList<Integer> bestPath;
    ArrayList<Integer> currentPath;
    ArrayList<ArrayList<Integer>> adjacencyMatrix;

    public BruteForceAlgorithm(ArrayList<ArrayList<Integer>> adjacencyMatrix) {
        this.bestPath = new ArrayList<>(adjacencyMatrix.size());
        currentPath = new ArrayList<>(adjacencyMatrix.size());
        bestCost = 0;
        this.adjacencyMatrix = adjacencyMatrix;

        for (int i = 0; i < adjacencyMatrix.size(); i++) currentPath.add(i);
    }

    public static ArrayList<Integer> bruteForceTSP(ArrayList<ArrayList<Integer>> adjacencyMatrix) {
        BruteForceAlgorithm bruteForce = new BruteForceAlgorithm(adjacencyMatrix);

        bruteForce.bruteForceTSP(0, adjacencyMatrix.size()-1);

        return bruteForce.bestPath;
    }

    private void bruteForceTSP(int l, int r)
    {
        if (l == r) {
            int cost = HelperFunctions.calculateCost(currentPath, adjacencyMatrix, false);
            if (bestCost >= cost || bestCost == 0) {
                bestCost = cost;
                bestPath = (ArrayList<Integer>) currentPath.clone();
            }
        }

        else {
            for (int i = l; i <= r; i++) {
                Collections.swap(currentPath, l, i);
                bruteForceTSP(l + 1, r);
                Collections.swap(currentPath, l, i);
            }
        }
    }
}
