package org.tymoonnext.bot;

import NexT.data.DObject;
import NexT.data.DParse;
import NexT.util.Toolkit;
import java.io.File;
import java.util.HashMap;

/**
 * DObject configuration wrapper.
 * @author Shinmera
 * @license GPLv3
 * @version 1.0.0
 */
public class Configuration {
    private DObject<HashMap<String,DObject>> conf;
    
    public Configuration(){
        conf = new DObject<HashMap<String,DObject>>();
    }
    
    public boolean load(File f){
        if(!f.exists())return false;
        Commons.log.info("[CONFIG] Loading "+f);
        conf = DParse.parse(f);
        return true;
    }
    
    public boolean save(File f){
        Commons.log.info("[CONFIG] Saving "+f);
        return Toolkit.saveStringToFile(DParse.parse(conf, true), f);
    }
    
    public boolean has(String name){return conf.contains(name);}
    
    public DObject get(){return conf;}
    public DObject get(String name){return (DObject)conf.get(name);}
    public DObject getO(String name){return (DObject)conf.get(name).get();}
    public String getS(String name){return (String)conf.get(name).get();}
    public int getI(String name){return (Integer)conf.get(name).get();}
    public boolean getB(String name){return (Boolean)conf.get(name).get();}
    public double getD(String name){return (Double)conf.get(name).get();}
    
    public void set(String name, DObject o){conf.set(name, o);}
    public void setO(String name, DObject o){conf.set(name, new DObject(o));}
    public void setS(String name, String s){conf.set(name, new DObject<String>(s));}
    public void setI(String name, int i){conf.set(name, new DObject<Integer>(i));}
    public void setB(String name, boolean b){conf.set(name, new DObject<Boolean>(b));}
    public void setD(String name, double d){conf.set(name, new DObject<Double>(d));}
}
