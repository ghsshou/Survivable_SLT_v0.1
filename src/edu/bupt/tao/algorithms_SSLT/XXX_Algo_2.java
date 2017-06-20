package edu.bupt.tao.algorithms_SSLT;

import edu.bupt.tao.content_graph.edu.bupt.tao.content_graph.model.Datacenter;
import edu.bupt.tao.edu.bupt.tao.graph_SSLT.Multicast_Graph;
import edu.bupt.tao.traffic_SSLT.basic_model.Multicast_Request;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by Gao Tao on 2017/6/16.
 * Algo_2 is the main algorithm
 * XXX denotes that the algorithm is not named yet
 */
public class XXX_Algo_2 {
    Multicast_Graph global_graph;



    public boolean procedure_for_one_MR(Multicast_Request mr){
        Map<Datacenter, List<User_Group_Info>> ugs = new User_Grouping(global_graph, mr).grouping_algo1();
        //store all groups in a list
        List<User_Group_Info> all_groups = new ArrayList<>(User_Group_Info);
        for(Map.Entry<Datacenter,List<User_Group_Info>> entry: ugs.entrySet()){
            for(User_Group_Info ugi : entry.getValue()){
                all_groups.add(ugi);
            }

        }
        for(User_Group_Info ugi: all_groups){
            for(int m = ugi.modulation_level; m >=1; m--){
                double required_slots = mr.capacity / (m * )
            }
        }
    }

}
