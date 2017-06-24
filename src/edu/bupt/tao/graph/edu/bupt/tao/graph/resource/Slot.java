package edu.bupt.tao.graph.edu.bupt.tao.graph.resource;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by Gao Tao on 2017/6/8.
 */
public class Slot {
    boolean use_state;//0 denotes used,1 not used
    int traffic_id;//denotes the traffic id to use it (occupy)
    int occupy_type;//occupy 1 or reserve 0
    boolean locked;//whether it is locked (temporarily allocated), true is locked, not used now
    Set<Intger> traffic_reserve = new HashSet<Integer>();//denotes the traffic that reserves resource on this slot

    public Slot(){
        this.use_state = true;
        this.traffic_id = -1;
        this.occupy_type = -1;
        this.locked = false;

    }
    public Slot(Slot s){
        this.use_state = s.use_state;
        this.traffic_id = s.traffic_id;
        this.occupy_type = s.occupy_type;
        this.locked = s.locked;
        this.traffic_reserve.clear();
        for(int i: s.traffic_reserve){
            this.traffic_reserve.add(i);
        }
    }
    public boolean reserve(int traffic_id){
        //if this slots is free
        if(this.isUse_state()){
            this.use_state = false;
            occupy_type = 0;
            traffic_reserve.add(traffic_id);
        }

        else{
            //if this slot is reserved by other bakup paths
            if(occupy_type == 0){
                traffic_reserve.add(traffic_id);
            }
            //if this slot is occupied by the primary path of the same traffic
            else{
                if(this.traffic_id == traffic_id){
                    traffic_reserve.add(traffic_id);
                }
                //if others
                else
                    return false;
            }
        }
        return true;
    }

    public void setSlot_free(){
        this.use_state = true;
        this.traffic_id = -1;
        this.occupy_type = -1;
        this.locked = false;
        this.traffic_reserve.clear();
    }

    public boolean isUse_state() {
        return use_state;
    }

    public void setUse_state(boolean use_state) {
        this.use_state = use_state;
    }

    public int getTraffic_id() {
        return traffic_id;
    }

    public void setTraffic_id(int traffic_id) {
        this.traffic_id = traffic_id;
    }

    public int getOccupy_type() {
        return occupy_type;
    }

    public void setOccupy_type(int occupy_type) {
        this.occupy_type = occupy_type;
    }


    @Override
    public String toString() {
        return "Slot{" +
                "use_state=" + use_state +
                ", traffic_id=" + traffic_id +
                ", occupy_type=" + occupy_type +
                '}';
    }
}
