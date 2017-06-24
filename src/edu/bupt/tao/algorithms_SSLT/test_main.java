package edu.bupt.tao.algorithms_SSLT;

import edu.bupt.tao.LogRec;
import edu.bupt.tao.content_graph.edu.bupt.tao.content_graph.model.Datacenter;
import edu.bupt.tao.edu.bupt.tao.graph_SSLT.Multicast_Graph;
import edu.bupt.tao.edu.bupt.tao.graph_SSLT.SpanningTree;
import edu.bupt.tao.graph.base_algorithms.Constrained_Steiner_Tree;
import edu.bupt.tao.traffic_SSLT.basic_model.Multicast_Request;
import edu.bupt.tao.traffic_SSLT.basic_model.Multicast_Service;

import org.apache.log4j.Logger;

import java.util.HashSet;
import java.util.Set;


/**
 * Created by Gao Tao on 2017/6/11.
 */
public class test_main {


    public static void main(String[] args) {

        Multicast_Graph multicast_graph = new Multicast_Graph("data/n6e8", false);
        Datacenter dc1 = new Datacenter(multicast_graph.get_vertex(0),100);
        Datacenter dc2 = new Datacenter(multicast_graph.get_vertex(3),200);
//        Datacenter dc3 = new Datacenter(multicast_graph.get_vertex(8),200);

        multicast_graph.addDc(dc1);
        multicast_graph.addDc(dc2);
//        multicast_graph.addDc(dc3);
        Multicast_Service multicast_service_1 = new Multicast_Service(0,20);
        multicast_service_1.addCopyToDC(dc1);
        multicast_service_1.addCopyToDC(dc2);
//        multicast_service_1.addCopyToDC(dc3);
        multicast_graph.addMulticast_services(multicast_service_1);

//        int[] req_users = new int[]{1, 5, 10, 9, 2, 6, 7};
//        Multicast_Request multicast_request = new Multicast_Request(0,0,req_users,30);
//        User_Grouping user_grouping = new User_Grouping(multicast_graph,multicast_request);
//        user_grouping.grouping_algo1();

//        Constrained_Steiner_Tree constrained_steiner_tree = new Constrained_Steiner_Tree(multicast_graph);
////        constrained_steiner_tree.print_tree(constrained_steiner_tree._FloydAlgo());
//        LogRec.log.info("TEST TREE CONSTRUCTING!!!!!!!");
//        Set<Integer> users = new HashSet<Integer>();
//        users.add(3);
//        users.add(9);
//        users.add(5);
//        users.add(8);
//        SpanningTree spanning_tree = constrained_steiner_tree.get_tree_w_src(0, users, 9999);
//        spanning_tree.print_tree();
//        int[] users = new int[]{1, 5, 10, 9, 2, 6, 7};
        int[] users = new int[]{1, 5, 2, 4};
        Multicast_Request test_mr = new Multicast_Request(1,0, users, 50);
        XXX_Algo_2 algo_2 = new XXX_Algo_2(multicast_graph);
        algo_2.procedure_for_one_MR(test_mr);
        algo_2.get_current_resource_utilization();
        LogRec.log.info("*****************TRAFFIC ID 2***********************************************************");
//        int[] users_2 = new int[]{1, 5, 10, 9, 2, 6, 7};
        int[] users_2 = new int[]{1, 5, 2, 4};
        Multicast_Request test_mr_2 = new Multicast_Request(2,0, users, 50);
        algo_2.procedure_for_one_MR(test_mr_2);
        algo_2.get_current_resource_utilization();



    }
}
