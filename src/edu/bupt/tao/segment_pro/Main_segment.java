package edu.bupt.tao.segment_pro;

import edu.bupt.tao.algorithms_SSLT.MainProcedure;
import edu.bupt.tao.algorithms_SSLT.User_Group_Info;
import edu.bupt.tao.algorithms_SSLT.User_Grouping;
import edu.bupt.tao.content_graph.edu.bupt.tao.content_graph.model.Datacenter;
import edu.bupt.tao.edu.bupt.tao.graph_SSLT.Multicast_Graph;
import edu.bupt.tao.graph.edu.bupt.tao.graph.resource.Resource;
import edu.bupt.tao.traffic_SSLT.basic_model.Multicast_Request;
import edu.bupt.tao.traffic_SSLT.basic_model.Multicast_Service;
import edu.bupt.tao.traffic_SSLT.basic_model.TrafficManager;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by Gao Tao on 2017/9/19.
 */
public class Main_segment {
    public static void main(String[] args) {

        Multicast_Graph multicast_graph = new Multicast_Graph("data/cost239", false);
        Datacenter dc1 = new Datacenter(multicast_graph.get_vertex(0),100);
        Datacenter dc2 = new Datacenter(multicast_graph.get_vertex(3),200);
        Datacenter dc3 = new Datacenter(multicast_graph.get_vertex(8),200);

        multicast_graph.addDc(dc1);
        multicast_graph.addDc(dc2);
        multicast_graph.addDc(dc3);
        Multicast_Service multicast_service_1 = new Multicast_Service(0,20);
        multicast_service_1.addCopyToDC(dc1);
        multicast_service_1.addCopyToDC(dc2);
        multicast_service_1.addCopyToDC(dc3);
        multicast_graph.addMulticast_services(multicast_service_1);

        int[] req_users = new int[]{1, 5, 10, 9, 2, 6, 7};
        Multicast_Request multicast_request = new Multicast_Request(0,0,req_users,30);

//        User_Grouping user_grouping = new User_Grouping(multicast_graph,multicast_request,0);
//        Map<Datacenter, List<User_Group_Info>> ugis = user_grouping.grouping_dis_only();
//        Set<Map.Entry<Datacenter, List<User_Group_Info>>> entry = ugis.entrySet();
//        Iterator<Map.Entry<Datacenter, List<User_Group_Info>>> it = entry.iterator();
//        while(it.hasNext()){
//            Datacenter dcc = it.next().getKey();
//            System.out.println("DC ID:" + dcc.vertex.get_id());
//            List<User_Group_Info> ugi = ugis.get(dcc);
//            if(ugi.isEmpty()){
//                continue;
//            }
//
//            Set<Integer> users = ugi.get(0).getUsers();
//            System.out.println("USERS:");
//            for(Integer i: users){
//                System.out.print(i + " ");
//            }
//            System.out.println();
//        }
        Segment_Protection_Algo segment_protection_algo = new Segment_Protection_Algo(multicast_graph, 0);
        segment_protection_algo.procedure_for_one_MR(multicast_request,"XXX");




        System.out.println("\nFinished");
        System.exit(0);
    }
}
