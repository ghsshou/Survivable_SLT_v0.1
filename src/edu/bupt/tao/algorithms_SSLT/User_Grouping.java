package edu.bupt.tao.algorithms_SSLT;

import edu.bupt.tao.content_graph.edu.bupt.tao.content_graph.model.Datacenter;
import edu.bupt.tao.graph.base_algorithms.DijkstraShortestPathAlg;
import edu.bupt.tao.graph.model.ModulationSelecting;
import edu.bupt.tao.graph.model.Path;
import edu.bupt.tao.traffic_SSLT.basic_model.Multicast_Graph;
import edu.bupt.tao.traffic_SSLT.basic_model.Multicast_Request;

import javax.xml.crypto.Data;
import java.util.*;

/**
 * Created by Gao Tao on 2017/6/8.
 */
public class User_Grouping {
    Multicast_Graph mr_Graph;
    Multicast_Request mr;
    DijkstraShortestPathAlg dspa;
    ModulationSelecting modulationSelecting = new ModulationSelecting();
    public int MP = 3;//maximum number of SLTs can be constructed
    private Map<Datacenter, List<Set<Integer>>> user_group;//list stores i-th SLT from datacenter, Set stores the users in this SLT

    public User_Grouping(Multicast_Graph mr_Graph, Multicast_Request mr) {
        this.mr_Graph = mr_Graph;
        this.mr = mr;
        dspa = new DijkstraShortestPathAlg(this.mr_Graph);
        user_group = new HashMap<>();
    }
    //stores the info for each user and its paths to different dcs
    class User_Path implements Comparable<User_Path> {
        Integer user;
        List<Path> paths;
        public User_Path(Integer i, List<Path> paths){
            this.user = i;
            this.paths = paths;
        }


        @Override
        public int compareTo(User_Path o) {
            return this.paths.get(0).compareTo(o.paths.get(0));
        }

        @Override
        public String toString() {
            return "[User ID:" + user +
                    ", paths:" + paths +
                    "]";
        }
    }



    private List<User_Path> paths_caculate(){

        List<User_Path> paths_of_users = new ArrayList<User_Path>();
        Set<Datacenter> dcs_w_ms;
        dcs_w_ms = mr_Graph.get_multicast_service(mr.req_service).getInDCs();
        for(Integer i: mr.users){
            Iterator<Datacenter> it = dcs_w_ms.iterator();
            List<Path> paths = new ArrayList<Path>();
            while (it.hasNext()){
                Path path = dspa.get_shortest_path(it.next().vertex, mr_Graph.get_vertex(i));
                paths.add(path);
            }
            Collections.sort(paths);
            User_Path up = new User_Path(i, paths);
            paths_of_users.add(up);
        }
        Collections.sort(paths_of_users);
        for(User_Path up : paths_of_users){
            System.out.println(up);
        }
        return paths_of_users;
    }
    public Map<Datacenter, List<User_Group_Info>> grouping_algo1(){

        List<User_Path> paths_of_users = paths_caculate();
        Map<Datacenter,List<User_Group_Info>> user_group_UG = new HashMap<Datacenter,List<User_Group_Info>>();

        ModulationSelecting modulationSelecting = new ModulationSelecting();

        //initialize
        Set<Datacenter> dcs_w_ms = mr_Graph.get_multicast_service(mr.req_service).getInDCs();
        Iterator<Datacenter> it_temp = dcs_w_ms.iterator();
        while (it_temp.hasNext()){
            user_group_UG.put(it_temp.next(), new ArrayList<User_Group_Info>());
        }
        //
        for(User_Path user_path : paths_of_users){
            boolean result_flag = false;
            for(int i = 1; i <= MP; i++){

                if(result_flag)
                    break;
                System.out.println("USER ID: " + user_path.user);
                for(Path path : user_path.paths){
                    Datacenter dc = this.mr_Graph.getDcs().get(path.get_src());
                    User_Group_Info ugi = null;
                    if(user_group_UG.get(dc).size() == i){
                        ugi = user_group_UG.get(dc).get(i-1);
                        System.out.println("UGI ML: " + ugi.modulation_level);
                        System.out.println("S_mn: " + modulationSelecting.generate_Smn(ugi.modulation_level, ugi.users.size() + 1));
                        System.out.println("Path dis: " + path.get_weight());
                        if(modulationSelecting.generate_Smn(ugi.modulation_level, ugi.users.size() + 1) >= path.get_weight()) {
                            ugi.users.add(user_path.user);
                            if (ugi.longest_dis < path.get_weight())
                                ugi.setLongest_dis(path.get_weight());
                            result_flag = true;
                            System.out.println("2-In group:" + ugi.dc.vertex.get_id() + " index: " + ugi.index);
                            break;
                        }

                    }
                    else{
                        System.out.println("III");
                        ugi = new User_Group_Info(dc, i);
                        ugi.users.add(user_path.user);
                        ugi.modulation_level = modulationSelecting.modulation_select(path.get_weight());
                        ugi.setLongest_dis(path.get_weight());
                        user_group_UG.get(dc).add(ugi);
                        result_flag = true;
                        System.out.println("1-In group:" + ugi.dc.vertex.get_id() + " index: " + ugi.index);
                        break;

                    }

                }

            }
            if(result_flag) {
                continue;
            }
            User_Group_Info ugi = get_group_w_min_users(user_group_UG);
            ugi.users.add(user_path.user);
            if(ugi.longest_dis < user_path.paths.get(0).get_weight())
                ugi.longest_dis = user_path.paths.get(0).get_weight();
            reset_modulation(ugi, modulationSelecting);
            System.out.println("3-In group:" + ugi.dc.vertex.get_id() + " index: " + ugi.index);

        }
        for(Map.Entry<Datacenter,List<User_Group_Info>> entry: user_group_UG.entrySet()){
            System.out.println("User Groups");
            for(User_Group_Info ugi : entry.getValue()){
                System.out.println("size" + ugi.users.size());
                if(ugi != null){
                    System.out.println("****************************");
                    System.out.println("SLT index: " + ugi.index + " DC: " + ugi.dc.vertex.get_id() + " ML: " + ugi.modulation_level + " Longest dis: " + ugi.longest_dis + " Users:");

                    Iterator<Integer> it = ugi.users.iterator();
                    while(it.hasNext()){
                        System.out.print(it.next() + " ");
                    }
                }
                System.out.println();
                System.out.println("****************************");
            }
        }

        return user_group_UG;

    }

    private User_Group_Info get_group_w_min_users(Map<Datacenter, List<User_Group_Info>> groups){
        User_Group_Info final_group = null;
        int min_users_no = Multicast_Graph.INF;
        System.out.println("YYYY:" + groups.size());
        for(Map.Entry<Datacenter, List<User_Group_Info>> entry : groups.entrySet()){
            for(User_Group_Info ugi : entry.getValue()){
                System.out.println("SSSS: " + ugi.users.size());
                if(min_users_no > ugi.users.size() ){
                    final_group = ugi;
                }
            }
        }
        return final_group;
    }
    private void reset_modulation(User_Group_Info ugi, ModulationSelecting ms){
        int m = ugi.modulation_level;
        int n = ugi.users.size();
        double s_mn = ms.generate_Smn(m,n);
        for(; m > 0;){
            if(s_mn >= ugi.longest_dis){
                ugi.setModulation_level(m);
                break;
            }
            s_mn = ms.generate_Smn(--m,n);
        }
        if(m == 0){
            ugi.setModulation_level(-1);
        }


    }




}
