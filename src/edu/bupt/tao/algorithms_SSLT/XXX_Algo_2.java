package edu.bupt.tao.algorithms_SSLT;

import com.sun.org.apache.bcel.internal.generic.LOR;
import edu.bupt.tao.LogRec;
import edu.bupt.tao.content_graph.edu.bupt.tao.content_graph.model.Datacenter;
import edu.bupt.tao.edu.bupt.tao.graph_SSLT.Multicast_Graph;
import edu.bupt.tao.edu.bupt.tao.graph_SSLT.SpanningTree;
import edu.bupt.tao.graph.base_algorithms.Constrained_Steiner_Tree;
import edu.bupt.tao.graph.base_algorithms.DijkstraShortestPathAlg;
import edu.bupt.tao.graph.edu.bupt.tao.graph.resource.Resource;
import edu.bupt.tao.graph.edu.bupt.tao.graph.resource.Slot;
import edu.bupt.tao.graph.model.ModulationSelecting;
import edu.bupt.tao.graph.model.Pair;
import edu.bupt.tao.graph.model.Path;
import edu.bupt.tao.graph.model.abstracts.BaseVertex;
import edu.bupt.tao.traffic_SSLT.basic_model.Multicast_Request;

import javax.xml.crypto.Data;
import java.lang.Math;

import java.lang.reflect.Array;
import java.util.*;

import static java.lang.Math.*;

/**
 * Created by Gao Tao on 2017/6/16.
 * Algo_2 is the main algorithm
 * XXX denotes that the algorithm is not named yet
 */
public class XXX_Algo_2 {
    Multicast_Graph global_graph;
    ModulationSelecting modulation_selecting;
    static final double epsilon = 0.05;//the small value used to share links between backup paths and primary paths.
    static final int MB = 2;//the maximum backup trees can be constructed originated from a datacenter


    public XXX_Algo_2(){}
    public XXX_Algo_2(Multicast_Graph g){
        this.global_graph = g;
        modulation_selecting = new ModulationSelecting();
        LogRec.log.debug("Edge no:" + g.get_edge_num());
    }

    //return false to block mr, remember to release the occupied resource
    public boolean procedure_for_one_MR(Multicast_Request mr){
        Map<Datacenter, List<User_Group_Info>> ugs = new User_Grouping(global_graph, mr).grouping_algo1();
        //store all groups in a list
        List<User_Group_Info> all_groups = new ArrayList<User_Group_Info>();
        LogRec.log.info("1.User grouping begin!");
        for(Map.Entry<Datacenter,List<User_Group_Info>> entry: ugs.entrySet()){
            for(User_Group_Info ugi : entry.getValue()){
                all_groups.add(ugi);
            }

        }

        LogRec.log.info("User grouping finished! Tree number:" + all_groups.size());
        LogRec.log.info("*********************************");

        //now, begin calculating for primary tree
        LogRec.log.info("2.Primary tree calculating begin!");
        List<SpanningTree> all_trees = new ArrayList<SpanningTree>();//
        for(User_Group_Info ugi: all_groups){
            boolean allocated_flag = false;
            for(int m = ugi.modulation_level; m >= 1 && !allocated_flag; m--){
                int required_slots = (int) Math.ceil(mr.capacity / modulation_selecting.get_capacity(m));
                //get the distance limited by ML
                double distance_limit = modulation_selecting.generate_Smn(m, ugi.users.size());
                int slot_i = 0;
                //construct a SWP
                for(; slot_i < Resource.SLOTS_NO - required_slots + 1; slot_i++){
                    Multicast_Graph SWP = global_graph.draw_SWP(global_graph, slot_i, required_slots);
                    SpanningTree tree = new Constrained_Steiner_Tree(SWP).get_tree_w_src(ugi.dc.vertex.get_id(), ugi.users, distance_limit);
                    if(tree == null){
                        continue;
                    }
                    else{
                        if(allocate_resource(tree, slot_i, required_slots, m, mr)){
                            tree.setModulationLevel(m);
                            tree.setStartSlots(slot_i);
                            tree.setUseSlots(required_slots);
                            all_trees.add(tree);
                            allocated_flag = true;
                            break;
                        }
                        else
                            return false;
                    }
                }
            }
        }
        if(all_trees.size() != all_groups.size()){
            LogRec.log.info("Not all trees are allocated! Success no:" + all_trees.size() + ", total no:" + all_groups.size());
//            release_occupied_slots(mr);
            return false;
        }
        LogRec.log.info("Here, successfully allocate resource for SLTs for MR:" + mr.id);
        for(SpanningTree st: all_trees){
            st.print_tree();
        }
        //end for allocating resources for all SLT of MR

        //now, calculate backup paths for each SD pair
        LogRec.log.info("*********************************");
        LogRec.log.info("3.Backup paths calculating begin!");
        List<Path> all_primary_paths = new ArrayList<Path>();
        for(SpanningTree tree: all_trees){
            all_primary_paths.addAll(tree.getPaths_of_tree());
        }
        //BG, to save backup paths originated from datacenter
        Map<Datacenter, ArrayList<Path>> bp_group = new HashMap<Datacenter, ArrayList<Path>>();
        //ascending order
        Collections.sort(all_primary_paths);
        for(Path path: all_primary_paths){
            BaseVertex dc = path.get_src();
            BaseVertex user = path.get_dst();
            Multicast_Graph auxiliary_g = new Multicast_Graph(global_graph);
            update_graph(auxiliary_g, mr.id, path);
            Set<Datacenter> dcs_w_s = global_graph.get_multicast_service(mr.req_service).getInDCs();
            Set<BaseVertex> dcs = new HashSet<BaseVertex>();
            //to get the set of datacenters, and not include the dst of corresponding primary path
            for(Datacenter d: dcs_w_s){
                if(d.vertex.get_id() != dc.get_id()){
                    dcs.add(d.vertex);
                }
            }
            DijkstraShortestPathAlg dspa_bp = new DijkstraShortestPathAlg(auxiliary_g);
            Path backup_path = dspa_bp.get_shortest_path(dcs, user, true);
            if(backup_path != null){
                Datacenter new_dc = global_graph.getDcs().get(backup_path.get_src());
                if(bp_group.containsKey(new_dc)){
                    List<Path> paths_from_dc = bp_group.get(new_dc);
                    paths_from_dc.add(backup_path);
                }
                else{
                    ArrayList<Path> paths_from_dc = new ArrayList<Path>();
                    paths_from_dc.add(backup_path);
                    bp_group.put(new_dc, paths_from_dc);
                    LogRec.log.debug("Backup Path For SRC:" + dc.get_id() + " DST:" + user.get_id());
                    LogRec.log.debug(backup_path);
                }
            }
            else{
                LogRec.log.error("CANNOT FIND BACKUP PATHS FOR: " + dc.get_id() + " TO " + user.get_id());
                return false;
            }
        }
        LogRec.log.info("Here, successfully calculate all backup paths!");
        LogRec.log.info("*********************************");
        //now, we begin to group the paths and aggregate them to a tree
        LogRec.log.info("4.Backup trees constructing begin!");
        Map<Datacenter, List<SpanningTree>> dc_w_btrees = new HashMap<Datacenter, List<SpanningTree>>();
        for(Map.Entry<Datacenter, ArrayList<Path>> dc_bpaths: bp_group.entrySet()){
            int low_ML = get_lowML_with_paths(dc_bpaths.getValue());
            int high_ML = get_highML_with_paths(dc_bpaths.getValue());
            //here, we finish checking all the paths originate from the same datacenter in the list
            LogRec.log.debug("low ML:" + low_ML + ",high ML:" + high_ML);
            if(low_ML == high_ML){
                SpanningTree tree = new SpanningTree();
                tree.setModulationLevel(low_ML);
                tree.getPaths_of_tree().addAll(dc_bpaths.getValue());
                List<SpanningTree> temp_trees = new ArrayList<SpanningTree>();
                temp_trees.add(tree);
                dc_w_btrees.put(dc_bpaths.getKey(), temp_trees);
                continue;
            }
            Collections.sort(dc_bpaths.getValue());


        }







        return true;

    }
    //allocate resource (occupied for primary tree)
    private boolean allocate_resource(SpanningTree tree, int start_slot, int required_slot, int modulation_level, Multicast_Request multicast_request){
//        boolean result = true;
        for(Path path: tree.getPaths_of_tree()){
            Resource res;
            path.setStartSlots(start_slot);
            path.setUseSlots(required_slot);
            path.setModulationLevel(modulation_level);
            for(int i = 0;i < path.get_vertices().size() - 1;i++)
            {
                res = global_graph.get_vertex_pair_weight_index().
                        get(new Pair<Integer,Integer>(path.get_vertices().get(i).get_id(),path.get_vertices().get(i+1).get_id()));
                for(int j = start_slot;j < start_slot + required_slot; j ++)
                {
                    LogRec.log.debug("j:" + j);
                    //if the slot has been occupied by other traffic
                    if(!res.slot_can_use(j, multicast_request.id)) {
                        LogRec.log.error("ERROR: SLOT[" + j + "] HAS BEEN OCCUPIED!");
//                        release_occupied_slots(tree, start_slot, required_slot, multicast_request);
                        return false;
                    }
                    res.use_slot(j, multicast_request.id, 1);
                }
            }
        }
        return true;

    }
    //release a range of slots occupied or reserved by a certain tree of a MR
    private void release_occupied_slots(SpanningTree tree, int start_slot, int required_slot, Multicast_Request multicast_request){
        LogRec.log.debug("Now Release Resource!");
        for(Path path: tree.getPaths_of_tree()){
            path.setStartSlots(-1);
            path.setUseSlots(-1);
            path.setModulationLevel(-1);
            Resource res;
            for(int i = 0;i < path.get_vertices().size() - 1;i++)
            {
                res = global_graph.get_vertex_pair_weight_index().
                        get(new Pair<Integer,Integer>(path.get_vertices().get(i).get_id(),path.get_vertices().get(i+1).get_id()));

                res.set_slots_free_for_MR(start_slot, start_slot + required_slot - 1, multicast_request.id);
            }
        }
    }
    //release all slots occupied or reserved by a MR
    private void release_occupied_slots(Multicast_Request mr){
        for(Pair pair : global_graph.get_pair_list()){
            global_graph.get_vertex_pair_weight_index().get(pair).set_slots_free_for_MR(mr.id);
            global_graph.get_vertex_pair_weight_index().get(new Pair<BaseVertex, BaseVertex>(pair.o2, pair.o1)).set_slots_free_for_MR(mr.id);
        }
    }
    //get resource utilization
    public double get_current_resource_utilization(){
        double rate;
        int total_used_slots = 0;
        int total_slots = Resource.SLOTS_NO * global_graph.get_edge_num();
        for(Pair pair : global_graph.get_pair_list()){
            Resource res = global_graph.get_vertex_pair_weight_index().get(pair);
            total_used_slots += res.total_used_slots();
            //now check the reversed edge
            res = global_graph.get_vertex_pair_weight_index().get(new Pair<Integer, Integer>((Integer)pair.o2, (Integer) pair.o1));
            total_used_slots += res.total_used_slots();
        }
        LogRec.log.info("Total used slots:" + total_used_slots + ",total slots:" + total_slots);
        rate = (double) total_used_slots / total_slots;
        LogRec.log.info("Resource Utilization:" + rate);
        return rate;
    }

    //update cost according the Equations
    private void update_graph(Multicast_Graph mg, int traffic_id, Path p){
        //path p denotes the primary path

        //first, we set the cost used by other backup paths of the same traffic to 0
        for(Pair<Integer, Integer> pair: mg.get_pair_list()){
            Resource res = mg.get_vertex_pair_weight_index().get(pair);
            if(res.reserved_slots_for_traffic(traffic_id) > 0){
                res.setCost(0);
            }
            res = mg.get_vertex_pair_weight_index().get(new Pair<Integer, Integer>(pair.o2, pair.o1));
            if(res.reserved_slots_for_traffic(traffic_id) > 0){
                res.setCost(0);
            }
        }
        //second, we set the cost used by other primary paths to a small value
        for(Pair<Integer, Integer> pair: mg.get_pair_list()){
            Resource res = mg.get_vertex_pair_weight_index().get(pair);
            if(res.occupied_slots_for_traffic(traffic_id) > 0){
                res.setCost(epsilon);
            }
            res = mg.get_vertex_pair_weight_index().get(new Pair<Integer, Integer>(pair.o2, pair.o1));
            if(res.occupied_slots_for_traffic(traffic_id) > 0){
                res.setCost(epsilon);
            }
        }
        //third, we set the cost of other links
        for(Pair<Integer, Integer> pair: mg.get_pair_list()){
            Resource res = mg.get_vertex_pair_weight_index().get(pair);
            int slots_no = res.reserved_slots_except_traffic(traffic_id);
            double new_cost = res.getCost() - (double) slots_no / Resource.SLOTS_NO;
            if(slots_no > 0){
                res.setCost(new_cost);
            }
            res = mg.get_vertex_pair_weight_index().get(new Pair<Integer, Integer>(pair.o2, pair.o1));
            slots_no = res.reserved_slots_except_traffic(traffic_id);
            new_cost = res.getCost() - (double) slots_no / Resource.SLOTS_NO;
            if(slots_no > 0){
                res.setCost(new_cost);
            }
        }

        //final, we set the cost of the nodes (v) in path p to INF, expect the src and the dst
        //we put this procedure at last to avoid the cast where although we set the link cost to INF, it will be changed by other procedures.
        for(int i = 1; i < p.get_vertices().size() - 1; i++){
            BaseVertex v = p.get_vertices().get(i);
            for(BaseVertex w: mg.get_adjacent_vertices(v)){
                Resource resource = mg.get_vertex_pair_weight_index().get(new Pair<BaseVertex, BaseVertex>(v, w));
                resource.setCost(Double.MAX_VALUE);
            }
            for(BaseVertex w: mg.get_precedent_vertices(v)){
                Resource resource = mg.get_vertex_pair_weight_index().get(new Pair<BaseVertex, BaseVertex>(v, w));
                resource.setCost(Double.MAX_VALUE);
            }
        }
        //here is the case that the path has only two nodes(src and dst)
        if(p.get_vertices().size() == 2){
            Resource resource = mg.get_vertex_pair_weight_index().get(new Pair<BaseVertex, BaseVertex>(p.get_src(), p.get_src()));
            resource.setCost(Double.MAX_VALUE);
        }

    }


    //the highest ML can adopt, i.e. each path can form a SLT, Eq.(3)
    public int get_highML_with_paths(List<Path> in_paths){
        BaseVertex one_src = in_paths.get(0).get_src();
        double shorest_dis = Double.MAX_VALUE;
        for(Path path: in_paths){
            if(one_src != path.get_src()){
                LogRec.log.error("ERROR! THE PATHS IN THIS GROUP DO NOT ORIGINATE FROM THE SAME DATACENTER!");
                return -1;
            }
            shorest_dis = shorest_dis > path.get_weight() ? path.get_weight() : shorest_dis;
        }
        return modulation_selecting.get_highest_ML(1, shorest_dis);
    }
    //consider the relation of Smn, Eq.(2)
    public int get_lowML_with_paths(List<Path> in_paths){
        BaseVertex one_src = in_paths.get(0).get_src();
        int n = in_paths.size();
        double longest_dis = 0;
        for(Path path: in_paths){
            if(one_src != path.get_src()){
                LogRec.log.error("ERROR! THE PATHS IN THIS GROUP DO NOT ORIGINATE FROM THE SAME DATACENTER!");
                return -1;
            }
            longest_dis = longest_dis < path.get_weight() ? path.get_weight() : longest_dis;
        }
        return modulation_selecting.get_highest_ML(n, longest_dis);
    }
    //this function aggregate the paths to one or several SLTs, which are originated from the same datacenter
    private void aggregate_btrees(Map<Datacenter, List<SpanningTree>> dc_w_btrees, List<Path> paths, int low_ML, int high_ML){
        Datacenter dc = global_graph.getDcs().get(paths.get(0).get_src());

        for(Path path: paths){
            boolean path_flag = false;
            for(int i = 1; i <= MB; i++){
                if(i <= dc_w_btrees.get(dc).size()){
                    double longest_dis = 0;
                    for(Path p: dc_w_btrees.get(dc).get(i - 1).getPaths_of_tree()){
                        longest_dis = longest_dis < p.get_weight() ? p.get_weight() : longest_dis;
                    }
                    int n = dc_w_btrees.get(dc).get(i - 1).getPaths_of_tree().size();
                    int m = dc_w_btrees.get(dc).get(i - 1).getModulationLevel();
                    if(modulation_selecting.generate_Smn(m, n + 1) >= path.get_weight()){
                        dc_w_btrees.get(dc).get(i - 1).add_path(path);
                        path_flag = true;
                        break;
                    }
                }
                else{
                    SpanningTree tree = new SpanningTree();
                    tree.add_path(path);
                    //set the modulation level
                    tree.setModulationLevel(modulation_selecting.modulation_select(path.get_weight()));
                    dc_w_btrees.get(dc).add(tree);
                    path_flag = true;
                    break;
                }
            }
            if(!path_flag){
                int shared_links = 0;
                SpanningTree final_tree = null;
                for(SpanningTree tree: dc_w_btrees.get(dc)){
                    int temp_shared_links = get_shared_links(tree, path);
                    if(shared_links < temp_shared_links){
                        final_tree = tree;
                        shared_links = shared_links;
                    }

                }
                //if we get the tree with most shared links
                if(final_tree != null){

                }

                else{

                }

            }
        }
    }
    //get the number of shared links between all paths in tree and the path
    private int get_shared_links(SpanningTree tree, Path path){
        Set<Pair<BaseVertex, BaseVertex>> edge_set = new HashSet<Pair<BaseVertex, BaseVertex>>();
        for(int i = 0; i < path.get_vertices().size() - 1; i++){
            edge_set.add(new Pair<BaseVertex, BaseVertex>(path.get_vertices().get(i), path.get_vertices().get(i + 1)));
        }
        Set<Pair<BaseVertex, BaseVertex>> edge_set_of_tree = new HashSet<Pair<BaseVertex, BaseVertex>>();
        for(Path p: tree.getPaths_of_tree()){
            for(int i = 0; i < p.get_vertices().size() - 1; i++){
                edge_set_of_tree.add(new Pair<BaseVertex, BaseVertex>(p.get_vertices().get(i), p.get_vertices().get(i + 1)));
            }
        }
        edge_set.retainAll(edge_set_of_tree);
        return edge_set.size();
    }


}
