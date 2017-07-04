package edu.bupt.tao.algorithms_SSLT;

import edu.bupt.tao.LogRec;
import edu.bupt.tao.content_graph.edu.bupt.tao.content_graph.model.Datacenter;
import edu.bupt.tao.edu.bupt.tao.graph_SSLT.Multicast_Graph;
import edu.bupt.tao.traffic_SSLT.basic_model.Multicast_Request;
import edu.bupt.tao.traffic_SSLT.basic_model.Multicast_Service;
import edu.bupt.tao.traffic_SSLT.basic_model.TrafficManager;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Gao Tao on 2017/6/27.
 */
public class MainProcedure {
    //set parameters:
    public double lambda;
    public double mu;
    public int max_p_tree_num;
    public int max_b_tree_num;
    public String protection_flag;
    //how many data to record
    private int no_record_num = 200;


    private Multicast_Graph multicast_graph = new Multicast_Graph("data/cost239", false);
    private XXX_Algo_2 xxx_algo_2;
    private TrafficManager traffic_manager;
    private Timer timer = new Timer();
    private int success_num = 0;
    private int tree_num = 0;
    //about resource utilization recording
    private double[] resource_utilization;

    public double getFinal_resource_utilization() {
        return final_resource_utilization;
    }

    public double getFinal_blocking_probability() {
        return final_blocking_probability;
    }

    public int getFinal_tree_num() {
        return final_tree_num;
    }

    //recording result
    double final_resource_utilization;
    double final_blocking_probability;
    int final_tree_num;



    MainProcedure(){
        topology_intialize();
        xxx_algo_2 = new XXX_Algo_2(this.multicast_graph);
        traffic_manager = new TrafficManager(this.multicast_graph);
        resource_utilization = new double[traffic_manager.get_traffic_no() - no_record_num * 2];


    }
    MainProcedure(double lambda, double mu, int max_p_tree_num, int max_b_tree_num, String protection_flag){
        topology_intialize();
        this.lambda = lambda;
        this.mu = mu;
        this.max_b_tree_num = max_b_tree_num;
        this.max_p_tree_num =max_p_tree_num;
        this.protection_flag = protection_flag;
        xxx_algo_2 = new XXX_Algo_2(this.multicast_graph);
        traffic_manager = new TrafficManager(this.multicast_graph, lambda, mu);
        resource_utilization = new double[traffic_manager.get_traffic_no() - no_record_num * 2];


    }
    private void topology_intialize(){
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

    }
    public void execute_function(){
//        System.out.println("Traffic Load:" + (int)(lambda / mu));
        traffic_send(this.traffic_manager);
//        System.out.println("Resource Utilization\tBlocking Probability\tTotal Tree Number");
        this.final_resource_utilization = get_avaerage(resource_utilization);
        this.final_blocking_probability = 1 - (double)success_num / traffic_manager.get_traffic_no();
        this.final_tree_num = tree_num;

//        System.out.println(String.format("%.4f",get_avaerage(resource_utilization)) + "\t" +
//                String.format("%.4f",(1 - (double)success_num / traffic_manager.get_traffic_no())) + "\t" + tree_num);

    }

    private void traffic_send(TrafficManager traffic_manager){
        for(int i = 0; i < traffic_manager.get_traffic_no(); i++){
//            System.out.println("MR[" + i + "]," + "Duetime" + traffic_manager.getPreTraffics().get(i).due_time);
            LogRec.log.debug("MR[" + i + "]");
//            if(i % 100 == 0){
//                System.out.println("Finish:" + 100.0 * i / traffic_manager.get_traffic_no() + "%");
//            }
            this.handle_traffic(traffic_manager.getPreTraffics().get(i), TrafficManager.BUILD);
            if(i >= no_record_num && i < traffic_manager.get_traffic_no() - no_record_num ){
                resource_utilization[i - no_record_num] = xxx_algo_2.get_current_resource_utilization();
            }

        }
//        System.out.println("BP:" + (1 - (double)success_num / traffic_manager.get_traffic_no()));

    }
    private synchronized void handle_traffic(Multicast_Request mr_to_handle, boolean build_or_delete){
        if(build_or_delete){
            boolean result = xxx_algo_2.procedure_for_one_MR(mr_to_handle, protection_flag);
            if(result){
                success_num++;
                tree_num += xxx_algo_2.get_tree_num(mr_to_handle);
                traffic_manager.onlineTraffic.add(mr_to_handle);
                timer.schedule(get_deletrafic_task(mr_to_handle), mr_to_handle.due_time);
            }
            else{
//                System.out.println("Block!");
                xxx_algo_2.release_occupied_slots(mr_to_handle);
            }
        }
        else{
            traffic_manager.onlineTraffic.remove(mr_to_handle);
            xxx_algo_2.release_occupied_slots(mr_to_handle);
        }
    }



    private DeleTrafficTask get_deletrafic_task(Multicast_Request t)
    {
        return new DeleTrafficTask(t);
    }

    class DeleTrafficTask extends TimerTask
    {
        Multicast_Request _task_tt;
        DeleTrafficTask(Multicast_Request tt)
        {
            _task_tt = tt;
        }

        public void run() {
            //System.out.println("test"+ new Date());
            handle_traffic(_task_tt,false);
        }

    }
    private double get_avaerage(double[] data){
        double total = 0;
        for (double aData : data) {
            total += aData;
        }
        return total / data.length;
    }

}
