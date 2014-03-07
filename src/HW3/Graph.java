package HW3;

import java.util.ArrayList;

class Graph {
    ArrayList<String> sVertices;
    ArrayList<Integer> nVertices;
    int[][] matrix;
    int number;
    boolean isInteger;

    public Graph(){

    }

    public Graph(int number, boolean isInteger){
        this.number = number;
        this.isInteger = isInteger;
        int i, j;
        if(isInteger == true){
            nVertices = new ArrayList<Integer>();
        }
        else{
            sVertices = new ArrayList<String>();
        }
        matrix = new int[number][number];
        for(i = 0; i < number; i++){
            for (j = 0; j < number; j++) {
                matrix[i][j] = 0;
            }
        }
    }

    public void addVertex(Object o){
        if(o instanceof String){
            sVertices.add((String) o);
        }
        if(o instanceof Integer){
            nVertices.add((Integer) o);
        }
    }

    public void addEdge(Object s1, Object s2){
        int indexOfS1, indexOfS2;
        if(s1 instanceof String){
            indexOfS1 = sVertices.indexOf(s1);
            indexOfS2 = sVertices.indexOf(s2);
        }
        else{
            indexOfS1 = nVertices.indexOf(s1);
            indexOfS2 = nVertices.indexOf(s2);
        }
        matrix[indexOfS1][indexOfS2] = 1;
        matrix[indexOfS2][indexOfS1] = 1;
    }

    public int getVertexNumber(){
        return number;
    }

    public int getEdgeNumber(){
        int counter = 0;
        for(int i = 0; i < number; i++){
            for(int j = 0; j < number; j++){
                if(matrix[i][j] == 1){
                    counter++;
                }
            }
        }
        return counter / 2;
    }
}