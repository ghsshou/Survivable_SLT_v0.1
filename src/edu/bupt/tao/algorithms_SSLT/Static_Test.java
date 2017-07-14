package edu.bupt.tao.algorithms_SSLT;

import edu.bupt.tao.traffic_SSLT.basic_model.Multicast_Request;

/**
 * Created by Gao Tao on 2017/7/14.
 */
public class Static_Test {
    public static void main(String[] args) {
        String pro_type = "Full";
        int test_num = 7;
        MainProcedure mp = new MainProcedure(2,2,pro_type,true);
        Multicast_Request[] mrs = new Multicast_Request[10];
        mrs[0] = new Multicast_Request(0,0, new int[]{2,5}, 50);
        mrs[1] = new Multicast_Request(1,0, new int[]{1,4}, 75);
        mrs[2] = new Multicast_Request(2,0, new int[]{1,2,4}, 100);
        mrs[3] = new Multicast_Request(3,0, new int[]{4}, 125);
        mrs[4] = new Multicast_Request(4,0, new int[]{2,4}, 150);
        mrs[5] = new Multicast_Request(5,0, new int[]{1,2}, 175);
        mrs[6] = new Multicast_Request(6,0, new int[]{2}, 200);
        mrs[7] = new Multicast_Request(7,0, new int[]{1,5}, 225);
        mrs[8] = new Multicast_Request(8,0, new int[]{4,5}, 250);
        mrs[9] = new Multicast_Request(9,0, new int[]{5}, 275);


        for(int i = 0; i < test_num; i++){
            System.out.println("Traffic:" + i);
            mp.getXxx_algo_2().procedure_for_one_MR(mrs[i],pro_type);
        }
        System.out.println(mp.getXxx_algo_2().get_max_used_slot_index());

    }
}
