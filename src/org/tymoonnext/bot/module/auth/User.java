package org.tymoonnext.bot.module.auth;

import NexT.data.DObject;
import java.util.logging.Level;
import org.tymoonnext.bot.Commons;

/**
 *
 * @author Shinmera
 * @license GPLv3
 * @version 0.0.0
 */
public class User {
    private String name;
    private String UID;
    private DObject config;
    
    public User(DObject conf){
        name = conf.get("name").toString();
        if(!conf.contains("UID")){
            UID = Commons.getUUID();
            conf.set("UID", UID);
            Commons.log.log(Level.WARNING, toString()+" No UID defined in config! Generated and set a new one.");
        }else{
            UID = conf.get("UID").toString();
        }
        
        if(!conf.contains("sessionTimeout"))    conf.set("sessionTimeout", 3*60*1000);
        if(!conf.contains("lastLogin"))         conf.set("lastLogin", 0);
        
        config = conf;
    }
    
    public void activateSession(){config.set("lastLogin", System.currentTimeMillis());}
    
    public String getName(){return name;}
    public String getUID(){return UID;}
    public DObject getConfig(){return config;}
    public long getLastLoginTime(){return (Long)config.get("lastLogin").get();}
    public long getSessionTimeout(){return (Long)config.get("sessionTimeout").get();}
    
    public String toString(){return "@" + this.getClass().getSimpleName() + ":" + UID + "@";}
}
