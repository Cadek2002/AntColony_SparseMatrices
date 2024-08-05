import HelperFunctions.*;

import java.util.ArrayList;
import java.util.Random;
import java.util.function.Function;

public class AntSystem {

    //Algo Name for parameter set
    String alias;
    static Random r = new Random();
    int numAnts, numIterations, bestCost, iterationsTillSuccess;
    ArrayList<Integer> bestPath;
    Ant[] colony;
    double preSuccessDistanceBiasCoefficient, biasedExplorationCoefficient, distanceBiasCoefficient, pheromoneUpdateCoefficient, globalPheromoneDecayCoefficient, initialPheromone, death_pen, lookahead_coef;
    ArrayList<ArrayList<Double>> pheromoneMatrix;
    ArrayList<ArrayList<Integer>> adjacencyMatrix;
    ArrayList<Integer> numProspectsList;
    double[] pheromoneBounds;

    double currentDistanceBiasCoefficient;


    //Variance Variable
    boolean cooperative, boundsUpdating, elitist;

    public AntSystem(int numAnts, int numIterations, double preSuccessDistanceBiasCoefficient, double biasedExplorationCoefficient, double distanceBiasCoefficient, double globalPheromoneDecayCoefficient, double localPheromoneUpdateCoefficient, double[] pheromoneBounds, boolean elitist, double lookahead_coef, boolean cooperative, double death_pen) {
        this.numAnts = numAnts;
        this.numIterations = numIterations;
        bestCost = -1;
        this.preSuccessDistanceBiasCoefficient = preSuccessDistanceBiasCoefficient;
        this.biasedExplorationCoefficient = biasedExplorationCoefficient;
        this.distanceBiasCoefficient = distanceBiasCoefficient;
        this.globalPheromoneDecayCoefficient = globalPheromoneDecayCoefficient;
        this.pheromoneUpdateCoefficient = localPheromoneUpdateCoefficient;
        this.pheromoneBounds = pheromoneBounds;
        this.lookahead_coef = lookahead_coef;
        this.cooperative = cooperative;
        this.elitist = elitist;
        this.death_pen = death_pen;
        colony = new Ant[numAnts];
    }

    public AntSystem(String alias, int numAnts, int numIterations, double preSuccessDistanceBiasCoefficient, double biasedExplorationCoefficient, double distanceBiasCoefficient, double globalPheromoneDecayCoefficient, double localPheromoneUpdateCoefficient, double[] pheromoneBounds, boolean elitist, double lookahead_coef, boolean cooperative, double death_pen) {
        this.alias = alias;
        this.numAnts = numAnts;
        this.numIterations = numIterations;
        bestCost = -1;
        this.preSuccessDistanceBiasCoefficient = preSuccessDistanceBiasCoefficient;
        this.biasedExplorationCoefficient = biasedExplorationCoefficient;
        this.distanceBiasCoefficient = distanceBiasCoefficient;
        this.globalPheromoneDecayCoefficient = globalPheromoneDecayCoefficient;
        this.pheromoneUpdateCoefficient = localPheromoneUpdateCoefficient;
        this.pheromoneBounds = pheromoneBounds;
        this.lookahead_coef = lookahead_coef;
        this.elitist = elitist;
        this.death_pen = death_pen;
        colony = new Ant[numAnts];
    }

    public static AlgoResult antSystemTSP(AntSystem algoObject, ArrayList<ArrayList<Integer>> adjMatrix) {
        return algoObject.applyAlgorithm(adjMatrix);
    }


    



/*    public static ArrayList<Integer> antColonyMethodTSPIterationTracking(int logInterval, int numAnts, int numIterations, double biasedExplorationCoefficient, double distanceBiasCoefficient, double globalPheromoneDecayCoefficient, double localPheromoneUpdateCoefficient, ArrayList<ArrayList<Integer>> adjMatrix) {
        AntSystem antColony = new AntSystem(numAnts, numIterations, biasedExplorationCoefficient, distanceBiasCoefficient, globalPheromoneDecayCoefficient, localPheromoneUpdateCoefficient, false, false, false);
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
    }*/


    AlgoResult applyAlgorithm(ArrayList<ArrayList<Integer>> adjMatrix) {
        adjacencyMatrix = adjMatrix;

        //initialPheromone = HelperFunctions.calculateCost(GreedyAlgorithim.greedyTSP(adjacencyMatrix), adjacencyMatrix, true);
        //NEW Initialization for Sparse Graphs (Average Choice Index)
        int sum, numViable;
        initialPheromone = 0;
        bestCost = -1;
        bestPath = null;
        currentDistanceBiasCoefficient = preSuccessDistanceBiasCoefficient;
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

        //Determine Bounding behavior
        if (pheromoneBounds != null && pheromoneBounds.length != 2) {
            pheromoneBounds = new double[2];
            boundsUpdating = true;
            UpdatePheromoneBounds(initialPheromone);
        }
        else boundsUpdating = false;

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
            }
            numProspectsList.add(numProspects);
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
        AlgoResult result = new AlgoResult(alias, bestCost, bestPath, iterationsTillSuccess);
        return result;
    }

    private void UpdatePheromoneBounds(double newMax) {
        pheromoneBounds[1] = newMax;
        pheromoneBounds[0] = pheromoneBounds[1]*(1-Math.pow((0.05),(1.0/ adjacencyMatrix.size())))/((adjacencyMatrix.size() /2.0-1)*Math.pow((0.05),(1.0/ adjacencyMatrix.size())));
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
        //bestPath = colony[0].path;
        for (int i = 0; i < numAnts; i++) {
            //System.out.printf(" %d", colony[i].cost);
            if (bestCost == -1 || (bestCost > colony[i].cost && colony[i].cost != -1)) {
                if (bestCost == -1 && colony[i].cost != -1) iterationsTillSuccess = i;
                bestCost = colony[i].cost;
                //Path is set to either the smallest successful path or the longest dead one
                if (bestPath == null || colony[i].path.size() >= bestPath.size())
                    bestPath = colony[i].path;
            }
        }
        if (bestCost != -1) {
            currentDistanceBiasCoefficient = distanceBiasCoefficient;
            if (pheromoneBounds != null && pheromoneBounds[1] != bestCost) UpdatePheromoneBounds(bestCost);
        }

        double updateSum = 0;
        //Global Update Pheromone Trails if best path was found
        if (bestCost != -1) {
            for (int i = 0; i < pheromoneMatrix.size(); i++) {
                for (int j = 0; j < pheromoneMatrix.size(); j++) {
                    //Ant Colony System Global Updates using best path

                    if (cooperative || elitist)
                        UpdatePheromone(i, j, globalPheromoneDecayCoefficient * (HelperFunctions.edgeInPath(bestPath, i, j) ? 1 / (double) bestCost : 0));
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
        }
        //If no path is found, use length to determine pheromone updates
        else {
            for (int i = 0; i < pheromoneMatrix.size(); i++) {
                for (int j = 0; j < pheromoneMatrix.size(); j++) {
                    //Ant Colony System Global Updates using best path
                    if (cooperative)
                        UpdatePheromone(i, j, globalPheromoneDecayCoefficient * (HelperFunctions.edgeInPath(bestPath, i, j) ? ((double) bestPath.size()) / adjacencyMatrix.size() : 0));
                    //Ant System Global Updates using any path visited this iteration
                    else {
                        updateSum = 0;
                        for (int k = 0; k < numAnts; k++) {
                            if (colony[k].cost != -1 && HelperFunctions.edgeInPath(colony[k].path, i, j))
                                updateSum += pheromoneUpdateCoefficient / (adjacencyMatrix.size() - bestPath.size());
                        }
                        UpdatePheromone(i, j, updateSum);

                    }
                }
            }
        }

        //Reset Ants (Keep Global best path
        for (int i = 0; i < numAnts; i++)
            colony[i].resetAnt();
    }

    private void UpdatePheromone(int i, int j, double updateAmount) {
        pheromoneMatrix.get(i).set(j, (1 - globalPheromoneDecayCoefficient) * pheromoneMatrix.get(i).get(j) + updateAmount);
        //Bound Pheromone
        if (pheromoneBounds != null) {
            if (pheromoneMatrix.get(i).get(j) > pheromoneBounds[1]) pheromoneMatrix.get(i).set(j, pheromoneBounds[1]);
            if (pheromoneMatrix.get(i).get(j) < pheromoneBounds[0]) pheromoneMatrix.get(i).set(j, pheromoneBounds[0]);
        }
    }

    public static AlgoResult antSystemMethodDefaultTSP(ArrayList<ArrayList<Integer>> adjMatrix) {
        return antSystemTSP(new AntSystem(10, 50, .9,.9, 2, .1, .1,null, false, 0, false,0), adjMatrix);
    }

    public static Function<ArrayList<ArrayList<Integer>>, AlgoResult> stageAntSystemTSP(AntSystem algorithmObject) {
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
            //Ignore non-existent Edges
            if (adjacencyMatrix.get(r).get(s) <= 0) return 0;
            //System.out.printf("Cost: %d\distanceBias: %f\tPheromone: %f\n", adjacencyMatrix.get(r).get(s), distanceBiasCoefficient, pheromoneMatrix.get(r).get(s));
            return Math.pow(1 / (double) adjacencyMatrix.get(r).get(s), currentDistanceBiasCoefficient) * pheromoneMatrix.get(r).get(s);
        }

        double calculateEdgeWeightIncludeFutureProspects(int r, int s) {
            if (adjacencyMatrix.get(r).get(s) <= 0) return 0;
            //System.out.printf("Cost: %d\t distanceBias: %f\tPheromone: %f\n", adjacencyMatrix.get(r).get(s), distanceBiasCoefficient, pheromoneMatrix.get(r).get(s));
            //System.out.println(((double) adjacencyMatrix.size() / numProspectsList.get(s)));
            return Math.pow(1 / (double) adjacencyMatrix.get(r).get(s), currentDistanceBiasCoefficient) * pheromoneMatrix.get(r).get(s) * Math.pow((double) adjacencyMatrix.size() / numProspectsList.get(s), lookahead_coef);
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
                        currentProbability = lookahead_coef > 0 ? calculateEdgeWeightIncludeFutureProspects(path.get(path.size() - 1), i) : calculateEdgeWeight(path.get(path.size() - 1), i);
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
                if (death_pen > 0) {
                    //System.out.println("Laying Death Trail:");
                    for (int i = 1; i < path.size(); i++) {
                        //System.out.printf("%f", pheromoneMatrix.get(i-1).get(i));
                        //Remove a portion of the failed paths pheromone reletive to the Death Penalty Coefficient and the length of the path)
                        pheromoneMatrix.get(i-1).set(i, pheromoneMatrix.get(i-1).get(i) * (1-death_pen*((matrixSize-path.size())/matrixSize)));
                        //System.out.printf("->%f\n", pheromoneMatrix.get(i-1).get(i));
                    }
                }

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