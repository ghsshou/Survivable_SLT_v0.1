/*
 *
 * Copyright (c) 2004-2008 Arizona State University.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY ARIZONA STATE UNIVERSITY ``AS IS'' AND
 * ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL ARIZONA STATE UNIVERSITY
 * NOR ITS EMPLOYEES BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 */
package edu.bupt.tao.graph.model;

import edu.bupt.tao.graph.model.abstracts.BaseElementWithWeight;
import edu.bupt.tao.graph.model.abstracts.BaseVertex;

import java.util.List;
import java.util.Vector;

/**
 * @author <a href='mailto:Yan.Qi@asu.edu'>Yan Qi</a>
 * @version $Revision: 673 $
 * @latest $Date: 2009-02-05 01:19:18 -0700 (Thu, 05 Feb 2009) $
 */
public class Path implements BaseElementWithWeight,Comparable<Path>
{
	List<BaseVertex> _vertex_list = new Vector<BaseVertex>();
	double _weight = -1;
	double cost = -1;
	int hop;
	int startSlots;
	int useSlots;
	int modulationLevel;
	
	double latency;	
	static double switchLatency = 8;//the switch time,ms
	static double transSpeed = 193.121; //in km


	public Path(){};
	//by Tao
	public Path(List<BaseVertex> _vertex_list, double _weight)
	{
		this._vertex_list = _vertex_list;

		this._weight = _weight;
		hop = _vertex_list.size() - 1;
		latency = switchLatency * hop + _weight / transSpeed;
	}


	public double get_weight()
	{
		return _weight;
	}
	
	public void set_weight(double weight)
	{
		_weight = weight;
	}
	
	public int getStartSlots() {
		return startSlots;
	}

	public void setStartSlots(int startSlots) {
		this.startSlots = startSlots;
	}

	public int getUseSlots() {
		return useSlots;
	}

	public void setUseSlots(int useSlots) {
		this.useSlots = useSlots;
	}

	public int getModulationLevel() {
		return modulationLevel;
	}

	public void setModulationLevel(int modulationLevel) {
		this.modulationLevel = modulationLevel;
	}

	public List<BaseVertex> get_vertices()
	{
		return _vertex_list;
	}

	public BaseVertex get_src(){
		return _vertex_list.get(0);
	}
	public BaseVertex get_dst(){
		return _vertex_list.get(_vertex_list.size() - 1);
	}

	public void setCost(double cost) {
		this.cost = cost;
	}

	public double getCost() {
		return cost;
	}

	public int getHop() {
		return hop;
	}
	
	//version 2.0: used to record the latency for paths
	public double getLatency(){
		return latency;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object right)
	{
		if(right instanceof Path)
		{
			Path r_path = (Path) right;
			return _vertex_list.equals(r_path._vertex_list);
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode()
	{
		return _vertex_list.hashCode();
	}
	
	public String toString()
	{
		return _vertex_list.toString()+":" + "distance: " +_weight+",hop:"+hop + ",cost:" + cost + ",m: " + modulationLevel + ",start: " + startSlots + ",useNum: " + useSlots;
	}

	@Override
	public int compareTo(Path o) {
		// TODO Auto-generated method stub
		if(o == null)
			return 1;
		int delta = (int) (this._weight - o.get_weight());
		if(delta == 0)
			delta = (int) (this.cost - o.cost);
		return delta;
	}
}
