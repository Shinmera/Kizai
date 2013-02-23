package org.tymoonnext.bot.module.auth;

import NexT.data.DObject;
import NexT.data.DParse;
import NexT.util.Toolkit;
import java.io.File;
import java.util.HashMap;
import java.util.logging.Level;
import org.tymoonnext.bot.Commons;
import org.tymoonnext.bot.Kizai;
import org.tymoonnext.bot.event.EventListener;
import org.tymoonnext.bot.event.auth.AuthEvent;
import org.tymoonnext.bot.event.auth.UserRegisterEvent;
import org.tymoonnext.bot.event.auth.UserRetrieveEvent;
import org.tymoonnext.bot.event.auth.UserVerifyEvent;
import org.tymoonnext.bot.module.Module;

/**
 * User database for the Auth system. Contains and manages user configurations
 * and propagates AuthEvents to UserVerifyEvents.
 * @author Shinmera
 * @license GPLv3
 * @version 0.0.0
 */
public class UserDB extends Module implements EventListener{
    public static final File CONFIGDIR = new File(Commons.f_CONFIGDIR, "users");
    private HashMap<String, User> users;
    private HashMap<String, User> userIDs;
    
    public UserDB(Kizai bot){
        super(bot);
        users = new HashMap<String, User>();
        userIDs = new HashMap<String, User>();
        //We have the last word on this, but also want to allow overrides.
        try{bot.bindEvent(AuthEvent.class, this, "onAuth", Integer.MIN_VALUE+2);}catch(NoSuchMethodException ex){}
        try{bot.bindEvent(UserRetrieveEvent.class, this, "onUser");}catch(NoSuchMethodException ex){}
        try{bot.bindEvent(UserRegisterEvent.class, this, "onUserRegister");}catch(NoSuchMethodException ex){}
        
        load();
    }
    
    /**
     * Register a new user in the database. This immediately also offloads the
     * user to save his configuration to disk.
     * @param user 
     * @see UserDB#offload(org.tymoonnext.bot.module.auth.User) 
     */
    public void register(User user){
        Commons.log.info(toString()+" Registering user "+user);
        users.put(user.getName().toLowerCase(), user);
        userIDs.put(user.getUID(), user);
        offload(user);
    }
    
    /**
     * Loads all users and their configurations. The username specified in the
     * configuration has to match the filename or the config in question will
     * be ignored.
     */
    public void load(){
        Commons.log.info(toString()+" Loading all users from disk...");
        File[] cfgfiles = CONFIGDIR.listFiles();
        for(File file : cfgfiles){
            DObject<HashMap<String, DObject>> cfg = DParse.parse(file);
            try{
                User user = new User(cfg);
                if(user.getName().equals(file.getName())){
                    Commons.log.info(toString()+" Loading user " + user +".");
                    users.put(user.getName().toLowerCase(), user);
                    userIDs.put(user.getUID(), user);
                }else{
                    Commons.log.warning(toString()+" File " + file.getPath() + " mismatches containing user " + user.getName()+". Skipping.");
                }
            }catch(IllegalArgumentException ex){
                Commons.log.log(Level.WARNING, toString()+" File " + file.getPath() + " failed to load.", ex);
            }
        }
    }
    
    /**
     * Offload all users.
     * @see UserDB#offload(org.tymoonnext.bot.module.auth.User) 
     */
    public void offload(){
        for(User user : users.values()){
            offload(user);
        }
    }
    
    /**
     * Saves the user's configuration file to disk. It also strictly sets the
     * configuration values of "name" and "UID" to the user's fixed name and
     * UID fields.
     * @param user The user to offload.
     */
    public void offload(User user){
        Commons.log.info(toString()+" Offloading " + user + "...");
        user.getConfig().set("name", user.getName().toLowerCase());
        user.getConfig().set("UID", user.getUID());
        Toolkit.saveStringToFile(DParse.parse(user.getConfig(), true), new File(CONFIGDIR, user.getName().toLowerCase()));
    }

    public void shutdown() {
        bot.unbindAllEvents(this);
        offload();
    }
    
    public void onAuth(AuthEvent evt){
        if(users.containsKey(evt.getCommand().getUser().toLowerCase())){
            User user = users.get(evt.getCommand().getUser().toLowerCase());
            UserVerifyEvent uevt = new UserVerifyEvent(evt.getStream(), user);
            bot.event(uevt, SessionImplementor.class);
            if(!user.isLoggedIn()){
                Commons.log.info(toString()+" "+user+" has no active session. Blocking.");
                evt.setGranted(false);
            }
        }else{
            Commons.log.info(toString()+" User '"+evt.getCommand().getUser()+"' is unknown. Blocking.");
            evt.setGranted(false);
        }
    }
    
    public void onUser(UserRetrieveEvent evt){
        if(users.containsKey(evt.getIdent().toLowerCase()))evt.setUser(users.get(evt.getIdent().toLowerCase()));
        if(userIDs.containsKey(evt.getIdent()))evt.setUser(userIDs.get(evt.getIdent()));
    }
    
    public void onUserRegister(UserRegisterEvent evt){
        register(evt.getUser());
    }
}
