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
        Function<ArrayList<ArrayList<Integer>>, AlgoResult>[] Lookahead = new Function[8];
        Function<ArrayList<ArrayList<Integer>>, AlgoResult>[] initialAlgorithims = new Function[1];
        //10 iteration ACM with params 10, 2, .9, .1, .1 (As described in orig paper)


        //initialAlgorithims[2] = AntSystem.stageAntSystemTSP(new AntSystem("AntSystem",15, 30, 1, 1, 2, .1, .1,null, false,0, false, 0));
        //initialAlgorithims[1] = AntSystem.stageAntSystemTSP(new AntSystem("AntColony", 15, 30, .9, .9, 2, .1, .1,null, false, 0, true, 0));
        initialAlgorithims[0] = AntSystem.stageAntSystemTSP(new AntSystem("MinMaxAntSystem", 15, 150, .9, .9, 2, .1, .1, new double[0], true, 0, false, 0));


        heuristicAlgorithms[0] = AntSystem.stageAntSystemTSP(new AntSystem("AntSystem",15, 25, .9, .9, 2, .1, .1, null, false,0, false, 0));
        heuristicAlgorithms[1] = AntSystem.stageAntSystemTSP(new AntSystem("LookaheadAntSystem",15, 25, .9, .9, 2, .1, .1, null, false,2, false, 0));
        heuristicAlgorithms[2] = AntSystem.stageAntSystemTSP(new AntSystem("AntColony", 15, 25, .9, .9, 2, .1, .1,null, false, 0, true, 0));
        heuristicAlgorithms[3] = AntSystem.stageAntSystemTSP(new AntSystem("LookaheadAntColony",15, 25, .9, .9, 2, .1, .1,null, false, 2, true, 0));
        heuristicAlgorithms[4] = AntSystem.stageAntSystemTSP(new AntSystem("DeathpenAntSystem",15, 25, .9, .9, 2, .1, .1,null, false, 0, false, 0));
        heuristicAlgorithms[5] = AntSystem.stageAntSystemTSP(new AntSystem("DeathpenAntColony",15, 25, .9, .9, 2, .1, .1,null, false, 0, true, 0));

        Lookahead[0] = AntSystem.stageAntSystemTSP(new AntSystem("AS",15, 40, 2, 1, 2, .1, .1, null, false,0, false, 0));
        Lookahead[1] = AntSystem.stageAntSystemTSP(new AntSystem("ACO",15, 40, 2, .9, 2, .1, .1, null, false,0, true, .0));
        Lookahead[2] = AntSystem.stageAntSystemTSP(new AntSystem("ASLo",15, 40, 0, 1, 2, .1, .1,null, false,2, false, 0));
        Lookahead[3] = AntSystem.stageAntSystemTSP(new AntSystem("ACOLo",15, 40, 0, .9, 2, .1, .1, null, false,2, true, 0));
        Lookahead[4] = AntSystem.stageAntSystemTSP(new AntSystem("ASDP",15, 40, 2, 1, 2, .1, .12, new double[0], false,0, false, .06));
        Lookahead[5] = AntSystem.stageAntSystemTSP(new AntSystem("ACODP",15, 40, 2, .9, 2, .1, .12, new double[0], false,0, true, .06));
        Lookahead[6] = AntSystem.stageAntSystemTSP(new AntSystem("ASLoDP",15, 40, 0, 1, 2, .1, .12, new double[0], false,2, false, .06));
        Lookahead[7] = AntSystem.stageAntSystemTSP(new AntSystem("ACOLoDP",15, 40, 0, .9, 2, .1, .12, new double[0], false,2, true, .06));
        //Lookahead[3] = AntSystem.stageAntSystemTSP(new AntSystem("DP8",15, 30, .9, .9, 2, .1, .1, new double[0], false,0, false, .08));

        //heuristicAlgorithms[2] = GreedyAlgorithim::greedyTSP; //not heuristic but its low cost and a nice baseline

        //0expRunner.runExperiment(8, 20, 4, 1, 1, allAlgorithms);
        //expRunner.exportResults("smallExp02.csv");

        double[] sparsity = {.05, .1, .15, .2, .25, .3};
        //double[] sparsity = {.01};

        for (int i = 0; i < sparsity.length; i++) {
            expRunner.runGeneratedMatrixExperiment(3, 500, 80, 0, 2, sparsity[i], Lookahead);
            expRunner.exportResults("MultiSystemTesting500Batch.csv", i != 0, String.format( "%3.0f",sparsity[i]*100));
        }

        //expRunner.runDatasetExperiment("MatrixDatasets/vlsi", heuristicAlgorithms);
        //expRunner.exportResults("VLSIMatrixTesting.csv", false);

        //System.out.println(TSPBruteForce(HelperFunctions.readVLSIAdjacencyList("MatrixDatasets/vlsi/xqf131.tsp", false)));
    }
}