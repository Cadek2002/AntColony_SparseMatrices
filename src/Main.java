import HelperFunctions.HelperFunctions;
import HelperFunctions.AlgoResult;
import java.util.ArrayList;
import java.util.function.Function;


public class Main {
//    public static void main(String[] args) {
//        ArrayList<ArrayList<Integer>> matrix =  HelperFunctions.readAdjacencyList("src/inputMatricies/VLSIDataset/bcl380.tsp", false);
//
//        HelperFunctions.printMatrix(matrix);
//    }


    public static ArrayList<Integer> TSPBruteForce(ArrayList<ArrayList<Integer>> adjMatrix) {
        ArrayList<ArrayList<Integer>> cycles = HelperFunctions.findAllHamiltonianCycles(adjMatrix);
        if (cycles.isEmpty()) return null;
        int minCost = HelperFunctions.calculateCost(cycles.get(0), adjMatrix, false), currentCost;
        int minCycleIndex = 0;


        for (int i = 1; i < cycles.size(); i++) {
            currentCost = HelperFunctions.calculateCost(cycles.get(i), adjMatrix, false);
            if (minCost > currentCost) {
                minCost = currentCost;
                minCycleIndex = i;
            }
        }
        return cycles.get(minCycleIndex);
    }

    public static void main(String[] args) {


        //Run Experiment
        AlgoExperiment expRunner = new AlgoExperiment();
        Function<ArrayList<ArrayList<Integer>>, AlgoResult>[] heuristicAlgorithms = new Function[3];
        Function<ArrayList<ArrayList<Integer>>, AlgoResult>[] lookahead = new Function[1];
        //10 iteration ACM with params 10, 2, .9, .1, .1 (As described in orig paper)

        heuristicAlgorithms[0] = AntSystem.stageAntSystemTSP("LookaheadAntSystem",10, 25, .9, 2, .1, .1, true, false);
        heuristicAlgorithms[1] = AntSystem.stageAntSystemTSP("AntSystem",10, 25, .9, 2, .1, .1, false, false);
        heuristicAlgorithms[2] = AntSystem.stageAntSystemTSP("AntColony",10, 25, .9, 2, .1, .1, false, true);

        lookahead[0] = AntSystem.stageAntSystemTSP("LookaheadAntSystem",10, 25, .9, 2, .1, .1, true, false);

        //heuristicAlgorithms[2] = GreedyAlgorithim::greedyTSP; //not heuristic but its low cost and a nice baseline

        //0expRunner.runExperiment(8, 20, 4, 1, 1, allAlgorithms);
        //expRunner.exportResults("smallExp02.csv");

        double[] sparsity = {1, .9, .8, .7, .6, .5, .4, .3, .2, .1, .05};
        //double[] sparsity = {1};

        for (int i = 0; i < sparsity.length; i++) {
            expRunner.runGeneratedMatrixExperiment(6, 100, 200, 0, 2, sparsity[i], heuristicAlgorithms);
            expRunner.exportResults("Lookahead Test.csv", i != 0, String.format( "%3.0f",sparsity[i]*100));
        }

        //expRunner.runDatasetExperiment("MatrixDatasets/vlsi", heuristicAlgorithms);
        //expRunner.exportResults("VLSIMatrixTesting.csv", false);

        //System.out.println(TSPBruteForce(HelperFunctions.readVLSIAdjacencyList("MatrixDatasets/vlsi/xqf131.tsp", false)));
    }
}