/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.tymoonnext.bot.module.auth;

import NexT.data.DObject;
import java.io.File;
import org.tymoonnext.bot.Commons;
import org.tymoonnext.bot.Configuration;
import org.tymoonnext.bot.Kizai;
import org.tymoonnext.bot.event.CommandListener;
import org.tymoonnext.bot.event.auth.UserRegisterEvent;
import org.tymoonnext.bot.event.auth.UserRetrieveEvent;
import org.tymoonnext.bot.event.auth.UserVerifyEvent;
import org.tymoonnext.bot.event.cmd.CommandInstanceEvent;
import org.tymoonnext.bot.event.core.CommandEvent;
import org.tymoonnext.bot.module.cmd.CommandInstance;
import org.tymoonnext.bot.module.core.ext.CommandModule;

/**
 *
 * @author hafnern
 */
public class LoginSessionImplementor extends SessionImplementor implements CommandListener{
    public static final File CONFIGFILE = new File(Commons.f_CONFIGDIR, "logins.cfg");
    private Configuration config;
    
    public LoginSessionImplementor(Kizai bot){
        super(bot);
        
        config = new Configuration();
        config.load(CONFIGFILE);
        
        String[] largs = {
            "password",
            "username[](ALPHA)",
            "timeout[60](INTEGER)"};
        String[] pargs = {
            "password",
            "username[](ALPHA)"};
        CommandModule.register(bot, "login", largs, "Log in to an account. Note that you will be automatically logged out after [timeout] seconds of inactivity.", this);
        CommandModule.register(bot, "logout", "".split(" "), "Log out your account.", this);
        CommandModule.register(bot, "passwd", pargs, "Register an account or change your password if you are already logged in.", this);
    }
    
    public void shutdown(){
        super.shutdown();
        config.save(CONFIGFILE);
    }

    public void onUserVerify(UserVerifyEvent evt) {
        DObject conf = evt.getUser().getConfig();
        if(conf.contains("loginuser") && conf.contains("logintimeout")){
            if(!conf.get("loginuser").toString().isEmpty()){
                long timeSinceLogin = System.currentTimeMillis() - evt.getUser().getLastLoginTime();
                if(timeSinceLogin < (Integer)conf.get("logintimeout").get()){
                    Commons.log.fine(toString()+evt.getUser()+" Session recognized and active; login refreshed.");
                    evt.getUser().activateSession();
                }else{
                    Commons.log.fine(toString()+evt.getUser()+" Session recognized but timed out; logged user out.");
                    conf.set("loginuser", "");
                }
            }
        }
    }

    @Override
    public void onCommand(CommandEvent cmd) {
        User user = ((UserRetrieveEvent)bot.event(new UserRetrieveEvent(cmd.getUser()))).getUser();
        if(user == null) user = ((UserRegisterEvent)bot.event(new UserRegisterEvent(cmd.getUser()))).getUser();
        CommandInstance ci = ((CommandInstanceEvent)cmd).get();
        
        if(cmd.getCommand().equals("logout")){
            user.getConfig().set("loginuser", "");
            cmd.getStream().send(toString()+" You have been logged out.", cmd.getChannel());
        
        }else if(cmd.getCommand().equals("passwd") ||
                cmd.getCommand().equals("login")){
            String hash = Commons.hash(ci.getValue("password"));
            String name = cmd.getUser().toLowerCase();
            if(!ci.getValue("username").isEmpty())
                name = ci.getValue("username").toLowerCase();
            
            if(cmd.getCommand().equals("passwd")){                
                if(config.has(name)){
                    if(!user.getConfig().contains("loginuser")){
                        cmd.getStream().send(toString()+" Error: This user already exists, but you are not logged in as him.", cmd.getChannel());
                    }else{
                        if(!user.getConfig().get("loginuser").equals(name)){
                            cmd.getStream().send(toString()+" Error: This user already exists, but you are not logged in as him.", cmd.getChannel());
                        }else{
                            config.setS(name, hash);
                            Commons.log.fine(toString()+cmd.getUser()+" Password for '"+name+"' updated.");
                            cmd.getStream().send(toString()+" Password updated.", cmd.getChannel());
                        }
                    }
                }else{
                    config.setS(name, hash);
                    Commons.log.fine(toString()+cmd.getUser()+" User '"+name+"' registered.");
                    cmd.getStream().send(toString()+" User registered.", cmd.getChannel());
                }
                
            }else{
                int timeout = Integer.parseInt(ci.getValue("timeout"));
                if(config.has(name)){
                    if(config.getS(name).equals(hash)){
                        user.getConfig().set("loginuser", name);
                        user.getConfig().set("logintimeout", timeout);
                        user.activateSession();
                        Commons.log.fine(toString()+cmd.getUser()+" logged in with '"+name+"' and "+timeout+"s timeout.");
                        cmd.getStream().send(toString()+" Login successful.", cmd.getChannel());
                    }else{
                        cmd.getStream().send(toString()+" Password or user wrong.", cmd.getChannel());
                    }
                }else{
                    cmd.getStream().send(toString()+" Password or user wrong.", cmd.getChannel());
                }
            }
        }
    }
    
}
