package edu.bupt.tao.edu.bupt.tao.graph_SSLT;


import edu.bupt.tao.LogRec;
import edu.bupt.tao.content_graph.edu.bupt.tao.content_graph.model.Datacenter;
import edu.bupt.tao.graph.edu.bupt.tao.graph.resource.Resource;
import edu.bupt.tao.graph.model.Graph;
import edu.bupt.tao.graph.model.Pair;
import edu.bupt.tao.graph.model.VariableGraph;
import edu.bupt.tao.graph.model.abstracts.BaseVertex;
import edu.bupt.tao.traffic_SSLT.basic_model.Multicast_Service;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by Gao Tao on 2017/6/11.
 */
public class Multicast_Graph extends VariableGraph {
    public Map<BaseVertex,Datacenter> dcs = new HashMap<BaseVertex, Datacenter>();
    public Map<Integer,Multicast_Service> multicast_services = new HashMap<Integer, Multicast_Service>();
    public static int INF  = 99999;
    int multicast_service_min_copies = 3;
    public Multicast_Graph(Graph graph, boolean new_resource) {
        super(graph, new_resource);
    }
    public Multicast_Graph(Graph graph) {
        super(graph);
    }
    public Multicast_Graph(String data_file_name, boolean w_cost) {
        super(data_file_name, w_cost);
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

    public Map<Integer, Multicast_Service> getMulticast_services() {
        return multicast_services;
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

    public Multicast_Graph draw_SWP(Multicast_Graph old_graph, int start_slot, int required_slots){
        Multicast_Graph multicast_graph = new Multicast_Graph(old_graph);
//        multicast_graph.get_vertex_list().addAll(old_graph.get_vertex_list());
//        multicast_graph.get_id_vertex_index().putAll(old_graph.get_id_vertex_index());
//        LogRec.log.debug("HERE: LIST SIZE:" + old_graph.get_vertex_list().size());
//        LogRec.log.debug("HERE: LIST SIZE:" + multicast_graph.get_vertex_list().size());
//        multicast_graph.set_vertex_num(old_graph.get_vertex_num());
        multicast_graph.setDcs(old_graph.getDcs());
        multicast_graph.setMulticast_services(old_graph.getMulticast_services());
        Resource resource;
        for(Pair<Integer, Integer> pair: old_graph.get_pair_list()){
            resource = old_graph.get_vertex_pair_weight_index().get(pair);
//          LogRec.log.debug(resource.getStart_index() + "->" + resource.getEnd_index());
            if(!resource.is_free_from_in(start_slot, required_slots)){
                LogRec.log.debug("Not Free:" + resource.getStart_index() + "->" + resource.getEnd_index() );
                multicast_graph.remove_edge(new Pair<>(resource.getStart_index(), resource.getEnd_index()));
            }
            resource = old_graph.get_vertex_pair_weight_index().get(new Pair<>(pair.o2, pair.o1));
            if(!resource.is_free_from_in(start_slot, required_slots)){
                LogRec.log.debug("Not Free:" + resource.getStart_index() + "->" + resource.getEnd_index() );
                multicast_graph.remove_edge(new Pair<>(resource.getStart_index(), resource.getEnd_index()));
            }
        }
        return multicast_graph;

    }


}
