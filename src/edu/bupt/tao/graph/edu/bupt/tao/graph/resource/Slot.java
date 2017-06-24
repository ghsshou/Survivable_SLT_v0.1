package edu.bupt.tao.graph.edu.bupt.tao.graph.resource;

/**
 * Created by Gao Tao on 2017/6/8.
 */
public class Slot implements Cloneable {
    boolean use_state;//0 denotes used,1 not used
    int traffic_id;//denotes the traffic id to use it
    int occupy_type;//occupy 1 or reserve 0
    boolean locked;//whether it is locked (temporarily allocated), true is locked, not used now

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
    }

    public void setSlot_free(){
        this.use_state = true;
        this.traffic_id = -1;
        this.occupy_type = -1;
        this.locked = false;
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

    public void setLocked(boolean locked) {
        this.locked = locked;
    }

    public boolean isLocked() {
        return locked;
    }

    @Override
    public String toString() {
        return "Slot{" +
                "use_state=" + use_state +
                ", traffic_id=" + traffic_id +
                ", occupy_type=" + occupy_type +
                '}';
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}
