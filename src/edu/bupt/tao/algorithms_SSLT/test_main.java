package edu.bupt.tao.algorithms_SSLT;

import edu.bupt.tao.LogRec;
import edu.bupt.tao.content_graph.edu.bupt.tao.content_graph.model.Datacenter;
import edu.bupt.tao.edu.bupt.tao.graph_SSLT.Multicast_Graph;
import edu.bupt.tao.edu.bupt.tao.graph_SSLT.SpanningTree;
import edu.bupt.tao.graph.base_algorithms.Constrained_Steiner_Tree;
import edu.bupt.tao.graph.edu.bupt.tao.graph.resource.Resource;
import edu.bupt.tao.traffic_SSLT.basic_model.Multicast_Request;
import edu.bupt.tao.traffic_SSLT.basic_model.Multicast_Service;

import edu.bupt.tao.traffic_SSLT.basic_model.TrafficManager;
import org.apache.log4j.Logger;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;


/**
 * Created by Gao Tao on 2017/6/11.
 */
public class test_main {


    public static void main(String[] args) {

//        Multicast_Graph multicast_graph = new Multicast_Graph("data/cost239", false);
//        Datacenter dc1 = new Datacenter(multicast_graph.get_vertex(0),100);
//        Datacenter dc2 = new Datacenter(multicast_graph.get_vertex(3),200);
//        Datacenter dc3 = new Datacenter(multicast_graph.get_vertex(8),200);
//
//        multicast_graph.addDc(dc1);
//        multicast_graph.addDc(dc2);
//        multicast_graph.addDc(dc3);
//        Multicast_Service multicast_service_1 = new Multicast_Service(0,20);
//        multicast_service_1.addCopyToDC(dc1);
//        multicast_service_1.addCopyToDC(dc2);
//        multicast_service_1.addCopyToDC(dc3);
//        multicast_graph.addMulticast_services(multicast_service_1);

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



//        int[] users = new int[]{2,7,6,4,5,1};
//        Multicast_Request test_mr = new Multicast_Request(1,0, users, 50);
//        XXX_Algo_2 algo_2 = new XXX_Algo_2(multicast_graph);
//        algo_2.procedure_for_one_MR(test_mr, "Full");
//        algo_2.get_current_resource_utilization();
////        algo_2.release_occupied_slots(test_mr);
//        LogRec.log.info("*****************TRAFFIC ID 2***********************************************************");
////        int[] users_2 = new int[]{1, 5, 10, 9, 2, 6, 7};
//        int[] users_2 = new int[]{1, 5, 2, 4};
//        Multicast_Request test_mr_2 = new Multicast_Request(2,0, users, 50);
//        algo_2.procedure_for_one_MR(test_mr_2, "Full");
//        algo_2.get_current_resource_utilization();


        /* test traffic manager*/
//        TrafficManager tm = new TrafficManager(multicast_graph);



//        System.out.println("Sharing");
//        long time_1 = System.currentTimeMillis();
//        MainProcedure mp = new MainProcedure(0.01,0.0001,2, 2, "Sharing");
//        mp.execute_function();
//        System.out.println("TIME CONSUMPTION:" + (System.currentTimeMillis() - time_1));
//
//        System.out.println("Full");
//        long time_2 = System.currentTimeMillis();
//        MainProcedure mp2 = new MainProcedure(0.01,0.0001,2, 2, "Full");
//        mp2.execute_function();
//        System.out.println("TIME CONSUMPTION:" + (System.currentTimeMillis() - time_2));
//
//        System.out.println("No");
//        long time_3 = System.currentTimeMillis();
//        MainProcedure mp3 = new MainProcedure(0.01,0.0001,2, 2, "No");
//        mp3.execute_function();
//        System.out.println("TIME CONSUMPTION:" + (System.currentTimeMillis() - time_3));


        //for formal data recording
//        double lambda = 0.01;
//        double duetime = 0.0001;
//        int max_primary = 2;
//        int max_backup = 2;
//        String protect_type = "Sharing";
//
//        //data structure to store data;
//        double step = 0.005;
//        int group_numer = 10;
//        double[] resource_utilization = new double[group_numer];
//        double[] blocking_probability = new double[group_numer];
//        int[] tree_num = new int[group_numer];
//        long[] time_consumption = new long[group_numer];
//        int index = 0;
//
//
//
//
//        double variable_lambda = lambda;
//        while(index < group_numer){
//            System.out.println("\nTask:" + index);
//            long time_1 = System.currentTimeMillis();
//            MainProcedure mp = new MainProcedure(variable_lambda,duetime,max_primary, max_backup, protect_type, true);
//            mp.execute_function();
//            resource_utilization[index] = mp.getFinal_resource_utilization();
//            blocking_probability[index] = mp.getFinal_blocking_probability();
//
//            tree_num[index] = mp.getFinal_tree_num();
//            time_consumption[index] = System.currentTimeMillis() - time_1;
//            System.out.print("\nTraffic Load:" + (int) (variable_lambda / duetime));
//            System.out.print(",Resource Utilization:" + String.format("%.4f", resource_utilization[index]));
//            System.out.println(",Blocking Probability:" + String.format("%.4f",blocking_probability[index]));
//            index ++;
//            variable_lambda += step;
//
//        }
//        System.out.println();
//        System.out.println("Traffic Load");
//        for(index = 0; index < group_numer; index++){
//            System.out.print(Math.ceil((lambda + index * step) / duetime) + " ");
//        }
//        System.out.println();
//        System.out.println("Resource Utilization");
//        for(int i = 0; i < resource_utilization.length; i++){
//            System.out.print(String.format("%.4f", resource_utilization[i]) + " ");
//        }
//        System.out.println();
//        System.out.println("Blocking Probability");
//        for(int i = 0; i < blocking_probability.length; i++){
//            System.out.print(String.format("%.4f",blocking_probability[i]) + " ");
//        }
//        System.out.println();
//        System.out.println("Tree Number");
//        for(int i = 0; i < tree_num.length; i++){
//            System.out.print(tree_num[i] + " ");
//        }
//        System.out.println();
//        System.out.println("Time Consumption");
//        for(int i = 0; i < time_consumption.length; i++){
//            System.out.print(time_consumption[i] + " ");
//        }


        File results = new File("Results.txt");
        int file_counter = 1;
        while(results.exists()){
//            results.delete();
            results = new File("Results_" + file_counter + ".txt");
            file_counter++;
        }
        System.out.print("Results File Name: Results");
        if(file_counter >= 1){
            System.out.print("_" + --file_counter);
        }
        System.out.println(".txt");


        try {
            FileWriter fw_1 = new FileWriter(results,true);
            BufferedWriter bw_1 = new BufferedWriter(fw_1);
            bw_1.write("~[SIMULATION INFO:" + "1. Topology:" + MainProcedure.topo_file_name +"]~");
            System.out.println("~[SIMULATION INFO:" + "1. Topology:" + MainProcedure.topo_file_name +"]~");
            bw_1.close();
            fw_1.close();
        } catch (IOException e) {
            e.printStackTrace();
        }


        //for formal data recording
        double lambda = 0.01;
        double duetime = 0.0001;
        double step = 0.005;
        int group_numer = 10;
        int case_counter = 1;
        String[] pro_type = new String[] {"Sharing","Full", "None"};
        boolean[] distributed_flag = new boolean[] {false, true};
        for(boolean distributed_or_not: distributed_flag){
            for(String protection: pro_type){
                int max_primary = 1;
                int max_backup = 1;
                for(;max_backup <= 3 && max_primary <= 3; max_backup++, max_primary++){
                    if(distributed_or_not) {
                        if(max_primary < 2 && max_backup < 2)
                            continue;
                    }
                    //for the case that there is no protection, i.e. the benchmark in SLT
                    if(protection.equals("None")) {
                        if(!(max_backup == 2 && max_primary == 2 && distributed_or_not)) {
                            break;
                        }
                    }
                    if(distributed_or_not) {
                        System.out.print("\n(" + case_counter + ") ########## Distributed SLT, " + protection + ", MP:" + max_primary
                                + ", MB:" + max_backup +" ########");
                    }
                    else if(max_backup == 1 && max_primary == 1){
                        System.out.print("\n(" + case_counter + ")########## Single Tree, " + protection + " ########");
                    }
                    else {
                        System.out.print("\n(" + case_counter + ")########## Sub-Light-Tree, " + protection + ", MP:" + max_primary
                                + ", MB:" + max_backup +" ########");
                    }

                    //data structure to store data;
                    double[] resource_utilization = new double[group_numer];
                    double[] blocking_probability = new double[group_numer];
                    int[] tree_num = new int[group_numer];
                    long[] time_consumption = new long[group_numer];
                    int index = 0;
                    double variable_lambda = lambda;
                    while(index < group_numer){
                        System.out.println("\nsub-Task:" + index);
                        long time_1 = System.currentTimeMillis();
                        MainProcedure mp = new MainProcedure(variable_lambda,duetime,max_primary, max_backup, protection, distributed_or_not);
                        mp.execute_function();
                        resource_utilization[index] = mp.getFinal_resource_utilization();
                        blocking_probability[index] = mp.getFinal_blocking_probability();
                        tree_num[index] = mp.getFinal_tree_num();
                        time_consumption[index] = System.currentTimeMillis() - time_1;
                        index ++;
                        variable_lambda += step;
                    }
                    try {
                        try (FileWriter fw = new FileWriter(results, true)) {
                            BufferedWriter bw = new BufferedWriter(fw);
                            if(protection.equals("None")) {
                                if(!(max_backup == 2 && max_primary == 2 && distributed_or_not)) {
                                    break;
                                }
                            }
                            if(distributed_or_not) {
                                bw.write("\n########## Distributed SLT, " + protection + ", MP:" + max_primary
                                        + ", MB:" + max_backup +" ########\n");
                            }
                            else if(max_backup == 1 && max_primary == 1){
                                bw.write("\n########## Single Tree, " + protection + " ########\n");
                            }
                            else {
                                bw.write("\n########## Sub-Light-Tree, " + protection + ", MP:" + max_primary
                                        + ", MB:" + max_backup +" ########\n");
                            }
                            bw.write("**********Relevant parameters: Slot:" + Resource.SLOTS_NO + ", Traffic Num:" +
                                    TrafficManager._traffic_NUM + ", Pro_type:" + protection + ", Distributed_flag:" + distributed_or_not
                                    +" ****************\n");
                            bw.write("Traffic Load:\n");
                            for(index = 0; index < group_numer; index++){
                                bw.write(Math.ceil((lambda + index * step) / duetime) + " ");
                            }
                            bw.write("\nResource Utilization:\n");
                            for(int i = 0; i < resource_utilization.length; i++){
                                bw.write(String.format("%.4f", resource_utilization[i]) + " ");
                            }
                            bw.write("\nBlocking Probability\n");
                            for(int i = 0; i < blocking_probability.length; i++){
                                bw.write(String.format("%.4f",blocking_probability[i]) + " ");
                            }
                            bw.write("\nTree Number\n");
                            for(int i = 0; i < tree_num.length; i++){
                                bw.write(tree_num[i] + " ");
                            }
                            bw.write("\nTime Consumption\n");
                            for(int i = 0; i < time_consumption.length; i++){
                                bw.write(time_consumption[i] + " ");
                            }
                            bw.close();
                            fw.close();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    case_counter ++;
                }

            }
        }
        System.out.println("\nFinished");
        System.exit(0);
    }
}
