package Project2;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

public class Hierarchical {
    
    static ArrayList<HashMap<Integer, ArrayList<Double>>> geneList = new ArrayList<HashMap<Integer, ArrayList<Double>>>();
    static HashMap<Integer, Integer> externalIndex = new HashMap<Integer, Integer>();
    static LinkedHashMap<Integer, ArrayList<Double>> reference = new LinkedHashMap<Integer, ArrayList<Double>>();
    static double epsilon = 0.00001;

    public static void main(String[] args){       
        String file = "/home/huiqiong/Desktop/cho.txt";
        System.out.println("File Name is " + file);
        ReadFromFile(file);
        System.out.println("total lines of file is " + geneList.size());
        Hierarchical();
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

    public static void Hierarchical(){        
        int count = 0;        
        ArrayList<HashMap<Integer, ArrayList<Double>>> geneListCopy = new ArrayList<HashMap<Integer, ArrayList<Double>>>(geneList);
        int size = geneListCopy.size();
        double[][] distanceMatrix = new double[size][size];
        int i, j;
        for(i = 0; i < size; i++){
            HashMap<Integer, ArrayList<Double>> geneEntry1 = geneListCopy.get(i);
            Integer geneKey1 = (Integer) geneEntry1.keySet().toArray()[0];
            ArrayList<Double> geneValue1 = geneEntry1.get(geneKey1);
            for(j = i + 1; j < size; j++){
                HashMap<Integer, ArrayList<Double>> geneEntry2 = geneListCopy.get(j);
                Integer geneKey2 = (Integer) geneEntry2.keySet().toArray()[0];
                ArrayList<Double> geneValue2 = geneEntry2.get(geneKey2);
                distanceMatrix[i][j] = EuclideanDistance(geneValue1, geneValue2);
            }
        }
        ArrayList<ArrayList<HashMap<Integer, ArrayList<Double>>>> initialClusters = new ArrayList<ArrayList<HashMap<Integer, ArrayList<Double>>>>();
        for(i = 0; i < size; i++){
            ArrayList<HashMap<Integer, ArrayList<Double>>> list = new ArrayList<HashMap<Integer, ArrayList<Double>>>();
            list.add(geneListCopy.get(i));
            initialClusters.add(list);
        }
        while(size > 5){
            count++;
            double minDistance = Double.MAX_VALUE;
            int start = -1;
            int end = -1;
            // System.out.println("** Loop Counter " + count);
            // scan the matrix
            for(i = 0; i < size - 1; i++){
                for(j = i + 1; j < size; j++){
                    if(distanceMatrix[i][j] - minDistance < epsilon){
                        minDistance = distanceMatrix[i][j];
                        start = i;
                        end = j;
                    }
                }
            }
            // System.out.println("****** start " + start + " end " + end);
            // System.out.print("  || before merging: size is" + size + " start and end " + start + "//" + end);
            // merge the two clusters           
            ArrayList<HashMap<Integer, ArrayList<Double>>> newCluster = new ArrayList<HashMap<Integer, ArrayList<Double>>>();
            newCluster.addAll(initialClusters.get(start));
            newCluster.addAll(initialClusters.get(end));
            initialClusters.remove(start);
            initialClusters.remove(end - 1);
            // System.out.println("New cluster size " + newCluster.size());
            ArrayList<ArrayList<HashMap<Integer, ArrayList<Double>>>> intermediateClusters = 
                    new ArrayList<ArrayList<HashMap<Integer, ArrayList<Double>>>>(initialClusters);
            intermediateClusters.add(newCluster);
            // update the matrix
            size = intermediateClusters.size();
            // System.out.println("|| after merging: size is" + size);
            distanceMatrix = new double[size][size];
            for(i = 0; i < size - 1; i++){
                for(j = i + 1; j < size; j++){
                    ArrayList<HashMap<Integer, ArrayList<Double>>> cluster1 = intermediateClusters.get(i);
                    ArrayList<HashMap<Integer, ArrayList<Double>>> cluster2 = intermediateClusters.get(j);
                    minDistance = Double.MAX_VALUE;
                    for(HashMap<Integer, ArrayList<Double>> entry1 : cluster1){
                        Integer key1 = (Integer) entry1.keySet().toArray()[0];
                        ArrayList<Double> l1 = entry1.get(key1);
                        for(HashMap<Integer, ArrayList<Double>> entry2 : cluster2){
                            Integer key2 = (Integer) entry2.keySet().toArray()[0];
                            ArrayList<Double> l2 = entry2.get(key2);
                            double d = EuclideanDistance(l1, l2);
                            if(d - minDistance < epsilon){
                                minDistance = d;
                            }
                        }
                    }
                    distanceMatrix[i][j] = minDistance;
                }
            }
            initialClusters = new ArrayList<ArrayList<HashMap<Integer, ArrayList<Double>>>>(intermediateClusters);
        }
        int cc = 0;
        for(ArrayList<HashMap<Integer, ArrayList<Double>>> cluster : initialClusters){
            cc += cluster.size();
            // System.out.println("Cluster Size " + cluster.size());
        }
        System.out.println("Total size after hierarchical " + cc);

        int clusterIndexSize = 0;
        HashMap<Integer, Integer> clusterIndex = new HashMap<Integer, Integer>();
        for(i = 0; i < initialClusters.size(); i++){
            ArrayList<HashMap<Integer, ArrayList<Double>>> cluster = initialClusters.get(i);
            clusterIndexSize += cluster.size();
            for(j = 0; j < cluster.size(); j++){
                HashMap<Integer, ArrayList<Double>> list = cluster.get(j);
                Integer listKey = (Integer) list.keySet().toArray()[0];
                ArrayList<Double> listValue = list.get(listKey);
                clusterIndex.put(listKey, i + 1);
            }
        }
        System.out.println("Jaccard Coefficient " + JCoefficient(externalIndex, clusterIndex));       
        ArrayList<ArrayList<ArrayList<Double>>> partitionedCopy = new ArrayList<ArrayList<ArrayList<Double>>>();      
        for(i = 0; i < initialClusters.size(); i++){
            ArrayList<ArrayList<Double>> clusterCopy = new ArrayList<ArrayList<Double>>();
            partitionedCopy.add(clusterCopy);
            ArrayList<HashMap<Integer, ArrayList<Double>>> cluster = initialClusters.get(i);
            for(j = 0; j < cluster.size(); j++){
                HashMap<Integer, ArrayList<Double>> list = cluster.get(j);
                Integer listKey = (Integer) list.keySet().toArray()[0];
                clusterCopy.add(list.get(listKey));
            }
        }
        cc = 0;
        for(ArrayList<ArrayList<Double>> lists : partitionedCopy){
            cc += lists.size();
        }
        // System.out.println("******partitionedCopy's size " + cc );
        double sCoefficient = SilhouetteCoefficient(partitionedCopy);
        System.out.println("Silhouette Coefficient " + sCoefficient);
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
}