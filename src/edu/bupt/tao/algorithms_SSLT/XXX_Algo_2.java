package edu.bupt.tao.algorithms_SSLT;

import edu.bupt.tao.LogRec;
import edu.bupt.tao.content_graph.edu.bupt.tao.content_graph.model.Datacenter;
import edu.bupt.tao.edu.bupt.tao.graph_SSLT.Multicast_Graph;
import edu.bupt.tao.edu.bupt.tao.graph_SSLT.SpanningTree;
import edu.bupt.tao.graph.base_algorithms.Constrained_Steiner_Tree;
import edu.bupt.tao.graph.edu.bupt.tao.graph.resource.Resource;
import edu.bupt.tao.graph.edu.bupt.tao.graph.resource.Slot;
import edu.bupt.tao.graph.model.ModulationSelecting;
import edu.bupt.tao.graph.model.Pair;
import edu.bupt.tao.graph.model.Path;
import edu.bupt.tao.traffic_SSLT.basic_model.Multicast_Request;
import java.lang.Math;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static java.lang.Math.*;

/**
 * Created by Gao Tao on 2017/6/16.
 * Algo_2 is the main algorithm
 * XXX denotes that the algorithm is not named yet
 */
public class XXX_Algo_2 {
    Multicast_Graph global_graph;
    ModulationSelecting modulation_selecting;


    public XXX_Algo_2(){}
    public XXX_Algo_2(Multicast_Graph g){
        this.global_graph = g;
        modulation_selecting = new ModulationSelecting();
        LogRec.log.debug("Edge no:" + g.get_edge_num());
    }

    //return false to block mr
    public boolean procedure_for_one_MR(Multicast_Request mr){
        Map<Datacenter, List<User_Group_Info>> ugs = new User_Grouping(global_graph, mr).grouping_algo1();
        //store all groups in a list
        List<User_Group_Info> all_groups = new ArrayList<User_Group_Info>();
        for(Map.Entry<Datacenter,List<User_Group_Info>> entry: ugs.entrySet()){
            for(User_Group_Info ugi : entry.getValue()){
                all_groups.add(ugi);
            }

        }
        LogRec.log.info("*********************************");
        LogRec.log.info("User grouping finished! Tree number:" + all_groups.size());
        //now begin calculating for primary tree
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
                    }
                }
            }
        }
        if(all_trees.size() != all_groups.size()){
            LogRec.log.info("Not all trees are allocated! Success no:" + all_trees.size() + ", total no:" + all_groups.size());
            release_occupied_slots(mr);
            return false;
        }
        LogRec.log.info("Here, successfully allocate resource for SLTs for MR:" + mr.id);
        for(SpanningTree st: all_trees){
            st.print_tree();
        }
        //end for allocating resources for all SLT of MR
        List<Path> all_primary_paths = new ArrayList<Path>();
        for(SpanningTree tree: all_trees){
            all_primary_paths.addAll(tree.getPaths_of_tree());
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
                        release_occupied_slots(tree, start_slot, required_slot, multicast_request);
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

}
