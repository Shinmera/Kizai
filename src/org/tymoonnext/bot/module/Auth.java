package org.tymoonnext.bot.module;

import NexT.data.DObject;
import NexT.util.Toolkit;
import java.io.File;
import java.util.HashMap;
import org.tymoonnext.bot.Commons;
import org.tymoonnext.bot.Configuration;
import org.tymoonnext.bot.Kizai;
import org.tymoonnext.bot.event.CommandEvent;
import org.tymoonnext.bot.event.EventListener;
import org.tymoonnext.bot.module.auth.Group;
import org.tymoonnext.bot.module.auth.PermissionTree;
import org.tymoonnext.bot.module.auth.User;

/**
 *
 * @author Shinmera
 * @license GPLv3
 * @version 0.0.0
 */
public class Auth extends Module implements EventListener{
    public static final File CONFIG = new File(Commons.f_BASEDIR, "auth.cfg");
    
    private HashMap<String, User> users;
    private HashMap<String, Group> groups;
    private Configuration conf;
    
    public Auth(Kizai bot){
        super(bot);
        users = new HashMap<String, User>();
        groups = new HashMap<String, Group>();
        conf = new Configuration();
        
        conf.load(CONFIG);
        for(Object group : conf.get("groups").getKeySet()){
            PermissionTree perms = new PermissionTree(conf.get("groups").get(group+"").toString().split("\n"));
            groups.put(group+"", new Group(group+"", perms));
        }
        for(Object username : conf.get("users").getKeySet()){
            DObject uconf = conf.get("users").get(username+"");
            User user = new User(username+"", uconf);
            if(uconf.contains("perms")){
                PermissionTree perms = new PermissionTree(uconf.get("perms").toString().split("\n"));
                user.setPermissions(perms);
            }
            if(uconf.contains("group")){
                user.setGroup(groups.get(uconf.get("group").toString()));
            }
            users.put(username+"", user);
        }
        
        //Believe me, we want to be the first with this.
        try{bot.bindEvent(CommandEvent.class, this, "checkCommand", Integer.MAX_VALUE);}catch(NoSuchMethodException ex){}
    }

    @Override
    public void shutdown() {
        bot.unbindAllEvents(this);
        conf.save(CONFIG);
    }
    
    public void checkCommand(CommandEvent cmd){
        if(!users.containsKey(cmd.getUser())){
            if(!users.containsKey("any")){
                Commons.log.info(toString()+" No user '"+cmd.getUser()+"', command blocked by default.");
                cmd.setHalted(true);
            }else if(!users.get("any").check("command."+cmd.getCommand())){
                cmd.setHalted(true);
            }
        }else if(!users.get(cmd.getUser()).check("command."+cmd.getCommand())){
            cmd.setHalted(true);
        }
        if(cmd.isHalted())cmd.getStream().send(toString()+" access denied.", cmd.getChannel());
    }
    
    public void addUser(User u){
        users.put(u.getID(), u);
        DObject user;
        if(u.getConfig() != null)user = u.getConfig();else user = new DObject();
        if(u.getGroup() != null)user.set("group", u.getGroup().getID());
        if(u.getPerms() != null)user.set("perms", Toolkit.implode(u.getPerms().getPermissions(), "\n"));
        conf.get("users").set(u.getID(), user);
    }
    public User getUser(String ID){return users.get(ID);}
}
