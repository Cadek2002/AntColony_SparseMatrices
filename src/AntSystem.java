import HelperFunctions.*;

import java.util.ArrayList;
import java.util.Random;
import java.util.function.Function;

public class AntSystem {

    //Algo Name for parameter set
    String alias;
    static Random r = new Random();
    int numAnts, numIterations, currentBest, iterationsTillSuccess;
    ArrayList<Integer> bestPath;
    Ant[] colony;
    double biasedExplorationCoefficient, distanceBiasCoefficient, pheromoneUpdateCoefficient, globalPheromoneDecayCoefficient, initialPheromone;
    ArrayList<ArrayList<Double>> pheromoneMatrix;
    ArrayList<ArrayList<Integer>> adjacencyMatrix;
    ArrayList<Integer> numProspectsList;


    //Variance Variable
    boolean lookahead;
    boolean cooperative;

    public AntSystem(int numAnts, int numIterations, double biasedExplorationCoefficient, double distanceBiasCoefficient, double globalPheromoneDecayCoefficient, double localPheromoneUpdateCoefficient, boolean lookahead, boolean cooperative) {
        this.numAnts = numAnts;
        this.numIterations = numIterations;
        currentBest = -1;
        this.biasedExplorationCoefficient = biasedExplorationCoefficient;
        this.distanceBiasCoefficient = distanceBiasCoefficient;
        this.globalPheromoneDecayCoefficient = globalPheromoneDecayCoefficient;
        this.pheromoneUpdateCoefficient = localPheromoneUpdateCoefficient;
        this.lookahead = lookahead;
        this.cooperative = cooperative;
        colony = new Ant[numAnts];
    }

    public AntSystem(String alias, int numAnts, int numIterations, double biasedExplorationCoefficient, double distanceBiasCoefficient, double globalPheromoneDecayCoefficient, double localPheromoneUpdateCoefficient, boolean lookahead, boolean cooperative) {
        this.alias = alias;
        this.numAnts = numAnts;
        this.numIterations = numIterations;
        currentBest = -1;
        this.biasedExplorationCoefficient = biasedExplorationCoefficient;
        this.distanceBiasCoefficient = distanceBiasCoefficient;
        this.globalPheromoneDecayCoefficient = globalPheromoneDecayCoefficient;
        this.pheromoneUpdateCoefficient = localPheromoneUpdateCoefficient;
        this.lookahead = lookahead;
        this.cooperative = cooperative;
        colony = new Ant[numAnts];
    }

    public static AlgoResult antSystemTSP(int numAnts, int numIterations, double biasedExplorationCoefficient, double distanceBiasCoefficient, double globalPheromoneDecayCoefficient, double localPheromoneUpdateCoefficient, boolean lookahead, boolean cooperative, ArrayList<ArrayList<Integer>> adjMatrix) {
        AntSystem antColony = new AntSystem(numAnts, numIterations, biasedExplorationCoefficient, distanceBiasCoefficient, globalPheromoneDecayCoefficient, localPheromoneUpdateCoefficient, lookahead, cooperative);
        return antColony.applyAlgorithm(adjMatrix);
    }




    public static ArrayList<Integer> antColonyMethodTSPIterationTracking(int logInterval, int numAnts, int numIterations, double biasedExplorationCoefficient, double distanceBiasCoefficient, double globalPheromoneDecayCoefficient, double localPheromoneUpdateCoefficient, ArrayList<ArrayList<Integer>> adjMatrix) {
        AntSystem antColony = new AntSystem(numAnts, numIterations, biasedExplorationCoefficient, distanceBiasCoefficient, globalPheromoneDecayCoefficient, localPheromoneUpdateCoefficient, false, false);
        antColony.adjacencyMatrix = adjMatrix;
        ArrayList<Integer> bestHistory = new ArrayList<>();
        int startingPoint;

        for (int i = 0; i < numIterations; i++) {
            //Initialize ants to distribution of starting nodes
            for (int j = 0; j < numAnts; j++) {
                antColony.colony[j].path.add(r.nextInt(adjMatrix.size()));
                antColony.colony[j].candidateList[antColony.colony[j].path.get(0)] = true;
            }
            //Step ants until looped around
            for (int j = 0; j < adjMatrix.size(); j++) {
                antColony.runStep();
            }
            //Find the best path and reset ants
            antColony.evaluateColony();
            if (i % logInterval == 0) bestHistory.add(antColony.currentBest);
        }
        antColony.bestPath.remove(antColony.bestPath.size() - 1);
        return bestHistory;
    }


    AlgoResult applyAlgorithm(ArrayList<ArrayList<Integer>> adjMatrix) {
        adjacencyMatrix = adjMatrix;

        //initialPheromone = HelperFunctions.calculateCost(GreedyAlgorithim.greedyTSP(adjacencyMatrix), adjacencyMatrix, true);
        //NEW Initialization for Sparse Graphs (Average Choice Index)
        int sum, numViable;
        initialPheromone = 0;
        currentBest = -1;
        bestPath = null;
        for (int i = 0; i < adjacencyMatrix.size(); i++) {
            sum = 0;
            numViable = 0;
            for (int j = 0; j < adjacencyMatrix.size(); j++) {
                if (adjacencyMatrix.get(i).get(j) > 0) {
                    sum += adjacencyMatrix.get(i).get(j);
                    numViable++;
                }
            }
            initialPheromone += sum / (double) numViable;
        }

        for (int i = 0; i < numAnts; i++) colony[i] = new Ant();

        pheromoneMatrix = new ArrayList<>(adjacencyMatrix.size());
        //Initialize PheromoneMatrix and numProspects list
        int numProspects;
        numProspectsList = new ArrayList<>(adjacencyMatrix.size());
        for (int i = 0; i < adjacencyMatrix.size(); i++) {
            pheromoneMatrix.add(new ArrayList<>(adjacencyMatrix.size()));
            numProspects = 0;
            for (int j = 0; j < adjacencyMatrix.size(); j++) {
                pheromoneMatrix.get(i).add(initialPheromone);
                if (adjacencyMatrix.get(i).get(j) > 0) numProspects++;
                numProspectsList.add(numProspects);
            }
        }
        iterationsTillSuccess = -1;

        for (int i = 0; i < numIterations; i++) {
            //Initialize ants to distribution of starting nodes
            for (int j = 0; j < numAnts; j++) {
                colony[j].path.add(r.nextInt(adjMatrix.size()));
                colony[j].candidateList[colony[j].path.get(0)] = true;
            }
            //Step ants until looped around
            for (int j = 0; j < adjMatrix.size(); j++) {
                runStep();
            }
            //Find the best path and reset ants
            evaluateColony();
        }
        //bestPath.remove(bestPath.size() - 1);
        AlgoResult result = new AlgoResult(alias, currentBest, bestPath, iterationsTillSuccess);
        return result;
    }

    //O(numAnts(M) * numNodes(N))
    void runStep() {
        for (int i = 0; i < numAnts; i++) {
            //iterate each ant, using the global state of the map, and the local state of the ant
            if (colony[i].cost != -1)
                colony[i].chooseEdge(r.nextDouble() > biasedExplorationCoefficient);
        }
    }

    void evaluateColony() {
        //Find Best Path
        //System.out.print("Colony Status (Costs): ");
        for (int i = 0; i < numAnts; i++) {
            //System.out.printf(" %d", colony[i].cost);
            if (currentBest == -1 || (currentBest > colony[i].cost && colony[i].cost != -1)) {
                if (currentBest == -1 && colony[i].cost != -1) iterationsTillSuccess = i;
                currentBest = colony[i].cost;
                bestPath = colony[i].path;
            }
        }
        double updateSum = 0;
        //Global Update Pheromone Trails if best path was found
        if (currentBest != -1)
            for (int i = 0; i < pheromoneMatrix.size(); i++) {
                for (int j = 0; j < pheromoneMatrix.size(); j++) {
                    //Ant Colony System Global Updates using best path
                    if (cooperative)
                        pheromoneMatrix.get(i).set(j, (1 - globalPheromoneDecayCoefficient) * pheromoneMatrix.get(i).get(j) + globalPheromoneDecayCoefficient * (HelperFunctions.edgeInPath(bestPath, i, j) ? 1 / (double) currentBest : 0));
                    //Ant System Global Updates using any path visited this iteration
                    else {
                        updateSum = 0;
                        for (int k = 0; k < numAnts; k++) {
                            if (colony[k].cost != -1 && HelperFunctions.edgeInPath(colony[k].path, i, j))
                                updateSum += pheromoneUpdateCoefficient / colony[k].cost;
                        }
                        pheromoneMatrix.get(i).set(j, (1 - globalPheromoneDecayCoefficient) * pheromoneMatrix.get(i).get(j) + updateSum);
                    }
                }
            }

        //Reset Ants (Keep Global best path
        for (int i = 0; i < numAnts; i++)
            colony[i].resetAnt();
    }

    public static AlgoResult antSystemMethodDefaultTSP(ArrayList<ArrayList<Integer>> adjMatrix) {
        return antSystemTSP(10, 50, .9, 2, .1, .1, false, false, adjMatrix);
    }

    public static Function<ArrayList<ArrayList<Integer>>, AlgoResult> stageAntSystemTSP(String setName, int numAnts, int numIterations, double biasedExplorationCoefficient, double distanceBiasCoefficient, double globalPheromoneDecayCoefficient, double localPheromoneUpdateCoefficient, boolean lookahead, boolean cooperative) {
        AntSystem algorithmObject = new AntSystem(setName, numAnts, numIterations, biasedExplorationCoefficient, distanceBiasCoefficient, globalPheromoneDecayCoefficient, localPheromoneUpdateCoefficient, lookahead, cooperative);
        return algorithmObject::applyAlgorithm;
    }

    //Defines Ant State
    public class Ant {
        ArrayList<Integer> path;
        boolean[] candidateList;
        int cost;
        int matrixSize;
        boolean alive;

        public Ant() {
            this.matrixSize = adjacencyMatrix.size();
            path = new ArrayList<>(matrixSize + 1);
            candidateList = new boolean[matrixSize];
            cost = 0;
        }

        static int biasedRandomEdgeSelection(ArrayList<edgeWeightPair> weights) {
            if (!weights.isEmpty()) {
                // generate random double between 0 and sum of all weights
                double choice = r.nextDouble() * weights.get(weights.size() - 1).iterativeWeight;
                for (int i = 0; i < weights.size(); i++) {
                    if (weights.get(i).iterativeWeight > choice) return weights.get(i).index;
                }
            }
            //If Edge selection fails return -1 code
            return -1;
        }

        public void resetAnt() {
            //Delete Path
            if (path != bestPath) path.clear();
            else path = new ArrayList<>(matrixSize + 1);
            //Reset candidate list
            for (int i = 0; i < matrixSize; i++) {
                candidateList[i] = false;
            }
            cost = 0;
        }

        double calculateEdgeWeight(int r, int s) {
            if (adjacencyMatrix.get(r).get(s) <= 0) return 0;
            //System.out.printf("Cost: %d\distanceBias: %f\tPheromone: %f\n", adjacencyMatrix.get(r).get(s), distanceBiasCoefficient, pheromoneMatrix.get(r).get(s));
            return Math.pow(1 / (double) adjacencyMatrix.get(r).get(s), distanceBiasCoefficient) * pheromoneMatrix.get(r).get(s);
        }

        double calculateEdgeWeightIncludeFutureProspects(int r, int s) {
            if (adjacencyMatrix.get(r).get(s) <= 0) return 0;
            //System.out.printf("Cost: %d\t distanceBias: %f\tPheromone: %f\n", adjacencyMatrix.get(r).get(s), distanceBiasCoefficient, pheromoneMatrix.get(r).get(s));
            return Math.pow(1 / (double) adjacencyMatrix.get(r).get(s), distanceBiasCoefficient) * pheromoneMatrix.get(r).get(s) * ((double) adjacencyMatrix.size() - (double) 1 / numProspectsList.get(s) - 1);
        }

        public void chooseEdge(boolean greedy) {
            //Utilize edge cost and pheromone concentration and heuristic probability function to determine edge, update state accordingly

            //Generate Probability Distribution from Candidate List
            ArrayList<edgeWeightPair> candidateProb = new ArrayList<>(matrixSize);
            double iterativeProbability = 0;
            double currentProbability, maxProbability = 0;
            int maxProbabilityIndex = -1, choice = -1;

            if (path.size() != adjacencyMatrix.size()) {
                for (int i = 0; i < matrixSize; i++) {
                    //If the city hasn't been visited
                    if (!candidateList[i]) {
                        //Calculate probability of current edge
                        currentProbability = lookahead ? calculateEdgeWeightIncludeFutureProspects(path.get(path.size() - 1), i) : calculateEdgeWeight(path.get(path.size() - 1), i);
                        //Set Iterative probability
                        iterativeProbability += currentProbability;
                        //Create data pair
                        candidateProb.add(new edgeWeightPair(currentProbability, iterativeProbability, i));

                        if (currentProbability > maxProbability) {
                            maxProbabilityIndex = i;
                            maxProbability = currentProbability;
                        }
                    }
                }
                //choose edge based on probability depending on greedy setting
                choice = (greedy ? maxProbabilityIndex : biasedRandomEdgeSelection(candidateProb));
            } else {
                //adjacencyMatrix.get(path.get(path.size() - 1)).get(path.get(0));
                choice = adjacencyMatrix.get(path.get(path.size() - 1)).get(path.get(0)) >= 1 ? path.get(0) : -1;
            }

            if (choice != -1) {
                //Local Update Pheromone (Edge distance * p + (1-p) * 1/(n * Lnn)
                if (cooperative) pheromoneMatrix.get(path.get(path.size() - 1)).set(choice, pheromoneMatrix.get(path.get(path.size() - 1)).get(choice)
                        * (1 - pheromoneUpdateCoefficient) + pheromoneUpdateCoefficient * (1 / (matrixSize * initialPheromone)));

                //Update Ant State if a valid choice exists, otherwise kill ant.
                cost += adjacencyMatrix.get(path.get(path.size() - 1)).get(choice);
                candidateList[choice] = true;
                path.add(choice);
            } else {
                //kill the ant
                cost = -1;
            }
            //System.out.printf("Greedy: %b\t%s\tCost: %d\n",greedy, path, cost);
        }

        static class edgeWeightPair {
            double weight;
            double iterativeWeight;
            int index;

            public edgeWeightPair(double weight, double iterativeWeight, int index) {
                this.weight = weight;
                this.iterativeWeight = iterativeWeight;
                this.index = index;
            }
        }
    }
}