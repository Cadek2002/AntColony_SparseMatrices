import java.util.ArrayList;

public class GreedyAlgorithim {
    public static ArrayList<Integer> greedyTSP(ArrayList<ArrayList<Integer>> adjMatrix) {
        //Initialize tracker
        ArrayList<Integer> path = new ArrayList<>(adjMatrix.size());
        boolean candList[] = new boolean[adjMatrix.size()];

        //Start At 0
        int min;
        path.add(0);
        candList[0] = true;

        while(path.size() < adjMatrix.size()) {
            min = -1;
            for(int i = 1; i < adjMatrix.size(); i++) {
                if ((min == -1 || adjMatrix.get(path.size()-1).get(i) < adjMatrix.get(path.size()-1).get(min)) && !candList[i]) min = i;
            }
            path.add(min);
            candList[min] = true;
        }
        path.add(path.get(0));

        return path;
    }

    public static int greedyTSPCost(ArrayList<ArrayList<Integer>> adjMatrix, ArrayList<Integer> path, int cost) {
        boolean candidates[] = new boolean[adjMatrix.size()];
        for (Integer node : path)
            candidates[node] = true;

        int size = path.size();
        int tempCost = cost;
        int min, last = path.get(size-1);

        while(size < adjMatrix.size()) {
            min = -1;
            for(int i = 1; i < adjMatrix.size(); i++) {
                if ((min == -1 || adjMatrix.get(last).get(i) < adjMatrix.get(last).get(min)) && !candidates[i]) min = i;
            }
            tempCost += adjMatrix.get(last).get(min);
            candidates[min] = true;
            last = min;
        }
       return tempCost + adjMatrix.get(last).get(path.get(0));
    }
}

