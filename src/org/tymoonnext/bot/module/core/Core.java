package org.tymoonnext.bot.module.core;

import NexT.data.DObject;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Set;
import java.util.logging.Level;
import org.tymoonnext.bot.Commons;
import org.tymoonnext.bot.Kizai;
import org.tymoonnext.bot.event.CommandListener;
import org.tymoonnext.bot.event.EventBind;
import org.tymoonnext.bot.event.EventListener;
import org.tymoonnext.bot.event.cmdgroup.GroupRegisterEvent;
import org.tymoonnext.bot.event.core.CommandEvent;
import org.tymoonnext.bot.module.Module;

/**
 * Core module that provides essential Kizai functionality. Also handles
 * initial module and dependency load.
 * @author Shinmera
 * @license GPLv3
 * @version 1.1.1
 */
public class Core extends Module implements CommandListener,EventListener{
    
    public Core(Kizai bot){
        super(bot);
        Commons.log.info(toString()+" Init!");
        
        //Core extensions
        bot.loadModule("core.CommandGroupModule");
        
        bot.registerCommand(CommandEvent.CMD_UNBOUND, this);
        bot.registerCommand("shutdown", this);
        bot.event(new GroupRegisterEvent("config", "save", this));
        bot.event(new GroupRegisterEvent("config", "load", this));
        bot.event(new GroupRegisterEvent("module", "load", this));
        bot.event(new GroupRegisterEvent("module", "unload", this));
        bot.event(new GroupRegisterEvent("module", "reload", this));
        bot.event(new GroupRegisterEvent("bind", "list", this));
        bot.event(new GroupRegisterEvent("bind", "add", this));
        bot.event(new GroupRegisterEvent("bind", "remove", this));
        bot.event(new GroupRegisterEvent("command", "list", this));
        bot.event(new GroupRegisterEvent("command", "add", this));
        bot.event(new GroupRegisterEvent("command", "remove", this));
        bot.registerCommand("info", this);
        try{bot.bindEvent(CommandEvent.class, this, "propagateCommandEvent");}catch(NoSuchMethodException ex){}
        
        Commons.log.info(toString()+" Autoloading modules...");
        HashMap<String,DObject> mods = (HashMap<String,DObject>)bot.getConfig().get("modules").get();
        for(String mod : mods.keySet()){
            if((Boolean)mods.get(mod).get("autoload").get() == true){
                Commons.log.info(toString()+" Loading "+mod);
                loadModByConfig(mod);
            }
        }
    }
    
    public boolean loadModByConfig(String mod){
        boolean depsfail = false;
        if(bot.getConfig().get("modules").contains(mod)){
            DObject modObj = bot.getConfig().get("modules").get(mod);
            if(modObj.contains("depends")){
                for(String dep : (Set<String>)modObj.get("depends").getKeySet()){
                    dep = modObj.get("depends").get(dep).toString();
                    Commons.log.info(toString()+" Loading dependency "+dep);
                    if(!loadModByConfig(dep)){
                        Commons.log.warning(toString()+" Dependency loading failed, abandoning module.");
                        depsfail = true;
                        break;
                    }
                }
            }
        }
        if(!depsfail)
            return bot.loadModule(mod);
        return false;
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
            if(cmd.getArgs() == null){
                cmd.getStream().send(toString()+" Required args: Module ...", cmd.getChannel());
                return;
            }
            
            cmd.getStream().send(toString()+" Loading module "+cmd.getArgs()+"...", cmd.getChannel());
            if(bot.loadModule(cmd.getArgs())){
                cmd.getStream().send(toString()+" Module loaded!", cmd.getChannel());
            }else{
                cmd.getStream().send(toString()+" Failed to load module!", cmd.getChannel());
            }
        }else if(cmd.getCommand().equals("module-unload")){
            if(cmd.getArgs() == null){
                cmd.getStream().send(toString()+" Required args: Module ...", cmd.getChannel());
                return;
            }
            
            cmd.getStream().send(toString()+" Unloading module "+cmd.getArgs()+" ...", cmd.getChannel());
            if(bot.unloadModule(cmd.getArgs())){
                cmd.getStream().send(toString()+" Module unloaded!", cmd.getChannel());
            }else{
                cmd.getStream().send(toString()+" Failed to unload module!", cmd.getChannel());
            }
        }else if(cmd.getCommand().equals("module-reload")){
            if(cmd.getArgs() == null){
                cmd.getStream().send(toString()+" Required args: Module ...", cmd.getChannel());
                return;
            }
            
            cmd.getStream().send(toString()+" Reloading module "+cmd.getArgs()+" ...", cmd.getChannel());
            if(bot.reloadModule(cmd.getArgs())){
                cmd.getStream().send(toString()+" Module reloaded!", cmd.getChannel());
            }else{
                cmd.getStream().send(toString()+" Failed to reload module!", cmd.getChannel());
            }
        }else if(cmd.getCommand().equals("bind-list")){
            cmd.getStream().send(toString()+" Binds (Event Priority Listener:Function)", cmd.getChannel());
            for(Class c : bot.getBoundEvents()){
                for(EventBind bind : bot.getEventBinds(c)){
                    cmd.getStream().send("> "+c.getSimpleName()+" "+bind.getPriority()+" "+bind.getListener().getClass().getSimpleName()+":"+bind.getMethod().getName(), cmd.getChannel());
                }
            }
        }else if(cmd.getCommand().equals("bind-add")){
            if((cmd.getArgs() == null) || (cmd.getArgs().split(" ").length<3)){
                cmd.getStream().send(toString()+" Required args: bindClass bindModule bindFunc [bindPrio]", cmd.getChannel());
                return;
            }
            
            String[] args = cmd.getArgs().split(" ");
            String bindClass = args[0];
            String bindModule = args[1];
            String bindFunc = args[2];
            int bindPrio = 0;
            if(args.length>3)bindPrio = Integer.parseInt(args[3]);
            
            if(bot.getModule(bindModule) == null){
                cmd.getStream().send(toString()+" Module '"+bindModule+"' not found.", cmd.getChannel());
                return;
            }
            if(!(bot.getModule(bindModule) instanceof EventListener)){
                cmd.getStream().send(toString()+" Module '"+bindModule+"' is not an event listener.", cmd.getChannel());
                return;
            }
            
            try{
                Class bindC = Class.forName(bindClass);
                try {
                    bot.bindEvent(bindC, (EventListener)bot.getModule(bindModule), bindFunc, bindPrio);
                } catch (NoSuchMethodException ex) {
                    cmd.getStream().send(toString()+" No such function '"+bindFunc+"'..", cmd.getChannel());
                    return;
                }
            }catch(ClassNotFoundException ex){
                cmd.getStream().send(toString()+" Bind class '"+bindClass+"' not found.", cmd.getChannel());
                return;
            }
            
            cmd.getStream().send(toString()+" Binding for "+bindClass+" to "+bindModule+":"+bindFunc+" with prio "+bindPrio+" added.", cmd.getChannel());
        }else if(cmd.getCommand().equals("bind-remove")){
            if((cmd.getArgs() == null) || (cmd.getArgs().split(" ").length<2)){
                cmd.getStream().send(toString()+" Required args: bindClass bindModule", cmd.getChannel());
                return;
            }
            
            String[] args = cmd.getArgs().split(" ");
            String bindClass = args[0];
            String bindModule = args[1];
            
            if(bot.getModule(bindModule) == null){
                cmd.getStream().send(toString()+" Module '"+bindModule+"' not found.", cmd.getChannel());
                return;
            }
            
            try{
                Class bindC = Class.forName(bindClass);
                bot.unbindEvent(bindC, (EventListener)bot.getModule(bindModule));
            }catch(ClassNotFoundException ex){
                cmd.getStream().send(toString()+" Bind class '"+bindClass+"' not found.", cmd.getChannel());
                return;
            }
            
            cmd.getStream().send(toString()+" Binding for "+bindClass+" to "+bindModule+" removed.", cmd.getChannel());
        }else if(cmd.getCommand().equals("info")){
            cmd.getStream().send(Commons.getVersionString(), cmd.getChannel());
        }else if(cmd.getCommand().equals(CommandEvent.CMD_UNBOUND)){
            cmd.getStream().send(toString()+" Unbound command: "+cmd.getArgs(), cmd.getChannel());
        }
    }

    public void propagateCommandEvent(CommandEvent e){
        try {
            Method cmd = bot.getClass().getDeclaredMethod("command", CommandEvent.class);
            cmd.setAccessible(true);
            cmd.invoke(bot, e);
            cmd.setAccessible(false);
        } catch (IllegalAccessException ex) {
            Commons.log.log(Level.WARNING,toString()+" Failed to propagate "+e+" to command function!", ex);
        } catch (IllegalArgumentException ex) {
            Commons.log.log(Level.WARNING,toString()+" Failed to propagate "+e+" to command function!", ex);
        } catch (InvocationTargetException ex) {
            Commons.log.log(Level.WARNING,toString()+" Failed to propagate "+e+" to command function!", ex);
        } catch (NoSuchMethodException ex) {
            Commons.log.log(Level.WARNING,toString()+" Failed to propagate "+e+" to command function!", ex);
        } catch (SecurityException ex) {
            Commons.log.log(Level.WARNING,toString()+" Failed to propagate "+e+" to command function!", ex);
        }
    }
}
