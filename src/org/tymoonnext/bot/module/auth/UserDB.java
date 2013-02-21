package org.tymoonnext.bot.module.auth;

import NexT.data.DObject;
import NexT.data.DParse;
import NexT.util.Toolkit;
import java.io.File;
import java.util.HashMap;
import org.tymoonnext.bot.Commons;
import org.tymoonnext.bot.ConfigLoader;
import org.tymoonnext.bot.Kizai;
import org.tymoonnext.bot.event.EventListener;
import org.tymoonnext.bot.event.auth.AuthEvent;
import org.tymoonnext.bot.event.auth.UserVerifyEvent;
import org.tymoonnext.bot.module.Module;

/**
 *
 * @author Shinmera
 * @license GPLv3
 * @version 0.0.0
 */
public class UserDB extends Module implements EventListener{
    
    private class C extends ConfigLoader{
        public File configdir = new File(Commons.f_CONFIGDIR, "users");
    }
    
    private C config = new C();
    private HashMap<String, User> users;
    
    public UserDB(Kizai bot){
        super(bot);
        users = new HashMap<String, User>();
        try{bot.bindEvent(AuthEvent.class, this, "onAuth");}catch(NoSuchMethodException ex){}
        
        config.load(bot.getConfig().get("modules").get("auth.UserDB"));
        load();
    }
    
    public void load(){
        File[] cfgfiles = config.configdir.listFiles();
        for(File file : cfgfiles){
            DObject<HashMap<String, DObject>> cfg = DParse.parse(file);
            User user = new User(cfg);
            if(user.getName().equals(file.getName())){
                Commons.log.info(toString()+" Loading user " + user +".");
                users.put(user.getName(), user);
            }else{
                Commons.log.warning(toString()+" File " + file.getPath() + " mismatches containing user " + user.getName()+". Skipping.");
            }
        }
    }
    
    public void offload(){
        for(User user : users.values()){
            Commons.log.info(toString()+" Offloading " + user + "...");
            Toolkit.saveStringToFile(DParse.parse(user.getConfig(), true), new File(config.configdir, user.getName()));
        }
    }

    @Override
    public void shutdown() {
        offload();
        config.save();
        bot.unbindAllEvents(this);
    }
    
    public void onAuth(AuthEvent evt){
        if(users.containsKey(evt.getCommand().getUser())){
            UserVerifyEvent uevt = new UserVerifyEvent(evt.getStream(), users.get(evt.getCommand().getUser()));
            bot.event(uevt, AuthImplementor.class);
        }
    }
}
