package org.tymoonnext.bot.module.core;

import NexT.data.DObject;
import NexT.util.StringUtils;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import org.tymoonnext.bot.Commons;
import org.tymoonnext.bot.Kizai;
import org.tymoonnext.bot.event.CommandListener;
import org.tymoonnext.bot.event.Event;
import org.tymoonnext.bot.event.EventBind;
import org.tymoonnext.bot.event.EventListener;
import org.tymoonnext.bot.event.cmd.CommandInstanceEvent;
import org.tymoonnext.bot.event.core.CommandEvent;
import org.tymoonnext.bot.module.Module;
import org.tymoonnext.bot.module.cmd.CommandInstance;

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
        bot.loadModule("core.CommandModule");
        
        bot.registerCommand(CommandEvent.CMD_UNBOUND, this);
        try{bot.bindEvent(CommandEvent.class, this, "propagateCommandEvent");}catch(NoSuchMethodException ex){}
        CommandModule.register(bot, "info",             null,                                           "Show some information about the bot.", this);
        CommandModule.register(bot, "shutdown",         "delay[0](INTEGER)".split(" "),                 "Safely shut down the bot.", this);
        CommandModule.register(bot, "config",           "todo{save|load}".split(" "),                   "Save or load the configuration.", this);
        CommandModule.register(bot, "module",           "todo{load|unload|reload} class".split(" "),    "Load a module.", this);
        CommandModule.register(bot, "bind", "list",     "event[] module[]".split(" "),                  "List all binds specific to a module or event.", this);
        CommandModule.register(bot, "bind", "add",      "event module function priority[0](INTEGER)".split(" "),"Bind a new event to a module.", this);
        CommandModule.register(bot, "bind", "remove",   "event module".split(" "),                      "Unbind a module from an event.", this);
        CommandModule.register(bot, "command", "list",  "module[]".split(" "),                          "List all commands specific to a module.", this);
        CommandModule.register(bot, "command", "add",   "cmd module".split(" "),                        "Register a new command for a module.", this);
        CommandModule.register(bot, "command", "remove","cmd".split(" "),                               "Unregister a command from a module.", this);
        CommandModule.register(bot, "stream", "list",   null,                                           "List all available streams.", this);
        CommandModule.register(bot, "stream", "close",  "stream".split(" "),                            "Close an internal stream.", this);
        CommandModule.register(bot, "stream", "send",   "stream channel message".split(" "),            "Send a custom message through a stream.", this);
        CommandModule.register(bot, "stream", "broadcast","message".split(" "),                         "Broadcast a custom message through all streams (and potentially channels).", this);
        
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
        if(!(cmd instanceof CommandInstanceEvent)){
            if(cmd.getCommand().equals(CommandEvent.CMD_UNBOUND))
                cmd.getStream().send(toString()+" Unbound command: "+cmd.getArgs(), cmd.getChannel());
            return;
        }
        
        CommandInstance i = ((CommandInstanceEvent)cmd).get();
        if(i.getName().equals("shutdown")){
            new Timer().schedule(new TimerTask(){
                public void run(){
                    bot.shutdown();
                }
            }, Integer.parseInt(i.getValue("delay")) * 1000);
            cmd.getStream().send(toString()+" Shutting down in "+i.getValue("delay")+"s ...", cmd.getChannel());
            
        }else if(i.getName().equals("config")){
            if(i.getValue("todo").equals("save")){
                cmd.getStream().send(toString()+" Saving config...", cmd.getChannel());
                bot.getConfig().save(Commons.f_CONFIG);
            }else if(i.getValue("todo").equals("load")){
                cmd.getStream().send(toString()+" Loading config...", cmd.getChannel());
                bot.getConfig().load(Commons.f_CONFIG);
            }
            
        }else if(i.getName().equals("module")){      
            if(i.getValue("todo").equals("load")){
                cmd.getStream().send(toString()+" Loading module "+cmd.getArgs()+"...", cmd.getChannel());
                if(bot.loadModule(cmd.getArgs()))   cmd.getStream().send(toString()+" Module loaded!", cmd.getChannel());
                else                                cmd.getStream().send(toString()+" Failed to load module!", cmd.getChannel());
            }
            
            else if(i.getValue("todo").equals("unload")){
                cmd.getStream().send(toString()+" Unloading module "+cmd.getArgs()+" ...", cmd.getChannel());
                if(bot.unloadModule(cmd.getArgs())) cmd.getStream().send(toString()+" Module unloaded!", cmd.getChannel());
                else                                cmd.getStream().send(toString()+" Failed to unload module!", cmd.getChannel());
            }
            
            else if(i.getValue("todo").equals("reload")){
                cmd.getStream().send(toString()+" Reloading module "+cmd.getArgs()+" ...", cmd.getChannel());
                if(bot.reloadModule(cmd.getArgs())) cmd.getStream().send(toString()+" Module reloaded!", cmd.getChannel());
                else                                cmd.getStream().send(toString()+" Failed to reload module!", cmd.getChannel());
            }
            
        }else if(i.getName().equals("bind add")){            
            if(bot.getModule(i.getValue("module")) == null){
                cmd.getStream().send(toString()+" Module '"+i.getValue("module")+"' not found.", cmd.getChannel());
                return;
            }
            if(!(bot.getModule(i.getValue("module")) instanceof EventListener)){
                cmd.getStream().send(toString()+" Module '"+i.getValue("module")+"' is not an event listener.", cmd.getChannel());
                return;
            }
            
            try{
                try {
                    Class bindC = Class.forName(i.getValue("event"));
                    bot.bindEvent(bindC, (EventListener)bot.getModule(i.getValue("module")), i.getValue("function"), Integer.parseInt(i.getValue("priority")));
                    cmd.getStream().send(toString()+" Binding for "+i.getValue("event")+" to "+i.getValue("module")+":"+i.getValue("function")+" with prio "+i.getValue("priority")+" added.", cmd.getChannel());
                } catch (NoSuchMethodException ex) {
                    cmd.getStream().send(toString()+" No such function '"+i.getValue("function")+"'..", cmd.getChannel());
                }
            }catch(ClassNotFoundException ex){
                cmd.getStream().send(toString()+" Bind class '"+i.getValue("event")+"' not found.", cmd.getChannel());
            }
            
        }else if(i.getName().equals("bind remove")){
            if(bot.getModule(i.getValue("module")) == null){
                cmd.getStream().send(toString()+" Module '"+i.getValue("module")+"' not found.", cmd.getChannel());
                return;
            }
            
            try{
                Class bindC = Class.forName(i.getValue("event"));
                bot.unbindEvent(bindC, (EventListener)bot.getModule(i.getValue("module")));
                cmd.getStream().send(toString()+" Binding for "+i.getValue("event")+" to "+i.getValue("module")+" removed.", cmd.getChannel());
            }catch(ClassNotFoundException ex){
                cmd.getStream().send(toString()+" Bind class '"+i.getValue("event")+"' not found.", cmd.getChannel());
            }
        }else if(i.getName().equals("bind list")){
            Class module = Object.class;
            Class event = Event.class;
            try{
                if(!i.getValue("module").isEmpty())module = bot.getModule(i.getValue("module")).getClass();
            }catch(NullPointerException ex){
                cmd.getStream().send(toString()+" Warning: Module '"+i.getValue("module")+"' not found. Defaulting to any.", cmd.getChannel());
            }try{
                if(!i.getValue("module").isEmpty())event = Class.forName(i.getValue("event"));
            }catch(ClassNotFoundException ex){
                cmd.getStream().send(toString()+" Warning: Event '"+i.getValue("event")+"' not found. Defaulting to any.", cmd.getChannel());
            }
            
            cmd.getStream().send(toString()+" Bind listing: ", cmd.getChannel());
            for(Class<? extends Event> c : bot.getBoundEvents()){
                if(event.isAssignableFrom(c)){
                    for(EventBind b : bot.getEventBinds(c)){
                        if(module.isAssignableFrom(b.getListener().getClass())){
                            cmd.getStream().send(" * "+event.getSimpleName()+" "+b.getPriority()+" "+b.getListener()+":"+b.getMethod().getName(), cmd.getChannel());
                        }
                    }
                }
            }
            
        }else if(i.getName().equals("command add")){            
            if(bot.getModule(i.getValue("module")) == null){
                cmd.getStream().send(toString()+" Module '"+i.getValue("module")+"' not found.", cmd.getChannel());
                return;
            }
            if(!(bot.getModule(i.getValue("module")) instanceof CommandListener)){
                cmd.getStream().send(toString()+" Module '"+i.getValue("module")+"' is not a command listener.", cmd.getChannel());
                return;
            }
            
            bot.registerCommand(i.getValue("cmd"), (CommandListener)bot.getModule(i.getValue("module")));
            cmd.getStream().send(toString()+" Command "+i.getValue("cmd")+" registered for "+i.getValue("module")+".", cmd.getChannel());
            
        }else if(i.getName().equals("command remove")){
            bot.unregisterCommand(i.getValue("cmd"));
            cmd.getStream().send(toString()+" Command "+i.getValue("cmd")+" removed.", cmd.getChannel());
            
        }else if(i.getName().equals("command list")){
            Class module = Object.class;
            try{
                if(!i.getValue("module").isEmpty())module = bot.getModule(i.getValue("module")).getClass();
            }catch(NullPointerException ex){
                cmd.getStream().send(toString()+" Warning: Module '"+i.getValue("module")+"' not found. Defaulting to any.", cmd.getChannel());
            }
            
            cmd.getStream().send(toString()+" Command listing: ", cmd.getChannel());
            for(String com : bot.getRegisteredCommands()){
                CommandListener lst = bot.getCommandListener(com);
                if(module.isAssignableFrom(lst.getClass())){
                    cmd.getStream().send(" * "+com+" "+lst, cmd.getChannel());
                }
            }
            
        }else if(i.getName().equals("stream send")){
            if(bot.getStream(i.getValue("stream")) == null){
                cmd.getStream().send(toString()+" No such stream '"+i.getValue("stream")+"'.", cmd.getChannel());
                return;
            }
            bot.getStream(i.getValue("stream")).send(StringUtils.implode(i.getAddPargs(), " "), i.getValue("channel"));
            
        }else if(i.getName().equals("stream send")){
            bot.broadcast(cmd.getArgs());
            
        }else if(i.getName().equals("stream close")){
            if(bot.getStream(i.getValue("stream")) == null){
                cmd.getStream().send(toString()+" No such stream '"+i.getValue("stream")+"'.", cmd.getChannel());
                return;
            }
            bot.unregisterStream(i.getValue("stream"));
            
        }else if(i.getName().equals("stream list")){
            
        }else if(i.getName().equals("info")){
            cmd.getStream().send(Commons.getVersionString(), cmd.getChannel());
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
