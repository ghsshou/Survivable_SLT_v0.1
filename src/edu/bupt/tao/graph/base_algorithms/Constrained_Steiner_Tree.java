package edu.bupt.tao.graph.base_algorithms;

import edu.bupt.tao.algorithms_SSLT.test_main;
import edu.bupt.tao.edu.bupt.tao.graph_SSLT.Multicast_Graph;
import edu.bupt.tao.edu.bupt.tao.graph_SSLT.SpanningTree;
import edu.bupt.tao.graph.model.Pair;
import edu.bupt.tao.graph.model.Path;
import edu.bupt.tao.graph.model.abstracts.BaseVertex;

import java.util.*;


/**
 * Created by Gao Tao on 2017/5/31.
 * in current version, the cost between to adjacent nodes is set to 1, if changed, the class Graph has to be changed too.
 */
public class Constrained_Steiner_Tree {
    public double[][] graph;
    public double[][] disGraph;
    public static int INF = 99999;
    int numOfVertex;
    int numOfEdge;

    public Multicast_Graph multicast_graph;



    public Constrained_Steiner_Tree(Multicast_Graph multicast_graph) {
        this.multicast_graph = multicast_graph;
        this.numOfVertex = multicast_graph.get_vertex_num();
        graph = new double[numOfVertex][numOfVertex];
        disGraph = new double[numOfVertex][numOfVertex];
        for(int i = 0; i < numOfVertex; i++){
            for(int j = 0; j < numOfVertex; j++){
                if(i==j){
                    graph[i][j] = 0;
                    disGraph[i][j] = 0;
                }
                else {
                    disGraph[i][j] = multicast_graph.get_edge_weight(multicast_graph.get_vertex(i),multicast_graph.get_vertex(j));
                    disGraph[j][i] = disGraph[i][j];
                    if(disGraph[i][j] == Double.MAX_VALUE){
                        graph[i][j] = INF;
                    }
                    else{
                        graph[i][j] = multicast_graph.get_edge_cost(multicast_graph.get_vertex(i),multicast_graph.get_vertex(j));
                        graph[j][i] = graph[i][j];
                    }
                }
            }
        }
//        test_main.log.debug("COST GRAPH:");
//        for (int v = 0; v < graph[0].length; v++)
//        {
//            for (int w = 0 ; w < graph[0].length; w++) {
//                if (graph[v][w] == INF)
//                    System.out.print("INF ");
//                else
//                    System.out.print(graph[v][w] + " ");
//            }
//            System.out.println();
//        }
    }

//input the costMatrix and the disMatrix, return the path matrix
    public int[][] _FloydAlgo(){
        double[][] costMatrix = this.graph;

        int[][] pathMatrix = new int[costMatrix[0].length][costMatrix[0].length];
        for(int i = 0; i < pathMatrix[0].length; i++){
            for(int j = 0; j < pathMatrix[0].length; j++){
                pathMatrix[i][j] = j;
            }
        }
        //The first step of the referred algorithm
        for(int m = 0; m < pathMatrix[0].length; m++){
            for(int i = 0; i < pathMatrix[0].length; i++){
                for(int j = 0; j < pathMatrix[0].length; j++){
                    if(costMatrix[i][j] > costMatrix[i][m] + costMatrix[m][j]){
                        costMatrix[i][j] = costMatrix[i][m] + costMatrix[m][j];
                        disGraph[i][j] = disGraph[i][m] + disGraph[m][j];
                        pathMatrix[i][j] = pathMatrix[i][m];
                    }
                    else if(costMatrix[i][j] == costMatrix[i][m] + costMatrix[m][j]){
                        if(disGraph[i][j] > disGraph[i][m] + disGraph[m][j]){
                            costMatrix[i][j] = costMatrix[i][m] + costMatrix[m][j];
                            disGraph[i][j] = disGraph[i][m] + disGraph[m][j];
                            pathMatrix[i][j] = pathMatrix[i][m];
                        }
                    }
                }
            }
        }
        return pathMatrix;
    }
    public void print_tree(int[][] path_matrix){
        int k;


        for (int v = 0; v < graph[0].length; v++)
        {
            for (int w = 0 ; w < graph[0].length; w++)
            {
                System.out.print("v" + v  +  "--" + "v" + w + " cost: " + graph[v][w] + "distance: " + disGraph[v][w] + " Path: " + v + ' ');
                k = path_matrix[v][w];
                while (k != w)
                {
                    System.out.print("-> " + k + " ");
                    k = path_matrix[k][w];
                }
                System.out.println("-> " + w);
            }
            System.out.println();
        }
//        System.out.println("Path Matrix");
//        for (int v = 0; v < graph[0].length; v++)
//        {
//            for (int w = 0 ; w < graph[0].length; w++)
//            {
//                System.out.print(path_matrix[v][w] + " ");
//            }
//            System.out.println();
//        }


    }
    public SpanningTree get_tree_w_src(Integer src, Set<Integer> users, double dis_constraint){
        int[][] paths_all = _FloydAlgo();
        print_tree(paths_all);
        //all nodes including src and dsts
        Set<Integer> all_node_V = new HashSet<Integer>();
        //visited nodes
        Set<Integer> visited_node_C = new HashSet<Integer>();
        //spanning tree
        Set<Pair<Integer,Integer>> spanning_tree_T = new HashSet<Pair<Integer,Integer>>();
        //cost of path to not X
        Map<Integer, Double> cost_path_P = new HashMap<Integer, Double>(users.size() + 1);

        double min = INF;
        Pair<Integer, Integer> next_edge;
        int dst_index = -1;
        int src_index = -1;
        double total_cost = 0;


        //Initialize
        visited_node_C.add(src);
        all_node_V.addAll(users);
        all_node_V.add(src);
        Set<Integer> temp = new HashSet<Integer>();
        temp.addAll(users);
        temp.remove(src);

        cost_path_P.put(src, (double) 0);
        for(Integer user: users){
            cost_path_P.put(user, (double) 0);
        }


        while(visited_node_C.size() != all_node_V.size()){
            test_main.log.debug("Visited node set size: " + visited_node_C.size());
            test_main.log.debug("All node set size: " + all_node_V.size());
            test_main.log.debug("Temp set size: " + temp.size());
            Iterator<Integer> it_1 = visited_node_C.iterator();
            Iterator<Integer> it_2 = temp.iterator();
            src_index = -1;
            dst_index = -1;
            min = INF;
            next_edge = null;
            int v = -1;
            int w = -1;
            while(it_1.hasNext()){
                v = it_1.next();
                while(it_2.hasNext()){
                    test_main.log.debug("Both has next");
                    w = it_2.next();
                    test_main.log.debug("Process here");
                    if(disGraph[v][w] <= dis_constraint){
                        double f_vw = graph[v][w];
                        if(f_vw < min){
                            min = f_vw;
                            next_edge = new Pair<Integer, Integer>(v, w);
                            src_index = v;
                            dst_index = w;
                        }
                    }
                }
            }
            if(next_edge != null & dst_index != -1){
                visited_node_C.add(dst_index);
                spanning_tree_T.add(next_edge);
                double temp_P = cost_path_P.get(src_index) + graph[src_index][dst_index];
                cost_path_P.put(dst_index, temp_P);
                temp.remove(dst_index);
            }else{
                test_main.log.error("Cannot Find Tree! Error Info [dst_index: " + dst_index + "]");
                return null;
            }
        }

        //Now we have got the set of edges which constructs a spanning tree with minimum cost (spanning_tree_T),
        //but in spanning_tree_T, the edges may consist of several actual edge of original graph.
        //Then we will transform it to the form we need
        test_main.log.debug("Size of Pair Set:" + spanning_tree_T.size());
        Multicast_Graph auxiliary_graph = new Multicast_Graph(multicast_graph);
        Set<Pair<BaseVertex, BaseVertex>> all_edges = new HashSet<Pair<BaseVertex, BaseVertex>>();
        for(Pair<Integer, Integer> edge_in_T: spanning_tree_T){
            int v = edge_in_T.first();
            int w = edge_in_T.second();
            //this log can see the edges chosen from the closure graph (i.e. aggregated edges)
            test_main.log.debug("src:" + v);
            test_main.log.debug("dst:" + w);
            int k;
            k = paths_all[v][w];

            do
            {
                all_edges.add(new Pair<BaseVertex, BaseVertex>(auxiliary_graph.get_vertex(v), auxiliary_graph.get_vertex(k)));
                total_cost += auxiliary_graph.get_edge_cost(auxiliary_graph.get_vertex(v), auxiliary_graph.get_vertex(k));
                v = k;
                test_main.log.debug("k:" + k);
                k = paths_all[k][w];
            }while (v != w);
        }
        test_main.log.debug("Size of all edge set: " + all_edges.size());
        //first we construct an auxiliary graph, then we calculate the path based on it
        List<BaseVertex> vertex_list = auxiliary_graph.get_vertex_list();
        for(BaseVertex bv: vertex_list){
            Set<BaseVertex> adjacent_vertexs = auxiliary_graph.get_adjacent_vertices(bv);
            Iterator<BaseVertex> it_adj = adjacent_vertexs.iterator();
            while(it_adj.hasNext()){
                BaseVertex adj_of_bv = it_adj.next();
                if(all_edges.contains(new Pair<BaseVertex, BaseVertex>(bv, adj_of_bv))){
                    test_main.log.debug("Remain Edge: " + bv.get_id() + "->" + adj_of_bv.get_id());
                    continue;
                }
                else
                    // remove the edges that are not in spanning tree
                    auxiliary_graph.remove_edge(new Pair<Integer, Integer>(bv.get_id(), adj_of_bv.get_id()));
            }
        }



        SpanningTree spanning_tree = new SpanningTree();
        DijkstraShortestPathAlg dsp = new DijkstraShortestPathAlg(auxiliary_graph);
        for(Integer user: users){
            //it seems the last parameter does not matter because there is only one path in the auxiliary graph.
            //but if true, the distance will
            Path p_user = dsp.get_shortest_path(auxiliary_graph.get_vertex(src), auxiliary_graph.get_vertex(user),true);
            test_main.log.debug("Automatically Generated Path Info: " + p_user);
//            p_user.setCost(cost_path_P.get(user));
//            test_main.log.info("After set cost Path info: " + p_user);
            spanning_tree.add_path(p_user);
        }
        //
        spanning_tree.setTotal_cost(total_cost);
        //check if all users can contained
        if(spanning_tree.getPaths_of_tree().size() != users.size()){
            test_main.log.error("Exist users that are not contained!");
            return null;
        }

        return spanning_tree;
    }





}
