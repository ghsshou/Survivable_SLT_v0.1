package edu.bupt.tao.content_graph.edu.bupt.tao.content_graph.model;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;


public class Content {
	public int id;
	public String name;
	private double size;
	public int maxCopies;
	public int reqs = 0;// include pre_traffics and online traffics
	private Set<Datacenter> inDCs = new HashSet<Datacenter>();
	
	public Content(){};
	
	public Content(int id, double size) {
		super();
		this.id = id;
		this.size = size;
	}

	public Content(int id, String name, double size, Set<Datacenter> inDCs) {
		super();
		this.id = id;
		this.name = name;
		this.size = size;
		this.inDCs = inDCs;
	}

	public double getSize() {
		return size;
	}
	public void setSize(double size) {
		this.size = size;
	}
	public Set<Datacenter> getInDCs() {
		return inDCs;
	}
	public void setInDCs(Set<Datacenter> inDCs) {
		this.inDCs = inDCs;
	}
	public void addCopyToDC(Datacenter dc){
		this.inDCs.add(dc);
	}

	public void setMaxCopies(int maxCopies) {
		this.maxCopies = maxCopies;
	}
	public void printDCs(){
		Iterator<Datacenter> it = inDCs.iterator();
		System.out.print("content[" + this.id + "] in dc: [ ");
		while(it.hasNext()){
			System.out.print(it.next().vertex.get_id() + " ");
		}
		System.out.println("]");

	}
	
	
}
