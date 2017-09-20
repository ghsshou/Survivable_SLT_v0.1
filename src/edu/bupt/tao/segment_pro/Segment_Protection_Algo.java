package edu.bupt.tao.segment_pro;

import com.sun.scenario.effect.impl.sw.sse.SSEBlend_SRC_OUTPeer;
import edu.bupt.tao.LogRec;
import edu.bupt.tao.algorithms_SSLT.User_Group_Info;
import edu.bupt.tao.algorithms_SSLT.User_Grouping;
import edu.bupt.tao.content_graph.edu.bupt.tao.content_graph.model.Datacenter;
import edu.bupt.tao.edu.bupt.tao.graph_SSLT.Multicast_Graph;
import edu.bupt.tao.edu.bupt.tao.graph_SSLT.SpanningTree;
import edu.bupt.tao.graph.base_algorithms.Constrained_Steiner_Tree;
import edu.bupt.tao.graph.edu.bupt.tao.graph.resource.Resource;
import edu.bupt.tao.graph.model.ModulationSelecting;
import edu.bupt.tao.graph.model.Pair;
import edu.bupt.tao.graph.model.Path;
import edu.bupt.tao.graph.model.abstracts.BaseEdge;
import edu.bupt.tao.graph.model.abstracts.BaseVertex;
import edu.bupt.tao.traffic_SSLT.basic_model.Multicast_Request;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Gao Tao on 2017/9/19.
 */
public class Segment_Protection_Algo {
    private Multicast_Graph global_graph;
    private ModulationSelecting modulation_selecting;
    private static final double epsilon = 0.05;//the small value used to share links between backup paths and primary paths.
    private int MP = 2;//the maximum primary trees can be constructed originated from a datacenter
    private Map<Integer, List<SpanningTree>> primary_tree_database;//store all primary trees for all traffics in the networks.

    private static String sharing = "Sharing";
    private static String full = "Full";

    public Segment_Protection_Algo(Multicast_Graph g, int MP) {
        this.global_graph = g;
        modulation_selecting = new ModulationSelecting();
        primary_tree_database = new ConcurrentHashMap<>();
        this.MP = MP;
    }
    public synchronized boolean procedure_for_one_MR(Multicast_Request mr, String protect_type) {
        LogRec.log.info("1.User grouping begin!");
        Map<Datacenter, List<User_Group_Info>> ugs = new User_Grouping(global_graph, mr, MP).grouping_dis_only();
        List<User_Group_Info> all_groups = new ArrayList<>();

        for (Map.Entry<Datacenter, List<User_Group_Info>> entry : ugs.entrySet()) {
            all_groups.addAll(entry.getValue());
        }
        LogRec.log.info("User group finished! Tree number:" + all_groups.size());
        LogRec.log.info("*********************************");

        //now, begin calculating for primary tree
        LogRec.log.info("2.Primary tree calculating and segment dividing  begin!");
        List<SpanningTree> all_trees = new ArrayList<>();
        Map<SpanningTree, Set<Segment>> segments_of_tree = new HashMap<>();

        for (User_Group_Info ugi : all_groups){
            double distance_limit = Double.MAX_VALUE;//not used
            SpanningTree tree = new Constrained_Steiner_Tree(this.global_graph).get_tree_w_src(ugi.dc.vertex.get_id(), ugi.users, distance_limit);
            tree.print_tree();
            Set<Segment> segments = divide_segments(tree, mr);
            all_trees.add(tree);
            segments_of_tree.put(tree, segments);
        }
        LogRec.log.info("*********************************");
        LogRec.log.info("3.Backup segments dividing  begin!");




        return true;


    }

    public Set<Segment> divide_segments(SpanningTree tree, Multicast_Request mr){
        //first, we use this variable to store all nodes of a tree.
        //Generally, input the key (node), we  will get all the edges from this node.
        Map<BaseVertex, Set<Pair<BaseVertex,BaseVertex>>> nodes_of_tree = new HashMap<>();
        for(Path path: tree.getPaths_of_tree()){
            for(int i = 0; i < path.get_vertices().size() - 1; i++){
                BaseVertex v = path.get_vertices().get(i);//get the node along the path one by one
                Set<Pair<BaseVertex, BaseVertex>> edges_from_node;
                if(!nodes_of_tree.containsKey(v)){
                    nodes_of_tree.put(v, new HashSet<Pair<BaseVertex, BaseVertex>>());
//                    System.out.println("NEW:" + v) ;
                }
//                System.out.println(path);
                edges_from_node =  nodes_of_tree.get(v);
//                System.out.println("GET:" + v + " SIZE:" + edges_from_node.size());
                edges_from_node.add(new Pair<BaseVertex, BaseVertex>(v, path.get_vertices().get(i + 1)));
//                System.out.println("ADD:" + v.get_id() + "->" + path.get_vertices().get(i + 1).get_id());
            }

        }
        //differ the start nodes and the terminate nodes
        Map<BaseVertex, Integer> start_nodes = new HashMap<>();
        Set<BaseVertex> end_nodes = new HashSet<>();
        BaseVertex start_node = tree.getPaths_of_tree().get(0).get_src();
        start_nodes.put(start_node, nodes_of_tree.get(start_node).size());
        for(Path path: tree.getPaths_of_tree()){
            end_nodes.add(path.get_dst());
        }
        for(Map.Entry<BaseVertex, Set<Pair<BaseVertex,BaseVertex>>> entry: nodes_of_tree.entrySet()){
            if(entry.getValue() == null){
                end_nodes.add(entry.getKey());
            }
            else if(entry.getValue().size() >= 2 ||(mr.is_one_user(entry.getKey().get_id()) && entry.getValue().size() == 1)){
                if(start_node != entry.getKey())
                    end_nodes.add(entry.getKey());
                start_nodes.put(entry.getKey(), entry.getValue().size());
            }
        }
        //here, convert set to list for next calculating
        Map<BaseVertex, List<Pair<BaseVertex,BaseVertex>>> nodes_of_tree_list = new HashMap<>();
        for(Map.Entry<BaseVertex, Set<Pair<BaseVertex,BaseVertex>>> entry: nodes_of_tree.entrySet()){
            List<Pair<BaseVertex, BaseVertex>> temp = new ArrayList<>();
            temp.addAll(entry.getValue());
            nodes_of_tree_list.put(entry.getKey(), temp);
        }

//        //here print for debugging
//        System.out.println("START NODES:");
//        for(Map.Entry<BaseVertex, Integer> entry: start_nodes.entrySet()){
//            System.out.println("NODE:" + entry.getKey().get_id() + " T:" + entry.getValue());
//        }
//        System.out.println("END NODES:");
//        for(BaseVertex bv : end_nodes){
//            System.out.println("NODE:" + bv.get_id());
//        }
//        //end for debugging

        Set<Segment> segments = new HashSet<>();
        segs_find(start_nodes, end_nodes, nodes_of_tree_list, segments);
//        for(Segment seg: segments){
//            seg.print_segment();
//        }
        return segments;
    }

    //this function return the segments that are stored in the last parameter.
    public void segs_find(Map<BaseVertex, Integer> start_nodes, Set<BaseVertex> end_nodes,
                          Map<BaseVertex, List<Pair<BaseVertex,BaseVertex>>> nodes_of_tree, Set<Segment> segs_store){

        for(Map.Entry<BaseVertex, Integer> start : start_nodes.entrySet()){
//            System.out.println("START NODE:" + start.getKey() +" BRANCH NO." + start.getValue());
         for(int i = 0; i < start.getValue(); i++){
             Segment seg = new Segment();
             seg.get_vertex_list().add(start.getKey());
//             System.out.println("SIZE:" + nodes_of_tree.get(start.getKey()).size());
             BaseVertex end = nodes_of_tree.get(start.getKey()).get(i).o2;
             while(!end_nodes.contains(end)){
                 seg.get_vertex_list().add(end);
                 end = nodes_of_tree.get(end).get(0).o2;
             }
             seg.get_vertex_list().add(end);
             segs_store.add(seg);
         }
        }




    }







}
