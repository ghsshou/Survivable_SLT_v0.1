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
//
//        int[] req_users = new int[]{1, 5, 10, 9, 2, 6, 7};
//        Multicast_Request multicast_request = new Multicast_Request(0,0,req_users,30);
//        int[] req_users2 = new int[]{1, 10, 9, 6, 7};
//        Multicast_Request multicast_request2 = new Multicast_Request(0,0,req_users2,50);

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
//        Segment_Protection_Algo segment_protection_algo = new Segment_Protection_Algo(multicast_graph);
//        segment_protection_algo.procedure_for_one_MR(multicast_request,"Full", "Path");
//        segment_protection_algo.procedure_for_one_MR(multicast_request2,"Full", "Path");

        File results = new File("SegProResults.txt");
        int file_counter = 1;
        while(results.exists()){
//            results.delete();
            results = new File("SegProResults" + file_counter + ".txt");
            file_counter++;
        }
        System.out.print("Results File Name: SegProResults");
        if(file_counter >= 1){
            System.out.print("_" + --file_counter);
        }
        System.out.println(".txt");


        try {
            FileWriter fw_1 = new FileWriter(results,true);
            BufferedWriter bw_1 = new BufferedWriter(fw_1);
            bw_1.write("~[SIMULATION INFO:" + "1. Topology:" + MainProcedureSeg.topo_file_name +"]~");
            System.out.println("~[SIMULATION INFO:" + "1. Topology:" + MainProcedureSeg.topo_file_name +"]~");
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
        String[] pro_type = new String[] {Segment_Protection_Algo.sharing, Segment_Protection_Algo.full};
        String[] seg_or_path = new String[] {Segment_Protection_Algo.seg_pro};
        boolean[] distributed_flag = new boolean[] {true, false};
        for(boolean distributed_or_not: distributed_flag) {
            for (String protection : pro_type) {
                for (String seg_flag : seg_or_path) {
                    System.out.println("\nLOOP:" + case_counter + " [Distributed Light Tree:" + distributed_or_not + "," + protection + ", " + seg_flag + "]");
                    //data structure to store data;
                    double[] resource_utilization = new double[group_numer];
                    double[] blocking_probability = new double[group_numer];
                    double[] failure_notation_time = new double[group_numer];
//                    int[] tree_num = new int[group_numer];
                    int index = 0;
                    double variable_lambda = lambda;
                    while (index < group_numer) {
                        System.out.println("\nsub-Task:" + (index + 1) + "/" + group_numer);
                        MainProcedureSeg mps = new MainProcedureSeg(variable_lambda, duetime, protection, distributed_or_not, seg_flag);
                        mps.execute_function_seg();
                        resource_utilization[index] = mps.getFinal_resource_utilization();
                        blocking_probability[index] = mps.getFinal_blocking_probability();
                        failure_notation_time[index] = mps.getFinal_notation_time();
                        index++;
                        variable_lambda += step;
                    }

                        try (FileWriter fw = new FileWriter(results, true)) {
                            BufferedWriter bw = new BufferedWriter(fw);
                            bw.write("\nLOOP:" + case_counter + " [Distributed Light Tree:" + distributed_or_not + "," + protection + ", " + seg_flag + "]");
                            bw.write("\n**********Relevant parameters: Slot:" + Resource.SLOTS_NO + ", Traffic Num:" +
                                    TrafficManager._traffic_NUM + " ****************\n");
                            bw.write("Traffic Load:\n");
                            for (index = 0; index < group_numer; index++) {
                                bw.write(Math.ceil((lambda + index * step) / duetime) + " ");
                            }
                            bw.write("\nResource Utilization:\n");
                            for (int i = 0; i < resource_utilization.length; i++) {
                                bw.write(String.format("%.4f", resource_utilization[i]) + " ");
                            }
                            bw.write("\nBlocking Probability\n");
                            for (int i = 0; i < blocking_probability.length; i++) {
                                bw.write(String.format("%.4f", blocking_probability[i]) + " ");
                            }
                            bw.write("\nFailure Notation Time\n");
                            for (int i = 0; i < failure_notation_time.length; i++) {
                                bw.write(String.format("%.4f",failure_notation_time[i]) + " ");

                            }
                            bw.close();
                            fw.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        finally {

                        }
                        case_counter++;
                }
            }
        }



        System.out.println("\nFinished");
        System.exit(0);
    }
}
