package edu.bupt.tao.algorithms_SSLT;

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
    public static Logger log = Logger.getLogger("src/log4j.properties3");

    public static void main(String[] args) {

        Multicast_Graph multicast_graph = new Multicast_Graph("data/cost239", false);
        Datacenter dc1 = new Datacenter(multicast_graph.get_vertex(3),100);
        Datacenter dc2 = new Datacenter(multicast_graph.get_vertex(4),200);
        Datacenter dc3 = new Datacenter(multicast_graph.get_vertex(8),200);

        multicast_graph.addDc(dc1);
        multicast_graph.addDc(dc2);
        multicast_graph.addDc(dc3);
        Multicast_Service multicast_service_1 = new Multicast_Service(0,20);
        multicast_service_1.addCopyToDC(dc1);
        multicast_service_1.addCopyToDC(dc2);
        multicast_service_1.addCopyToDC(dc3);
        multicast_graph.addMulticast_services(multicast_service_1);

//        int[] req_users = new int[]{1, 5, 10, 9, 2, 6, 7};
//        Multicast_Request multicast_request = new Multicast_Request(0,0,req_users,30);
//        User_Grouping user_grouping = new User_Grouping(multicast_graph,multicast_request);
//        user_grouping.grouping_algo1();
        Constrained_Steiner_Tree constrained_steiner_tree = new Constrained_Steiner_Tree(multicast_graph);
//        constrained_steiner_tree.print_tree(constrained_steiner_tree._FloydAlgo());
        log.info("TEST TREE CONSTRUCTING!!!!!!!");
        Set<Integer> users = new HashSet<Integer>();
        users.add(3);
        users.add(9);
        users.add(5);
        users.add(8);
        SpanningTree spanning_tree = constrained_steiner_tree.get_tree_w_src(0, users, 9999);
        spanning_tree.print_tree();



    }
}
