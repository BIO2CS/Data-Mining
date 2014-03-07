package Project2;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;

public class KMeans {
    
    static ArrayList<HashMap<Integer, ArrayList<Double>>> geneList = new ArrayList<HashMap<Integer, ArrayList<Double>>>();
    static HashMap<Integer, Integer> externalIndex = new HashMap<Integer, Integer>();
    static LinkedHashMap<Integer, ArrayList<Double>> reference = new LinkedHashMap<Integer, ArrayList<Double>>();
    static double epsilon = 0.00001;

    public static void main(String[] args){
        String file = "/home/huiqiong/Desktop/cho.txt";
        System.out.println("File Name is " + file);
        ReadFromFile(file);
        System.out.println("total lines of file is " + geneList.size());
        KMeans();
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
                // reference.put(Integer.parseInt(values[0]), gene);
            }
            br.close();
        }catch(FileNotFoundException e1){
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }catch (IOException e){
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public static void KMeans(){
        ArrayList<ArrayList<HashMap<Integer, ArrayList<Double>>>> partitionedLists = new ArrayList<ArrayList<HashMap<Integer, ArrayList<Double>>>>();
        int totalSize = geneList.size();
        ArrayList<HashMap<Integer, ArrayList<Double>>> geneListCopy = new ArrayList<HashMap<Integer, ArrayList<Double>>>(geneList);
        Collections.shuffle(geneListCopy);
        int kValue = 5;
        int partitionSize = totalSize / kValue;
        ArrayList<HashMap<Integer, ArrayList<Double>>> initialCentroids = new ArrayList<HashMap<Integer, ArrayList<Double>>>(kValue);

        int i = 0, j = 0;       
        for(i = 0; i < kValue; i++){
            HashMap<Integer, ArrayList<Double>> l = geneListCopy.get((i + 1) * partitionSize / 2);
            initialCentroids.add(l);
            ArrayList<HashMap<Integer, ArrayList<Double>>> pList = new ArrayList<HashMap<Integer, ArrayList<Double>>>();
            pList.add(l);
            partitionedLists.add(pList);
        }
        for(i = 0; i < totalSize; i++){
            double minDistance = Double.MAX_VALUE;
            HashMap<Integer, ArrayList<Double>> list = geneList.get(i);
            Integer key = (Integer) list.keySet().toArray()[0];
            ArrayList<Double> listValue = list.get(key);
            int partitionedListIndex = -1;
            for(j = 0; j < kValue; j++){               
                HashMap<Integer, ArrayList<Double>> centroid = initialCentroids.get(j);
                Integer centroidKey = (Integer) centroid.keySet().toArray()[0];
                ArrayList<Double> centroidValue = centroid.get(centroidKey);
                double d = EuclideanDistance(listValue, centroidValue);
                if(d - minDistance < epsilon){
                    minDistance = d;
                    partitionedListIndex = j;
                }
            }
            partitionedLists.get(partitionedListIndex).add(list);
        }
        int c = 0;
        for(ArrayList<HashMap<Integer, ArrayList<Double>>> pList : partitionedLists){
            System.out.println("size of pList " + (pList.size() - 1));
            c += pList.size() - 1;
        }
        System.out.println("C size " + c);      
        int counter = 0;
        boolean hasChanged = true;
        while(hasChanged){
            int centroidIndex = totalSize;
            counter++;
            hasChanged = false;
            partitionedLists = new ArrayList<ArrayList<HashMap<Integer, ArrayList<Double>>>>(kValue);
            for(i = 0; i < kValue; i++){
                ArrayList<HashMap<Integer, ArrayList<Double>>> pList = new ArrayList<HashMap<Integer, ArrayList<Double>>>();
                HashMap<Integer, ArrayList<Double>> centroid = initialCentroids.get(i);
                pList.add(centroid); // the first is always the centroid
                partitionedLists.add(pList);
            }
            for(i = 0; i < totalSize; i++){
                double minDistance = Double.MAX_VALUE;
                HashMap<Integer, ArrayList<Double>> list = geneList.get(i);
                Integer key = (Integer) list.keySet().toArray()[0];
                ArrayList<Double> listValue = list.get(key);
                int partitionedListIndex = -1;
                for(j = 0; j < kValue; j++){                   
                    HashMap<Integer, ArrayList<Double>> centroid = initialCentroids.get(j);
                    Integer centroidKey = (Integer) centroid.keySet().toArray()[0];
                    ArrayList<Double> centroidValue = centroid.get(centroidKey);
                    double d = EuclideanDistance(listValue, centroidValue);
                    if(d - minDistance < epsilon){
                        minDistance = d;
                        partitionedListIndex = j;
                    }
                }
                partitionedLists.get(partitionedListIndex).add(list);
            }

            ArrayList<HashMap<Integer, ArrayList<Double>>> newCentroids = new ArrayList<HashMap<Integer, ArrayList<Double>>>(kValue);
            for(i = 0; i < kValue; i++){
                HashMap<Integer, ArrayList<Double>> centroid = new HashMap<Integer, ArrayList<Double>>();
                ArrayList<Double> centroidValue = new ArrayList<Double>();
                newCentroids.add(centroid);
                ArrayList<HashMap<Integer, ArrayList<Double>>> list = partitionedLists.get(i);
                HashMap<Integer, ArrayList<Double>> first = list.get(0);
                Integer key = (Integer) first.keySet().toArray()[0];
                ArrayList<Double> firstValue = first.get(key);
                int size = firstValue.size();
                for(j = 0; j < size; j++){
                    Double d = 0.0;
                    for(int k = 1; k < list.size(); k++){
                        HashMap<Integer, ArrayList<Double>> dataList = list.get(k);
                        Integer dataKey = (Integer) dataList.keySet().toArray()[0];
                        ArrayList<Double> dataValue = dataList.get(dataKey);
                        d += dataValue.get(j);
                    }
                    centroidValue.add(d / list.size());
                }
                centroid.put(centroidIndex++, centroidValue);
            }
            // compare newCentroids before and after. It no change, done!!!
            Integer k;
            for(i = 0; i < kValue; i++){
                HashMap<Integer, ArrayList<Double>> mapA = newCentroids.get(i);
                k = (Integer) mapA.keySet().toArray()[0];
                ArrayList<Double> a = mapA.get(k);
                HashMap<Integer, ArrayList<Double>> mapB = partitionedLists.get(i).get(0);
                k = (Integer) mapB.keySet().toArray()[0];
                ArrayList<Double> b = mapB.get(k);
                for(j = 0; j < a.size(); j++){
                    if(a.get(j) - b.get(j) > epsilon){
                        break;
                    }
                }
                if(j < a.size()){
                    break;
                }
            }
            if(i < kValue){
                hasChanged = true;
                initialCentroids = new ArrayList<HashMap<Integer, ArrayList<Double>>>(newCentroids);
            }
        }
        int clusterIndexSize = 0;
        HashMap<Integer, Integer> clusterIndex = new HashMap<Integer, Integer>();
        for(i = 0; i < partitionedLists.size(); i++){
            ArrayList<HashMap<Integer, ArrayList<Double>>> cluster = partitionedLists.get(i);
            clusterIndexSize += cluster.size() - 1;
            for(j = 1; j < cluster.size(); j++){
                HashMap<Integer, ArrayList<Double>> list = cluster.get(j);
                Integer listKey = (Integer) list.keySet().toArray()[0];
                ArrayList<Double> listValue = list.get(listKey);
                clusterIndex.put(listKey, i + 1);
            }
        }
        ArrayList<Integer> orderedIndex = new ArrayList<Integer>();
        Iterator<Integer> it = clusterIndex.keySet().iterator();
        while(it.hasNext()){
            orderedIndex.add(it.next());
        }
        Collections.sort(orderedIndex);
        double jCoefficient = JCoefficient(externalIndex, clusterIndex);
        System.out.println("\nJaccard Coefficient " + jCoefficient);
        ArrayList<ArrayList<ArrayList<Double>>> partitionedCopy = new ArrayList<ArrayList<ArrayList<Double>>>();
        for(i = 0; i < partitionedLists.size(); i++){
            ArrayList<ArrayList<Double>> clusterCopy = new ArrayList<ArrayList<Double>>();
            partitionedCopy.add(clusterCopy);
            ArrayList<HashMap<Integer, ArrayList<Double>>> cluster = partitionedLists.get(i);
            for(j = 1; j < cluster.size(); j++){
                HashMap<Integer, ArrayList<Double>> list = cluster.get(j);
                Integer listKey = (Integer) list.keySet().toArray()[0];
                clusterCopy.add(list.get(listKey));
            }
        }
        int cc = 0;
        for (ArrayList<ArrayList<Double>> lists : partitionedCopy) {
            cc += lists.size();
        }
        // System.out.println("******partitionedCopy's size " + cc );
        double sCoefficient = SilhouetteCoefficient(partitionedCopy);
        System.out.println("\nSilhouette Coefficient " + sCoefficient);      
        System.out.println("Total Iterations " + counter);        
    }
    
    public static int listCompare(ArrayList<Double> l1, ArrayList<Double> l2){       
        int size = l1.size();
        int i;
        double[] diff = new double[size];
        for(i = 0; i < size; i++){
            Double d1 = l1.get(i);
            Double d2 = l2.get(i);
            if(d1 - d2 > epsilon){
                diff[i] = 1;
            }
            else{
                diff[i] = 0;
            }
        }
        for(i = 0; i < size; i++){
            if(diff[i] == 1){
                return 1;
            }
        }
        return 0;
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