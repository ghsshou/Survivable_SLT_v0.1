package edu.bupt.tao.edu.bupt.tao.graph_SSLT;

import edu.bupt.tao.LogRec;
import edu.bupt.tao.graph.model.Pair;
import edu.bupt.tao.graph.model.Path;
import edu.bupt.tao.graph.model.abstracts.BaseVertex;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by Gao Tao on 2017/6/20.
 */
public class SpanningTree {
    List<Path> paths_of_tree;
    double total_cost;
    int startSlots;
    int useSlots;
    int modulationLevel;
    public SpanningTree(){
        paths_of_tree = new ArrayList<Path>();
    }
    Set<Pair<BaseVertex, BaseVertex>> get_all_edges(){
        Set<Pair<BaseVertex, BaseVertex>> all_edges = new HashSet<Pair<BaseVertex, BaseVertex>>();
        for(Path p: paths_of_tree){
            List<BaseVertex> vertex_list = p.get_vertices();
            BaseVertex src;
            BaseVertex dst;
            int index = 0;
            if(vertex_list.size() <= 1){
                LogRec.log.error("Path Error! Not Enough Edges!");
            }
            while(index <= vertex_list.size() - 2){
                src = vertex_list.get(index);
                dst = vertex_list.get(index + 1);
                all_edges.add(new Pair<BaseVertex, BaseVertex>(src, dst));
                index ++;
            }

        }
        return all_edges;
    }
    public void add_path(Path path){
        this.paths_of_tree.add(path);
    }

    public void setStartSlots(int startSlots) {
        this.startSlots = startSlots;
    }

    public void setUseSlots(int useSlots) {
        this.useSlots = useSlots;
    }

    public void setModulationLevel(int modulationLevel) {
        this.modulationLevel = modulationLevel;
    }

    public int getStartSlots() {
        return startSlots;
    }

    public int getUseSlots() {
        return useSlots;
    }

    public int getModulationLevel() {
        return modulationLevel;
    }

    public List<Path> getPaths_of_tree() {
        return paths_of_tree;
    }

    public void setTotal_cost(double total_cost) {
        this.total_cost = total_cost;
    }

    public void print_tree(){
        System.out.println("-------------Tree Info---------------");
        System.out.println("Total Cost:" + total_cost + ", start index:" + startSlots + ", used slots:" + useSlots + ", ML:" + modulationLevel);
        for(int i = 0; i < paths_of_tree.size(); i++){
            System.out.println("Branch: " + i);
            System.out.println(paths_of_tree.get(i));

        }
        System.out.println();
    }
    public double get_longest_dis(){
        double long_dis = -1;
        for(Path p: paths_of_tree){
            long_dis = long_dis > p.get_weight() ? long_dis : p.get_weight();
        }
        return long_dis;

    }

    public void pritn_tree(){
        System.out.println("TREE INFO:");
        System.out.print("SRC:" + paths_of_tree.get(0).get_src() + " USERS:");
        for(Path p: paths_of_tree){
            System.out.print(p.get_dst() + " ");
        }
        System.out.print("TOTAL COST:" + total_cost + " START SLOT:" + startSlots + " USED SLOTS:" + useSlots + " ML:" + modulationLevel);
    }
}
