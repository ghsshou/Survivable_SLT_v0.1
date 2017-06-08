package edu.bupt.tao.algorithms_SSLT;

import edu.bupt.tao.content_graph.ContentGraph;
import edu.bupt.tao.content_graph.edu.bupt.tao.content_graph.model.Datacenter;
import edu.bupt.tao.graph.base_algorithms.DijkstraShortestPathAlg;
import edu.bupt.tao.graph.model.ModulationSelecting;
import edu.bupt.tao.graph.model.Path;
import edu.bupt.tao.traffic_SSLT.basic_model.Multicast_Request;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by Gao Tao on 2017/6/8.
 */
public class User_Grouping {
    ContentGraph contentGraph;
    Multicast_Request mr;
    DijkstraShortestPathAlg dspa;
    ModulationSelecting modulationSelecting = new ModulationSelecting();
    public int MP = 3;//maximum number of SLTs can be constructed
    private Map<Datacenter, List<Set<Integer>>> user_group;

    public User_Grouping(ContentGraph contentGraph, Multicast_Request mr) {
        this.contentGraph = contentGraph;
        this.mr = mr;
        dspa = new DijkstraShortestPathAlg(this.contentGraph);
        user_group = new HashMap<>();
    }
    private Map<Integer, List<Path>> paths_caculate(){







    }
    public Map<Datacenter, List<Set<Integer>>> grouping_algo1(){



        return user_group;

    }




}
