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
    public int MP = 2;//maximum number of SLTs can be constructed
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
                System.out.println("------------------------------------");
                System.out.println("USER ID: " + user_path.user);
                boolean path_can_use = true;
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
                            System.out.println("Existed group:" + ugi.dc.vertex.get_id() + " index: " + ugi.index);
                            break;
                        }
                        else {
                            Path path_1 = path;
                            Path path_2 = null;
                            int path_index = user_path.paths.indexOf(path_1);
                            if(path_index != user_path.paths.size() - 1){
                                path_2 = user_path.paths.get(path_index + 1);

                            }
                            if(judge_modulation(path_1, path_2, ugi)){
                                System.out.println("Path 1 is smaller");
                                if(user_path.paths.indexOf(path_2) == user_path.paths.size() - 1){
                                    System.out.println("Last path");
                                    path_can_use = false;
                                    continue;
                                }


                            }

                        }


                    }
                    else if(path_can_use){
                        System.out.println("Create new Group");
                        ugi = new User_Group_Info(dc, i);
                        ugi.users.add(user_path.user);
                        ugi.modulation_level = modulationSelecting.modulation_select(path.get_weight());
                        ugi.setLongest_dis(path.get_weight());
                        user_group_UG.get(dc).add(ugi);
                        result_flag = true;
                        System.out.println("new group:" + ugi.dc.vertex.get_id() + " index: " + ugi.index);
                        break;

                    }

                }

            }
            if(result_flag) {
                continue;
            }
            Datacenter temp_dc = this.mr_Graph.getDcs().get(user_path.paths.get(0).get_src());
            User_Group_Info ugi = get_group_w_min_users_from_d(user_group_UG, temp_dc);
            ugi.users.add(user_path.user);
            if(ugi.longest_dis < user_path.paths.get(0).get_weight())
                ugi.longest_dis = user_path.paths.get(0).get_weight();
            reset_modulation(ugi);
            System.out.println("Add to existed group with degradation:" + ugi.dc.vertex.get_id() + " index: " + ugi.index);

        }
        System.out.println("User Groups");
        for(Map.Entry<Datacenter,List<User_Group_Info>> entry: user_group_UG.entrySet()){

            for(User_Group_Info ugi : entry.getValue()){

                if(ugi != null){
                    System.out.println("****************************");
                    System.out.println("size" + ugi.users.size());
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

    //return true if the sum of ML degradation when path_1 is added to the i-th group of the datacenter (path_1.src) is larger than that when path_2 is used
    private boolean judge_modulation(Path path_1, int i_group_path1, Path path_2, Map<Datacenter,List<User_Group_Info>> user_group_UG){
        //delta_m denotes the ML degradation when path is considered into a user group
        if(path_2 == null)
            return true;
        User_Group_Info ugi_1 = user_group_UG.get(this.mr_Graph.getDcs().get(path_1.get_src())).get(i_group_path1);
        double delta_m_1 = (ugi_1.users.size() + 1) / (double) get_modulation_w_new_path(ugi_1, path_1) -
                ugi_1.users.size() / (double) ugi_1.modulation_level;
        double delta_m_2 = Multicast_Graph.INF;

        if(user_group_UG.get(this.mr_Graph.getDcs().get(path_2.get_src())).isEmpty())
            delta_m_2 = 1 / get_modulation_of_path(path_2);
        else{
            for(User_Group_Info ugi_2 : user_group_UG.get(this.mr_Graph.getDcs().get(path_2.get_src()))){

            }

        }

        return delta_m_1 > delta_m_2 ? true : false;

    }
    //return true if the sum of ML degradation when path_1 is added to the group is larger than that when path_2 is the only path for a new group
    private boolean judge_modulation(Path path_1, Path path_2, User_Group_Info ugi_1){
        //delta_m denotes the ML degradation when path is considered into a user group
        if(path_2 == null)
            return true;
        double delta_m_1 = (ugi_1.users.size() + 1) / (double) get_modulation_w_new_path(ugi_1, path_1) -
                ugi_1.users.size() / (double) ugi_1.modulation_level;
        double delta_m_2 = 1 / get_modulation_of_path(path_2);
        return delta_m_1 > delta_m_2 ? true : false;

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

    //return the group with minimum users among all groups from d
    private User_Group_Info get_group_w_min_users_from_d(Map<Datacenter, List<User_Group_Info>> groups, Datacenter d){
        User_Group_Info final_group = null;
        int min_users_no = Multicast_Graph.INF;
            for(User_Group_Info ugi : groups.get(d)){
                if(min_users_no > ugi.users.size() ){
                    final_group = ugi;
                }
            }

        return final_group;
    }
    private void reset_modulation(User_Group_Info ugi){
        int m = ugi.modulation_level;
        int n = ugi.users.size();
        ModulationSelecting ms = this.modulationSelecting;
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
    //get the modulation level of user group i with a new path, and the path has not been added formally yet
    private int get_modulation_w_new_path(User_Group_Info ugi, Path new_path){
        int m = ugi.modulation_level;
        int n = ugi.users.size() + 1;
        ModulationSelecting ms = this.modulationSelecting;
        double longest_dis = new_path.get_weight() > ugi.longest_dis ? new_path.get_weight() : ugi.longest_dis;
        double s_mn = ms.generate_Smn(m,n);
        for(; m > 0;){
            if(s_mn >= longest_dis){
                break;
            }
            s_mn = ms.generate_Smn(--m,n);
        }
        return m;
    }
    //get the modulation level of the path, when the path is added to a new group
    private int get_modulation_of_path(Path new_path){
        int n = 1;
        ModulationSelecting ms = this.modulationSelecting;
        int m = ms.ModulationFormats.size();
        double longest_dis = new_path.get_weight();
        double s_mn = ms.generate_Smn(m,n);
        for(; m > 0;){
            if(s_mn >= longest_dis){
                break;
            }
            s_mn = ms.generate_Smn(--m,n);
        }
        return m;
    }




}
