package edu.bupt.tao.graph.edu.bupt.tao.graph.resource;

import edu.bupt.tao.LogRec;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Resource {

    public static final int SLOTS_NO = 500;
    public double weight;
    public double cost;//by Tao, 6/20/2017
    public Slot[] slots = new Slot[SLOTS_NO];
    private int slots_empty_num = SLOTS_NO;

    private int start_index;
    private int end_index;
    private Set<Integer> reserved_traffic;

    public Resource() {
        slots = new Slot[SLOTS_NO];
        for (int i = 0; i < SLOTS_NO; i++)
            slots[i] = new Slot();
        reserved_traffic = new HashSet<>();
        this.start_index = -1;
        this.end_index = -1;
        this.weight = -1;
        this.cost = -1;
    }

    public Resource(Resource source) {
        this.weight = source.weight;
        this.cost = source.cost;
        this.start_index = source.start_index;
        this.end_index = source.end_index;
        this.slots_empty_num = source.slots_empty_num;
        for (int i = 0; i < SLOTS_NO; i++)
            slots[i] = new Slot(source.slots[i]);
        reserved_traffic = new HashSet<>();
        for (int i : source.reserved_traffic) {
            this.reserved_traffic.add(i);
        }
    }

    public void add_reserved_traffic(int traffic_id) {
        reserved_traffic.add(traffic_id);
    }

    public boolean contains_reserved_traffic(int traffic_id) {
        return reserved_traffic.contains(traffic_id);
    }

    public void clear_slots() {
        for (int i = 0; i < SLOTS_NO; i++)
            slots[i].setSlot_free();
        slots_empty_num = SLOTS_NO;
    }

    //whether the slots are free (no true reserved or occupied, i.e. slot.use_state = 1) from ~ to ~
    public boolean is_free_from_in(int start_slot, int required_slots) {
        boolean result = true;
        for (int j = start_slot; j < start_slot + required_slots; j++) {
            if (!slots[j].use_state) {
                result = false;
                break;
            }
        }
        return result;
    }


    public void set_slots_free_for_MR(int traffic_id) {

        for (Slot slot : slots) {
            if (!slot.isUse_state()) {
                if (slot.getOccupy_type() == 1) {
                    //if occupy
                    if (slot.get_occupy_traffic_id() == traffic_id) {
                        slot.setSlot_free();
                        slots_empty_num++;
                    }
                } else {
                    //if reserve
                    if (slot.traffic_reserve.keySet().contains(traffic_id)) {
                        slot.traffic_reserve.remove(traffic_id);
                        if(slot.traffic_reserve.size() == 0){
                            slot.setSlot_free();
                            slots_empty_num ++;
                        }
                    }
                }
            }
        }
        if (reserved_traffic.contains(traffic_id))
            reserved_traffic.remove(traffic_id);
    }

    public double getCost() {
        return cost;
    }

    public int getStart_index() {
        return start_index;
    }

    public void setStart_index(int start_index) {
        this.start_index = start_index;
    }

    public int getEnd_index() {
        return end_index;
    }

    public void setEnd_index(int end_index) {
        this.end_index = end_index;
    }

    //judge for primary tree, tree id is exclusive
    public boolean slot_can_use(int slot_index, int traffic_id, int tree_id) {
        if (slots[slot_index].isUse_state())
            return true;
        else if (traffic_id == slots[slot_index].get_occupy_traffic_id() && tree_id == slots[slot_index].get_occupy_tree_id()) {
            return true;
        }
        LogRec.log.debug("SRC:" + this.start_index + ",DST:" + this.end_index + ",SLOT INDEX:" +
                slot_index + ",SLOT TRAFFIC ID:" + slots[slot_index].get_occupy_traffic_id() + ",TREE ID:" + slots[slot_index].get_occupy_tree_id());
        return false;
    }

    //calculate the number of slots when allocating resource for traffic_id, return -1, if the range of slots cannot be used.
    public int extra_slots_for_reserve(int traffic_id, int start_index,
                                       int required_slots, Set<Integer> banned_paths) {
        int counter = 0;
        for (int i = start_index; i <= start_index + required_slots - 1; i++) {
            if (slots[i].isUse_state()) {
                counter++;
            }
            else {
                //if this slot is occupied by the path of same traffic or reserved by other backup paths
                if (slots[i].getOccupy_type() == 1) {
                    if (traffic_id == slots[i].get_occupy_traffic_id()) {
                    }
                    else return -1;
                } else {
                    //if any banned path id is included in the info of this slot, then the slot cannot be used,
                    // firstly, we get all the paths that have reserved resource on this slot
                    Set<Integer> all_paths_of_slot = new HashSet<>();
                    for(Map.Entry<Integer, Set<ID>> entry: slots[i].traffic_reserve.entrySet()){
                        for(ID id: entry.getValue()){
                            all_paths_of_slot.add(id.path_id);
                        }
                    }
                    //then, we check whether these paths contain any banned path
                    all_paths_of_slot.retainAll(banned_paths);
                    if(all_paths_of_slot.isEmpty()){
                    }
                    //if any, return -1 to tell that the range from start_index to ~ cannot be used.
                    else{
                        return  -1;
                    }
                }
            }
        }
        return counter;
    }

    public void setCost(double cost) {
        this.cost = cost;
    }

//    public boolean set_slots_free_for_MR(int start, int end, int traffic_id) {
//        for (int j = start; j <= end; j++) {
//            //if the slot has been occupied
//            if (!slots[j].isUse_state() && slots[j].getTraffic_id() == traffic_id) {
//                slots[j].setSlot_free();
//                this.slots_empty_num++;
//            }
//
//        }
//        return true;
//    }

    //only for primary path occupying
    public void use_slot(int index, int traffic_id, int tree_id, int use_type) {
        //to avoid the cast where two paths of a single tree traverse a same link, if we do not judge like this, then the empty_num will minus twice
        if (slots[index].isUse_state()) {
            this.slots_empty_num--;
        }
        slots[index].setUse_state(false);
        slots[index].setOccupy_type(use_type);
        slots[index].set_traffic_id(traffic_id);
        slots[index].set_tree_id(tree_id);

    }

    //reserve resource
    public boolean reserve_slot(int index, int traffic_id, int tree_id, int path_id) {
        boolean pre_state = slots[index].isUse_state();
        boolean result = slots[index].reserve(traffic_id, tree_id, path_id);
        //if the slot is free before
        if (!result){
            return false;
        }
        if(pre_state)
            this.slots_empty_num --;
        return true;
    }

    public int total_used_slots() {
        LogRec.log.debug("src:" + start_index + ",dst:" + end_index + ",used slots:" + (SLOTS_NO - this.slots_empty_num));
        int counter = 0;
        for(Slot s: slots){
            counter += s.isUse_state() ? 0 : 1;
        }
        return counter;
    }

//    public int reserved_slots_for_traffic(int traffic_id) {
//        int total = 0;
//        for (Slot slot : slots) {
//            if (!slot.isUse_state() && slot.getOccupy_type() == 0 && slot.traffic_reserve.containsKey(traffic_id)) {
//                total++;
//            }
//        }
//        return total;
//    }

    public int occupied_slots_for_traffic(int traffic_id) {
        int total = 0;
        for (Slot slot : slots) {
            if (!slot.isUse_state() && traffic_id == slot.get_occupy_traffic_id() && slot.getOccupy_type() == 1) {
                total++;
            }
        }
        return total;
    }

    //reserved slots by other backup paths of other traffics, not including the ones whose primary path is
    //jointed with its protected one.
    public int reserved_slots_except_joint(Set<Integer> jointed_path_id) {
        int total = 0;
        for (Slot slot : slots) {
            if (!slot.isUse_state() && slot.getOccupy_type() == 0) {
                //by this, we get all the path ids that reserved resource on this slot.
                Set<Integer> temp_int = new HashSet<>();
                for (Map.Entry<Integer, Set<ID>> entry : slot.traffic_reserve.entrySet()) {
                    for (ID id : entry.getValue()) {
                        temp_int.add(id.path_id);
                    }
                }
                temp_int.retainAll(jointed_path_id);
                if (temp_int.isEmpty()) {
                    total++;
                }
            }
        }
        return total;
    }

    //calculate all reserved slots number
    public int reserved_slots() {
        int total = 0;
        for (Slot slot : slots) {
            if (!slot.isUse_state() && slot.getOccupy_type() == 0) {
                total++;
            }
        }
        return total;
    }

    public int get_largest_used_index(){
        int index = 0;
        for(int i = 0; i < slots.length; i++){
            if(!slots[i].isUse_state()){
//                System.out.println(this.getStart_index() + "->" + this.getEnd_index() + " index:" + i);
                index = index <= i ? i : index;
            }
        }
        return index;
    }


}
