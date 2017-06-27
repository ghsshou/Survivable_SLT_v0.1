package edu.bupt.tao.graph.edu.bupt.tao.graph.resource;

import java.util.*;

/**
 * Created by Gao Tao on 2017/6/8.
 */
//the info used to denote a tree and a path
class ID{
    int tree_id = -1;
    int path_id = -1;
    ID(){}
    ID(int tree_id, int path_id){
        this.path_id = path_id;
        this.tree_id = tree_id;
    }
}
public class Slot {
    boolean use_state;//0 denotes used,1 not used
    int occupy_type;//occupy 1 or reserve 0
    int traffic_occupy;//denotes the traffic id and tree id to use it (occupy)
    int tree_occupy;
//    int path_occupy;
    Map<Integer, Set<ID>> traffic_reserve = new HashMap<Integer, Set<ID>>();//denotes the traffic that reserves resource on this slot, key is traffic id

    public Slot(){
        this.use_state = true;
        this.occupy_type = -1;
        this.tree_occupy = -1;
        this.traffic_occupy = -1;

    }
    public Slot(Slot s){
        this.use_state = s.use_state;
        this.occupy_type = s.occupy_type;
        this.traffic_occupy = s.traffic_occupy;
        this.tree_occupy = s.tree_occupy;
        this.traffic_reserve.clear();
        for(Map.Entry<Integer, Set<ID>> entry: s.traffic_reserve.entrySet()){
            int new_traffic_id = entry.getKey();
            Set<ID> new_id = new HashSet<ID>();
            for(ID id: entry.getValue()){
               new_id.add(new ID(id.tree_id, id.path_id));
            }
            this.traffic_reserve.put(new_traffic_id, new_id);
        }
    }
    //reserve resource for a backup tree
    public boolean reserve(int traffic_id, int tree_id, int path_id){
        ID new_id = new ID(tree_id, path_id);
        //if this slots is free
        if(this.isUse_state()){
            this.use_state = false;
            occupy_type = 0;
            Set<ID> new_tree_id = new HashSet<>();
            new_tree_id.add(new_id);
            traffic_reserve.put(traffic_id, new_tree_id);
        }
        else{
            //if this slot is reserved by other backup paths
            //if this slot is occupied by the primary path of the same traffic
            if(occupy_type == 0 || (occupy_type == 1 && this.traffic_occupy == traffic_id)){
                //to avoid the other backup trees of a same traffic
                if(traffic_reserve.containsKey(traffic_id)){
                    traffic_reserve.get(traffic_id).add(new_id);
                }
                else{
                    Set<ID> id_set = new HashSet<>();
                    id_set.add(new_id);
                    traffic_reserve.put(traffic_id, id_set);
                }
            }
            //if others
            else
                return false;
            }
        return true;
    }

    public void setSlot_free(){
        this.use_state = true;
        this.occupy_type = -1;
        this.traffic_reserve.clear();
        this.traffic_occupy = -1;
        this.tree_occupy = -1;
    }

    public boolean isUse_state() {
        return use_state;
    }

    public void setUse_state(boolean use_state) {
        this.use_state = use_state;
    }

    public int get_occupy_traffic_id() {
        return traffic_occupy;
    }
    public int get_occupy_tree_id() {
        return tree_occupy;
    }

    public void set_traffic_id(int traffic_id) {
        this.traffic_occupy = traffic_id;
    }

    public void set_tree_id(int tree_occupy){
        this.tree_occupy = tree_occupy;
    }

    public int getOccupy_type() {
        return occupy_type;
    }

    public void setOccupy_type(int occupy_type) {
        this.occupy_type = occupy_type;
    }


}
