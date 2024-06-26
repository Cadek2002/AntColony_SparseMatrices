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
        Function<ArrayList<ArrayList<Integer>>, AlgoResult>[] heuristicAlgorithms = new Function[6];
        Function<ArrayList<ArrayList<Integer>>, AlgoResult>[] singleTesting = new Function[6];
        //10 iteration ACM with params 10, 2, .9, .1, .1 (As described in orig paper)



        heuristicAlgorithms[0] = AntSystem.stageAntSystemTSP(new AntSystem("AntSystem",15, 25, .9, .9, 2, .1, .1, 0, false, 0));
        heuristicAlgorithms[1] = AntSystem.stageAntSystemTSP(new AntSystem("LookaheadAntSystem",15, 25, .9, .9, 2, .1, .1, 2, false, 0));
        heuristicAlgorithms[2] = AntSystem.stageAntSystemTSP(new AntSystem("AntColony", 15, 25, .9, .9, 2, .1, .1, 0, true, 0));
        heuristicAlgorithms[3] = AntSystem.stageAntSystemTSP(new AntSystem("LookaheadAntColony",15, 25, .9, .9, 2, .1, .1, 2, true, 0));
        heuristicAlgorithms[4] = AntSystem.stageAntSystemTSP(new AntSystem("DeathpenAntSystem",15, 25, .9, .9, 2, .1, .1, 0, false, 0));
        heuristicAlgorithms[5] = AntSystem.stageAntSystemTSP(new AntSystem("DeathpenAntColony",15, 25, .9, .9, 2, .1, .1, 0, true, 0));

        singleTesting[0] = AntSystem.stageAntSystemTSP(new AntSystem("BaseAS",10, 15, .4, .9, 2, .1, .1, 4, false, 0));
        singleTesting[1] = AntSystem.stageAntSystemTSP(new AntSystem("LookaheadAS.5",10, 15, .5, .9, 2, .1, .1, 4, false, 0));
        singleTesting[2] = AntSystem.stageAntSystemTSP(new AntSystem("LookaheadAS.6",10, 15, .6, .9, 2, .1, .1, 4, false, 0));
        singleTesting[3] = AntSystem.stageAntSystemTSP(new AntSystem("LookaheadAS.7",10, 15, .7, .9, 2, .1, .1, 4, false, 0));
        singleTesting[4] = AntSystem.stageAntSystemTSP(new AntSystem("LookaheadAS.8",10, 15, .8, .9, 2, .1, .1, 4, false, 0));
        singleTesting[5] = AntSystem.stageAntSystemTSP(new AntSystem("LookaheadAS.9",10, 15, .9, .9, 2, .1, .1, 4, false, 0));
        //heuristicAlgorithms[2] = GreedyAlgorithim::greedyTSP; //not heuristic but its low cost and a nice baseline

        //0expRunner.runExperiment(8, 20, 4, 1, 1, allAlgorithms);
        //expRunner.exportResults("smallExp02.csv");

        double[] sparsity = {.4, .3, .2, .1, .05};
        //double[] sparsity = {.2};

        for (int i = 0; i < sparsity.length; i++) {
            expRunner.runGeneratedMatrixExperiment(5, 100, 20, 0, 2, sparsity[i], singleTesting);
            expRunner.exportResults("LookaheadCoefTesting.csv", i != 0, String.format( "%3.0f",sparsity[i]*100));
        }

        //expRunner.runDatasetExperiment("MatrixDatasets/vlsi", heuristicAlgorithms);
        //expRunner.exportResults("VLSIMatrixTesting.csv", false);

        //System.out.println(TSPBruteForce(HelperFunctions.readVLSIAdjacencyList("MatrixDatasets/vlsi/xqf131.tsp", false)));
    }
}