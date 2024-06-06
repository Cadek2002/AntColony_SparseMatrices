import HelperFunctions.HelperFunctions;

import java.util.ArrayList;
import java.util.Random;

public class AntSystem {
    static Random r = new Random();
    int numAnts, currentBest;
    ArrayList<Integer> bestPath;
    Ant[] colony;
    double biasedExplorationCoefficient, distanceBiasCoefficient, localPheromoneUpdateCoefficient, globalPheromoneDecayCoefficient, initialPheromone;
    ArrayList<ArrayList<Double>> pheromoneMatrix;
    ArrayList<ArrayList<Integer>> adjacencyMatrix;


    public AntSystem(int numAnts, double biasedExplorationCoefficient, double distanceBiasCoefficient, double globalPheromoneDecayCoefficient, double localPheromoneUpdateCoefficient, ArrayList<ArrayList<Integer>> adjacencyMatrix) {
        this.numAnts = numAnts;
        this.adjacencyMatrix = adjacencyMatrix;
        currentBest = -1;
        this.biasedExplorationCoefficient = biasedExplorationCoefficient;
        this.distanceBiasCoefficient = distanceBiasCoefficient;
        this.globalPheromoneDecayCoefficient = globalPheromoneDecayCoefficient;
        this.localPheromoneUpdateCoefficient = localPheromoneUpdateCoefficient;
        colony = new Ant[numAnts];

        initialPheromone = HelperFunctions.calculateCost(GreedyAlgorithim.greedyTSP(adjacencyMatrix), adjacencyMatrix, true);

        for (int i = 0; i < numAnts; i++) colony[i] = new Ant();

        pheromoneMatrix = new ArrayList<>(adjacencyMatrix.size());
        for (int i = 0; i < adjacencyMatrix.size(); i++) {
            pheromoneMatrix.add(new ArrayList<>(adjacencyMatrix.size()));
            for (int j = 0; j < adjacencyMatrix.size(); j++) pheromoneMatrix.get(i).add(initialPheromone);
        }
    }

    public static ArrayList<Integer> antColonyMethodTSP(int numAnts, int numIterations, double biasedExplorationCoefficient, double distanceBiasCoefficient, double globalPheromoneDecayCoefficient, double localPheromoneUpdateCoefficient, ArrayList<ArrayList<Integer>> adjMatrix) {
        AntSystem antColony = new AntSystem(numAnts, biasedExplorationCoefficient, distanceBiasCoefficient, globalPheromoneDecayCoefficient, localPheromoneUpdateCoefficient, adjMatrix);
        ArrayList<Integer> best;
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
        }
        antColony.bestPath.remove(antColony.bestPath.size() - 1);
        return antColony.bestPath;
    }
    public static ArrayList<Integer> antColonyMethodTSPIterationTracking(int logInterval, int numAnts, int numIterations, double biasedExplorationCoefficient, double distanceBiasCoefficient, double globalPheromoneDecayCoefficient, double localPheromoneUpdateCoefficient, ArrayList<ArrayList<Integer>> adjMatrix) {
        AntSystem antColony = new AntSystem(numAnts, biasedExplorationCoefficient, distanceBiasCoefficient, globalPheromoneDecayCoefficient, localPheromoneUpdateCoefficient, adjMatrix);
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
                currentBest = colony[i].cost;
                bestPath = colony[i].path;
            }
        }
        //System.out.printf(" Best: %d, %s\n", currentBest, bestPath);
        //System.out.println(currentBest);
        //Global Update Pheromone Trails if a best path was found

        if (currentBest != -1)
            for (int i = 0; i < pheromoneMatrix.size(); i++) {
                for (int j = 0; j < pheromoneMatrix.size(); j++) {
                    pheromoneMatrix.get(i).set(j, (1 - globalPheromoneDecayCoefficient) * pheromoneMatrix.get(i).get(j) + globalPheromoneDecayCoefficient * (HelperFunctions.edgeInPath(bestPath, i, j) ? 1 / (double) currentBest : 0));
                }
            }

        //Reset Ants (Keep Global best path
        for (int i = 0; i < numAnts; i++)
            colony[i].resetAnt();
    }

    public static ArrayList<Integer> antSystemMethodDefaultTSP(ArrayList<ArrayList<Integer>> adjMatrix) {
        return antColonyMethodTSP(10, 25, .9, 2, .1, .1, adjMatrix);
    }

    //Defines Ant State
    public class Ant {
        ArrayList<Integer> path;
        boolean candidateList[];
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
            if (weights.size() >= 1) {
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
            //System.out.printf("Cost: %d\tdistanceBias: %f\tPheromone: %f\n", adjacencyMatrix.get(r).get(s), distanceBiasCoefficient, pheromoneMatrix.get(r).get(s));
            return Math.pow(1 / (double) adjacencyMatrix.get(r).get(s), distanceBiasCoefficient) * pheromoneMatrix.get(r).get(s);
        }


        
        public int chooseEdge(boolean greedy) {
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
                        currentProbability = calculateEdgeWeight(path.get(path.size() - 1), i);
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
                adjacencyMatrix.get(path.get(path.size()-1)).get(path.get(0));
                choice = adjacencyMatrix.get(path.get(path.size()-1)).get(path.get(0)) >= 1 ? path.get(0) : -1;
            }

            if (choice != -1) {
                //Local Update Pheromone (Edge distance * p + (1-p) * 1/(n * Lnn)

                pheromoneMatrix.get(path.get(path.size() - 1)).set(choice, pheromoneMatrix.get(path.get(path.size() - 1)).get(choice)
                    * (1 - localPheromoneUpdateCoefficient) + localPheromoneUpdateCoefficient * (1 / (matrixSize * initialPheromone)));
            //Update Ant State if a valid choice exists, otherwise kill ant.

                cost += adjacencyMatrix.get(path.get(path.size() - 1)).get(choice);
                candidateList[choice] = true;
                path.add(choice);
            }
            else {
                //kill the ant
                cost = -1;
            }
            //System.out.printf("Greedy: %b\t%s\tCost: %d\n",greedy, path, cost);
            return choice;
        }

        class edgeWeightPair {
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