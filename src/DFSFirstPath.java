import HelperFunctions.AlgoResult;
import HelperFunctions.HelperFunctions;

import java.util.ArrayList;
import java.util.Arrays;

class DFSFirstPathAlgorithm

{
    int N;
    int[] final_path;
    boolean[] visited;
    ArrayList<ArrayList<Integer>> adj;
    boolean pathFound;

    public AlgoResult apply(ArrayList<ArrayList<Integer>> adj) {
        N = adj.size();
        this.adj = adj;

        final_path = new int[N + 1];
        visited = new boolean[N];
        pathFound = false;

        findHamiltonianPath();
        ArrayList<Integer> finalArraylist = new ArrayList<>(N+1);
        for (int i : final_path) finalArraylist.add(i);
        return new AlgoResult("HamiltonianPathDFS", pathFound ? HelperFunctions.calculateCost(finalArraylist, adj, false) : -1, finalArraylist, 0);
    }

    void copyToFinal(int[] curr_path) {
        System.arraycopy(curr_path, 0, final_path, 0, N);
        final_path[N] = curr_path[0];
    }

    void dfs(int v, int level, int[] path) {
        if (pathFound) return;

        path[level] = v;
        visited[v] = true;

        if (level == N - 1) {
            if (adj.get(path[level]).get(path[0]) > 0) {
                copyToFinal(path);
                pathFound = true;
            }
            visited[v] = false;
            return;
        }

        for (int i = 0; i < N; i++) {
            if (!visited[i] && adj.get(v).get(i) > 0) {
                dfs(i, level + 1, path);
            }
        }

        visited[v] = false;
    }

    void findHamiltonianPath() {
        int[] path = new int[N + 1];
        Arrays.fill(visited, false);
        dfs(0, 0, path);
    }

    public static void main(String[] args) {
        ArrayList<ArrayList<Integer>> matrix = new ArrayList<>(5);

        matrix.add(new ArrayList<>(Arrays.asList(0, 0, 0, 5, 7)));
        matrix.add(new ArrayList<>(Arrays.asList(3, 0, 1, 7, 0)));
        matrix.add(new ArrayList<>(Arrays.asList(1, 4, 0, 5, 2)));
        matrix.add(new ArrayList<>(Arrays.asList(0, 2, 0, 0, 1)));
        matrix.add(new ArrayList<>(Arrays.asList(3, 4, 6, 5, 0)));

        DFSFirstPathAlgorithm algo = new DFSFirstPathAlgorithm();
        AlgoResult result = algo.apply(matrix);

        if (result.cost != -1) {
            System.out.printf("Cost: %d Path Found: %s%n", result.cost, result.path.toString());
        } else {
            System.out.println("No Path Found");
        }

        // Apply the algorithm again on a different matrix
        ArrayList<ArrayList<Integer>> newMatrix = new ArrayList<>(5);

        newMatrix.add(new ArrayList<>(Arrays.asList(0, 3, 0, 5, 9)));
        newMatrix.add(new ArrayList<>(Arrays.asList(3, 0, 1, 7, 2)));
        newMatrix.add(new ArrayList<>(Arrays.asList(0, 1, 0, 6, 4)));
        newMatrix.add(new ArrayList<>(Arrays.asList(5, 7, 6, 0, 8)));
        newMatrix.add(new ArrayList<>(Arrays.asList(9, 2, 4, 8, 0)));

        result = algo.apply(newMatrix);

        if (result.cost != -1) {
            System.out.printf("Cost: %d, Path Found: %s%n", result.cost, result.path.toString());
        } else {
            System.out.println("No Path Found");
        }
    }
}