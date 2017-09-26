package edu.bupt.tao.algorithms_SSLT;


import edu.bupt.tao.LogRec;
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


import java.lang.Math;


import java.util.*;
import java.util.concurrent.ConcurrentHashMap;


/**
 * Created by Gao Tao on 2017/6/16.
 * Algo_2 is the main algorithm
 * XXX denotes that the algorithm is not named yet
 */
public class XXX_Algo_2 {
    private Multicast_Graph global_graph;
    private ModulationSelecting modulation_selecting;
    private static final double epsilon = 0.05;//the small value used to share links between backup paths and primary paths.
    private int MB = 2;//the maximum backup trees can be constructed originated from a datacenter
    private Map<Integer, List<SpanningTree>> primary_tree_database;//store all primary trees for all traffics in the networks.
    private Map<Integer, List<SpanningTree>> backup_tree_database;//store all backup trees for all traffics in the networks.

    private int MP = 2;

    private static String sharing = "Sharing";
    private static String full = "Full";


    public XXX_Algo_2(Multicast_Graph g, int MP, int MB) {
        this.global_graph = g;
        modulation_selecting = new ModulationSelecting();
        primary_tree_database = new ConcurrentHashMap<>();
        backup_tree_database = new ConcurrentHashMap<>();
        LogRec.log.debug("Edge no:" + g.get_edge_num());
        this.MP = MP;
        this.MB = MB;
    }

    //return false to block mr, remember to release the occupied resource
    public synchronized boolean procedure_for_one_MR(Multicast_Request mr, String protect_type) {
        LogRec.log.info("1.User grouping begin!");
        Map<Datacenter, List<User_Group_Info>> ugs = new User_Grouping(global_graph, mr, MP).grouping_algo1();
        //store all groups in a list
        List<User_Group_Info> all_groups = new ArrayList<>();

        for (Map.Entry<Datacenter, List<User_Group_Info>> entry : ugs.entrySet()) {
            all_groups.addAll(entry.getValue());
        }
        LogRec.log.info("User grouping finished! Tree number:" + all_groups.size());
        LogRec.log.info("*********************************");

        //now, begin calculating for primary tree
        LogRec.log.info("2.Primary tree calculating begin!");
        List<SpanningTree> all_trees = new ArrayList<>();//
        for (User_Group_Info ugi : all_groups) {
            boolean allocated_flag = false;
            for (int m = ugi.modulation_level; m >= 1 && !allocated_flag; m--) {
                int required_slots = (int) Math.ceil(mr.capacity / modulation_selecting.get_capacity(m));
                //get the distance limited by ML
                double distance_limit = modulation_selecting.generate_Smn(m, ugi.users.size());
                LogRec.log.debug("DIS LIMIT:" + distance_limit);
//                LogRec.log.debug("Vertex List Size:" + global_graph.get_vertex_list().size());
                int slot_i = 0;
                //construct a SWP
                for (; slot_i < Resource.SLOTS_NO - required_slots + 1; slot_i++) {

                    Multicast_Graph SWP = global_graph.draw_SWP(global_graph, slot_i, required_slots);
                    SpanningTree tree = new Constrained_Steiner_Tree(SWP).get_tree_w_src(ugi.dc.vertex.get_id(), ugi.users, distance_limit);
                    SWP.recover_removed_edges();
                    if (tree == null) {
                        LogRec.log.debug("TREE is NULL");
                    } else {
                        //Generally, after searching SWP, the procedure of allocating resource should be executed successfully
                        if (allocate_resource(tree, slot_i, required_slots, m, mr)) {
                            tree.setModulationLevel(m);
                            tree.setStartSlots(slot_i);
                            tree.setUseSlots(required_slots);
                            all_trees.add(tree);
                            LogRec.log.debug("SLOT INDEX in SWP:" + slot_i);
                            allocated_flag = true;
                            break;
                        } else {
//                            System.out.println("Allocating resource for Ptree Failed");
                            return false;
                        }

                    }
                }
            }
        }
        //Check if all user groups have been allocated resource successfully
        if (all_trees.size() != all_groups.size()) {
            LogRec.log.info("Not all trees are allocated! Success no:" + all_trees.size() + ", total no:" + all_groups.size());
//            System.out.println("Allocating resource for All Ptree Failed");
//            release_occupied_slots(mr);
            return false;
        }
        LogRec.log.info("Here, successfully allocate resource for SLTs for MR:" + mr.id);

//        System.out.println("Primary Tree!!!!");
//        for (SpanningTree st : all_trees) {
//            st.print_tree();
//
//        }
//        System.out.println("Now Backup Tree!!!!");

        
        primary_tree_database.put(mr.id, all_trees);
        //end for allocating resources for all SLT of MR

        boolean temp_flag = true;
        if(sharing.equals(protect_type)){
            temp_flag = sharing_protection(mr, all_trees);
        }
        else if(full.equals(protect_type)){
            temp_flag = full_protection(mr, all_trees);

        }
        return temp_flag;

    }
    private synchronized boolean sharing_protection(Multicast_Request mr, List<SpanningTree> all_trees){
        //now, calculate backup paths for each SD pair
        LogRec.log.info("*********************************");
        LogRec.log.info("3.Backup paths calculating begin!");
        List<Path> all_primary_paths = new ArrayList<>();
        for (SpanningTree tree : all_trees) {
            all_primary_paths.addAll(tree.getPaths_of_tree());
        }
        //BG, to save backup paths originated from datacenter
        Map<Datacenter, ArrayList<Path>> bp_group = new HashMap<>();
        //ascending order
        Collections.sort(all_primary_paths);
        for (Path path : all_primary_paths) {
            LogRec.log.debug("Protection For Primary Path: " + path);
            BaseVertex dc = path.get_src();
            BaseVertex user = path.get_dst();
            Multicast_Graph auxiliary_g = new Multicast_Graph(global_graph, true);
            update_graph(auxiliary_g, mr.id, path, sharing);
            Set<Datacenter> dcs_w_s = global_graph.get_multicast_service(mr.req_service).getInDCs();
            Set<BaseVertex> dcs = new HashSet<>();
            //to get the set of datacenters, and not include the dst of corresponding primary path
            for (Datacenter d : dcs_w_s) {
//                if (d.vertex.get_id() != dc.get_id()) {
                    dcs.add(d.vertex);
//                }
            }

            DijkstraShortestPathAlg dspa_bp = new DijkstraShortestPathAlg(auxiliary_g);
            Path backup_path = dspa_bp.get_shortest_path(dcs, user, true);
            if (backup_path != null) {
                Datacenter new_dc = global_graph.getDcs().get(backup_path.get_src());
                if (bp_group.containsKey(new_dc)) {
                    bp_group.get(new_dc).add(backup_path);
                } else {
                    ArrayList<Path> paths_from_dc = new ArrayList<>();
                    paths_from_dc.add(backup_path);
                    bp_group.put(new_dc, paths_from_dc);

                }
                LogRec.log.debug("Backup Path For SRC:" + dc.get_id() + " DST:" + user.get_id());
                LogRec.log.debug(backup_path);
                //if we find a backup we mark the links in the path to be reserved by this traffic
                for (int i = 0; i < backup_path.get_vertices().size() - 1; i++) {
                    BaseVertex v = backup_path.get_vertices().get(i);
                    BaseVertex w = backup_path.get_vertices().get(i + 1);
                    Resource resource = global_graph.get_vertex_pair_weight_index().get(new Pair<>(v.get_id(), w.get_id()));
                    resource.add_reserved_traffic(mr.id);
                }
            } else {
                LogRec.log.error("CANNOT FIND BACKUP PATHS FOR: " + dc.get_id() + " TO " + user.get_id());
                return false;
            }
        }
        LogRec.log.info("Here, successfully calculate all backup paths!");
        LogRec.log.info("*********************************");
        //now, we begin to group the paths and aggregate them to a tree
        LogRec.log.info("4.Backup trees constructing begin!");
        Map<Datacenter, List<SpanningTree>> dc_w_btrees = new HashMap<>();
        //initialization
        for (Datacenter d : global_graph.get_multicast_service(mr.req_service).getInDCs()) {
            List<SpanningTree> temp_tree_list = new ArrayList<>();
            dc_w_btrees.put(d, temp_tree_list);
        }
        for (Map.Entry<Datacenter, ArrayList<Path>> dc_bpaths : bp_group.entrySet()) {
            int low_ML = get_lowML_with_paths(dc_bpaths.getValue());
            int high_ML = get_highML_with_paths(dc_bpaths.getValue());
            //here, we finish checking all the paths originate from the same datacenter in the list
            LogRec.log.debug("low ML:" + low_ML + ",high ML:" + high_ML);
            if (low_ML == high_ML) {
                SpanningTree tree = new SpanningTree();
                tree.setModulationLevel(low_ML);
                tree.getPaths_of_tree().addAll(dc_bpaths.getValue());
                List<SpanningTree> temp_trees = new ArrayList<>();
                temp_trees.add(tree);
                dc_w_btrees.put(dc_bpaths.getKey(), temp_trees);
                continue;
            }
            Collections.sort(dc_bpaths.getValue());
            //add the paths to one or several SLTs and add them to the Map
            if(!aggregate_btrees(dc_w_btrees, dc_bpaths.getValue())){
                return false;
            }
        }
        LogRec.log.info("Here, successfully aggregate all backup paths to trees!");
//        for (Map.Entry<Datacenter, List<SpanningTree>> entry : dc_w_btrees.entrySet()) {
//            for (SpanningTree st : entry.getValue()) {
//                st.print_tree();
//            }
//        }
        LogRec.log.info("*********************************");
        //now, we get all backup trees.
        LogRec.log.info("5.Reserving resource for backup trees begin!");
        List<SpanningTree> all_backup_trees = new ArrayList<>();
        for (Map.Entry<Datacenter, List<SpanningTree>> entry : dc_w_btrees.entrySet()) {
            all_backup_trees.addAll(entry.getValue());
        }
        Multicast_Graph auxiliary_g_bp = new Multicast_Graph(global_graph, true);
        for (SpanningTree st : all_backup_trees) {
//            System.out.println(st.getModulationLevel());
            if(st.getModulationLevel() <= 0)
                return false;
            int required_slots = (int) Math.ceil(mr.capacity / modulation_selecting.get_capacity(st.getModulationLevel()));
            int optimal_index = -1;
            double extra_slots = Double.MAX_VALUE;
            for (int i = 0; i < Resource.SLOTS_NO - required_slots + 1; i++) {
                int temp_extra = calculate_extra_slots(auxiliary_g_bp, mr.id, st, i, required_slots);
                if (temp_extra == -1) {
                    continue;
                }
                if (extra_slots > temp_extra) {
                    optimal_index = i;
                    extra_slots = temp_extra;
                }
            }
            if (optimal_index != -1) {
                if (!reserve_resource(st, mr.id, st.getId(), optimal_index, required_slots))
                    return false;
            }
            else
                return false;
//            st.print_tree();

        }
        LogRec.log.info("Here, successfully reserve resource for all backup trees!");
        backup_tree_database.put(mr.id, all_backup_trees);
        return true;
    }

    private synchronized boolean full_protection(Multicast_Request mr, List<SpanningTree> all_trees){
        //now, calculate backup paths for each SD pair
        LogRec.log.info("*********************************");
        LogRec.log.info("3.Backup paths (1+1) calculating begin!");
        List<Path> all_primary_paths = new ArrayList<>();
        for (SpanningTree tree : all_trees) {
            all_primary_paths.addAll(tree.getPaths_of_tree());
        }
        //BG, to save backup paths originated from datacenter
        Map<Datacenter, ArrayList<Path>> bp_group = new HashMap<>();
        //ascending order
        Collections.sort(all_primary_paths);
        for (Path path : all_primary_paths) {
            LogRec.log.debug("Protection For Primary Path: " + path);
            BaseVertex dc = path.get_src();
            BaseVertex user = path.get_dst();
            Multicast_Graph auxiliary_g = new Multicast_Graph(global_graph, true);
            update_graph(auxiliary_g, mr.id, path, "Full");
            Set<Datacenter> dcs_w_s = global_graph.get_multicast_service(mr.req_service).getInDCs();
            Set<BaseVertex> dcs = new HashSet<>();
            //to get the set of datacenters, and not include the dst of corresponding primary path
            for (Datacenter d : dcs_w_s) {
//                if (d.vertex.get_id() != dc.get_id()) {
                    dcs.add(d.vertex);
//                }
            }

            DijkstraShortestPathAlg dspa_bp = new DijkstraShortestPathAlg(auxiliary_g);
            Path backup_path = dspa_bp.get_shortest_path(dcs, user, true);
            if (backup_path != null) {
                Datacenter new_dc = global_graph.getDcs().get(backup_path.get_src());
                if (bp_group.containsKey(new_dc)) {
                    bp_group.get(new_dc).add(backup_path);
                } else {
                    ArrayList<Path> paths_from_dc = new ArrayList<>();
                    paths_from_dc.add(backup_path);
                    bp_group.put(new_dc, paths_from_dc);

                }
                LogRec.log.debug("Backup Path For SRC:" + dc.get_id() + " DST:" + user.get_id());
                LogRec.log.debug(backup_path);
                //if we find a backup we mark the links in the path to be reserved by this traffic
                for (int i = 0; i < backup_path.get_vertices().size() - 1; i++) {
                    BaseVertex v = backup_path.get_vertices().get(i);
                    BaseVertex w = backup_path.get_vertices().get(i + 1);
                    Resource resource = global_graph.get_vertex_pair_weight_index().get(new Pair<>(v.get_id(), w.get_id()));
                    resource.add_reserved_traffic(mr.id);
                }
            } else {
//                System.out.println("CANNOT FIND BACKUP PATHS FOR: " + dc.get_id() + " TO " + user.get_id());
//                System.out.println("Cannot find bpaths!");
                return false;
            }
        }
        LogRec.log.info("Here, successfully calculate all backup paths!");
        LogRec.log.info("*********************************");
        //now, we begin to group the paths and aggregate them to a tree
        LogRec.log.info("4.Backup trees constructing begin!");
        Map<Datacenter, List<SpanningTree>> dc_w_btrees = new HashMap<>();
        //initialization
        for (Datacenter d : global_graph.get_multicast_service(mr.req_service).getInDCs()) {
            List<SpanningTree> temp_tree_list = new ArrayList<>();
            dc_w_btrees.put(d, temp_tree_list);
        }
        for (Map.Entry<Datacenter, ArrayList<Path>> dc_bpaths : bp_group.entrySet()) {
            int low_ML = get_lowML_with_paths(dc_bpaths.getValue());
            int high_ML = get_highML_with_paths(dc_bpaths.getValue());
            //here, we finish checking all the paths originate from the same datacenter in the list
            LogRec.log.debug("low ML:" + low_ML + ",high ML:" + high_ML);
            if (low_ML == high_ML) {
                SpanningTree tree = new SpanningTree();
                tree.setModulationLevel(low_ML);
                tree.getPaths_of_tree().addAll(dc_bpaths.getValue());
                List<SpanningTree> temp_trees = new ArrayList<>();
                temp_trees.add(tree);
                dc_w_btrees.put(dc_bpaths.getKey(), temp_trees);
                continue;
            }
            Collections.sort(dc_bpaths.getValue());
            //add the paths to one or several SLTs and add them to the Map
            if(!aggregate_btrees(dc_w_btrees, dc_bpaths.getValue())){
//                System.out.println("BP Tree constructing failed");
                return false;
            }
        }
        LogRec.log.info("Here, successfully aggregate all backup paths to trees!");
//        for (Map.Entry<Datacenter, List<SpanningTree>> entry : dc_w_btrees.entrySet()) {
//            for (SpanningTree st : entry.getValue()) {
//                st.print_tree();
//            }
//        }
        LogRec.log.info("*********************************");
        //now, we get all backup trees.
        LogRec.log.info("5.Reserving resource for backup trees begin!");
        List<SpanningTree> all_backup_trees = new ArrayList<>();
        for (Map.Entry<Datacenter, List<SpanningTree>> entry : dc_w_btrees.entrySet()) {
            all_backup_trees.addAll(entry.getValue());
        }
        Multicast_Graph auxiliary_g_bp = new Multicast_Graph(global_graph, true);
        for (SpanningTree st : all_backup_trees) {
            LogRec.log.debug("Traffic ID:" + mr.id + ",Tree ID:" + st.getId() + "User Size:" + mr.users.length);
            boolean success_flag = false;
//            System.out.println(st.getModulationLevel());
            int required_slots = (int) Math.ceil(mr.capacity / modulation_selecting.get_capacity(st.getModulationLevel()));
            for (int i = 0; i < Resource.SLOTS_NO - required_slots + 1; i++) {
//                System.out.println("III:" + i);
                if(resource_can_use(st, i, required_slots, mr)){
//                    System.out.println("Available");
                    if(allocate_resource(st, i, required_slots, st.getModulationLevel(), mr)){
                        st.setStartSlots(i);
                        st.setUseSlots(required_slots);
                        success_flag = true;
                        break;
                    }
                }
            }
//            st.print_tree();
            if(!success_flag){
//                System.out.println("BP Tree Reserving Resource failed!");
                return false;
            }

        }
        LogRec.log.info("Here, successfully reserve resource for all backup trees!");
        backup_tree_database.put(mr.id, all_backup_trees);
        return true;
    }
    //allocate resource (occupied for primary tree)
    private boolean allocate_resource(SpanningTree tree, int start_slot, int required_slot,
                                      int modulation_level, Multicast_Request multicast_request) {
//        boolean result = true;
//        tree.print_tree();
        for (Path path : tree.getPaths_of_tree()) {
            Resource res;
//            path.setStartSlots(start_slot);
//            path.setUseSlots(required_slot);
            path.setModulationLevel(modulation_level);
            for (int i = 0; i < path.get_vertices().size() - 1; i++) {
                res = global_graph.get_vertex_pair_weight_index().
                        get(new Pair<>(path.get_vertices().get(i).get_id(), path.get_vertices().get(i + 1).get_id()));
                LogRec.log.debug("v->w:" + res.getStart_index() + "->" + res.getEnd_index());

                for (int j = start_slot; j < start_slot + required_slot; j++) {
                    LogRec.log.debug("start slot:" + j);
                    //if the slot has been occupied by other traffic
                    if (!res.slot_can_use(j, multicast_request.id, tree.getId())) {
                        LogRec.log.error("ERROR: SLOT[" + j + "] HAS BEEN OCCUPIED!");
//                        release_occupied_slots(tree, start_slot, required_slot, multicast_request);
                        return false;
                    }
                    res.use_slot(j, multicast_request.id, tree.getId(), 1);
                }
            }
        }
        return true;

    }
    //check whether resource can be used
    //allocate resource (occupied for primary tree)
    private boolean resource_can_use(SpanningTree tree, int start_slot, int required_slot,
                                      Multicast_Request multicast_request) {
//        boolean result = true;
//        tree.print_tree();
        for (Path path : tree.getPaths_of_tree()) {
            Resource res;
//            path.setStartSlots(start_slot);
//            path.setUseSlots(required_slot);
//            path.setModulationLevel(modulation_level);
            for (int i = 0; i < path.get_vertices().size() - 1; i++) {
                res = global_graph.get_vertex_pair_weight_index().
                        get(new Pair<>(path.get_vertices().get(i).get_id(), path.get_vertices().get(i + 1).get_id()));
                for (int j = start_slot; j < start_slot + required_slot; j++) {
                    LogRec.log.debug("j:" + j);
                    //if the slot has been occupied by other traffic
                    if (!res.slot_can_use(j, multicast_request.id, tree.getId())) {
//                        LogRec.log.error("ERROR: SLOT[" + j + "] HAS BEEN OCCUPIED!");
//                        release_occupied_slots(tree, start_slot, required_slot, multicast_request);
                        return false;
                    }
//                    res.use_slot(j, multicast_request.id, tree.getId(), 1);
                }
            }
        }
        return true;

    }

    //release a range of slots occupied or reserved by a certain tree of a MR
//    private void release_occupied_slots(SpanningTree tree, int start_slot, int required_slot, Multicast_Request multicast_request) {
//        LogRec.log.debug("Now Release Resource!");
//        for (Path path : tree.getPaths_of_tree()) {
//            path.setStartSlots(-1);
//            path.setUseSlots(-1);
//            path.setModulationLevel(-1);
//            Resource res;
//            for (int i = 0; i < path.get_vertices().size() - 1; i++) {
//                res = global_graph.get_vertex_pair_weight_index().
//                        get(new Pair<Integer, Integer>(path.get_vertices().get(i).get_id(), path.get_vertices().get(i + 1).get_id()));
//
//                res.set_slots_free_for_MR(start_slot, start_slot + required_slot - 1, multicast_request.id);
//            }
//        }
//    }

    //release all slots occupied or reserved by a MR
    public void release_occupied_slots(Multicast_Request mr) {
        for (Pair<Integer, Integer> pair : global_graph.get_pair_list()) {
            global_graph.get_vertex_pair_weight_index().get(pair).set_slots_free_for_MR(mr.id);
            global_graph.get_vertex_pair_weight_index().get(new Pair<>
                    (pair.o2, pair.o1)).set_slots_free_for_MR(mr.id);
        }
        if(primary_tree_database.containsKey(mr.id)){
            primary_tree_database.remove(mr.id);
        }
        if(backup_tree_database.containsKey(mr.id)){
            backup_tree_database.remove(mr.id);
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
    public int get_max_used_slot_index(){
        int index = -1;
        for (Pair pair : global_graph.get_pair_list()) {
            Resource res = global_graph.get_vertex_pair_weight_index().get(pair);
            index = index <= res.get_largest_used_index() ? res.get_largest_used_index() : index;
            res = global_graph.get_vertex_pair_weight_index().
                    get(new Pair<>((Integer) pair.o2, (Integer) pair.o1));
            index = index <= res.get_largest_used_index() ? res.get_largest_used_index() : index;
        }
        return index;

    }

    //update cost according the Equations,????there is some problems in the results got before??
    private void update_graph(Multicast_Graph mg, int traffic_id, Path p, String protect_type) {
        //path p denotes the primary path
        for (Pair<Integer, Integer> pair : mg.get_pair_list()) {
            Resource res = mg.get_vertex_pair_weight_index().get(pair);
            Resource res2 = mg.get_vertex_pair_weight_index().get(new Pair<>(pair.o2, pair.o1));
            boolean flag = true;//denote the link (res) cost has been changed
            boolean flag2 = true;//denote the link (res2) cost has been changed

            //we set the cost used by other backup paths of the same traffic to 0, actually, this is not the meaning of sharing
            if(flag && res.contains_reserved_traffic(traffic_id)){
                res.setCost(0);
                flag = false;
            }
            if(flag2 && res2.contains_reserved_traffic(traffic_id)){
                res2.setCost(0);
                flag2 = false;
            }
            if(sharing.equals(protect_type)){
                //we set the cost used by other primary paths to a small value, i.e. self-sharing
                if(flag && res.occupied_slots_for_traffic(traffic_id) > 0){
                    res.setCost(epsilon);
                    flag = false;
                }
                if(flag2 && res2.occupied_slots_for_traffic(traffic_id) > 0){
                    res2.setCost(epsilon);
                    flag2 = false;
                }
                //we set the cost of other links, i.e. cross-sharing
                Set<Integer> joint_paths = this.get_joint_bp_paths(traffic_id, p);
                int slots_no = res.reserved_slots_except_joint(joint_paths);
                double new_cost = res.getCost() - (double) slots_no / Resource.SLOTS_NO;
                if (flag && slots_no > 0) {
                    res.setCost(new_cost);
                }
                int slots_no2 = res2.reserved_slots_except_joint(joint_paths);
                double new_cost2 = res2.getCost() - (double) slots_no2 / Resource.SLOTS_NO;
                if (flag2 && slots_no2 > 0) {
                    res2.setCost(new_cost2);
                }
            }
            else if("Full".equals(protect_type)){
            }
        }
        //link-disjointed
        for (int i = 0; i < p.get_vertices().size() - 1; i++) {
            BaseVertex v = p.get_vertices().get(i);
            BaseVertex w = p.get_vertices().get(i + 1);
//            BaseVertex v = p.get_vertices().get(i);
            Resource resource = mg.get_vertex_pair_weight_index().get(new Pair<>(v.get_id(), w.get_id()));
            resource.setCost(Double.MAX_VALUE);
            resource = mg.get_vertex_pair_weight_index().get(new Pair<>(w.get_id(), v.get_id()));
            resource.setCost(Double.MAX_VALUE);
        }

        //here is the case that the path has only two nodes(src and dst)
        if (p.get_vertices().size() == 2) {
            LogRec.log.debug("Update graph!");
//            LogRec.log.debug("src:" + p.get_src().get_id() + ",dst:" + p.get_dst().get_id());
            Resource resource = mg.get_vertex_pair_weight_index().get(new Pair<>(p.get_src().get_id(), p.get_dst().get_id()));
            resource.setCost(Double.MAX_VALUE);
        }

    }
    //return tree number including primary and backup for calculating
    public int get_tree_num(Multicast_Request mr){
        int bp_tree_num = backup_tree_database.isEmpty()? 0 : backup_tree_database.get(mr.id).size();
//        System.out.println("PTree Num:" + primary_tree_database.get(mr.id).size() + "BTree num:" + bp_tree_num);


        return primary_tree_database.get(mr.id).size() + bp_tree_num;
    }


    //the highest ML can adopt, i.e. each path can form a SLT, Eq.(3)
    public int get_highML_with_paths(List<Path> in_paths) {
        BaseVertex one_src = in_paths.get(0).get_src();
        double shortest_dis = Double.MAX_VALUE;
        for (Path path : in_paths) {
            if (one_src != path.get_src()) {
                LogRec.log.error("ERROR! THE PATHS IN THIS GROUP DO NOT ORIGINATE FROM THE SAME DATACENTER!");
                return -1;
            }
            shortest_dis = shortest_dis > path.get_weight() ? path.get_weight() : shortest_dis;
        }
        return modulation_selecting.get_highest_ML(1, shortest_dis);
    }

    //consider the relation of Smn, Eq.(2)
    public int get_lowML_with_paths(List<Path> in_paths) {
        BaseVertex one_src = in_paths.get(0).get_src();
        int n = in_paths.size();
        double longest_dis = 0;
        for (Path path : in_paths) {
            if (one_src != path.get_src()) {
                LogRec.log.error("ERROR! THE PATHS IN THIS GROUP DO NOT ORIGINATE FROM THE SAME DATACENTER!");
                return -1;
            }
            longest_dis = longest_dis < path.get_weight() ? path.get_weight() : longest_dis;
        }
        return modulation_selecting.get_highest_ML(n, longest_dis);
    }

    //this function aggregate the paths to one or several SLTs, which are originated from the same datacenter
    private boolean aggregate_btrees(Map<Datacenter, List<SpanningTree>> dc_w_btrees, List<Path> paths) {
        Datacenter dc = global_graph.getDcs().get(paths.get(0).get_src());
        for (Path path : paths) {
            LogRec.log.debug("Aggregate path:" + path);
            boolean path_flag = false;
            for (int i = 1; i <= MB; i++) {
                if (i <= dc_w_btrees.get(dc).size()) {
                    LogRec.log.debug("Exist!---index:" + (i - 1));
                    double longest_dis = 0;
                    for (Path p : dc_w_btrees.get(dc).get(i - 1).getPaths_of_tree()) {
                        longest_dis = longest_dis < p.get_weight() ? p.get_weight() : longest_dis;
                    }
                    int n = dc_w_btrees.get(dc).get(i - 1).getPaths_of_tree().size();
                    int m = dc_w_btrees.get(dc).get(i - 1).getModulationLevel();
                    if (modulation_selecting.generate_Smn(m, n + 1) >= path.get_weight()) {
                        dc_w_btrees.get(dc).get(i - 1).add_path(path);
                        path_flag = true;
//                        System.out.println("1:" + dc_w_btrees.get(dc).get(i - 1).getModulationLevel());
                        break;
                    }
                } else {
                    LogRec.log.debug("Create new tree!---index:" + (i - 1));
                    SpanningTree tree = new SpanningTree();
                    tree.add_path(path);
                    //set the modulation level
                    tree.setModulationLevel(modulation_selecting.modulation_select(path.get_weight()));
//                    System.out.println("2:" + tree.getModulationLevel());
                    if(tree.getModulationLevel() <= 0)
                        return false;
                    dc_w_btrees.get(dc).add(tree);
                    path_flag = true;
                    break;
                }
            }
            if (!path_flag) {
                LogRec.log.debug("Select one and degrade ML!");
                int shared_links = 0;
                SpanningTree final_tree = null;
                for (SpanningTree tree : dc_w_btrees.get(dc)) {
                    int temp_shared_links = get_shared_links(tree, path);
                    if (shared_links < temp_shared_links) {
                        final_tree = tree;
                        shared_links = temp_shared_links;
                    }
                    LogRec.log.debug("Shared links:" + shared_links);

                }
                //if we get the tree with most shared links
                if (final_tree != null) {
                    final_tree.add_path(path);
                    reset_modulation(final_tree);
                    if(final_tree.getModulationLevel() != -1){
//                        System.out.println("3:" + final_tree.getModulationLevel());
//                        System.out.println("Dis:" + final_tree.get_longest_dis() +" Size:" + final_tree.getPaths_of_tree().size());
                        continue;
                    }
                    else{
                        final_tree.getPaths_of_tree().remove(path);
                        reset_modulation(final_tree);
                    }
                }
                //if none tree shares links with this path, we select the tree with minimum users(path)
                int smallest_users = Integer.MAX_VALUE;
                SpanningTree smallest_tree = dc_w_btrees.get(dc).get(0);
                for (SpanningTree tree : dc_w_btrees.get(dc)) {
                    if (smallest_users < tree.getPaths_of_tree().size()) {
                        smallest_tree = tree;
                        smallest_users = tree.getPaths_of_tree().size();
                    }

                }
                smallest_tree.add_path(path);
                reset_modulation(smallest_tree);
//                System.out.println("4:" + smallest_tree.getModulationLevel());
                if(smallest_tree.getModulationLevel() != -1){
                }
                else{
                    //if we still cannot get a tree that satisfies Smn, then we choose anyone can be used
                    boolean success = false;
                    smallest_tree.getPaths_of_tree().remove(path);
                    reset_modulation(smallest_tree);
//                    SpanningTree available_tree = dc_w_btrees.get(dc).get(0);
                    for (SpanningTree tree : dc_w_btrees.get(dc)) {
                        tree.add_path(path);
                        reset_modulation(tree);
                        if(tree.getModulationLevel() != -1){
                            success = true;
                            break;
                        }
                        else{
                            tree.getPaths_of_tree().remove(path);
                            reset_modulation(tree);
                        }
                    }
                    if(!success){
                        LogRec.log.debug("FATAL ERROR: NO AVAILABLE ML");
                        return false;
                    }
                }
            }
        }
        return true;
    }

    //get the number of shared links between all paths in tree and the path
    private int get_shared_links(SpanningTree tree, Path path) {
        Set<Pair<BaseVertex, BaseVertex>> edge_set = new HashSet<>();
        for (int i = 0; i < path.get_vertices().size() - 1; i++) {
            edge_set.add(new Pair<>(path.get_vertices().get(i), path.get_vertices().get(i + 1)));
        }
        Set<Pair<BaseVertex, BaseVertex>> edge_set_of_tree = new HashSet<>();
        for (Path p : tree.getPaths_of_tree()) {
            for (int i = 0; i < p.get_vertices().size() - 1; i++) {
                edge_set_of_tree.add(new Pair<>(p.get_vertices().get(i), p.get_vertices().get(i + 1)));
            }
        }
        edge_set.retainAll(edge_set_of_tree);
        return edge_set.size();
    }

    private void reset_modulation(SpanningTree tree) {
        int m = this.modulation_selecting.ModulationFormats.size();
        int n = tree.getPaths_of_tree().size();
        double s_mn = modulation_selecting.generate_Smn(m, n);
        double longest_dis = tree.get_longest_dis();
        for (; m > 0; ) {
            if (s_mn >= longest_dis) {
                tree.setModulationLevel(m);
                break;
            }
            s_mn = modulation_selecting.generate_Smn(--m, n);
        }
        if (m == 0) {
            tree.setModulationLevel(-1);
        }
    }

    private int calculate_extra_slots(Multicast_Graph mg, int traffic_id,
                                      SpanningTree bp_tree, int start_slot, int required_slots) {
        Set<Integer> banned_paths = new HashSet<>();
        for(Path p: bp_tree.getPaths_of_tree()){
            Path pri_p = get_primary_path(traffic_id, p);
            Set<Integer> temp_paths = get_joint_bp_paths(traffic_id, pri_p);
            banned_paths.addAll(temp_paths);
        }
        int extra_slots = 0;
//        LogRec.log.debug("Input parameter: start index: " + start_slot);
        for (Path p : bp_tree.getPaths_of_tree()) {
            for (int i = 0; i < p.get_vertices().size() - 1; i++) {
                BaseVertex v = p.get_vertices().get(i);
                BaseVertex w = p.get_vertices().get(i + 1);
                Resource res = mg.get_vertex_pair_weight_index().get(new Pair<>(v.get_id(), w.get_id()));
                int temp_counter = res.extra_slots_for_reserve(traffic_id, start_slot, required_slots, banned_paths);
                if (temp_counter == -1)
                    return -1;
                extra_slots += temp_counter;
            }
        }
        LogRec.log.debug("Extra slots at start index:" + start_slot + ",using extra slots:" + extra_slots);
        return extra_slots;
    }

    private boolean reserve_resource(SpanningTree st, int traffic_id, int tree_id, int start_index, int required_slots) {
        for (Path p : st.getPaths_of_tree()) {
            for (int i = 0; i < p.get_vertices().size() - 1; i++) {
                BaseVertex v = p.get_vertices().get(i);
                BaseVertex w = p.get_vertices().get(i + 1);
                Resource res = this.global_graph.get_vertex_pair_weight_index().get(new Pair<>(v.get_id(), w.get_id()));
                for (int j = start_index; j < start_index + required_slots; j++) {
                    if (!res.reserve_slot(j, traffic_id, tree_id, p.getId())) {
                        LogRec.log.debug("Reserve Failed! @Slot:" + j + ",Link:" + res.getStart_index() + "->" + res.getEnd_index());
                        return false;
                    }
                }
            }
            p.setUseSlots(required_slots);
            p.setStartSlots(start_index);
        }
        st.setUseSlots(required_slots);
        st.setStartSlots(start_index);
        return true;
    }

    //this func returns the primary protected by the input backup path,
    // and their traffic id is also a input parameter
    private Path get_primary_path(int traffic_id, Path bp_path) {
        Path final_path = null;
        for (SpanningTree p_tree : this.primary_tree_database.get(traffic_id)) {
            for (Path p : p_tree.getPaths_of_tree()) {
                if (bp_path.get_dst().get_id() == p.get_dst().get_id()) {
                    final_path = p;
                }
            }

        }
        return final_path;
    }

    //this func returns the backup path whose protected primary is the input parameter
    private Path get_backup_path(int traffic_id, Path p_path) {
        Path final_path = null;
        for (SpanningTree p_tree : this.backup_tree_database.get(traffic_id)) {
            for (Path p : p_tree.getPaths_of_tree()) {
                if (p_path.get_dst().get_id() == p.get_dst().get_id()) {
                    final_path = p;
                }
            }

        }
        return final_path;
    }

    //judge whether two paths are link-jointed
    private boolean is_disjointed(Path path_1, Path path_2) {
        Set<Pair<BaseVertex, BaseVertex>> pair_1 = new HashSet<>();
        for (int i = 0; i < path_1.get_vertices().size() - 1; i++) {
            pair_1.add(new Pair<>(path_1.get_vertices().get(i), path_1.get_vertices().get(i + 1)));
            //reversed edge
            pair_1.add(new Pair<>(path_1.get_vertices().get(i + 1), path_1.get_vertices().get(i)));
        }
        Set<Pair<BaseVertex, BaseVertex>> pair_2 = new HashSet<>();
        for (int i = 0; i < path_2.get_vertices().size() - 1; i++) {
            pair_2.add(new Pair<>(path_2.get_vertices().get(i), path_2.get_vertices().get(i + 1)));
            pair_2.add(new Pair<>(path_2.get_vertices().get(i + 1), path_2.get_vertices().get(i)));
        }
        pair_1.retainAll(pair_2);
        return pair_1.isEmpty();
    }

    //the input parameter denotes the primary path, we return the backup paths whose protected primary paths
    //are jointed with the input one.
    private Set<Integer> get_joint_bp_paths(int traffic_id, Path p_path) {
        Set<Integer> joint_paths = new HashSet<>();
        //to search the primary paths jointed with  p_path
        for (Map.Entry<Integer, List<SpanningTree>> entry : primary_tree_database.entrySet()) {
            if(traffic_id != entry.getKey())
                for (SpanningTree st : entry.getValue()) {
                    for (Path p : st.getPaths_of_tree()) {
                        //if the primary paths are jointed, then add corresponding backup path to the Set
                        if (is_disjointed(p_path, p)) {
                            joint_paths.add(get_backup_path(entry.getKey(), p).getId());
                        }

                    }
                }
        }
        return joint_paths;
    }


}
