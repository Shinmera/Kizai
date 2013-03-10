package org.tymoonnext.bot.module.auth;

import NexT.data.DObject;
import NexT.data.DParse;
import NexT.util.StringUtils;
import NexT.util.Toolkit;
import java.io.File;
import java.util.HashMap;
import java.util.logging.Level;
import org.tymoonnext.bot.Commons;
import org.tymoonnext.bot.Kizai;
import org.tymoonnext.bot.event.CommandListener;
import org.tymoonnext.bot.event.EventListener;
import org.tymoonnext.bot.event.auth.AuthEvent;
import org.tymoonnext.bot.event.auth.UserRegisterEvent;
import org.tymoonnext.bot.event.auth.UserRetrieveEvent;
import org.tymoonnext.bot.event.auth.UserVerifyEvent;
import org.tymoonnext.bot.event.cmd.CommandInstanceEvent;
import org.tymoonnext.bot.event.core.CommandEvent;
import org.tymoonnext.bot.meta.Info;
import org.tymoonnext.bot.module.Module;
import org.tymoonnext.bot.module.cmd.CommandInstance;
import org.tymoonnext.bot.module.core.ext.CommandModule;

/**
 * User database for the Auth system. Contains and manages user configurations
 * and propagates AuthEvents to UserVerifyEvents.
 * @author Shinmera
 * @license GPLv3
 * @version 0.0.0
 */

@Info("User database for the Auth system. Contains and manages user configurations and propagates AuthEvents to UserVerifyEvents.")
public class UserDB extends Module implements CommandListener, EventListener{
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
        CommandModule.register(bot, "userdb", "list",       null,               "List all known users.", this);
        CommandModule.register(bot, "userdb", "info",       "name".split(" "),  "Retrieve user information.", this);
        CommandModule.register(bot, "userdb", "register",   "name".split(" "),  "Register a new user.", this);
        CommandModule.register(bot, "userdb", "load",       null,               "Load the userdb storage from disk.", this);
        CommandModule.register(bot, "userdb", "offload",    null,               "Offload the userdb storage to disk.", this);
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
            Commons.log.finer(toString()+" Received auth event: "+evt);
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
        Commons.log.finer(toString()+" Modifying UserRetrieveEvent: "+evt);
        if(users.containsKey(evt.getIdent().toLowerCase()))evt.setUser(users.get(evt.getIdent().toLowerCase()));
        if(userIDs.containsKey(evt.getIdent()))evt.setUser(userIDs.get(evt.getIdent()));
    }
    
    public void onUserRegister(UserRegisterEvent evt){
        register(evt.getUser());
    }

    public void onCommand(CommandEvent cmd){
        CommandInstance i = ((CommandInstanceEvent)cmd).get();
        if(cmd.getCommand().equals("userdb list")){
            cmd.getStream().send(toString()+" Registered users listing:", cmd.getChannel());
            for(User u : users.values()){
                cmd.getStream().send(" * "+u.getUID()+" "+u.getName(), cmd.getChannel());
            }
            
        }else if(cmd.getCommand().equals("userdb info")){
            if(users.containsKey(i.getValue("name").toLowerCase())){
                User u = users.get(i.getValue("name").toLowerCase());
                cmd.getStream().send(toString()+" Information for '"+u.getName()+"':", cmd.getChannel());
                cmd.getStream().send("UID: "+u.getUID(), cmd.getChannel());
                cmd.getStream().send("Last login: "+StringUtils.toHumanTime(u.getLastLoginTime()), cmd.getChannel());
                cmd.getStream().send("Session timeout: "+StringUtils.toHumanTime(u.getSessionTimeout(), "HH:mm:ss"), cmd.getChannel());
                cmd.getStream().send("Logged in: "+((u.isLoggedIn())? "Yes" : "No"), cmd.getChannel());
            }else{
                cmd.getStream().send(toString()+" No such user '"+i.getValue("name")+"'.", cmd.getChannel());
            }
            
        }else if(cmd.getCommand().equals("userdb register")){
            if(users.containsKey(i.getValue("name").toLowerCase())){
                cmd.getStream().send(toString()+" User '"+i.getValue("name")+"' already exists.", cmd.getChannel());
            }else{
                DObject settings = new DObject(new HashMap<String,DObject>());
                settings.set("name", i.getValue("name").toLowerCase());
                User u = new User(settings);
                register(u);
                cmd.getStream().send(toString()+" User '"+i.getValue("name")+"' created with UID "+u.getUID()+".", cmd.getChannel());
            }
            
        }else if(cmd.getCommand().equals("userdb load")){
            cmd.getStream().send(toString()+" Loading userdb...", cmd.getChannel());
            load();
            
        }else if(cmd.getCommand().equals("userdb offload")){
            cmd.getStream().send(toString()+" Offloading userdb...", cmd.getChannel());
            offload();
        }
    }
}
