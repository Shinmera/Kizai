package org.tymoonnext.bot.module;

import NexT.data.DObject;
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
    private HashMap<String, User> users;
    private HashMap<String, Group> groups;
    private Configuration conf;
    
    public Auth(Kizai bot){
        super(bot);
        users = new HashMap<String, User>();
        groups = new HashMap<String, Group>();
        conf = new Configuration();
        
        conf.load(new File(Commons.f_BASEDIR, "auth.cfg"));
        for(Object group : conf.get("groups").getKeySet()){
            System.out.println(">> "+group+" : "+conf.get("groups").get(group+"").toString());
            PermissionTree perms = new PermissionTree(conf.get("groups").get(group+"").toString().split("\n"));
            groups.put(group+"", new Group(group+"", perms));
        }
        
        //Believe me, we want to be the first with this.
        try{bot.bindEvent(CommandEvent.class, this, "checkCommand", Integer.MAX_VALUE);}catch(NoSuchMethodException ex){}
    }

    @Override
    public void shutdown() {
        bot.unbindAllEvents(this);
    }
    
    public void checkCommand(CommandEvent cmd){
        if(!users.containsKey(cmd.getUser())){
            Commons.log.info(toString()+" No user '"+cmd.getUser()+"', command blocked by default.");
            cmd.setHalted(true);
        }else if(!users.get(cmd.getUser()).check("command."+cmd.getCommand())){
            cmd.setHalted(true);
        }
        if(cmd.isHalted())cmd.getStream().send(toString()+" access denied.", cmd.getChannel());
    }
    
    public void addUser(User u){
        if(conf.get("users").contains(u.getID())){
            DObject uconf = conf.get("users").get(u.getID());
            if(uconf.contains("group")){
                String group = uconf.get("group").toString();
                if(groups.containsKey(group))
                    u.setGroup(groups.get(group));
            }
            if(uconf.contains("perms")){
                u.setPermissions(new PermissionTree(uconf.get("perms").toString().split("\n")));
            }
        }
        users.put(u.getID(), u);
    }
    public User getUser(String ID){return users.get(ID);}
}
