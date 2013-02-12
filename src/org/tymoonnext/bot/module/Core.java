package org.tymoonnext.bot.module;

import NexT.data.DObject;
import java.util.HashMap;
import org.tymoonnext.bot.Commons;
import org.tymoonnext.bot.Kizai;
import org.tymoonnext.bot.event.CommandEvent;
import org.tymoonnext.bot.event.CommandListener;
import org.tymoonnext.bot.event.EventListener;

/**
 * 
 * @author Shinmera
 * @license GPLv3
 * @version 0.0.0
 */
public class Core extends Module implements CommandListener,EventListener{
    
    public Core(Kizai bot){
        super(bot);
        Commons.log.info(toString()+" Init!");
        bot.registerCommand(CommandEvent.CMD_UNBOUND, this);
        bot.registerCommand("shutdown", this);
        bot.registerCommand("config-save", this);
        bot.registerCommand("config-load", this);
        bot.registerCommand("module-load", this);
        bot.registerCommand("module-reload", this);
        bot.registerCommand("module-unload", this);
        bot.registerCommand("info", this);
        try{bot.bindEvent(CommandEvent.class, this, "propagateCommandEvent");}catch(NoSuchMethodException ex){}
        
        HashMap<String,DObject> mods = (HashMap<String,DObject>)bot.getConfig().get("modules").get();
        for(String mod : mods.keySet()){
            if((Boolean)mods.get(mod).get("autoload").get() == true){
                Commons.log.info(toString()+" Autoloading "+mod);
                bot.loadModule(mod);
            }
        }
    }

    public void shutdown(){
        bot.unbindAllEvents(this);
        bot.unregisterAllCommands(this);
    }

    public void onCommand(CommandEvent cmd){
        if(cmd.getCommand().equals("shutdown")){
            cmd.getStream().send(toString()+" Shutting down...", cmd.getChannel());
            bot.shutdown();
        }else if(cmd.getCommand().equals("config-save")){
            cmd.getStream().send(toString()+" Saving config...", cmd.getChannel());
            bot.getConfig().save(Commons.f_CONFIG);
        }else if(cmd.getCommand().equals("config-load")){
            cmd.getStream().send(toString()+" Loading config...", cmd.getChannel());
            bot.getConfig().load(Commons.f_CONFIG);
        }else if(cmd.getCommand().equals("module-load")){
            cmd.getStream().send(toString()+" Loading module "+cmd.getArgs()+"...", cmd.getChannel());
            if(bot.loadModule(cmd.getArgs())){
                cmd.getStream().send(toString()+" Module loaded!", cmd.getChannel());
            }else{
                cmd.getStream().send(toString()+" Failed to load module!", cmd.getChannel());
            }
        }else if(cmd.getCommand().equals("module-unload")){
            cmd.getStream().send(toString()+" Unloading module "+cmd.getArgs()+" ...", cmd.getChannel());
            if(bot.unloadModule(cmd.getArgs())){
                cmd.getStream().send(toString()+" Module unloaded!", cmd.getChannel());
            }else{
                cmd.getStream().send(toString()+" Failed to unload module!", cmd.getChannel());
            }
        }else if(cmd.getCommand().equals("module-reload")){
            cmd.getStream().send(toString()+" Reloading module "+cmd.getArgs()+" ...", cmd.getChannel());
            if(bot.reloadModule(cmd.getArgs())){
                cmd.getStream().send(toString()+" Module reloaded!", cmd.getChannel());
            }else{
                cmd.getStream().send(toString()+" Failed to reload module!", cmd.getChannel());
            }
        }else if(cmd.getCommand().equals("info")){
            cmd.getStream().send(Commons.FQDN+" v"+Commons.VERSION+" ("+Commons.LICENSE+") by "+Commons.COREDEV+" "+Commons.WEBSITE, cmd.getChannel());
        }else if(cmd.getCommand().equals(CommandEvent.CMD_UNBOUND)){
            cmd.getStream().send(toString()+" Unbound command: "+cmd.getArgs(), cmd.getChannel());
        }
    }

    public void propagateCommandEvent(CommandEvent e){
        bot.command(e);
    }
}
