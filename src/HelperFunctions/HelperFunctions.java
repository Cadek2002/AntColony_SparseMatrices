package HelperFunctions;
import java.io.*;
import java.util.*;
import java.util.stream.IntStream;

import static java.lang.Integer.parseInt;

public class HelperFunctions {

    //File Input
    public static ArrayList<ArrayList<Integer>> readAdjMatrixFile(String inputFile, ArrayList<Integer> verticesMap) {
        try {
            Scanner matrixScanner = new Scanner(new File(inputFile));
            matrixScanner.useDelimiter(System.lineSeparator());
            Scanner rowScanner;
            ArrayList<ArrayList<Integer>> matrix = new ArrayList<>();
            //Import Cycle Set into 2d Vector
            while (matrixScanner.hasNextLine()) {
                matrix.add(new ArrayList<>());
                rowScanner = new Scanner(matrixScanner.nextLine());
                while (rowScanner.hasNextInt()) {
                    matrix.get(matrix.size()-1).add(verticesMap == null ? rowScanner.nextInt() : verticesMap.indexOf(rowScanner.nextInt()));
                }
            }
            matrixScanner.close();
            return matrix;
        } catch (FileNotFoundException e) {
            System.out.println(e.getMessage());
            return null;
        }
    }

    public static ArrayList<ArrayList<Integer>> readVLSIAdjacencyList(String fileName, boolean isDirected) {
        ArrayList<ArrayList<Integer>> adjacencyMatrix = new ArrayList<>();
        int node;
        int neighbor;
        int cost;

        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            String line;
            boolean readingNodes = false;
            int dimension = 0;

            while ((line = br.readLine()) != null) {
                if (line.startsWith("DIMENSION")) {


                    dimension = Integer.parseInt(line.split(":")[1].trim())+1;
                    System.out.printf("File: %s Dimension: %d\n",fileName, dimension);
                    //System.out.println(dimension);
                    for (int i = 0; i < dimension; i++) {
                        adjacencyMatrix.add(new ArrayList<>(Collections.nCopies(dimension, 0)));
                    }
                }

                if (line.equals("NODE_COORD_SECTION")) {
                    readingNodes = true;
                    continue;
                }

                if (readingNodes) {
                    if (line.equals("EOF")) {
                        break;
                    }

                    String[] parts = line.split("\\s+");
                    node = Integer.parseInt(parts[0]);
                    neighbor = Integer.parseInt(parts[1]);
                    cost = Integer.parseInt(parts[2]);
                    adjacencyMatrix.get(node).set(neighbor, cost);

                    if (!isDirected) {
                        adjacencyMatrix.get(neighbor).set(node, cost);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return adjacencyMatrix;
    }

    //File Output
    public static void exportFile(ArrayList<ArrayList<Integer>> matrix, String outputName, ArrayList<Integer> verticesMap) {
        File output = new File(outputName);
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(output));
            for (ArrayList<Integer> row : matrix) {
                for (Integer i : row) writer.write(String.format("%c ", verticesMap == null ? (i >= 0 ? i+48 : '/') : verticesMap.get(i)));
                writer.newLine();
            }
            writer.close();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }
    public static void printMatrix(ArrayList<ArrayList<Integer>> adjMatrix) {
        System.out.println("\n");
        for (ArrayList<Integer> row : adjMatrix) {
            for (Integer i : row) System.out.printf("%4d", i);
            System.out.println();
        }
    }

    public static void printMatrix(ArrayList<ArrayList<Double>> matrix, int pre) {
        System.out.println("\n");
        for (ArrayList<Double> row : matrix) {
            for (Double i : row) System.out.printf("%6f", i);
            System.out.println();
        }
    }

    public static ArrayList<ArrayList<Integer>> createMatrix(int size, int min, int max, double lambda, String outputName) {
        //initiate matrix
        ArrayList<ArrayList<Integer>> output = new ArrayList<>(size);

        for (int i = 0; i < size; i++) {
            output.add(new ArrayList<>());
            //populate the list with nulls, setting the diagonals to 0
            for(int j = 0; j < size; j++) output.get(i).add(i == j ? 0 : -1);
        }

        ArrayList<Integer> sequence = new ArrayList<>(IntStream.range(0, size).boxed().toList());
        Collections.shuffle(sequence);
        int next, x, y, element, offset;

        for (int i = 0; i < size; i++) {
            next = ( i+1 < size ? i+1 : 0);
            output.get(sequence.get(i)).set(sequence.get(next), (int)(Math.random() * (max-min) + min));
        }
        int elements = (int)((size * (size-1)) * lambda);

        ArrayList<Integer> sequenceFull = new ArrayList<>(IntStream.range(0, size*(size-1)).boxed().toList());
        Collections.shuffle(sequenceFull);

        while (--elements >= 0) {
            element = sequenceFull.get(elements);
            offset = (element / (size))+1;
            x = (element % size) + offset - ((((element % size) + offset) >= size) ? size : 0);
            y = (element / size) + ((((element % size) + offset) >= size) ? 1 : 0);
            output.get(x).set(y, (int)(Math.random() * (max-min) + min));
        }
        exportFile(output, outputName, null);
        return output;
    }
    //Find Cycles
    public static void findAllHamiltonianCycles(ArrayList<ArrayList<Integer>> adjMatrix, LinkedHashSet<Integer> path, int last, int first, ArrayList<LinkedHashSet<Integer>> cycles) {
        if (last == first && path.size() == adjMatrix.size()) {
            cycles.add(path);
            return;
        }
        path.add(last);
        for (int i = 0; i < adjMatrix.size(); i++)
            if (adjMatrix.get(last).get(i) > 0 && ((i == first && path.size() == adjMatrix.size()) || !path.contains(i))) {
                findAllHamiltonianCycles(adjMatrix, new LinkedHashSet<>(path), i, first, cycles);
            }
    }

    public static ArrayList<ArrayList<Integer>> findAllHamiltonianCycles(ArrayList<ArrayList<Integer>> adjMatrix) {
        ArrayList<LinkedHashSet<Integer>> cycles = new ArrayList<>();
        findAllHamiltonianCycles(adjMatrix, new LinkedHashSet<>(), 0, 0, cycles);
        ArrayList<ArrayList<Integer>> fullList = new ArrayList<>();

        //Convert to Arraylist
        for (LinkedHashSet<Integer> cycle: cycles)
            fullList.add(new ArrayList<>(cycle));
        return fullList;
    }
    // Function to calculateCost
    public static int calculateCost(ArrayList<Integer> path, ArrayList<ArrayList<Integer>> adjMatrix, boolean cycle) {
        if (path.size() <= 2 || path.size() < adjMatrix.size()) return -1;
        int sum = 0;
        for (int i = 0; i < path.size() - 1; i++) {
            sum += adjMatrix.get(path.get(i)).get(path.get(i + 1));
        }
       if (cycle && path.size() == adjMatrix.size()) sum += adjMatrix.get(path.get(path.size() - 1)).get(path.get(0));
        return sum;
    }
    public static boolean edgeInPath(ArrayList<Integer> path, int x, int y) {
        for (int i = 0; i < path.size(); i++) {
            if (path.get(i) == x)
                return y == path.get(i == path.size()-1 ? path.get(0) : path.get(i+1));
        }
        return false;
    }
    public static String getStringCSV(ArrayList<Integer> array) {
        StringBuilder string = new StringBuilder();
        for (int i : array) {
            string.append(i);
            string.append(' ');
        }
        return string.toString();
    }
}
