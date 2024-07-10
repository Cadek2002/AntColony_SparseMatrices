// Java program to solve Traveling Salesman Problem
// using Branch and Bound.

//Implemented and Modified from GFG https://www.geeksforgeeks.org/traveling-salesman-problem-using-branch-and-bound-2/
import HelperFunctions.AlgoResult;

import java.util.*;

class BranchAndBoundAlgorithm
{

    int N;
    // final_path[] stores the final solution ie, the
    // path of the salesman.
    ArrayList<Integer> final_path = new ArrayList<>(N);
    // visited[] keeps track of the already visited nodes
    // in a particular path
    boolean visited[];

    // Stores the final minimum weight of shortest tour.
     int final_res;

    ArrayList<ArrayList<Integer>> adj;

    // Function to copy temporary solution to
    // the final solution

    public AlgoResult apply(ArrayList<ArrayList<Integer>> adj) {
        N = adj.size();
        this.adj = adj;

        final_path = new ArrayList<>(Collections.nCopies(N+1, 0));
        visited = new boolean[N];
        final_res = Integer.MAX_VALUE;

        TSP();
        return new AlgoResult("BranchAndBound", final_res, final_path, 0);
    }


    void copyToFinal(ArrayList<Integer> curr_path)
    {
        for (int i = 0; i < N; i++)
            final_path.set(i, curr_path.get(i));
        final_path.set(N, curr_path.get(0));
    }

    // Function to find the minimum edge cost
    // having an end at the vertex i
    int firstMin(int i)
    {
        int min = Integer.MAX_VALUE;
        for (int k = 0; k < N; k++)
            if (adj.get(i).get(k) > 0 && adj.get(i).get(k) < min  && i != k)
                min = adj.get(i).get(k);
        return min;
    }

    // function to find the second minimum edge cost
    // having an end at the vertex i
    int secondMin(int i)
    {
        int first = Integer.MAX_VALUE, second = Integer.MAX_VALUE;
        for (int j=0; j<N; j++)
        {
            if (i == j)
                continue;

            if (adj.get(i).get(j) > 0 && adj.get(i).get(j) <= first)
            {
                second = first;
                first = adj.get(i).get(j);
            }
            else if (adj.get(i).get(j) <= second &&
                    adj.get(i).get(j) != first)
                second = adj.get(i).get(j);
        }
        return second;
    }

    // function that takes as arguments:
    // curr_bound -> lower bound of the root node
    // curr_weight-> stores the weight of the path so far
    // level-> current level while moving in the search
    //         space tree
    // curr_path[] -> where the solution is being stored which
    //             would later be copied to final_path[]
    void TSPRec(int curr_bound, int curr_weight,
                       int level, ArrayList<Integer> curr_path)
    {
        // base case is when we have reached level N which
        // means we have covered all the nodes once
        if (level == N)
        {
            // check if there is an edge from last vertex in
            // path back to the first vertex
            if (adj.get(curr_path.get(level - 1)).get(curr_path.get(0)) > 0)
            {
                // curr_res has the total weight of the
                // solution we got
                int curr_res = curr_weight +
                        adj.get(curr_path.get(level - 1)).get(curr_path.get(0));

                // Update final result and final path if
                // current result is better.
                if (curr_res < final_res)
                {
                    copyToFinal(curr_path);
                    final_res = curr_res;
                }
            }
            return;
        }

        // for any other level iterate for all vertices to
        // build the search space tree recursively
        for (int i = 0; i < N; i++)
        {
            // Consider next vertex if it is not same (diagonal
            // entry in adjacency matrix and not visited
            // already)
            if (adj.get(curr_path.get(level - 1)).get(i) > 0 &&
                    !visited[i])
            {
                int temp = curr_bound;
                curr_weight += adj.get(curr_path.get(level - 1)).get(i);

                // different computation of curr_bound for
                // level 2 from the other levels
                if (level==1)
                    curr_bound -= ((firstMin(curr_path.get(level - 1)) +
                            firstMin(i))/2);
                else
                    curr_bound -= ((secondMin(curr_path.get(level - 1)) +
                            firstMin(i))/2);

                // curr_bound + curr_weight is the actual lower bound
                // for the node that we have arrived at
                // If current lower bound < final_res, we need to explore
                // the node further
                if (curr_bound + curr_weight < final_res)
                {
                    curr_path.set(level, i);
                    //System.out.println(curr_path.toString());
                    visited[i] = true;

                    // call TSPRec for the next level
                    TSPRec(curr_bound, curr_weight, level + 1,
                            curr_path);
                }

                // Else we have to prune the node by resetting
                // all changes to curr_weight and curr_bound
                curr_weight -= adj.get(curr_path.get(level - 1)).get(i);
                curr_bound = temp;

                // Also reset the visited array
                Arrays.fill(visited,false);
                for (int j = 0; j <= level - 1; j++)
                    visited[curr_path.get(j)] = true;
            }
        }
    }

    // This function sets up final_path[] 
    void TSP()
    {
        ArrayList<Integer> curr_path = new ArrayList<>(Collections.nCopies(N+1, -2));

        // Calculate initial lower bound for the root node
        // using the formula 1/2 * (sum of first min +
        // second min) for all edges.
        // Also initialize the curr_path and visited array
        int curr_bound = 0;
        Arrays.fill(visited, false);

        // Compute initial bound
        for (int i = 0; i < N; i++)
            curr_bound += (firstMin(i) +
                    secondMin(i));

        // Rounding off the lower bound to an integer
        curr_bound = (curr_bound==1)? curr_bound/2 + 1 :
                curr_bound/2;

        // We start at vertex 1 so the first vertex
        // in curr_path[] is 0
        visited[0] = true;
        curr_path.set(0, 0);

        // Call to TSPRec for curr_weight equal to
        // 0 and level 1
        TSPRec(curr_bound, 0, 1, curr_path);
    }

    public static void main(String[] args) {

        ArrayList<ArrayList<Integer>> matrix = new ArrayList<>(5);

        matrix.add(new ArrayList<Integer>(Arrays.asList(0, 0, 0, 5, 7)));
        matrix.add(new ArrayList<Integer>(Arrays.asList(3, 0, 1, 7, 0)));
        matrix.add(new ArrayList<Integer>(Arrays.asList(1, 4, 0, 5, 2)));
        matrix.add(new ArrayList<Integer>(Arrays.asList(0, 2, 0, 0, 1)));
        matrix.add(new ArrayList<Integer>(Arrays.asList(3, 4, 6, 5, 0)));

        BranchAndBoundAlgorithm algo = new BranchAndBoundAlgorithm();

        algo.apply(matrix);

        System.out.printf("Min Cost: %d, MinPath: %s", algo.final_res, algo.final_path);


    }

}