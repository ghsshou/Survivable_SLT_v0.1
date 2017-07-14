package edu.bupt.tao.graph.model;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Gao Tao on 2017/6/8.
 */
public class ModulationSelecting {
    public Map<Integer,ModulationFormat> ModulationFormats = new HashMap<Integer,ModulationFormat>();
    public ModulationSelecting(){
        ModulationFormats.put(1,new ModulationFormat("BPSK", 1, 6000));
        ModulationFormats.put(2,new ModulationFormat("QPSK", 2, 3000));
//        ModulationFormats.put(3,new ModulationFormat("8QAM", 3, 1500));
//        ModulationFormats.put(4,new ModulationFormat("16QAM", 4,750));
    }
    public int get_highest_ML(){
        int level = 0;
        for(Map.Entry<Integer, ModulationFormat> entry: this.ModulationFormats.entrySet()){
            level = level < entry.getKey()? entry.getKey() : level;
        }
        return level;
    }

    public double generate_Smn(int m, int n){
        if(m == 0)
            return -1;
        return ModulationFormats.get(m).getDistance() / (Math.log10(n) + 1);
    }
    public int modulation_select(double dis){
        double sm_1;
        for(int m = ModulationFormats.size(); m >= 1; m--){
            if(generate_Smn(m,1) >= dis)
                return m;
        }
        return -1;

    }
    public double get_capacity(int level){
        return ModulationFormats.get(level).getCapacity();
    }
    public int get_highest_ML(int n, double longes_dis){
        int m = this.get_highest_ML();
        for(; m > 0; m --){
            if(generate_Smn(m, n) > longes_dis){
                return m;
            }
        }
        return -1;

    }






}
