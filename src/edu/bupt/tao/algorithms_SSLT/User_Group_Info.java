package edu.bupt.tao.algorithms_SSLT;

import edu.bupt.tao.content_graph.edu.bupt.tao.content_graph.model.Datacenter;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by Gao Tao on 2017/6/11.
 */
public class User_Group_Info {
    Datacenter dc;
    int index;
    Set<Integer> users;
    int modulation_level;
    double longest_dis;
    User_Group_Info(){
        this.users = new HashSet<Integer>();
        this.modulation_level = 0;
        this.longest_dis = -1;
    }
    User_Group_Info(Datacenter dc, int index){
            this.users = new HashSet<Integer>();
            this.modulation_level = 0;
            this.dc = dc;
            this.index = index;
    }
    public void setLongest_dis(double dis){
        this.longest_dis = dis;
    }

    public void setModulation_level(int modulation_level) {
        this.modulation_level = modulation_level;
    }

    @Override
    public String toString() {
        return "User_Group_Info{" +
                "dc=" + dc +
                ", index=" + index +
                ", users=" + users +
                ", modulation_level=" + modulation_level +
                ", longest_dis=" + longest_dis +
                '}';
    }
}
