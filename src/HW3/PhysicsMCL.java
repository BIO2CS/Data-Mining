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

public class PhysicsMCL {

    static String file2 = "/home/huiqiong/Desktop/physics_collaboration_net.txt";
    static boolean isInteger = true;
    static ArrayList<String> sNodes;
    static ArrayList<ArrayList<String>> sEdges;
    static ArrayList<String> outputNodes2;
    static ArrayList<ArrayList<String>> stringClusters;

    public static void main(String[] args){
        String inputFile = file2;
        int numberOfNodes;
        Graph g;
        int i, j;
        numberOfNodes = readFromFile(inputFile);
        isInteger = false;
        g = new Graph(numberOfNodes, false);
        for(i = 0; i < sNodes.size(); i++){
            g.addVertex(sNodes.get(i));
        }
        for(i = 0; i < sEdges.size(); i++){
            ArrayList<String> list = sEdges.get(i);
            g.addEdge(list.get(0), list.get(1));
        }
        System.out.println("In main: Number of Graph nodes " + g.getVertexNumber());
        // System.out.println("In main: Number of Edges " + g.getEdgeNumber());
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
                bw.write(outputNodes2.get(i));
                bw.write("\"" + "\t\t\t\t\t\t\t\t\t\t" + "0.0000\t0.0000\t0.5000\n");
            }
            bw.write("*Edges\n");
            for(i = 0; i < sEdges.size(); i++){
                ArrayList<String> list = sEdges.get(i);
                int index1 = outputNodes2.indexOf(list.get(0)) + 1;
                int index2 = outputNodes2.indexOf(list.get(1)) + 1;
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
        int counter = 0;
        String outputFileName = fileName + ".clu";
        try{
            BufferedWriter bw = new BufferedWriter(new FileWriter(outputFileName));
            bw.write("*Vertices " + numberOfNodes + "\n");
            ArrayList<String> list1 = stringClusters.get(0);
            System.out.println("old list1 " + list1.size());
            ArrayList<String> list5 = stringClusters.get(4);
            System.out.println("old list5 " + list5.size());
            ArrayList<String> list3 = stringClusters.get(2);
            System.out.println("old list3 " + list3.size());
            ArrayList<String> list4 = stringClusters.get(3);
            System.out.println("old list4 " + list4.size());
            ArrayList<String> list6 = stringClusters.get(5);
            System.out.println("old list6 " + list6.size());
            ArrayList<String> list7 = stringClusters.get(6);
            System.out.println("old list7 " + list7.size());
            ArrayList<String> newList1 = new ArrayList<String>();
            newList1.addAll(list1);
            newList1.addAll(list5);
            System.out.println("size of newlist1 " + newList1.size());
            ArrayList<String> newList3 = new ArrayList<String>();
            newList3.addAll(list3);
            newList3.addAll(list4);
            System.out.println("size of newlist3 " + newList3.size());
            ArrayList<String> newList6 = new ArrayList<String>();
            newList6.addAll(list6);
            newList6.addAll(list7);
            System.out.println("size of newlist6 " + newList6.size());
            ArrayList<ArrayList<String>> newClusters = new ArrayList<ArrayList<String>>();
            newClusters.add(newList1);
            newClusters.add(newList3);
            newClusters.add(newList6);
            newClusters.add(stringClusters.get(1));

            for(i = 0; i < numberOfNodes; i++){
                String s = outputNodes2.get(i);
                for(j = 0; j < newClusters.size(); j++){
                    ArrayList<String> list = newClusters.get(j);
                    if(list.contains(s)){
                        bw.write(String.valueOf(j + 1));
                        bw.write("\n");
                    }
                }
            }       
            for(i = 0; i < newClusters.size(); i++){
                ArrayList<String> list = newClusters.get(i);
                System.out.println("Cluster " + (++counter) + ": size is " + list.size());
                ArrayList<Integer> printList = new ArrayList<Integer>();
                for(String ss : list){
                    int d = outputNodes2.indexOf(ss);
                    // System.out.print(d+1 + " ");
                    printList.add(d + 1);
                }
                Collections.sort(printList);
                for(Integer v : printList){
                    System.out.print(v + " ");
                }
                System.out.println();            
            }
            System.out.println("String integer cluster size " + newClusters.size());
            bw.close();
        }catch(IOException e){
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
            expandedMatrix = inflation(expandedMatrix, 1.305);
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
        stringClusters = new ArrayList<ArrayList<String>>();
        for(i = 0; i < numberOfNodes; i++){
            ArrayList<String> list = new ArrayList<String>();
            for(j = 0; j < numberOfNodes; j++){
                if(nMatrix.get(i, j) > 0.01){
                    list.add(g.sVertices.get(j));
                }
            }
            if(list.size() > 0){
                stringClusters.add(list);
                System.out.println("\ncluster " + i + "\'s size " + list.size());
                for(String s : list){
                    int d = outputNodes2.indexOf(s);
                    System.out.print(d + 1 + " ");
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
        if (copyA.length != copyB.length || copyA[0].length != copyB[0].length) {
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
        sNodes = new ArrayList<String>();
        sEdges = new ArrayList<ArrayList<String>>();
        outputNodes2 = new ArrayList<String>();
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
                ArrayList<String> list = new ArrayList<String>();
                list.add(values[0]);
                list.add(values[1]);
                sEdges.add(list);
                if(!outputNodes2.contains(values[0])){
                    outputNodes2.add(values[0]);
                }
                if(!outputNodes2.contains(values[1])){
                    outputNodes2.add(values[1]);
                }
            }
            numberOfNodes = records.size();
            Set<String> nodes = records.keySet();
            System.out.println("@@@@@@ number of nodes in the key set " + nodes.size());
            // System.out.println("Number of Lines for file 2 " + test.size());
            Iterator it = nodes.iterator();
            while(it.hasNext()){
                sNodes.add((String) it.next());
            }
            if(sNodes != null){
                System.out.println("There are a total number of " + sNodes.size() + " nodes");
                System.out.println("Total number of edges " + sEdges.size());
                for(int i = 0; i < outputNodes2.size(); i++){
                    System.out.println(i + 1 + "\t" + outputNodes2.get(i));
                }
            }
            if(br != null){
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