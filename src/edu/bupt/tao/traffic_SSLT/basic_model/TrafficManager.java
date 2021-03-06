package edu.bupt.tao.traffic_SSLT.basic_model;

import edu.bupt.tao.LogRec;
import edu.bupt.tao.edu.bupt.tao.graph_SSLT.Multicast_Graph;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by Gao Tao on 2017/6/27.
 */
public class TrafficManager {
    Multicast_Graph multicast_graph;


    public static final int _traffic_NUM = 2000;
    public static final boolean BUILD = true;
//    public static final boolean DELE = false;

    private int min_users = 1;
    /*
    minus_users denotes the number that will be subtracted from the number of total number of nodes.
    control_probability denotes that when a number generated randomly is larger than control_probability, then a new traffic that
    can select any number from the range from 'min_users' to the number of total number of nodes will be created. Otherwise, the
    new created traffic will randomly select the user size according to 'minus_users'.
     */
    private int minus_users = 6;//denote the number of users that
    private float control_probability = 0;


    static int traffic_counter = 0;
    public double setLambda;//0.01
    public double setDurTimeFactor;//0.0001

//    public static final double UpperCapacity = 150.0;
//    public static final double LowerCapacity = 50.0;
    private static final double[] optional_Capacity = new double[]{50, 75, 100, 125, 150, 175, 200, 225, 250, 275, 300};
    private List<Multicast_Request> preTraffics = new Vector<>();

    public int[] getPreSleepTime() {
        return preSleepTime;
    }

    private int[] preSleepTime = new int[_traffic_NUM];
    public List<Multicast_Request> onlineTraffic = new CopyOnWriteArrayList<>();

    public TrafficManager(Multicast_Graph multicast_graph){
        this.multicast_graph = multicast_graph;
        Random rand = new Random(1001);
        for(int i = 0; i <_traffic_NUM; i++){
            LogRec.log.debug("Generate MR:" + i);
            preSleepTime[i] = (int) nextTime(setLambda);
            preTraffics.add(new_MR(rand, minus_users));
        }

    }


    public TrafficManager(Multicast_Graph multicast_graph, double lambda, double duetime){
        this.multicast_graph = multicast_graph;
        this.setLambda = lambda;
        this.setDurTimeFactor = duetime;
        Random rand = new Random(1001);
        for(int i = 0; i <_traffic_NUM; i++){
            preSleepTime[i] = (int) nextTime(setLambda);
            preTraffics.add(new_MR(rand, minus_users));
        }

    }

    //generate a MR according to the rand
    private Multicast_Request new_MR(Random rand, int minimus_users){
        int req_service = rand.nextInt(multicast_graph.getMulticast_services().keySet().size());
        //decide how many users in this MR
        int max_nodes_num_larger = multicast_graph.get_vertex_num()
                - multicast_graph.getDcs().keySet().size();
        int max_nodes_num_smaller = max_nodes_num_larger - minimus_users;
        int user_size = 0;
        if(Math.random() > control_probability){
            user_size = rand.nextInt(max_nodes_num_larger - min_users)
                    + min_users;
        }
        else{
            user_size = rand.nextInt(max_nodes_num_smaller - min_users)
                    + min_users;
        }
        //get the nodes set that can be chosen as users
        List<Integer> optional_nodes = new ArrayList<>();
        for(int i = 0; i < multicast_graph.get_vertex_num(); i ++){
            if(multicast_graph.isDC(i)){
                continue;
            }
            optional_nodes.add(i);
        }



        int[] users = new int[user_size];
        LogRec.log.debug("USER SIZE:" + user_size);
        for(int i = 0; i < user_size; i ++){
            int user = rand.nextInt(optional_nodes.size());
            users[i] = optional_nodes.get(user);
            optional_nodes.remove(user);
        }
        int select_capacity = rand.nextInt(optional_Capacity.length);
        long duetime = (int) nextTime(setDurTimeFactor);
        Multicast_Request new_MR = new Multicast_Request(traffic_counter, req_service,
                users, optional_Capacity[select_capacity], duetime);
        traffic_counter ++;

        return new_MR;
    }

//    private Multicast_Request new_MR(){
//        int req_service = java.util.concurrent.ThreadLocalRandom.current()
//                .nextInt(multicast_graph.getMulticast_services().keySet().size());
//        //decide how many users in this MR
//        int max_nodes_num = multicast_graph.get_vertex_num() - multicast_graph.getDcs().keySet().size();
//        int user_size = java.util.concurrent.ThreadLocalRandom.current()
//                .nextInt(max_nodes_num - min_users)
//                + min_users;
//
//        //get the nodes set that can be chosen as users
//        List<Integer> optional_nodes = new ArrayList<>();
//        for(int i = 0; i < multicast_graph.get_vertex_num(); i ++){
//            if(multicast_graph.isDC(i)){
//                continue;
//            }
//            optional_nodes.add(i);
//        }
//
//        int[] users = new int[user_size];
//        LogRec.log.debug("USER SIZE:" + user_size);
//        for(int i = 0; i < user_size; i ++){
//            int user = java.util.concurrent.ThreadLocalRandom.current()
//                    .nextInt(optional_nodes.size());
//            users[i] = optional_nodes.get(user);
//            optional_nodes.remove(user);
//        }
//        int select_capacity = java.util.concurrent.ThreadLocalRandom.current()
//                .nextInt(optional_Capacity.length);
//        long duetime = (int) nextTime(setDurTimeFactor);
//        Multicast_Request new_MR = new Multicast_Request(traffic_counter, req_service,
//                users, optional_Capacity[select_capacity], duetime);
//        traffic_counter ++;
//        return new_MR;
//    }

    private double nextTime(double lambda){
        return - Math.log(Math.random()) / lambda;
    }
    public int get_traffic_no(){
        return preTraffics.size();
    }

    public List<Multicast_Request> getPreTraffics() {
        return preTraffics;
    }
}
