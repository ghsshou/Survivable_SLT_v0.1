package edu.bupt.tao.content_graph.edu.bupt.tao.content_graph.model;

import java.util.HashSet;
import java.util.Set;

import edu.bupt.tao.graph.model.abstracts.BaseVertex;

public class Datacenter {
	public BaseVertex vertex;
	private double capacity;
	private Set<Content> contents = new HashSet<Content>();
	
	private Set<BaseVertex> serviceNodes = new HashSet<BaseVertex>();
	
	public Datacenter(){};
	public Datacenter(BaseVertex vertex, double capacity) {
		super();
		this.vertex = vertex;
		this.capacity = capacity;
	}
	public Datacenter(BaseVertex vertex, double capacity,
			 Set<Content> contents) {
		super();
		this.vertex = vertex;
		this.capacity = capacity;
		this.contents = contents;
	}
	public double getCapacity() {
		return capacity;
	}
	public void setCapacity(double capacity) {
		this.capacity = capacity;
	}
	public Set<BaseVertex> getServiceNodes() {
		return serviceNodes;
	}
	public void setServiceNodes(Set<BaseVertex> serviceNodes) {
		this.serviceNodes = serviceNodes;
	}
	public Set<Content> getContents() {
		return contents;
	}
	public void setContents(Set<Content> contents) {
		this.contents = contents;
	}
	

}
