package edu.bupt.tao.graph.model;

public class ModulationFormat {
	String name;
	int level;
	double distance;
	double capacity;
	public double BasicCapacity = 12.5;
	public ModulationFormat(String name, int level, double distance) {
		this.name = name;
		this.level = level;
		this.distance = distance;
		this.capacity = BasicCapacity * level;
		// TODO Auto-generated constructor stub
	}

	public double getDistance() {
		return distance;
	}

	public double getCapacity() {
		return capacity;
	}

}