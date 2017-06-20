package edu.bupt.tao.content_graph;
import edu.bupt.tao.content_graph.edu.bupt.tao.content_graph.model.*;
import edu.bupt.tao.graph.base_algorithms.DijkstraShortestPathAlg;
import edu.bupt.tao.graph.model.*;
import edu.bupt.tao.graph.model.abstracts.*;

import java.util.*;
import java.util.List;
import java.util.Map;

public class ContentGraph extends VariableGraph {
	public Map<BaseVertex,Datacenter> dcs = new HashMap<BaseVertex, Datacenter>();
	public Map<Integer,Content> contents = new HashMap<Integer, Content>();
	public static int INF  = 99999;
	int contentMinCopies = 2;
	public ContentGraph() {
		super();
	}
	public ContentGraph(Graph graph) {
		super(graph);
	}
	public ContentGraph(String data_file_name) {
		super(data_file_name, false);
	}

	
	public Map<BaseVertex, Datacenter> getDcs() {
		return dcs;
	}
	public void setDcs(Map<BaseVertex, Datacenter> dcs) {
		this.dcs = dcs;
	}
	public void addDc(Integer nodeID, Datacenter dc){
		dcs.put(this.get_vertex(nodeID), dc);		
	}
	public void addDc(Datacenter dc){
		dcs.put(dc.vertex, dc);
	}
	public Map<Integer, Content> getContents() {
		return contents;
	}
	public void setContents(Map<Integer, Content> contents) {
		this.contents = contents;
	}
	public void addContent(Integer ctID, Content ct){
		contents.put(ctID, ct);
	}
	public void addContent(Content ct){
		contents.put(ct.id, ct);
	}
	public boolean isDC(int node){
		return dcs.containsKey(this.get_vertex(node));
	}
	public int getContentsNum(){
		return contents.size();
	}
	
	public int getContentMinCopies() {
		return contentMinCopies;
	}
	//find out the nearest data center(s) for every node
	public void setServicedNodes(){
		List<Datacenter> dcList = new ArrayList<Datacenter>(this.dcs.values());
		List<Path> paths = new ArrayList<Path>();
		int hop = INF;
		DijkstraShortestPathAlg dspa = new DijkstraShortestPathAlg(this);
		for(BaseVertex v: this.get_vertex_list()){
			if(!this.isDC(v.get_id())){
				for(Datacenter d: dcList){
					Path path = dspa.get_shortest_path(v, d.vertex, false);
					paths.add(path);
//					System.out.println(path);
					if(hop > path.getHop()){
						hop = path.getHop();
//						System.out.println("hop" + hop);
					}
				}
				for(int i = 0; i < dcList.size(); i++){
					if(hop == paths.get(i).getHop()){
						dcList.get(i).getServiceNodes().add(v);
					}
				}
				hop = INF;
				paths.clear();
			}
		}
	}
	public List<Pair<Integer, Integer>> getAdjacentEdgesOfNode(int nodeID)
	{
		List<Pair<Integer, Integer>> adjacentEdges = new Vector<Pair<Integer, Integer>>();
			for(BaseVertex cur_adjacent_vertex : this.get_fanout_vertices_index().get(nodeID))
			{
				Pair<Integer,Integer> pair_tem = new Pair<Integer,Integer>(nodeID,cur_adjacent_vertex.get_id());
				adjacentEdges.add(pair_tem);
			}
			return adjacentEdges;
	}
	public void printSetOfServicedNodes(){
		for(Datacenter d: dcs.values()){
			System.out.println("dc" + d.vertex.get_id());
			for(BaseVertex v: d.getServiceNodes()){
				System.out.println(" " + v.get_id());
			}
			System.out.println();
		}
	}
	public void printContentPlacement(){
		Iterator<Content> it = contents.values().iterator();
		System.out.println("content placment:");
		while(it.hasNext()){
			it.next().printDCs();
		}
	}
}
