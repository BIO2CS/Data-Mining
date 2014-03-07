package HW3;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import Jama.Matrix;

public class YeastMCL {
    static String file3 = "/home/huiqiong/Desktop/yeast_undirected_metabolic.txt";
    static boolean isInteger = true;
    static ArrayList<Integer> nNodes;
    static ArrayList<ArrayList<Integer>> nEdges;
    static ArrayList<Integer> outputNodes1;
    static ArrayList<ArrayList<Integer>> integerClusters;

    public static void main(String[] args){
        String inputFile = file3;
        int numberOfNodes;
        Graph g;
        int i, j;
        numberOfNodes = readFromFile(inputFile);
        g = new Graph(numberOfNodes, true);
        for(i = 0; i < nNodes.size(); i++){
            g.addVertex(nNodes.get(i));
        }
        for(i = 0; i < nEdges.size(); i++){
            ArrayList<Integer> list = nEdges.get(i);
            g.addEdge(list.get(0), list.get(1));
        }
        System.out.println("In main: Number of Graph nodes " + g.getVertexNumber());
        System.out.println("\n************Edges***********");
        double[][] matrix = MCLAlgorithm(g, numberOfNodes);
        System.out.println("In main: size of matrix " + matrix.length + "\t" + matrix[0].length);
        writeGraph(inputFile, numberOfNodes);
        writeCluster(matrix, inputFile, numberOfNodes);
    }

    public static void writeGraph(String inputFile, int numberOfNodes){
        String[] paths = inputFile.split("/");
        int n = paths.length;
        String last = paths[n - 1];
        String fileName = last.substring(0, last.indexOf('.'));
        int i;
        String outputFileName = fileName + ".net";
        try{
            BufferedWriter bw = new BufferedWriter(new FileWriter(outputFileName));
            bw.write("*Vertices " + numberOfNodes + "\n");
            for(i = 0; i < numberOfNodes; i++){
                bw.write("   " + (i + 1) + " " + "\"");
                bw.write(String.valueOf(outputNodes1.get(i)));
                bw.write("\"" + "\t\t\t\t\t\t\t\t\t\t" + "0.0000\t0.0000\t0.5000\n");
            }
            bw.write("*Edges\n");
            for(i = 0; i < nEdges.size(); i++){
                ArrayList<Integer> list = nEdges.get(i);
                int index1 = outputNodes1.indexOf(list.get(0)) + 1;
                int index2 = outputNodes1.indexOf(list.get(1)) + 1;
                bw.write("  " + index1 + "  " + index2 + "  5 c Red\n");
            }
            bw.close();
        }catch(IOException e){
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public static void writeCluster(double[][] matrix, String inputFile, int numberOfNodes){
        String[] paths = inputFile.split("/");
        int n = paths.length;
        String last = paths[n - 1];
        String fileName = last.substring(0, last.indexOf('.'));
        int i, j, k;
        String outputFileName = fileName + ".clu";
        try{
            BufferedWriter bw = new BufferedWriter(new FileWriter(outputFileName));
            bw.write("*Vertices " + numberOfNodes + "\n");
            int counter = 0;
            ArrayList<ArrayList<Integer>> newClusters = new ArrayList<ArrayList<Integer>>();
            newClusters.add(integerClusters.get(0));
            newClusters.add(integerClusters.get(1));
            newClusters.add(integerClusters.get(2));
            newClusters.add(integerClusters.get(5));
            newClusters.add(integerClusters.get(6));
            ArrayList<Integer> oldList4 = integerClusters.get(3);
            ArrayList<Integer> oldList5 = integerClusters.get(4);
            oldList4.addAll(oldList5);
            newClusters.add(oldList4);
            System.out.println("Size of new Clusters " + newClusters.size());

            for(i = 0; i < numberOfNodes; i++){
                Integer d = outputNodes1.get(i);
                for(j = 0; j < newClusters.size(); j++){
                    ArrayList<Integer> list = newClusters.get(j);
                    if(list.contains(d)){
                        bw.write(String.valueOf(j + 1));
                        bw.write("\n");
                    }
                }
            }
            for(i = 0; i < newClusters.size(); i++){
                ArrayList<Integer> list = newClusters.get(i);
                System.out.println("Cluster " + (++counter) + ": size is " + list.size());
                ArrayList<Integer> printList = new ArrayList<Integer>();
                for(Integer v : list){
                    int d = outputNodes1.indexOf(v);
                    // System.out.print(d+1 + " ");
                    printList.add(d + 1);
                }
                Collections.sort(printList);
                for(Integer v : printList){
                    System.out.print(v + " ");
                }
                System.out.println();
            }
            System.out.println("FINAL integer cluster size " + newClusters.size());
            bw.close();
        }catch (IOException e){
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public static double[][] MCLAlgorithm(Graph g, int numberOfNodes){
        int i, j, k;
        int[][] matrixCopy = new int[numberOfNodes][numberOfNodes];
        for(i = 0; i < numberOfNodes; i++){
            for(j = 0; j < numberOfNodes; j++){
                matrixCopy[i][j] = g.matrix[i][j];
            }
        }
        for(i = 0; i < numberOfNodes; i++){
            for(j = 0; j < numberOfNodes; j++){
                if(i == j){
                    matrixCopy[i][j] = 1;
                    // System.out.print(g.matrix[i][j] + "\t");
                    // counter++;
                }
            }
            // System.out.println();
        }
        double[][] normalizedMatrix = normalizeMatrix(matrixCopy, numberOfNodes);
        Matrix nMatrix = new Matrix(normalizedMatrix);
        int counter = 0;
        for(i = 0; i < numberOfNodes; i++){
            for(j = 0; j < numberOfNodes; j++){
                if(g.matrix[i][j] == 1){
                    counter++;
                }
                // System.out.print(g.matrix[i][j] + "\t");
            }
            // System.out.println();
        }
        System.out.println("Number of ones in graph " + counter);
        counter = 0;
        while(true){
            counter++;
            Matrix expandedMatrix = nMatrix.times(nMatrix); 
            // inflation
            Matrix expandedCopy = expandedMatrix.copy();
            //every element is squared; need to normalize
            expandedMatrix = inflation(expandedMatrix, 1.205);
            Matrix inflatedMatrix = normalizeMatrixDouble(expandedMatrix, numberOfNodes);
            if(matrixDifference(nMatrix, inflatedMatrix) == 0){
                break;
            }
            else{
                nMatrix = inflatedMatrix.copy();
            }
        }
        // writeCluster(nMatrix);
        System.out.println("Total Iteration " + counter);
        HashMap<Integer, Integer> clusterInfo1 = new HashMap<Integer, Integer>();
        HashMap<String, Integer> clusterInfo2 = new HashMap<String, Integer>();
        counter = 0;
        integerClusters = new ArrayList<ArrayList<Integer>>();
        for(i = 0; i < numberOfNodes; i++){
            ArrayList<Integer> list = new ArrayList<Integer>();
            for(j = 0; j < numberOfNodes; j++){
                if(nMatrix.get(i, j) > 0.01){
                    list.add(g.nVertices.get(j));
                }
            }
            if(list.size() > 0){
                integerClusters.add(list);
                System.out.println("cluster " + i + "\'s size " + list.size());
                for(Integer d : list){
                    System.out.print(d + "\t");
                }
                System.out.println();
                counter++;
            }
        }
        System.out.println("Number of Clusters " + counter);
        return nMatrix.getArrayCopy();
    }

    public static Matrix inflation(Matrix expandedMatrix, double inflationCoefficient){
        int m = expandedMatrix.getRowDimension();
        int n = expandedMatrix.getColumnDimension();
        double[][] matrixCopy = new double[m][n];
        int i, j;
        for(i = 0; i < m; i++){
            for(j = 0; j < n; j++){
                matrixCopy[i][j] = Math.pow(expandedMatrix.get(i, j), inflationCoefficient);
            }
        }
        return new Matrix(matrixCopy);
    }

    public static int matrixDifference(Matrix A, Matrix B){
        double[][] copyA = A.getArrayCopy();
        double[][] copyB = B.getArrayCopy();
        if(copyA.length != copyB.length || copyA[0].length != copyB[0].length){
            return -1; // error
        }
        int i, j;
        int m = copyA.length;
        int n = copyA[0].length;
        for(i = 0; i < m; i++){
            for(j = 0; j < n; j++){
                if(copyA[i][j] - copyB[i][j] > 0.00001){
                    break;
                }
            }
            if(j == n){
                continue;
            }
            else{
                break;
            }
        }
        if(i == m){
            return 0;
        }
        else{
            return 1;
        }
    }

    public static Matrix normalizeMatrixDouble(Matrix matrixCopy, int number){
        double[][] ret = new double[number][number];
        int i, j;
        for(j = 0; j < number; j++){
            double sum = 0;
            for(i = 0; i < number; i++){
                sum += matrixCopy.get(i, j);
            }
            for(i = 0; i < number; i++){
                ret[i][j] = matrixCopy.get(i, j) / sum;
            }
        }
        return new Matrix(ret);
    }

    public static double[][] normalizeMatrix(int[][] matrixCopy, int number){
        double[][] normalizedMatrix = new double[number][number];
        int i, j;
        for(j = 0; j < number; j++){
            int sum = 0;
            for(i = 0; i < number; i++){
                sum += matrixCopy[i][j];
            }
            for(i = 0; i < number; i++){
                normalizedMatrix[i][j] = matrixCopy[i][j] * 1.0 / sum;
            }
        }
        return normalizedMatrix;
    }

    public static int readFromFile(String inputFile){
        String line = null;
        HashMap<String, Integer> records = new HashMap<String, Integer>();
        int numberOfNodes = 0;
        int counter = 0;
        nNodes = new ArrayList<Integer>();
        nEdges = new ArrayList<ArrayList<Integer>>();
        outputNodes1 = new ArrayList<Integer>();
        try{
            BufferedReader br = new BufferedReader(new FileReader(inputFile));
            while((line = br.readLine()) != null){
                // test.put(line, 1);
                String[] values;
                if(inputFile.endsWith("yeast_undirected_metabolic.txt")){
                    values = line.split("\t"); // tab-delimited
                }
                else{
                    values = line.split(" "); // space-delimited
                }
                records.put(values[0], 1);
                records.put(values[1], 1);
                ArrayList<Integer> list = new ArrayList<Integer>();
                Integer i1 = Integer.parseInt(values[0]);
                Integer i2 = Integer.parseInt(values[1]);
                list.add(i1);
                list.add(i2);
                nEdges.add(list);
                if(!outputNodes1.contains(i1)){
                    outputNodes1.add(i1);
                }
                if(!outputNodes1.contains(i2)){
                    outputNodes1.add(i2);
                }
            }
            numberOfNodes = records.size();
            Set<String> nodes = records.keySet();
            System.out.println("@@@@@@ number of nodes in the key set " + nodes.size());
            // System.out.println("Number of Lines for file 2 " + test.size());
            Iterator it = nodes.iterator();
            while(it.hasNext()){
                Integer value = Integer.parseInt((String) it.next());
                nNodes.add(value);
                // System.out.println(counter + "\t"+ it.next());
            }
            System.out.println("There are a total number of " + nNodes.size() + " nodes");
            System.out.println("Total number of edges " + nEdges.size());
            // System.out.println("\n*********HI\n");
            for (int i = 0; i < outputNodes1.size(); i++) {
                System.out.println(i + 1 + "\t" + outputNodes1.get(i));
            }
            // System.out.println("\n**********END OF HI\n");
            if (br != null) {
                br.close();
            }
        }catch(FileNotFoundException e){
            // TODO Auto-generated catch block
            e.printStackTrace();
        }catch(IOException e){
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return numberOfNodes;
    }
}