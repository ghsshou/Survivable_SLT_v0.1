package edu.bupt.tao.segment_pro;

import edu.bupt.tao.LogRec;
import edu.bupt.tao.edu.bupt.tao.graph_SSLT.Multicast_Graph;
import edu.bupt.tao.graph.model.Path;
import edu.bupt.tao.traffic_SSLT.basic_model.Multicast_Request;
import edu.bupt.tao.traffic_SSLT.basic_model.TrafficManager;

import java.util.TimerTask;


/**
 * Created by Gao Tao on 2017/9/24.
 */
public class MainProcedureSeg extends edu.bupt.tao.algorithms_SSLT.MainProcedure {

    private Segment_Protection_Algo segment_protection_algo;
    String seg_or_path;


    double final_notation_time;


    MainProcedureSeg(double lambda, double mu, String protection_flag, boolean distributed_or_not, String seg_or_path){
        super();
        this.multicast_graph = new Multicast_Graph(topo_file_name, false);
        topology_initialize(distributed_or_not);
        this.lambda = lambda;
        this.mu = mu;
        this.protection_flag = protection_flag;
        segment_protection_algo = new Segment_Protection_Algo(this.multicast_graph);
        traffic_manager = new TrafficManager(this.multicast_graph, lambda, mu);
        resource_utilization = new double[traffic_manager.get_traffic_no() - no_record_num * 2];
        this.seg_or_path = seg_or_path;
    }

    protected void execute_function_seg(){
//        System.out.println("Traffic Load:" + (int)(lambda / mu));
        traffic_send_seg(this.traffic_manager);
//        System.out.println("Resource Utilization\tBlocking Probability\tTotal Tree Number");
        this.final_resource_utilization = get_avaerage(resource_utilization);
        this.final_blocking_probability = 1 - (double)success_num / traffic_manager.get_traffic_no();
        this.final_notation_time = segment_protection_algo.getTotal_distance() / (Path.transSpeed * this.success_num);
//        this.final_tree_num = tree_num;

//        System.out.println(String.format("%.4f",get_avaerage(resource_utilization)) + "\t" +
//                String.format("%.4f",(1 - (double)success_num / traffic_manager.get_traffic_no())) + "\t" + tree_num);
    }

    protected void traffic_send_seg(TrafficManager traffic_manager){
        for(int i = 0; i < traffic_manager.get_traffic_no(); i++){
            LogRec.log.debug("MR[" + i + "]");
            if(i % (TrafficManager._traffic_NUM / 10) == 0){
//                System.out.println("Finish:" + 100.0 * i / traffic_manager.get_traffic_no() + "%");
                System.out.print("*");
            }
            this.handle_traffic_seg(traffic_manager.getPreTraffics().get(i), TrafficManager.BUILD);
            if(i >= no_record_num && i < traffic_manager.get_traffic_no() - no_record_num ){
                resource_utilization[i - no_record_num] = segment_protection_algo.get_current_resource_utilization();
            }
            try {
                Thread.sleep(traffic_manager.getPreSleepTime()[i]);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }
//        System.out.println("BP:" + (1 - (double)success_num / traffic_manager.get_traffic_no()));
    }
    protected synchronized void handle_traffic_seg(Multicast_Request mr_to_handle, boolean build_or_delete){
        if(build_or_delete){
            boolean result = segment_protection_algo.procedure_for_one_MR(mr_to_handle, protection_flag, seg_or_path);
            if(result){
                success_num++;
//                System.out.println(tree_num);
                traffic_manager.onlineTraffic.add(mr_to_handle);
                timer.schedule(get_deletrafic_task_seg(mr_to_handle), mr_to_handle.due_time);
            }
            else{
//                System.out.println("Block!");
                segment_protection_algo.release_all_slots(mr_to_handle);
            }
        }
        else{
            traffic_manager.onlineTraffic.remove(mr_to_handle);
            segment_protection_algo.release_all_slots(mr_to_handle);
        }
    }

    protected DeleTrafficTaskSeg get_deletrafic_task_seg(Multicast_Request t)
    {
        return new DeleTrafficTaskSeg(t);
    }

    class DeleTrafficTaskSeg extends TimerTask
    {
        Multicast_Request _task_tt;
        DeleTrafficTaskSeg(Multicast_Request tt)
        {
            _task_tt = tt;
        }

        public void run() {
            //System.out.println("test"+ new Date());
            handle_traffic_seg(_task_tt,false);
        }

    }
    public double getFinal_notation_time() {
        return final_notation_time;
    }






}
