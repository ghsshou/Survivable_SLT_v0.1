package edu.bupt.tao.segment_pro;


import edu.bupt.tao.graph.model.abstracts.BaseVertex;

import java.util.List;
import java.util.Vector;

/**
 * Created by Gao Tao on 2017/9/19.
 */
public class Segment {


    List<BaseVertex> _vertex_list = new Vector<>();
    private static int counter = -1;
    double _weight = 0;
    int id;
    private boolean type;//denote the primary (true) or backup (false)
    private int pro_traffic_id = -1;//if it is a primary seg, the var denotes the MR id it belongs, else it denotes the MR ID it protects
    private int pro_tree_id = -1;//if it is a primary seg, the var denotes the tree id it belongs, else it denotes the tree ID it protects
    private int pro_seg_id = -1;//if it is a primary seg, the var denotes the backup segment protecting it, else it denotes the seg ID it protects

    public boolean isType() {
        return type;
    }

    public void setType(boolean type) {
        this.type = type;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setPro_traffic_id(int pro_traffic_id) {
        this.pro_traffic_id = pro_traffic_id;
    }

    public void setPro_tree_id(int pro_tree_id) {
        this.pro_tree_id = pro_tree_id;
    }

    public void setPro_seg_id(int pro_seg_id) {
        this.pro_seg_id = pro_seg_id;
    }

    public int getPro_traffic_id() {
        return pro_traffic_id;
    }

    public int getPro_tree_id() {
        return pro_tree_id;
    }

    public int getPro_seg_id() {
        return pro_seg_id;
    }

    public Segment(){
        counter++;
        this.id = counter;

    }
    public Segment(boolean type){
        counter++;
        this.id = counter;
        this.type = type;

    }
    public Segment(List<BaseVertex> _vertex_list, double _weight)
    {
        this._vertex_list = _vertex_list;
        this.counter ++;
        this.id = counter;
        this._weight = _weight;
    }



    public void set_weight(double _weight) {
        this._weight = _weight;
    }

    public List<BaseVertex> get_vertex_list() {
        return _vertex_list;
    }

    public void print_segment(){
        System.out.print("SEGMENT [Type:" + type + ",ID:" + id + ",Traffic ID:" + pro_traffic_id + ",Tree ID:" + pro_tree_id + ",Seg ID:" + pro_seg_id + "]~~Path:");
        for(BaseVertex bv: _vertex_list){
            System.out.print(bv + " ");
        }
        System.out.println();

    }
}
