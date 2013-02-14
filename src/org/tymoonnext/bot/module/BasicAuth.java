package org.tymoonnext.bot.module;

import NexT.data.DObject;
import org.tymoonnext.bot.Commons;
import org.tymoonnext.bot.Kizai;
import org.tymoonnext.bot.event.CommandEvent;
import org.tymoonnext.bot.event.CommandListener;
import org.tymoonnext.bot.module.auth.SessionFactory;
import org.tymoonnext.bot.module.auth.User;

/**
 * 
 * @author Shinmera
 * @license GPLv3
 * @version 0.0.0
 */
public class BasicAuth extends Module implements CommandListener{
    private SF sf;
    
    public BasicAuth(Kizai bot){
        super(bot);
        sf = new SF(bot);
        
        bot.registerCommand("basic-login", this);
        bot.registerCommand("basic-logout", this);
    }

    @Override
    public void shutdown(){
        bot.unregisterAllCommands(this);
    }

    public void onCommand(CommandEvent cmd){
        if(cmd.getCommand().equals("basic-login")){
            if((cmd.getArgs() == null) ||
               (cmd.getArgs().split(" ").length < 2 && cmd.getUser() == null ) ||
               (cmd.getArgs().split(" ").length < 1)){
                cmd.getStream().send(toString()+" Insufficient parameters.", cmd.getChannel());
                return;
            }
            String user,pw;
            String[] args = cmd.getArgs().split(" ");
            if(args.length == 1){
                user=cmd.getUser();
                pw=args[0];
            }else{
                user=args[0];
                pw=args[1];
            }
            
            if(sf.authenticate(user, pw)){
                cmd.getStream().send(toString()+" Logged in successfully.", cmd.getChannel());
            }else{
                cmd.getStream().send(toString()+" Login failed.", cmd.getChannel());
            }
        }else if(cmd.getCommand().equals("basic-logout")){
            
        }
    }

    
    private class SF extends SessionFactory{
        public SF(Kizai bot){super(bot);}
        public boolean authenticate(String... userinf) {
            User u = getUser(userinf[0]);
            if(u != null){
                if(u.getConfig().contains("basic-hash")){
                    if(u.getConfig().get("basic-hash").toString().equals(Commons.hash(userinf[1]))){
                        u.getSession().makeValid();
                        return true;
                    }
                }
            }
            return false;
        }
    
    }
}
