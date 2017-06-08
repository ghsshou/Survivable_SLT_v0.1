package edu.bupt.tao.graph.edu.bupt.tao.graph.resource;



public class Resource {

	public static final int SLOTS_NO = 190;
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
}
