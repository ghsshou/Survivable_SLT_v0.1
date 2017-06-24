package edu.bupt.tao.graph.edu.bupt.tao.graph.resource;


import com.sun.org.apache.regexp.internal.RE;
import edu.bupt.tao.LogRec;

import java.util.HashSet;
import java.util.Set;

public class Resource{

	public static final int SLOTS_NO = 190;
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
		reserved_traffic = new HashSet<Integer>();
		this.start_index = -1;
		this.end_index = -1;
		this.weight = -1;
		this.cost = -1;
	}
	public Resource(Resource source){
		this.weight = source.weight;
		this.cost = source.cost;
		this.start_index = source.start_index;
		this.end_index = source.end_index;
		this.slots_empty_num = source.slots_empty_num;
		for (int i = 0; i < SLOTS_NO; i++)
			slots[i] = new Slot(source.slots[i]);
		reserved_traffic = new HashSet<Integer>();
		for(int i: source.reserved_traffic){
			this.reserved_traffic.add(i);
		}
	}
	public void add_reserved_traffic(int traffic_id){
		reserved_traffic.add(traffic_id);
	}
	public boolean contains_reserved_traffic(int traffic_id){
		return reserved_traffic.contains(traffic_id);
	}
	public void clear_slots()
	{
		for(int i =0 ; i<SLOTS_NO ;i++)
			slots[i].setSlot_free();
		slots_empty_num = SLOTS_NO;
	}
	//whether the slots are free (not reserved or occupied, i.e. slot.use_state = true) from ~ to ~
	public boolean is_free_from_in(int start_slot, int required_slots){
		boolean result = true;
		for(int j = start_slot; j < start_slot + required_slots; j ++)
		{
			if(!slots[j].use_state || slots[j].locked){
				result = false;
				break;
			}
		}
		return result;
	}


	public Slot[] getSlots() {
		return slots;
	}
	public void set_slots_free_for_MR(int traffic_id){
		for(Slot slot: slots){
			if(slot.getTraffic_id() == traffic_id){
				slot.setSlot_free();
				slots_empty_num ++;
			}
		}
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
	//judge for primary tree
	public boolean slot_can_use(int slot_index, int traffic_id){
		if(slots[slot_index].isUse_state())
			return true;
		else if(traffic_id == slots[slot_index].getTraffic_id())
			return true;
		LogRec.log.debug("SRC:" + this.start_index + ",DST:" + this.end_index + ",SLOT INDEX:" +
				slot_index + ",SLOT TRAFFIC ID:" + slots[slot_index].getTraffic_id());
		return false;
	}
	//calculate the number of slots when allocating resource for traffic_id, return -1, if the range of slots cannot be used.
	public int extra_slots_for_reserve(int traffic_id, int start_index, int required_slots){
		int counter = 0;
		for(int i = start_index; i <= start_index + required_slots - 1; i++){
			if(slots[i].isUse_state()){
				counter++;
			}
			else{
				//if this slot is occupied by the path of same traffic or reserved by other backup paths
				if((slots[i].getOccupy_type() == 1 && traffic_id = slots[i].traffic_id) || slots[i].getOccupy_type() == 0){
					contine;
				}
				else{
					return -1;
				}
			}
		}
		return counter;
    }

	public void setCost(double cost) {
		this.cost = cost;
	}

	public boolean set_slots_free_for_MR(int start, int end, int traffic_id){
		for(int j = start; j <= end; j ++)
		{
			//if the slot has been occupied
			if(!slots[j].isUse_state() && slots[j].getTraffic_id() == traffic_id) {
				slots[j].setSlot_free();
				this.slots_empty_num ++;
			}

		}
		return true;
	}
	//only for primary path occupying
	public boolean use_slot(int index, int traffic_id, int use_type){
		//to avoid the cast where two paths of a single tree traverse a same link, if we do not judge like this, then the empty_num will minus twice
		if(slots[index].isUse_state()){
			this.slots_empty_num --;
		}
		slots[index].setUse_state(false);
		slots[index].setOccupy_type(use_type);
		slots[index].setTraffic_id(traffic_id);
		return true;

	}
	public int total_used_slots(){
		LogRec.log.debug("src:" + start_index + ", dst:" + end_index + ",used slots:" + (SLOTS_NO - this.slots_empty_num));
		return (SLOTS_NO - this.slots_empty_num);
	}
	public int reserved_slots_for_traffic(int traffic_id){
		int total = 0;
		for(Slot slot: slots){
			if(slot.isUse_state() == false && slot.getTraffic_id() == traffic_id && slot.getOccupy_type() == 0){
				total ++;
			}
		}
		return total;
	}
	public int occupied_slots_for_traffic(int traffic_id){
		int total = 0;
		for(Slot slot: slots){
			if(slot.isUse_state() == false && slot.getTraffic_id() == traffic_id && slot.getOccupy_type() == 1){
				total ++;
			}
		}
		return total;
	}
	public int reserved_slots_except_traffic(int traffic_id){
		int total = 0;
		for(Slot slot: slots){
			if(slot.isUse_state() == false && slot.getTraffic_id() != traffic_id && slot.getOccupy_type() == 0){
				total ++;
			}
		}
		return total;
	}


}
