import HelperFunctions.AlgoResult;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

class BranchAndBoundFirstPathAlgorithm {
    int N;
    int[] final_path;
    boolean[] visited;
    int final_res;
    ArrayList<ArrayList<Integer>> adj;

    boolean pathFound;

    public AlgoResult apply(ArrayList<ArrayList<Integer>> adj) {
        N = adj.size();
        this.adj = adj;

        final_path = new int[N + 1];
        visited = new boolean[N];
        final_res = Integer.MAX_VALUE;
        pathFound = false;

        TSP();

        ArrayList<Integer> finalArraylist = new ArrayList<>(N+1);
        for (int i : final_path) finalArraylist.add(i);
        return new AlgoResult("BranchAndBoundLimited", final_res, finalArraylist, 0);
    }

    void copyToFinal(int[] curr_path) {
        System.arraycopy(curr_path, 0, final_path, 0, N);
        final_path[N] = curr_path[0];
    }

    void TSPRec(int curr_weight, int level, int[] curr_path) {
        if (pathFound) return;

        if (level == N) {
            if (adj.get(curr_path[level - 1]).get(curr_path[0]) > 0) {
                int curr_res = curr_weight + adj.get(curr_path[level - 1]).get(curr_path[0]);
                copyToFinal(curr_path);
                final_res = curr_res;
                pathFound = true;
            }
            return;
        }

        for (int i = 0; i < N; i++) {
            if (pathFound) return;
            if (adj.get(curr_path[level - 1]).get(i) > 0 && !visited[i]) {
                curr_weight += adj.get(curr_path[level - 1]).get(i);
                curr_path[level] = i;
                visited[i] = true;

                TSPRec(curr_weight, level + 1, curr_path);

                curr_weight -= adj.get(curr_path[level - 1]).get(i);
                visited[i] = false;
            }
        }
    }

    void TSP() {
        int[] curr_path = new int[N + 1];
        Arrays.fill(visited, false);

        visited[0] = true;
        curr_path[0] = 0;

        TSPRec(0, 1, curr_path);
    }

    public static void main(String[] args) {
        ArrayList<ArrayList<Integer>> matrix = new ArrayList<>(5);

        matrix.add(new ArrayList<>(Arrays.asList(0, 0, 0, 5, 7)));
        matrix.add(new ArrayList<>(Arrays.asList(3, 0, 1, 7, 0)));
        matrix.add(new ArrayList<>(Arrays.asList(1, 4, 0, 5, 2)));
        matrix.add(new ArrayList<>(Arrays.asList(0, 2, 0, 0, 1)));
        matrix.add(new ArrayList<>(Arrays.asList(3, 4, 6, 5, 0)));

        BranchAndBoundFirstPathAlgorithm algo = new BranchAndBoundFirstPathAlgorithm();
        AlgoResult result = algo.apply(matrix);

        System.out.printf("Min Cost: %d, MinPath: %s%n", result.cost, result.path.toString());

        // Apply the algorithm again on a different matrix
        ArrayList<ArrayList<Integer>> newMatrix = new ArrayList<>(5);

        newMatrix.add(new ArrayList<>(Arrays.asList(0, 3, 0, 5, 9)));
        newMatrix.add(new ArrayList<>(Arrays.asList(3, 0, 1, 7, 2)));
        newMatrix.add(new ArrayList<>(Arrays.asList(0, 1, 0, 6, 4)));
        newMatrix.add(new ArrayList<>(Arrays.asList(5, 7, 6, 0, 8)));
        newMatrix.add(new ArrayList<>(Arrays.asList(9, 2, 4, 8, 0)));

        result = algo.apply(newMatrix);

        System.out.printf("Min Cost: %d, MinPath: %s%n", result.cost, result.path.toString());
    }
}