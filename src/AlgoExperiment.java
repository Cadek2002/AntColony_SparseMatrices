import HelperFunctions.HelperFunctions;

import java.io.*;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.function.Function;

public class AlgoExperiment {
    ArrayList<ArrayList<AlgoResult>> results;
    private class AlgoResult {
        int cost;
        ArrayList<Integer> path;
        long Seconds;
        long NanoSeconds;
        int batchID;
        String alias;
        int iteration;

        public AlgoResult(int batchID, int cost, ArrayList<Integer> path, long seconds, long nanoSeconds) {
            this.cost = cost;
            this.path = path;
            Seconds = seconds;
            NanoSeconds = nanoSeconds;
            this.batchID = batchID;
            alias = Integer.toString(batchID);
            this.iteration = -1;

        }

        public AlgoResult(String alias, int batchID, int cost, ArrayList<Integer> path, long seconds, long nanoSeconds) {
            this.cost = cost;
            this.path = path;
            Seconds = seconds;
            NanoSeconds = nanoSeconds;
            this.batchID = batchID;
            this.alias = alias;
            this.iteration = -1;
        }

        public AlgoResult(String alias,int iteration, int batchID, int cost, ArrayList<Integer> path, long seconds, long nanoSeconds) {
            this.cost = cost;
            this.path = path;
            Seconds = seconds;
            NanoSeconds = nanoSeconds;
            this.batchID = batchID;
            this.alias = alias;
            this.iteration = iteration;
        }
    }
    public static class ACMPackage {
        int ants, iterations;
        double biasedExplorationCoefficient, distanceBiasCoefficient, localPheromoneUpdateCoefficient, globalPheromoneDecayCoefficient;
        String alias;
        //Custom ACM Params
        public ACMPackage(String alias, int ants, int iterations, double biasedExplorationCoefficient, double distanceBiasCoefficient, double localPheromoneUpdateCoefficient, double globalPheromoneDecayCoefficient) {
            this.alias = alias;
            this.ants = ants;
            this.iterations = iterations;
            this.biasedExplorationCoefficient = biasedExplorationCoefficient;
            this.distanceBiasCoefficient = distanceBiasCoefficient;
            this.localPheromoneUpdateCoefficient = localPheromoneUpdateCoefficient;
            this.globalPheromoneDecayCoefficient = globalPheromoneDecayCoefficient;
        }
        //Default ACM params
        public ACMPackage(int ants, int iterations) {
            this.ants = ants;
            this.iterations = iterations;
            alias = "Default";
            this.biasedExplorationCoefficient = .9;
            this.distanceBiasCoefficient = 2;
            this.localPheromoneUpdateCoefficient = .1;
            this.globalPheromoneDecayCoefficient = .1;
        }

        public ArrayList<Integer> run(ArrayList<ArrayList<Integer>> matrix) {
            return AntSystem.antColonyMethodTSPIterationTracking(5, ants, iterations, biasedExplorationCoefficient, distanceBiasCoefficient, globalPheromoneDecayCoefficient, localPheromoneUpdateCoefficient, matrix);
        }
    }
    //Me messing with javaDocs
    /**
     * Runs the determined algorithms
     * <p>
     * This method runs the specified algorithms according the
     * Experiment params results are returned as a 2D array of
     * Experiment results
     *
     * @param numBatches  number of batches to run
     * @param batchSize how many matrices the algorithms run on per batch
     * @param startBatchGraphSize initial size of the matricies for batch 0
     * @param batchGrowthConstant added to matrix size after each batch
     * @param batchGrowthFactor multiplied by previous batch size before batchGrowthConstant is added
     * @param algoArray an array of algorithms that takes a complete, directional adjacency matrix and outputs a hamiltonian path
     */
    public void runGeneratedMatrixExperiment(int numBatches, int batchSize, int startBatchGraphSize, int batchGrowthConstant, int batchGrowthFactor, double matrixDensity, Function<ArrayList<ArrayList<Integer>>, ArrayList<Integer>>[] algoArray) {
        int n = startBatchGraphSize;
        ArrayList<ArrayList<Integer>> matrix;
        ArrayList<Integer> pathResult;
        System.out.println("\nRunning Experiment");

        ArrayList<ArrayList<AlgoResult>> results = new ArrayList<>(algoArray.length);
        for (int i = 0; i < algoArray.length; i++) results.add(new ArrayList<>(numBatches * batchSize));

        for (int i = 0; i < numBatches; i++) {
            System.out.printf("\nRunning Batch %d, matrix size %d: ", i, n);
            for (int j = 0; j < batchSize; j++) {
                System.out.printf(" %d", j);
                matrix = HelperFunctions.createMatrix(n, 1, 9, matrixDensity, String.format("experimentMatrices: %d_matrix%d_%d.csv", n, i, j));
                HelperFunctions.printMatrix(matrix);
                for (int k = 0; k < algoArray.length; k++) {
                    Instant start = Instant.now();
                    pathResult = algoArray[k].apply(matrix);
                    Instant end = Instant.now();
                    results.get(k).add(new AlgoResult(i, HelperFunctions.calculateCost(pathResult, matrix, true), pathResult, Duration.between(start, end).getSeconds(), Duration.between(start, end).getNano()));
                }
            }
            n = (n * batchGrowthFactor) + batchGrowthConstant;
        }
        this.results = results;
    }

    public void runDatasetExperiment(String dataset, Function<ArrayList<ArrayList<Integer>>, ArrayList<Integer>>[] algoArray) {
        File dir = new File(dataset);
        File[] files = dir.listFiles();

        for (File file  : files) {

        }
    }

    public void runACMExperiment(int numBatches, int batchSize, int startBatchGraphSize, int batchGrowthConstant, int batchGrowthFactor, ArrayList<ACMPackage> paramArray) {
        int n = startBatchGraphSize;
        ArrayList<ArrayList<Integer>> matrix;
        ArrayList<Integer> pathResult;
        System.out.println("Running Experiment");

        ArrayList<ArrayList<AlgoResult>> results = new ArrayList<>(paramArray.size());
        for (int i = 0; i < paramArray.size(); i++) results.add(new ArrayList<>(numBatches * batchSize));

        for (int i = 0; i < numBatches; i++) {
            System.out.printf("\nRunning Batch %d, matrix size %d: ", i, n);
            for (int j = 0; j < batchSize; j++) {
                System.out.printf("%d", j);
                matrix = HelperFunctions.createMatrix(n, 1, 9, 1, String.format("experimentMatrices: %d_matrix%d_%d.csv", n, i, j));
                for (int k = 0; k < paramArray.size(); k++) {
                    Instant start = Instant.now();
                    pathResult = paramArray.get(k).run(matrix);
                    Instant end = Instant.now();
                    results.get(k).add(new AlgoResult(paramArray.get(k).alias, n, pathResult.get(pathResult.size()-1), pathResult, Duration.between(start, end).getSeconds(), Duration.between(start, end).getNano()));
                }
            }
            n = (n * batchGrowthFactor) + batchGrowthConstant;
        }
        this.results = results;
    }



    public void exportResults(String outputName, boolean append, String... additionalArgs) {
        File output = new File(outputName);
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(output, append));
            if (!append)
                writer.write("Algorithm, ResultID, BatchID, Cost, Path, Seconds, Nanoseconds, Total\n");

            for (int i = 0; i < results.size(); i++) {
                for (AlgoResult res : results.get(i))
                    if (res.iteration == -1) {
                        writer.write(String.format("%s,%d,%d,%d,%s,%d,%d,%f", res.alias, i, res.batchID, res.cost, HelperFunctions.getStringCSV(res.path), res.Seconds, res.NanoSeconds, res.Seconds + (double) res.NanoSeconds / 1000000000));
                        for (String item : additionalArgs) {
                            writer.write(String.format(",%s", item));
                        }
                        writer.write("\n");
                    }
            }
            writer.close();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }
}
