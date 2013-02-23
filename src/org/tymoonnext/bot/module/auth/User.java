package org.tymoonnext.bot.module.auth;

import NexT.data.DObject;
import java.util.logging.Level;
import org.tymoonnext.bot.Commons;

/**
 * Auth system user representation. This user class contains an arbitrary
 * information storage so that multiple AuthImplementors can store their
 * required data in the user's configuration. If possible, a sub-object should
 * be used for every AuthImplementor as to avoid collisions.
 * @author Shinmera
 * @license GPLv3
 * @version 0.0.0
 */
public class User {
    private String name;
    private String UID;
    private DObject config;
    private boolean loggedIn = false;
    
    public User(DObject conf){
        if(!conf.contains("name")) throw new IllegalArgumentException("DObject does not contain name attribute!");
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
    
    /**
     * Sets the last login time to the current time and sets the loggedIn flag
     * to true.
     */
    public void activateSession(){
        config.set("lastLogin", System.currentTimeMillis());
        loggedIn = true;
    }
    
    /**
     * Sets the loggedIn flag to false.
     */
    public void deactivateSession(){
        loggedIn = false;
    }
    
    /**
     * Returns whether the user is still under an active session. This also
     * checks the session timeout and actively sets the loggedIn flag in case
     * the timeout has been reached.
     * @return Whether the user is still logged in or not.
     */
    public boolean isLoggedIn(){
        long timeSinceLastActivation = System.currentTimeMillis() - getLastLoginTime();
        if(timeSinceLastActivation > getSessionTimeout()) deactivateSession();
        return loggedIn;
    }
    
    public String getName(){return name;}
    public String getUID(){return UID;}
    public DObject getConfig(){return config;}
    public long getLastLoginTime(){return (Long)config.get("lastLogin").get();}
    public long getSessionTimeout(){return (Long)config.get("sessionTimeout").get();}
    
    public String toString(){return "@" + this.getClass().getSimpleName() + "|" + UID + ":" + name + "@";}
}
