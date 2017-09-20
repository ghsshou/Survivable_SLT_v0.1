package edu.bupt.tao.segment_pro;


import edu.bupt.tao.graph.model.abstracts.BaseVertex;

import java.util.List;
import java.util.Vector;

/**
 * Created by Gao Tao on 2017/9/19.
 */
public class Segment {


    List<BaseVertex> _vertex_list = new Vector<BaseVertex>();
    static int counter = -1;
    int id;
    int pro_traffic_id;
    int pro_tree_id;

    public Segment(){
        counter++;
        this.id = counter;

    }




    public List<BaseVertex> get_vertex_list() {
        return _vertex_list;
    }

    public void print_segment(){
        System.out.print("SEGMENT-" + id + ": ");
        for(BaseVertex bv: _vertex_list){
            System.out.print(bv + " ");
        }
        System.out.println();

    }
}
