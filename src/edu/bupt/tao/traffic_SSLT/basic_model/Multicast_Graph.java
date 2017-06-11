package edu.bupt.tao.traffic_SSLT.basic_model;


import edu.bupt.tao.content_graph.edu.bupt.tao.content_graph.model.Content;
import edu.bupt.tao.content_graph.edu.bupt.tao.content_graph.model.Datacenter;
import edu.bupt.tao.graph.base_algorithms.DijkstraShortestPathAlg;
import edu.bupt.tao.graph.model.Graph;
import edu.bupt.tao.graph.model.Pair;
import edu.bupt.tao.graph.model.Path;
import edu.bupt.tao.graph.model.VariableGraph;
import edu.bupt.tao.graph.model.abstracts.BaseVertex;

import java.util.*;

/**
 * Created by Gao Tao on 2017/6/11.
 */
public class Multicast_Graph extends VariableGraph {
    public Map<BaseVertex,Datacenter> dcs = new HashMap<BaseVertex, Datacenter>();
    public Map<Integer,Multicast_Service> multicast_services = new HashMap<Integer, Multicast_Service>();
    public static int INF  = 99999;
    int multicast_service_min_copies = 2;
    public Multicast_Graph() {
        super();
    }
    public Multicast_Graph(Graph graph) {
        super(graph);
    }
    public Multicast_Graph(String data_file_name) {
        super(data_file_name);
    }


    public Map<BaseVertex, Datacenter> getDcs() {
        return dcs;
    }
    public void setDcs(Map<BaseVertex, Datacenter> dcs) {
        this.dcs = dcs;
    }
    public void addDc(Integer nodeID, Datacenter dc){
        dcs.put(this.get_vertex(nodeID), dc);
    }
    public void addDc(Datacenter dc){
        dcs.put(dc.vertex, dc);
    }
    public Multicast_Service get_multicast_service(int req_mr){
        return multicast_services.get(req_mr);
    }
    public void setMulticast_services(Map<Integer, Multicast_Service> mss) {
        this.multicast_services = mss;
    }
    public void addMulticast_services(Integer ms_ID, Multicast_Service ms){
        multicast_services.put(ms_ID, ms);
    }
    public void addMulticast_services(Multicast_Service ms){
        multicast_services.put(ms.id,ms);
    }
    public boolean isDC(int node){
        return dcs.containsKey(this.get_vertex(node));
    }
    public int getMS_no(){
        return multicast_services.size();
    }

    public int getMulticast_service_min_copies() {
        return multicast_service_min_copies;
    }
    public void printMS_Placement(){
        Iterator<Multicast_Service> it = multicast_services.values().iterator();
        System.out.println("Multicast_Service placement:");
        while(it.hasNext()){
            it.next().printDCs();
        }
    }


}
