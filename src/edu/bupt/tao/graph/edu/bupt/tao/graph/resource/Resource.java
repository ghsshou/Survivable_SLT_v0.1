package edu.bupt.tao.graph.edu.bupt.tao.graph.resource;


import edu.bupt.tao.LogRec;

public class Resource {

	public static final int SLOTS_NO = 190;
	public double weight;
	public double cost;//by Tao, 6/20/2017
	public Slot[] slots;
	private int slots_empty_num = SLOTS_NO;
	
	private int start_index;
	private int end_index;
	
	public Resource() {
		slots = new Slot[SLOTS_NO];
		for (int i = 0; i < SLOTS_NO; i++)
			slots[i] = new Slot();
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
	public boolean slot_can_use(int slot_index, int traffic_id){
		if(slots[slot_index].isUse_state())
			return true;
		else if(traffic_id == slots[slot_index].getTraffic_id())
			return true;
		return false;
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
	public boolean use_slot(int index, int traffic_id, int use_type){
		slots[index].setUse_state(false);
		slots[index].setOccupy_type(use_type);
		slots[index].setTraffic_id(traffic_id);
		this.slots_empty_num--;
		return true;

	}
	public int total_used_slots(){
		LogRec.log.debug("src:" + start_index + ", dst:" + end_index + ",used slots:" + (SLOTS_NO - this.slots_empty_num));
		return (SLOTS_NO - this.slots_empty_num);
	}

}
