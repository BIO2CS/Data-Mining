package Project2;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class DBScan {

    static ArrayList<HashMap<Integer, ArrayList<Double>>> geneList = new ArrayList<HashMap<Integer, ArrayList<Double>>>();
    // <geneID, ground truth cluster>
    static HashMap<Integer, Integer> externalIndex = new HashMap<Integer, Integer>();
    static ArrayList<HashMap<Integer, ArrayList<Double>>> nextCluster;
    static ArrayList<HashMap<Integer, ArrayList<Double>>> noisePoints = new ArrayList<HashMap<Integer, ArrayList<Double>>>();
    // for each gene, record exps, visited
    static HashMap<ArrayList<Double>, Boolean> visitedMap = new HashMap<ArrayList<Double>, Boolean>();
    // a collection of all expanded clusters
    static ArrayList<ArrayList<HashMap<Integer, ArrayList<Double>>>> clusters = new ArrayList<ArrayList<HashMap<Integer, ArrayList<Double>>>>();
    static double epsilon = 0.00001;

    public static void main(String[] args){
        String file = "/home/huiqiong/Desktop/cho.txt";
        System.out.println("File Name is " + file);
        ReadFromFile(file);
        System.out.println("total lines of file is " + geneList.size());
        DBScan();
    }

    public static void ReadFromFile(String fileName){
        BufferedReader br = null;
        try{
            br = new BufferedReader(new FileReader(fileName));
            String line = null;
            while((line = br.readLine()) != null){
                HashMap<Integer, ArrayList<Double>> geneEntry = new HashMap<Integer, ArrayList<Double>>();
                ArrayList<Double> gene = new ArrayList<Double>();
                String[] values = line.split("\t");
                if(Integer.parseInt(values[1]) > -1){
                    externalIndex.put(Integer.parseInt(values[0]), Integer.parseInt(values[1]));
                    for(int i = 2; i < values.length; i++){
                        gene.add(Double.parseDouble(values[i]));
                    }
                    geneEntry.put(Integer.parseInt(values[0]), gene);
                    geneList.add(geneEntry);
                }
            }
            br.close();
        }catch(FileNotFoundException e1){
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }catch(IOException e){
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public static void DBScan(){
        ArrayList<HashMap<Integer, ArrayList<Double>>> geneListCopy = new ArrayList<HashMap<Integer, ArrayList<Double>>>(geneList);
        int minPoints = 5; // or 10
        int size = geneListCopy.size();
        int i, j, k;
        double[] epsPlot = new double[size];
        for(i = 0; i < size; i++){
            double[] temp = new double[size - 1];
            for(j = 0, k = 0; j < size; j++){
                if(j != i){
                    HashMap<Integer, ArrayList<Double>> entry1 = geneListCopy.get(i);
                    HashMap<Integer, ArrayList<Double>> entry2 = geneListCopy.get(j);
                    Integer key1 = (Integer) entry1.keySet().toArray()[0];
                    Integer key2 = (Integer) entry2.keySet().toArray()[0];
                    ArrayList<Double> value1 = entry1.get(key1);
                    ArrayList<Double> value2 = entry2.get(key2);
                    double d = EuclideanDistance(value1, value2);
                    temp[k] = d;
                    k++;
                }
            }
            java.util.Arrays.sort(temp);
            epsPlot[i] = temp[minPoints - 1];
        }
        java.util.Arrays.sort(epsPlot);
        double epsDistance = 0.0;
        // DecimalFormat df = new DecimalFormat("#.###");
        for(i = 0; i < size; i++){
            // System.out.println(df.format(epsPlot[i]) + "\t");
            epsDistance += epsPlot[i];
        }
        System.out.println("\nepsDistance value " + epsDistance / epsPlot.length);
        System.out.println("minPts value " + minPoints);
        double eps = epsDistance / epsPlot.length;
        for(i = 0; i < size; i++){
            HashMap<Integer, ArrayList<Double>> entry = geneListCopy.get(i);
            Integer key = (Integer) entry.keySet().toArray()[0];
            ArrayList<Double> list = entry.get(key);
            visitedMap.put(list, false);
        }
        // how many points visited
        int counter1 = 0;
        // how many non-noise points
        int counter2 = 0;
        int counter = 0;
        for(i = 0; i < size; i++){
            counter++;
            HashMap<Integer, ArrayList<Double>> entry = geneListCopy.get(i);
            Integer key = (Integer) entry.keySet().toArray()[0];
            ArrayList<Double> list = entry.get(key);
            if(visitedMap.get(list) == false){
                counter1++;
                visitedMap.put(list, true);
                // get all neighbor points
                ArrayList<HashMap<Integer, ArrayList<Double>>> neighborPts = regionQuery(
                        geneListCopy, list, eps);
                if(neighborPts.size() < minPoints){
                    HashMap<Integer, ArrayList<Double>> newEntry = new HashMap<Integer, ArrayList<Double>>();
                    newEntry.put(key, list);
                    noisePoints.add(newEntry);
                }
                else{
                    counter2++;
                    HashMap<Integer, ArrayList<Double>> newEntry = new HashMap<Integer, ArrayList<Double>>();
                    newEntry.put(key, list);
                    nextCluster = new ArrayList<HashMap<Integer, ArrayList<Double>>>();
                    clusters.add(nextCluster);
                    nextCluster.add(newEntry);
                    ConcurrentLinkedQueue<HashMap<Integer, ArrayList<Double>>> nei = new ConcurrentLinkedQueue<HashMap<Integer, ArrayList<Double>>>();
                    for(HashMap<Integer, ArrayList<Double>> a : neighborPts){
                        nei.add(a);
                    }
                    for(HashMap<Integer, ArrayList<Double>> a : nei){
                        Integer kk = (Integer) a.keySet().toArray()[0];
                        ArrayList<Double> l = a.get(kk);
                        if(visitedMap.get(l) == false){
                            visitedMap.put(l, true);
                            ArrayList<HashMap<Integer, ArrayList<Double>>> newNeighborPts = regionQuery(geneList, l, eps);
                            if(newNeighborPts.size() >= minPoints){
                                // neighborPts.add(l);
                                for(HashMap<Integer, ArrayList<Double>> ll : newNeighborPts){
                                    if(!nei.contains(ll)){
                                        nei.add(ll);
                                    }
                                }
                            }
                        }
                        boolean isMember = false;
                        ArrayList<ArrayList<Double>> tempBuffer = new ArrayList<ArrayList<Double>>();
                        for(ArrayList<HashMap<Integer, ArrayList<Double>>> c : clusters){
                            for(HashMap<Integer, ArrayList<Double>> e : c){
                                kk = (Integer) e.keySet().toArray()[0];
                                ArrayList<Double> v = e.get(kk);
                                tempBuffer.add(v);
                            }
                            if(tempBuffer.contains(l)){
                                isMember = true;
                                break;
                            }
                        }
                        if(isMember == false){
                            nextCluster.add(a);
                        }
                    }
                }
            }
        }
        // int cc = 0;
        // System.out.println("\nnumber of clusters " + clusters.size());
        // for (ArrayList<HashMap<Integer, ArrayList<Double>>> cluster :
        // clusters) {
        // cc += cluster.size();
        // }
        // System.out.println("***Total size of clusters " + cc);
        // System.out.println("COunter2 " + counter2);
        // System.out.println("\nnumber of noise points " + noisePoints.size());
        // System.out.println("COunter " + counter1);
        // System.out.println("\n\nCOunter " + counter);

        HashMap<Integer, Integer> clusterIndex = new HashMap<Integer, Integer>();
        for(i = 0; i < clusters.size(); i++){
            ArrayList<HashMap<Integer, ArrayList<Double>>> cluster = clusters.get(i);
            for(j = 1; j < cluster.size(); j++){
                HashMap<Integer, ArrayList<Double>> list = cluster.get(j);
                Integer listKey = (Integer) list.keySet().toArray()[0];
                ArrayList<Double> listValue = list.get(listKey);
                clusterIndex.put(listKey, i + 1);
            }
        }
        double jCoefficient = JCoefficient(externalIndex, clusterIndex);
        System.out.println("\nJaccard Coefficient " + jCoefficient);
        ArrayList<ArrayList<ArrayList<Double>>> partitionedCopy = new ArrayList<ArrayList<ArrayList<Double>>>();
        for(i = 0; i < clusters.size(); i++){
            ArrayList<ArrayList<Double>> clusterCopy = new ArrayList<ArrayList<Double>>();
            partitionedCopy.add(clusterCopy);
            ArrayList<HashMap<Integer, ArrayList<Double>>> cluster = clusters.get(i);
            for(j = 1; j < cluster.size(); j++){
                HashMap<Integer, ArrayList<Double>> list = cluster.get(j);
                Integer listKey = (Integer) list.keySet().toArray()[0];
                clusterCopy.add(list.get(listKey));
            }
        }
        int cc = 0;
        for(ArrayList<ArrayList<Double>> lists : partitionedCopy){
            cc += lists.size();
        }
        double sCoefficient = SilhouetteCoefficient(partitionedCopy);
        System.out.println("Silhouette Coefficient " + sCoefficient);
        System.out.println("\n\n************After Clustering***********");
        for(i = 0; i < clusters.size(); i++){
            ArrayList<HashMap<Integer, ArrayList<Double>>> cluster = clusters.get(i);
            size = 0;
            for(j = 1; j < cluster.size(); j++){
                HashMap<Integer, ArrayList<Double>> list = cluster.get(j);
                size++;
                Integer listKey = (Integer) list.keySet().toArray()[0];
                ArrayList<Double> expression = list.get(listKey);
                System.out.print(listKey + "\t" + (i + 1) + "\t");
                for(Double d : expression){
                    System.out.print(d + "\t");
                }
                System.out.println();
            }
            System.out.println("Cluster " + (i + 1) + " has " + size + " genes");
        }
    }

    public static ArrayList<HashMap<Integer, ArrayList<Double>>> regionQuery(
            ArrayList<HashMap<Integer, ArrayList<Double>>> geneListCopy, ArrayList<Double> list, double eps){
        ArrayList<HashMap<Integer, ArrayList<Double>>> result = new ArrayList<HashMap<Integer, ArrayList<Double>>>();
        int size = geneListCopy.size();
        for(int i = 0; i < size; i++){
            HashMap<Integer, ArrayList<Double>> entry = geneListCopy.get(i);
            Integer key = (Integer) entry.keySet().toArray()[0];
            ArrayList<Double> neighborList = entry.get(key);
            if(EuclideanDistance(list, neighborList) - eps < epsilon){
                HashMap<Integer, ArrayList<Double>> newEntry = new HashMap<Integer, ArrayList<Double>>();
                newEntry.put(key, neighborList);
                result.add(newEntry);               
            }
        }
        return result;
    }

    public static double EuclideanDistance(ArrayList<Double> l1, ArrayList<Double> l2){
        if(l1.size() != l2.size()){
            System.err.print("erro in input size\n");
        }
        int size = l1.size();
        double sum = 0;
        for(int i = 0; i < size; i++){
            sum += (l1.get(i) - l2.get(i)) * (l1.get(i) - l2.get(i));
        }
        return Math.sqrt(sum);
    }

    public static double JCoefficient(HashMap<Integer, Integer> externalIndex, HashMap<Integer, Integer> clusterIndex){
        double efficient = 0.0;
        int size = clusterIndex.size();
        double[][] groundTruth = new double[size][size];
        double[][] clustering = new double[size][size];
        int SS = 0;
        int DD = 0;
        int SD = 0;
        int DS = 0;
        int i, j;
        for(i = 0; i < size; i++){
            for(j = 0; j < size; j++){
                if(externalIndex.get(i) == externalIndex.get(j)){
                    groundTruth[i][j] = 1;
                }
                else{
                    groundTruth[i][j] = 0;
                }
            }
        }
        for(i = 0; i < size; i++){
            for(j = 0; j < size; j++){
                if(clusterIndex.get(i) == clusterIndex.get(j)){
                    clustering[i][j] = 1;
                }
                else{
                    clustering[i][j] = 0;
                }
            }
        }
        for(i = 0; i < size; i++){
            for(j = 0; j < size; j++){
                if(groundTruth[i][j] == 1 && clustering[i][j] == 1){
                    SS++;
                }
                else if(groundTruth[i][j] == 0 && clustering[i][j] == 0){
                    DD++;
                }
                else if(groundTruth[i][j] == 1 && clustering[i][j] == 0){
                    DS++;
                }
                else{
                    SD++;
                }
            }
        }       
        efficient = 1.0 * SS / (SS + SD + DS);
        return efficient;
    }

    public static double SilhouetteCoefficient(ArrayList<ArrayList<ArrayList<Double>>> partitionedLists){
        double coefficient = 0.0;
        int size = partitionedLists.size();
        int count = 0;
        for(ArrayList<ArrayList<Double>> l : partitionedLists){
            count += l.size();
        }
        double[][] ab = new double[count][2];
        int i, j, k, h;    
        int index = 0;
        for(i = 0; i < size; i++){
            ArrayList<ArrayList<Double>> cluster = partitionedLists.get(i);
            double sumA = 0.0;
            ArrayList<Double> list1 = null;
            for(j = 0; j < cluster.size(); j++){
                list1 = cluster.get(j);
                for(k = 0; k < cluster.size(); k++){
                    ArrayList<Double> list2 = cluster.get(k);
                    sumA += EuclideanDistance(list1, list2);
                }
                ab[index++][0] = sumA / cluster.size();
            }
        }
        index = 0;
        for(i = 0; i < size; i++){
            ArrayList<ArrayList<Double>> cluster = partitionedLists.get(i);
            for(j = 0; j < cluster.size(); j++){
                ArrayList<Double> list1 = cluster.get(j);
                double minD = Double.MAX_VALUE;
                for(h = 0; h < size; h++){
                    if(i != h){
                        Double d = 0.0;
                        ArrayList<ArrayList<Double>> otherCluster = partitionedLists.get(h);
                        for(ArrayList<Double> l : otherCluster){
                            d += EuclideanDistance(list1, l);
                        }
                        if(d / otherCluster.size() - minD < epsilon){
                            minD = d;
                        }
                    }
                }
                ab[index++][1] = minD;
            }
        }
        double sumS = 0.0;
        for(i = 0; i < size; i++){
            sumS += (ab[i][0] < ab[i][1] ? (1 - ab[i][0] / ab[i][1]) : (ab[i][0] / ab[i][1] - 1));
        }
        coefficient = sumS / size;
        return coefficient;
    }
}