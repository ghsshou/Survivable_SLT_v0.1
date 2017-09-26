package edu.bupt.tao.segment_pro;

import edu.bupt.tao.LogRec;
import edu.bupt.tao.algorithms_SSLT.User_Group_Info;
import edu.bupt.tao.algorithms_SSLT.User_Grouping;
import edu.bupt.tao.content_graph.edu.bupt.tao.content_graph.model.Datacenter;
import edu.bupt.tao.edu.bupt.tao.graph_SSLT.Multicast_Graph;
import edu.bupt.tao.edu.bupt.tao.graph_SSLT.SpanningTree;
import edu.bupt.tao.graph.base_algorithms.Constrained_Steiner_Tree;
import edu.bupt.tao.graph.base_algorithms.DijkstraShortestPathAlg;
import edu.bupt.tao.graph.edu.bupt.tao.graph.resource.Resource;
import edu.bupt.tao.graph.model.ModulationSelecting;
import edu.bupt.tao.graph.model.Pair;
import edu.bupt.tao.graph.model.Path;
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
//    private int MP = 2;//the maximum primary trees can be constructed originated from a datacenter
    private Map<Integer, List<SpanningTree>> primary_tree_database;//store all primary trees for all traffics in the networks.
    private Map<Integer, Segment> primary_seg_database;//store the primary segments that can be searched for judging the joint relationship
    private Map<Integer, Segment> backup_seg_database;//store the backup segments
    private double total_distance = 0;//record the distance of all backup segments (succeed calculation) to calculate the failure notation time

    public final static String sharing = "Sharing";
    public final static String full = "Full";
    public final static String seg_pro = "Segment";
    public final static String path_pro = "Path";

    public Segment_Protection_Algo(Multicast_Graph g) {
        this.global_graph = g;
        modulation_selecting = new ModulationSelecting();
        primary_tree_database = new ConcurrentHashMap<>();
        primary_seg_database = new ConcurrentHashMap<>();
        backup_seg_database = new ConcurrentHashMap<>();
    }
    public synchronized boolean procedure_for_one_MR(Multicast_Request mr, String protect_type, String seg_or_path) {
        LogRec.log.info("1.User grouping begin!");
        Map<Datacenter, List<User_Group_Info>> ugs = new User_Grouping(global_graph, mr, 0).grouping_dis_only();
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
//            tree.print_tree();
            Set<Segment> segments = divide_segments(tree, mr, seg_or_path);
            all_trees.add(tree);
            segments_of_tree.put(tree, segments);
        }
        LogRec.log.info("*********************************");
        LogRec.log.info("3.Backup segments calculating  begin!");
        Map<SpanningTree, Set<Segment>> backup_segments_of_tree = segment_protection(mr,segments_of_tree,protect_type);
        if(backup_segments_of_tree == null){
            System.out.println("Backup Segment Finding Failed!");
            return false;
        }
        LogRec.log.info("*********************************");
        LogRec.log.info("4.Allocating and Reserve Resource Begin!");
        boolean res_flag = true;
        for(SpanningTree tree: all_trees){
            res_flag = allocate_and_reserve_resource_for_segments(mr, tree, segments_of_tree.get(tree), backup_segments_of_tree.get(tree), protect_type);
            if(!res_flag){
                System.out.println("Resource Allocation Failed!");
                break;
            }
        }
        if(res_flag){
            for(Map.Entry<SpanningTree, Set<Segment>> entry : segments_of_tree.entrySet()){
                for(Segment seg: entry.getValue()){
                    total_distance += seg._weight;

                }
            }

        }

        return res_flag;


    }

    //divide primary segments for a tree
    public Set<Segment> divide_segments(SpanningTree tree, Multicast_Request mr, String seg_or_path){
        Set<Segment> segments = new HashSet<>();//var to return
        if(seg_or_path.equals(path_pro)){
            for(Path p: tree.getPaths_of_tree()){
                segments.add(convert_Path_to_Segment(p));
            }
            return segments;
        }


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


        segs_find(mr.id, tree.getId(),start_nodes, end_nodes, nodes_of_tree_list, segments);
//        for(Segment seg: segments){
//            seg.print_segment();
//        }
        return segments;
    }

    //this function return the primary segments that are stored in the last parameter.
    public void segs_find(int MR_ID, int tree_ID, Map<BaseVertex, Integer> start_nodes, Set<BaseVertex> end_nodes,
                          Map<BaseVertex, List<Pair<BaseVertex,BaseVertex>>> nodes_of_tree, Set<Segment> segs_store){

        for(Map.Entry<BaseVertex, Integer> start : start_nodes.entrySet()){
//            System.out.println("START NODE:" + start.getKey() +" BRANCH NO." + start.getValue());
         for(int i = 0; i < start.getValue(); i++){
             Segment seg = new Segment(true);//primary segment
             seg.setPro_traffic_id(MR_ID);
             seg.setPro_tree_id(tree_ID);
             seg.get_vertex_list().add(start.getKey());
//             System.out.println("SIZE:" + nodes_of_tree.get(start.getKey()).size());
             BaseVertex end = nodes_of_tree.get(start.getKey()).get(i).o2;
             while(!end_nodes.contains(end)){
                 seg.get_vertex_list().add(end);
                 end = nodes_of_tree.get(end).get(0).o2;
             }
             seg.get_vertex_list().add(end);
             //calculate weight of segment
             double weight = 0;
             for(int j = 0; j < seg._vertex_list.size() - 1; j ++){
                 weight += this.global_graph.get_edge_weight(seg.get_vertex_list().get(j), seg.get_vertex_list().get(j+1));
             }
             seg.set_weight(weight);
             segs_store.add(seg);
             primary_seg_database.put(seg.id, seg);
         }
        }




    }
    private synchronized Map<SpanningTree, Set<Segment>> segment_protection(Multicast_Request mr, Map<SpanningTree, Set<Segment>> segments_of_tree,
                                                                            String pro_type){
        Map<SpanningTree, Set<Segment>> backup_segments_of_trees = new HashMap<>();
        for(Map.Entry<SpanningTree, Set<Segment>> entry: segments_of_tree.entrySet()){
            Set<Segment> backup_segments = new HashSet<>();
            for(Segment seg: entry.getValue()){
//                LogRec.log.info("~~~~~FOR PRIMARY SEGMENT~~~~~~~");
//                seg.print_segment();
//                LogRec.log.info("NOW INFO");
                Path path = convert_Segment_to_Path(seg);
                BaseVertex start = path.get_src();
                BaseVertex end = path.get_dst();
                Multicast_Graph auxiliary_graph = new Multicast_Graph(global_graph, true);
                update_graph(auxiliary_graph, entry.getValue(), backup_segments, seg, pro_type);
                DijkstraShortestPathAlg dspa_bp = new DijkstraShortestPathAlg(auxiliary_graph);
                Path backup_path = dspa_bp.get_shortest_path(start, end, true);
                if(backup_path != null){
                    Segment temp_seg = convert_Path_to_Segment(backup_path);
                    temp_seg.setType(false);//backup segment
                    temp_seg.setPro_traffic_id(mr.id);//the traffic it protects
                    temp_seg.setPro_tree_id(entry.getKey().getId());//the tree it protects
                    temp_seg.setPro_seg_id(seg.id);//its primary segment;
                    backup_segments.add(temp_seg);
                    backup_seg_database.put(temp_seg.id,temp_seg);
                    seg.setPro_seg_id(temp_seg.id);//set the primary seg's id to the segment id which protects it
                    //debugging
//                    temp_seg.print_segment();
                }
                else{

                }
            }
            if(backup_segments.size() < entry.getValue().size()){
//                System.out.println("BLOCKING! TRAFFIC ID:" + mr.id);
                return null;
            }
            else{
                backup_segments_of_trees.put(entry.getKey(), backup_segments);
            }
        }
        return backup_segments_of_trees;

    }
    //update cost according the Equations, "primary_segs" denotes the primary_segs of a distributed tree, not all trees from a MR
    private void update_graph(Multicast_Graph mg, Set<Segment> primary_segs, Set<Segment> backup_segs, Segment pri_seg, String protect_type) {
        //get the primary edges from "primary_segs"
        Set<Pair<BaseVertex, BaseVertex>> primary_edges = new HashSet<>();
        for(Segment seg: primary_segs){
            primary_edges.addAll(convert_Segment_to_EdgeSet(seg));
        }
        Set<Pair<BaseVertex, BaseVertex>> backup_edges = new HashSet<>();
        for(Segment seg: backup_segs){
            backup_edges.addAll(convert_Segment_to_EdgeSet(seg));
        }

        //get the primary edges from the primary path
        Set<Pair<BaseVertex,BaseVertex>> primary_path_edges = convert_Segment_to_EdgeSet(pri_seg);
        //path p denotes the primary path
        Path p = convert_Segment_to_Path(pri_seg);
        for (Pair<Integer, Integer> pair : mg.get_pair_list()) {
            Pair<BaseVertex, BaseVertex> temp_edge = new Pair<>(mg.get_vertex(pair.o1),mg.get_vertex(pair.o2));
            Pair<BaseVertex, BaseVertex> temp_edge2 = new Pair<>(mg.get_vertex(pair.o2),mg.get_vertex(pair.o1));
            Resource res = mg.get_vertex_pair_weight_index().get(pair);
            Resource res2 = mg.get_vertex_pair_weight_index().get(new Pair<>(pair.o2, pair.o1));
            boolean flag = true;//denote the link (res) cost has been changed
            boolean flag2 = true;//denote the link (res2) cost has been changed
            //we set the cost used by other backup paths of the same traffic to 0, actually, this is not the meaning of sharing
            if(flag && backup_edges.contains(temp_edge)){
                res.setCost(0);
                LogRec.log.info("Change cost to 0:" + temp_edge.o1 + "->" + temp_edge.o2);
                flag = false;
            }
            if(flag2 && backup_edges.contains(temp_edge2)){
                res2.setCost(0);
                LogRec.log.info("Change cost to 0:" + temp_edge.o2 + "->" + temp_edge.o1);
                flag2 = false;
            }
            if(sharing.equals(protect_type)){
                //we set the cost used by other primary paths to a small value, i.e. self-sharing
                if(flag && primary_edges.contains(temp_edge) && (!primary_path_edges.contains(temp_edge))) {
                    LogRec.log.info("Change cost to EPSILON:" + temp_edge.o1 + "->" + temp_edge.o2);
                    res.setCost(epsilon);
                }
                if(flag2 && primary_edges.contains(temp_edge2) && (!primary_path_edges.contains(temp_edge2))) {
                    LogRec.log.info("Change cost to EPSILON:" + temp_edge.o2 + "->" + temp_edge.o1);
                    res2.setCost(epsilon);
                }
                //we set the cost of other links, cross sharing
                int slots_no = res.reserved_slots();
                double new_cost = res.getCost() - (double) slots_no / Resource.SLOTS_NO;
                if (flag && slots_no > 0) {
                    res.setCost(new_cost);
                    LogRec.log.info("Change cost to [" + new_cost + "]:" + temp_edge.o1 + "->" + temp_edge.o2);
                    flag = false;
                }
                int slots_no2 = res2.reserved_slots();
                double new_cost2 = res2.getCost() - (double) slots_no / Resource.SLOTS_NO;
                if (flag2 && slots_no2 > 0) {
                    res2.setCost(new_cost2);
                    LogRec.log.info("Change cost to [" + new_cost2 + "]:" + temp_edge.o1 + "->" + temp_edge.o2);
                    flag2 = false;
                }
            }
            else if(full.equals(protect_type)){
            }
        }
        for (int i = 0; i < p.get_vertices().size() - 1; i++) {
            BaseVertex v = p.get_vertices().get(i);
            BaseVertex w = p.get_vertices().get(i + 1);
            Resource resource = mg.get_vertex_pair_weight_index().get(new Pair<>(v.get_id(), w.get_id()));
            resource.setCost(Double.MAX_VALUE);
            LogRec.log.info("Change cost to MAX:" + v + "->" + w);
        }
        //if we use "Full" protection, the primary segment and the backup segment of the same tree cannot share any resource,
        //so we set the cost of links on the tree to MAX.
        if(protect_type.equals(full)){
            for(Segment seg: primary_segs){
                for (int i = 0; i < seg._vertex_list.size() - 1; i++) {
                    BaseVertex v = seg._vertex_list.get(i);
                    BaseVertex w = seg._vertex_list.get(i + 1);
                    Resource resource = mg.get_vertex_pair_weight_index().get(new Pair<>(v.get_id(), w.get_id()));
                    resource.setCost(Double.MAX_VALUE);
                    LogRec.log.info("Change cost to MAX:" + v + "->" + w);
                }

            }
        }



    }

    //allocate resource and reserve resource for primary and backup segments respectively (for one tree!)
    private boolean allocate_and_reserve_resource_for_segments(Multicast_Request mr, SpanningTree tree,
                                                               Set<Segment> primary_segs, Set<Segment> backup_segs, String pro_type){
        //modulation selecting, (not considering that the limitation of user number)
//        tree.print_tree();
//        System.out.println("SSSS" + primary_segs.size());
        get_the_dis_w_bpsegment(tree, primary_segs);
        int modulation = modulation_selecting.modulation_select(tree.get_weight());
        if(modulation < 1){
            System.out.println("Cannot Find Modulation!");
            return false;
        }
        tree.setModulationLevel(modulation);
        LogRec.log.info("Selected ML:" + modulation + ",Dis:" + tree.get_weight());
        //resource allocation and reservation
        //first, search and check which slot can use
        int required_slots = (int) Math.ceil(mr.capacity / modulation_selecting.get_capacity(tree.getModulationLevel()));
        boolean resouce_available = false;
        int start_slot = 0;
        while(!resouce_available){
            if(check_available_res_for_primary_segment(primary_segs, start_slot, required_slots)
                    && check_available_res_for_backup_segment(mr.id, backup_segs, start_slot, required_slots)){
                resouce_available = true;
            }
            else{
                start_slot ++;
                if(start_slot > Resource.SLOTS_NO - required_slots){
                    return false;
                }
            }
        }
        //second, allocate resource for primary segments (tree)
        if(!allocate_resource(tree, start_slot, required_slots, mr)){

            return false;
        }
        else {
//            System.out.println("-----------------------");
//            tree.print_tree();
//            System.out.println("FOR PRIMARY TREE: [S:" + start_slot + ", R:" + required_slots + "]");
        }

        //third, reserve resource backup segments
        if(!reserve_resource(backup_segs, mr.id, tree.getId(), start_slot, required_slots, pro_type)){
            return false;
        }
        else{
            //debugging
//            for(Segment seg: backup_segs){
//                seg.print_segment();
//            }
//            System.out.println("FOR BACKUP SEGMENTS: [S:" + start_slot + ", R:" + required_slots + "]");
//            System.out.println("-----------------------");
        }
        return true;
    }
    //reserve resource
    private boolean reserve_resource(Set<Segment> backup_segs, int traffic_id, int tree_id, int start_slot, int required_slots, String protect_type){
        for(Segment seg: backup_segs){
            List<BaseVertex> nodes = seg._vertex_list;
            for(int i = 0; i < nodes.size() - 1; i ++){
                Resource res = this.global_graph.get_vertex_pair_weight_index()
                        .get(new Pair<>(nodes.get(i).get_id(), nodes.get(i + 1).get_id()));
                for (int j = start_slot; j < start_slot + required_slots; j++) {
                    if(protect_type.equals(sharing)){
                        if (!res.reserve_slot(j, traffic_id, tree_id, seg.id)) {
                            LogRec.log.debug("Reserve Failed! @Slot:" + j + ",Link:" + res.getStart_index() + "->" + res.getEnd_index());
                            return false;
                        }
                    }
                    else if(protect_type.equals(full)){
                        if (!res.slot_can_use(j, traffic_id, tree_id)) {
                            LogRec.log.error("ERROR: SLOT[" + j + "] HAS BEEN OCCUPIED!");
                            return false;
                        }
                        res.use_slot(j, traffic_id, tree_id, 1);
                    }
                    else{
                        LogRec.log.error("ERROR! PROTECT TYPE WRONG!!");
                    }

                }
            }
        }
        return true;
    }

    //allocate resource (occupied for primary tree)
    private boolean allocate_resource(SpanningTree tree, int start_slot, int required_slot,
                                      Multicast_Request multicast_request) {
        for (Path path : tree.getPaths_of_tree()) {
            Resource res;
            path.setModulationLevel(tree.getModulationLevel());
            for (int i = 0; i < path.get_vertices().size() - 1; i++) {
                res = global_graph.get_vertex_pair_weight_index().
                        get(new Pair<>(path.get_vertices().get(i).get_id(), path.get_vertices().get(i + 1).get_id()));
                LogRec.log.debug("v->w:" + res.getStart_index() + "->" + res.getEnd_index());
                for (int j = start_slot; j < start_slot + required_slot; j++) {
                    //if the slot has been occupied by other traffic
                    if (!res.slot_can_use(j, multicast_request.id, tree.getId())) {
                        LogRec.log.error("ERROR: SLOT[" + j + "] HAS BEEN OCCUPIED!");
                        return false;
                    }
                    res.use_slot(j, multicast_request.id, tree.getId(), 1);
                }
            }
        }
        return true;

    }

    private boolean check_available_res_for_backup_segment(int traffic_id, Set<Segment> backup_segs, int start_slot, int required_slot){
        for(Segment seg: backup_segs){
            Segment primary_seg = primary_seg_database.get(seg.getPro_seg_id());
            Set<Integer> banned_segs = get_joint_bp_segs(traffic_id, primary_seg);
                List<BaseVertex> nodes = seg._vertex_list;
                for(int i = 0; i < nodes.size() - 1; i ++){
                    Resource res = this.global_graph.get_vertex_pair_weight_index()
                            .get(new Pair<>(nodes.get(i).get_id(), nodes.get(i + 1).get_id()));
                    if(res.extra_slots_for_reserve(traffic_id, start_slot, required_slot, banned_segs) == -1)
                        return false;
                }
        }
        return true;
    }
    //check whether the resource on the links of primary segments is available
    private boolean check_available_res_for_primary_segment(Set<Segment> primary_segs, int start_slot, int required_slot){
        for(Segment seg: primary_segs){
            List<BaseVertex> nodes = seg._vertex_list;
            for(int i = 0; i < nodes.size() - 1; i ++){
                Resource res = this.global_graph.get_vertex_pair_weight_index()
                        .get(new Pair<>(nodes.get(i).get_id(), nodes.get(i + 1).get_id()));
                if(!res.is_free_from_in(start_slot, required_slot))
                    return false;
            }
        }
        return true;
    }

    private boolean get_the_dis_w_bpsegment(SpanningTree tree, Set<Segment> primary_segments){
        double longest_distance = tree.get_longest_dis();
        for(Path path: tree.getPaths_of_tree()){
//            System.out.println("Path INFO:" + path);

            Set<BaseVertex> path_nodes = new HashSet<>();
            path_nodes.addAll(path.get_vertices());
            Set<Integer> include_pri_segs_id = new HashSet<>();
            for(Segment seg: primary_segments){
                Set<BaseVertex> pri_seg_nodes = new HashSet<>();
                pri_seg_nodes.addAll(seg._vertex_list);
                pri_seg_nodes.removeAll(path_nodes);
                if(pri_seg_nodes.isEmpty()){
                    include_pri_segs_id.add(seg.id);
                    Segment backup_seg = backup_seg_database.get(seg.getPro_seg_id());
                    double new_path_dis = path.get_weight() - seg._weight + backup_seg._weight;
                    LogRec.log.info("Original Path Distance:" + longest_distance);
                    longest_distance = longest_distance < new_path_dis ? new_path_dis : longest_distance;
                    //debugging
                    LogRec.log.info("Path [" + path.getId() + "] include Segment [" + seg.id + "]");
//                    seg.print_segment();
                    LogRec.log.info("Its backup segment is:" + backup_seg.id);
//                    backup_seg.print_segment();
                    LogRec.log.info("New Path Distance:" + longest_distance);
                }
            }
        }
        tree.set_weight(longest_distance);
        return true;

    }



    //convert segment to edges set
    private Set<Pair<BaseVertex, BaseVertex>> convert_Segment_to_EdgeSet(Segment seg){
        Set<Pair<BaseVertex,BaseVertex>> results = new HashSet<>();
        for(int i = 0; i < seg._vertex_list.size() - 1; i++){
            results.add(new Pair<>(seg._vertex_list.get(i), seg._vertex_list.get(i + 1)));
        }
        return results;
    }
    private Path convert_Segment_to_Path(Segment seg){
        Path path = new Path(seg._vertex_list,seg._weight);
        path.setId(seg.id);
        return path;

    }
    private Segment convert_Path_to_Segment(Path p){
        Segment segment = new Segment(p.get_vertices(), p.get_weight());
        segment.setId(p.getId());
        return segment;
    }
    //the input parameter denotes the primary segment, we return the backup segments whose protected primary paths
    //are jointed with the input one.
    private Set<Integer> get_joint_bp_segs(int traffic_id, Segment primary_segment) {
        Set<Integer> joint_segments = new HashSet<>();
        //to search the primary segments jointed with  primary_segment
        for(Map.Entry<Integer, Segment> entry: primary_seg_database.entrySet()){
            if(traffic_id != entry.getValue().getPro_traffic_id()){
                if(!is_disjointed(primary_segment,entry.getValue())){
                    joint_segments.add(entry.getValue().getPro_seg_id());
                }
            }
        }
        return joint_segments;
    }
    //judge whether two segments are node-disjoint (true = disjoint)
    private boolean is_disjointed(Segment seg1, Segment seg2){
        Set<BaseVertex> nodes1 = new HashSet<>();
        Set<BaseVertex> nodes2 = new HashSet<>();
        nodes1.addAll(seg1._vertex_list);
        nodes1.addAll(seg2._vertex_list);
        nodes1.retainAll(nodes2);
        return nodes1.isEmpty();
    }
    //release all slots occupied or reserved by a MR
    public void release_all_slots(Multicast_Request mr) {
        for (Pair<Integer, Integer> pair : global_graph.get_pair_list()) {
            global_graph.get_vertex_pair_weight_index().get(pair).set_slots_free_for_MR(mr.id);
            global_graph.get_vertex_pair_weight_index().get(new Pair<>
                    (pair.o2, pair.o1)).set_slots_free_for_MR(mr.id);
        }
        if(primary_tree_database.containsKey(mr.id)){
            primary_tree_database.remove(mr.id);
        }
        for(Map.Entry<Integer, Segment> entry: primary_seg_database.entrySet()){
            if(entry.getValue().getPro_traffic_id() == mr.id){
                primary_seg_database.remove(entry.getKey());
                LogRec.log.info("Release traffic id [" + mr.id + "] from primary_seg_database");
            }
        }
        for(Map.Entry<Integer, Segment> entry: backup_seg_database.entrySet()){
            if(entry.getValue().getPro_traffic_id() == mr.id){
                backup_seg_database.remove(entry.getKey());
                LogRec.log.info("Release traffic id [" + mr.id + "] from backup_seg_database");
            }
        }
    }
    //get resource utilization
    public double get_current_resource_utilization() {
        double rate;
        int total_used_slots = 0;


        //to eliminate the links terminated at dcs
        int exclusive_edges_num = 0;
//        Set<Integer> dcs = new HashSet<>();
        for(Map.Entry<BaseVertex, Datacenter> entry: global_graph.getDcs().entrySet()){
//            dcs.add(entry.getValue().vertex.get_id());
            exclusive_edges_num += global_graph.get_precedent_vertices(entry.getValue().vertex).size();
            exclusive_edges_num += global_graph.get_adjacent_vertices(entry.getValue().vertex).size();

        }
        int total_slots = Resource.SLOTS_NO * (global_graph.get_edge_num() - exclusive_edges_num);

        for (Pair pair : global_graph.get_pair_list()) {
            Resource res = global_graph.get_vertex_pair_weight_index().get(pair);
//            if(!dcs.contains(res.getEnd_index()))
            total_used_slots += res.total_used_slots();
            //now check the reversed edge
            res = global_graph.get_vertex_pair_weight_index().
                    get(new Pair<>((Integer) pair.o2, (Integer) pair.o1));
//            if(!dcs.contains(res.getEnd_index()))
            total_used_slots += res.total_used_slots();
        }

        LogRec.log.info("Total used slots:" + total_used_slots + ",total slots:" + total_slots);
        rate = (double) total_used_slots / total_slots;
//        System.out.println("Total used slots:" + total_used_slots + ",total slots:" + total_slots);
//        System.out.println("Resource Utilization:" + rate);
        LogRec.log.info("Resource Utilization:" + rate);
        return rate;
    }


    public double getTotal_distance() {
        return total_distance;
    }
}
