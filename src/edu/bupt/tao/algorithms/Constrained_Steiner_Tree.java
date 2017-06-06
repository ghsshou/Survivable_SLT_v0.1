package edu.bupt.tao.algorithms;

/**
 * Created by Gao Tao on 2017/5/31.
 */
public class Constrained_Steiner_Tree {
    public float[][] graph;
    public float[][] disGraph;
    public static int INF = 99999;
    int numOfVertex;
    int numOfEdge;

    public Constrained_Steiner_Tree() {

    }
    public Constrained_Steiner_Tree(int numOfVertex) {
        this.numOfVertex = numOfVertex;
        graph = new float[numOfVertex][numOfVertex];
        disGraph = new float[numOfVertex][numOfVertex];
        for(int i = 0; i < numOfVertex; i++){
            for(int j = 0; j < numOfVertex; j++){
                if(i==j){
                    graph[i][j] = 0;
                    disGraph[i][j] = 0;
                }
                else {
                    graph[i][j] = INF;
                    disGraph[i][j] = INF;
                }
            }
        }
    }
    public void addEdge(int v, int w, int cost, int dis){
        this.graph[v][w] = cost;
        this.disGraph[v][w] = dis;
        this.graph[w][v] = cost;
        this.disGraph[w][v] = dis;
    }

    public Constrained_Steiner_Tree(float[][] inGraph){
        this.graph = inGraph.clone();
    }
//input the costMatrix and the disMatrix, return the path matrix
    public int[][] _FloydAlgo(float[][] costGraph, float[][] disGraph){
        float[][] costMatrix = costGraph;

        int[][] pathMatrix = new int[costMatrix[0].length][costMatrix[0].length];
        for(int i = 0; i < pathMatrix[0].length; i++){
            for(int j = 0; j < pathMatrix[0].length; j++){
                pathMatrix[i][j] = j;
            }
        }
        for(int m = 0; m < pathMatrix[0].length; m++){
            for(int i = 0; i < pathMatrix[0].length; i++){
                for(int j = 0; j < pathMatrix[0].length; j++){
                    if(costMatrix[i][j] > costMatrix[i][m] + costMatrix[m][j]){
                        costMatrix[i][j] = costMatrix[i][m] + costMatrix[m][j];
                        pathMatrix[i][j] = pathMatrix[i][m];
                    }
                    else if(costMatrix[i][j] == costMatrix[i][m] + costMatrix[m][j]){
                        if(disGraph[i][j] > disGraph[i][j] + disGraph[i][j]){
                            costMatrix[i][j] = costMatrix[i][m] + costMatrix[m][j];
                            pathMatrix[i][j] = pathMatrix[i][m];
                        }
                    }
                }
            }
        }
        return pathMatrix;
    }
    public static void main(String[] args){
//        float[][] testGraph = {{0,1,5,INF,INF,INF,INF,INF,INF},
//                {1,0,3,7,5,INF,INF,INF,INF},
//                {5,3,0,INF,1,7,INF,INF,INF},
//                {INF,7,INF,0,2,INF,3,INF,INF},
//                {INF,5,1,2,0,3,6,9,INF},
//                {INF,INF,7,INF,3,0,INF,5,INF},
//                {INF,INF,INF,3,6,INF,0,2,7},
//                {INF,INF,INF,INF,9,5,2,0,4},
//                {INF,INF,INF,INF,INF,INF,7,4,0}};
        int k;
//      int [][] pathMatrix = new Constrained_Steiner_Tree()._FloydAlgo(testGraph);
        Constrained_Steiner_Tree cst = new Constrained_Steiner_Tree(8);
        cst.addEdge(0,1,10,1);
        cst.addEdge(0,4,2,1);
        cst.addEdge(0,5,1,2);
        cst.addEdge(1,2,7,1);
        cst.addEdge(1,6,9,1);
        cst.addEdge(2,3,3,2);
        cst.addEdge(2,6,8,1);
        cst.addEdge(3,4,5,1);
        cst.addEdge(3,5,6,1);
        cst.addEdge(4,7,2,1);
        cst.addEdge(5,7,10,2);
        cst.addEdge(6,7,1,3);
        for (int v = 0; v < cst.graph[0].length; v++)
        {
            for (int w = 0 ; w < cst.graph[0].length; w++)
            {
                System.out.print(cst.graph[v][w] + " ");
            }
            System.out.println();
        }
        int[][] pathMatrix = cst._FloydAlgo(cst.graph, cst.disGraph);
        for (int v = 0; v < cst.graph[0].length; v++)
        {
            for (int w = 0 ; w < cst.graph[0].length; w++)
            {
                System.out.print("v" + v  +  "--" + "v" + w + " cost: " + cst.graph[v][w] + " Path: " + v + ' ');
                k = pathMatrix[v][w];
                while (k != w)
                {
                    System.out.print("-> " + k + " ");
                    k = pathMatrix[k][w];
                }
                System.out.println("-> " + w);
            }
            System.out.println();
        }
        System.out.println("Path Matrix");
        for (int v = 0; v < cst.graph[0].length; v++)
        {
            for (int w = 0 ; w < cst.graph[0].length; w++)
            {
                System.out.print(pathMatrix[v][w] + " ");
            }
            System.out.println();
        }
        for (int v = 0; v < cst.graph[0].length; v++)
        {
            for (int w = 0 ; w < cst.graph[0].length; w++)
            {
                System.out.print(cst.graph[v][w] + " ");
            }
            System.out.println();
        }


    }





}
