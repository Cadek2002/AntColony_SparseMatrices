import HelperFunctions.HelperFunctions;

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
        if (cycles.size() == 0) return null;
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

        //ArrayList<Integer> vertices = readVertices(verticesFileName);
        //ArrayList<ArrayList<Integer>> cycles = HelperFunctions.readAdjMatrixFile(setFileName, null);
        //ArrayList<ArrayList<Integer>> adjMatrix = HelperFunctions.readAdjacencyList(matrixFileName, true);
        //ArrayList<Integer> minCycle1 = null, minCycle2, minCycle3;
        // GENERATES NEW MATRIX/CYCLES, if you want to keep a particular matrix comment these out
        // Create Matrix (Size, min_val, max_val, density, output file name)
        //adjMatrix = HelperFunctions.createMatrix(12, 1, 9, 1, "matrix.txt");


        if (true) {
            //Instant start1 = Instant.now();
            // Algorithm 1: Brute Force, generate all hamiltonian cycles and calculate their costs, find the smallest
            //Calculate size of all Hamiltonian Paths

            // Create Cycle Set
            //minCycle1 = BruteForceAlgorithm.bruteForceTSP(adjMatrix);
            /**
             * MANUAL DISPLAY
             adjMatrix = HelperFunctions.HelperFunctions.createMatrix(12, 1, 9, 1, "matrix.txt");

             // END ALGO CODE`
             Instant end1 = Instant.now();
             // START ALGO2
             Instant start2 = Instant.now();
             // Algorithm 2: Call ACM
             minCycle2 = GreedyAlgorithim.greedyTSP(adjMatrix);
             // END ALGO2
             Instant end2 = Instant.now();

             // START ALGO2
             Instant start3 = Instant.now();
             // Algorithm 2: Call ACM
             minCycle3 = AntColony.antColonyMethodTSP(10, 10, .9, 2, .1, .1, adjMatrix);
             // END ALGO2
             Instant end3 = Instant.now();

             System.out.printf("Brute Force Run Duration:\t\t%s Secs\t%s NS\tCost: %d\n", Duration.between(start1, end1).getSeconds(), Duration.between(start1, end1).getNano(), HelperFunctions.HelperFunctions.calculateCost(minCycle1, adjMatrix));
             System.out.printf("Nearest Neighbor Run Duration:\t%s Secs\t%s NS\tCost: %d\n", Duration.between(start2, end2).getSeconds(), Duration.between(start2, end2).getNano(), HelperFunctions.HelperFunctions.calculateCost(minCycle2, adjMatrix));
             System.out.printf("Ant Colony Method Run Duration:\t%s Secs\t%s NS\tCost: %d\n", Duration.between(start3, end3).getSeconds(), Duration.between(start3, end3).getNano(), HelperFunctions.HelperFunctions.calculateCost(minCycle3, adjMatrix));
             //printMatrix(cycles);

             // Export updated matrix into file
             HelperFunctions.HelperFunctions.exportFile(adjMatrix, outputName, null);
             **/

            //Run Experiment
            AlgoExperiment expRunner = new AlgoExperiment();
            Function<ArrayList<ArrayList<Integer>>, ArrayList<Integer>>[] allAlgorithms = new Function[3];
            Function<ArrayList<ArrayList<Integer>>, ArrayList<Integer>>[] heuristicAlgorithms = new Function[3];

            allAlgorithms[0] = BruteForceAlgorithm::bruteForceTSP;
            allAlgorithms[1] = GreedyAlgorithim::greedyTSP;
            allAlgorithms[2] = AntColony::antColonyMethodDefaultTSP; //10 iteration ACM with params 10, 2, .9, .1, .1 (As described in orig paper)

            heuristicAlgorithms[0] = AntSystem::antSystemMethodDefaultTSP;
            heuristicAlgorithms[1] = AntColony::antColonyMethodDefaultTSP; //10 iteration ACM with params 10, 2, .9, .1, .1 (As described in orig paper)
            heuristicAlgorithms[2] = GreedyAlgorithim::greedyTSP;

            //0expRunner.runExperiment(8, 20, 4, 1, 1, allAlgorithms);
            //expRunner.exportResults("smallExp02.csv");

            double[] sparsity = {1, .9, .8, .7, .6, .5, .4, .3, .2, .1, .05};
            //double[] sparsity = {1};

            for (int i = 0; i < sparsity.length; i++) {
                expRunner.runGeneratedMatrixExperiment(7, 50, 10, 0, 2, sparsity[i], heuristicAlgorithms);
                expRunner.exportResults(String.format("%fSparseMatrixTesting.csv", sparsity[i]*100), false);
                expRunner.exportResults("MasterSparseMatrixTesting.csv", i != 0, String.format( "%3.0f",sparsity[i]*100));
            }



//            int ANTS = 10;
//            int ITERATIONS = 50;
//
//            ArrayList<AlgoExperiment.ACMPackage> ACMParams = new ArrayList<>();
//            ACMParams.add(new AlgoExperiment.ACMPackage("Squared", ANTS, ITERATIONS, .9, .5, .1, .1));
//            ACMParams.add(new AlgoExperiment.ACMPackage("Equal", ANTS, ITERATIONS, .9, 1, .1, .1));
//            ACMParams.add(new AlgoExperiment.ACMPackage(ANTS, ITERATIONS));
//            ACMParams.add(new AlgoExperiment.ACMPackage("Cubed", ANTS, ITERATIONS, .9, 3, .1, .1));
//
//            expRunner.runACMExperiment(5, 25, 25, 0, 2, ACMParams);
//            expRunner.exportResults("ACMDistanceBViCoefTestingSparsity.csv");


        }
    }
}