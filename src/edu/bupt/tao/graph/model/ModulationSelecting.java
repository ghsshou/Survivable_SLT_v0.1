package edu.bupt.tao.graph.model;

import java.util.Map;

/**
 * Created by Gao Tao on 2017/6/8.
 */
public class ModulationSelecting {
    public Map<Integer,ModulationFormat> ModulationFormats = new HashMap<Integer,ModulationFormat>();
    public double BasicCapacity = 12.5;
    public ModulationSet(){
        ModulationFormats.put(1,new ModulationFormat("BPSK", 1, 2400));
        ModulationFormats.put(2,new ModulationFormat("QPSK", 2, 1200));
        ModulationFormats.put(3,new ModulationFormat("8QAM", 3, 600));
        ModulationFormats.put(4,new ModulationFormat("16QAM", 4, 300));
    }

    public double generate_Smn(int m, int n){
        return ModulationFormats.get(m).getDistance() / (Math.log10(n) + 1);
    }





}
