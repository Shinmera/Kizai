package org.tymoonnext.bot.module.core;

import org.tymoonnext.bot.module.core.ext.CommandModule;
import NexT.data.DObject;
import NexT.data.DParse;
import NexT.util.StringUtils;
import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.tymoonnext.bot.Commons;
import org.tymoonnext.bot.Kizai;
import org.tymoonnext.bot.event.CommandListener;
import org.tymoonnext.bot.event.Event;
import org.tymoonnext.bot.event.EventBind;
import org.tymoonnext.bot.event.EventListener;
import org.tymoonnext.bot.event.cmd.CommandInstanceEvent;
import org.tymoonnext.bot.event.core.CommandEvent;
import org.tymoonnext.bot.meta.Info;
import org.tymoonnext.bot.module.Module;
import org.tymoonnext.bot.module.cmd.CommandInstance;
import org.tymoonnext.bot.stream.Stream;

/**
 * Core module that provides essential Kizai functionality. Also handles
 * initial module and dependency load.
 * @author Shinmera
 * @license GPLv3
 * @version 1.3.1
 */
public class Core extends Module implements CommandListener,EventListener{
    
    public Core(Kizai bot){
        super(bot);
        Commons.log.info(toString()+" Init!");
        
        //Core extensions
        Set<Class<? extends Module>> extensions = Commons.getPackageReflections("org.tymoonnext.bot.module.core.ext").getSubTypesOf(Module.class);
        for(Class<? extends Module> c : extensions){
            bot.loadModule(c.getName().replace(Commons.MODULE_PACKAGE, ""));
        }
        
        bot.registerCommand(CommandEvent.CMD_UNBOUND, this);
        try{bot.bindEvent(CommandEvent.class, this, "propagateCommandEvent");}catch(NoSuchMethodException ex){}
        CommandModule.register(bot, "info",             null,                                           "Show some information about the bot.", this);
        CommandModule.register(bot, "shutdown",         "delay[0](INTEGER)".split(" "),                 "Safely shut down the bot.", this);
        CommandModule.register(bot, "config", "show",   "file[]".split(" "),                            "Show the contents of a config file.", this);
        CommandModule.register(bot, "config", "save",   "file[]".split(" "),                            "Save the configuration to disk.", this);
        CommandModule.register(bot, "config", "load",   "file[]".split(" "),                            "Load the configuration from disk.", this);
        CommandModule.register(bot, "config", "get",    "branch".split(" "),                            "Get a specific configuration value through branch spec. Branch example: modules>irc.IRCBot>autoload", this);
        CommandModule.register(bot, "config", "set",    "branch value".split(" "),                      "Set a specific configuration value through branch spec. Branch example: modules>irc.IRCBot>autoload", this);
        CommandModule.register(bot, "module", "list",   null,                                           "List all known module classes.", this);
        CommandModule.register(bot, "module", "info",   "module".split(" "),                            "Show available information about a module.", this);
        CommandModule.register(bot, "module", "load",   "module".split(" "),                            "Attempt to load a module.", this);
        CommandModule.register(bot, "module", "unload", "module".split(" "),                            "Unload (shutdown) a module.", this);
        CommandModule.register(bot, "module", "reload", "module".split(" "),                            "Attempt to reload a module.", this);
        CommandModule.register(bot, "event", "list",    null,                                           "List all available event classes.", this);
        CommandModule.register(bot, "event", "info",    "event".split(" "),                             "Show information about an event.", this);
        CommandModule.register(bot, "event", "invoke",  "event".split(" "),                             "Attempt to invoke an event. Note that you need to add all required "+
                                                                                                        "event arguments into the command, to satisfy the constructor. Also "+
                                                                                                        "note that this may still fail spectacularly if a constructor argument "+
                                                                                                        "Expects anything other than primitve types or strings. Core will try "+
                                                                                                        "its best to parse your arguments into the required values, but may "+
                                                                                                        "fail to do so regardless.", this);
        CommandModule.register(bot, "bind", "list",     "event[] module[]".split(" "),                  "List all binds specific to a module or event.", this);
        CommandModule.register(bot, "bind", "add",      "event module function priority[0](INTEGER)".split(" "),"Bind a new event to a module.", this);
        CommandModule.register(bot, "bind", "remove",   "event module".split(" "),                      "Unbind a module from an event.", this);
        CommandModule.register(bot, "command", "list",  "module[]".split(" "),                          "List all commands specific to a module.", this);
        CommandModule.register(bot, "command", "add",   "cmd module".split(" "),                        "Register a new command for a module.", this);
        CommandModule.register(bot, "command", "remove","cmd".split(" "),                               "Unregister a command from a module.", this);
        CommandModule.register(bot, "stream", "list",   null,                                           "List all available streams.", this);
        CommandModule.register(bot, "stream", "close",  "stream".split(" "),                            "Close an internal stream.", this);
        CommandModule.register(bot, "stream", "remove", "stream".split(" "),                            "Remove an internal stream.", this);
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
            
        }else if(i.getName().equals("config show")){
            cmd.getStream().send(toString()+" The config is: ", cmd.getChannel());
            if(i.getValue("file").isEmpty())    cmd.getStream().send(DParse.parse(bot.getConfig().get(), false), cmd.getChannel());
            else                                cmd.getStream().send(DParse.parse(DParse.parse(new File(Commons.f_BASEDIR, i.getValue("file"))), false), cmd.getChannel());
            
        }else if(i.getName().equals("config save")){
            cmd.getStream().send(toString()+" Saving config...", cmd.getChannel());
            if(i.getValue("file").isEmpty())    bot.getConfig().save(Commons.f_CONFIG);
            else                                bot.getConfig().save(new File(Commons.f_BASEDIR, i.getValue("file")));
            
        }else if(i.getName().equals("config load")){
            cmd.getStream().send(toString()+" Loading config...", cmd.getChannel());
            if(i.getValue("file").isEmpty())    bot.getConfig().load(Commons.f_CONFIG);
            else                                bot.getConfig().load(new File(Commons.f_BASEDIR, i.getValue("file")));
            
        }else if(i.getName().equals("config get")){
            try{
                DObject field = DParse.get(bot.getConfig().get(), i.getValue("branch"), ">");
                cmd.getStream().send(toString()+" "+field, cmd.getChannel());
            }catch(IllegalArgumentException ex){
                cmd.getStream().send(toString()+" Failed to get field: "+ex.getMessage(), cmd.getChannel());
            }
            
        }else if(i.getName().equals("config set")){
            String branch = i.getValue("branch");
            DObject orig = null;
            try{orig = DParse.get(bot.getConfig().get(), branch, ">");
            }catch(IllegalArgumentException ex){cmd.getStream().send(toString()+" Failed to get field: "+ex.getMessage(), cmd.getChannel());}
            
            DObject val = DParse.parse("noop: "+i.getValue("value")+";").get("noop");
            if(!branch.contains("."))bot.getConfig().set(branch, val);
            else{
                DObject parent = DParse.get(bot.getConfig().get(), branch.substring(0, branch.lastIndexOf('>')), ">");
                parent.set(branch.substring(branch.lastIndexOf('.')), val);
            }
            cmd.getStream().send(toString()+" '"+orig+"' changed to '"+val+"' ("+val.getType()+")", cmd.getChannel());
            
        }else if(i.getName().equals("module list")){
            Set<Class<? extends Module>> modules = Commons.reflections.getSubTypesOf(Module.class);
            cmd.getStream().send(toString()+" Module listing: ", cmd.getChannel());
            for(Class<? extends Module> c : modules){
                if(!Modifier.isAbstract(c.getModifiers()))
                    cmd.getStream().send(" * "+c.getName().replace(Commons.MODULE_PACKAGE, ""), cmd.getChannel());
            }
            
        }else if(i.getName().equals("module info")){
            try{
                Class module = Class.forName(Commons.MODULE_PACKAGE+i.getValue("module"));
                
                cmd.getStream().send(toString()+" Information about "+i.getValue("module")+":", cmd.getChannel());
                if(module.getAnnotation(Info.class) != null){
                    cmd.getStream().send(((Info)module.getAnnotation(Info.class)).txt(), cmd.getChannel());
                }
                cmd.getStream().send("  This module is currently "+((bot.getModule(i.getValue("module"))==null)? "unloaded" : "loaded")+".", cmd.getChannel());
            }catch(ClassNotFoundException ex){
                cmd.getStream().send(toString()+" Module '"+i.getValue("module")+"' not found.", cmd.getChannel());
            }
            
        }else if(i.getName().equals("module load")){
            cmd.getStream().send(toString()+" Loading module "+cmd.getArgs()+"...", cmd.getChannel());
            if(bot.loadModule(cmd.getArgs()))   cmd.getStream().send(toString()+" Module loaded!", cmd.getChannel());
            else                                cmd.getStream().send(toString()+" Failed to load module!", cmd.getChannel());
            
        }else if(i.getName().equals("module unload")){
            cmd.getStream().send(toString()+" Unloading module "+cmd.getArgs()+" ...", cmd.getChannel());
            if(bot.unloadModule(cmd.getArgs())) cmd.getStream().send(toString()+" Module unloaded!", cmd.getChannel());
            else                                cmd.getStream().send(toString()+" Failed to unload module!", cmd.getChannel());
            
        }else if(i.getName().equals("module reload")){
            cmd.getStream().send(toString()+" Reloading module "+cmd.getArgs()+" ...", cmd.getChannel());
            if(bot.reloadModule(cmd.getArgs())) cmd.getStream().send(toString()+" Module reloaded!", cmd.getChannel());
            else                                cmd.getStream().send(toString()+" Failed to reload module!", cmd.getChannel());
            
        }else if(i.getName().equals("event list")){
            Set<Class<? extends Event>> events = Commons.reflections.getSubTypesOf(Event.class);
            cmd.getStream().send(toString()+" Module listing: ", cmd.getChannel());
            for(Class<? extends Event> c : events){
                if(!Modifier.isAbstract(c.getModifiers()))
                    cmd.getStream().send(" * "+c.getName().replace(Commons.EVENT_PACKAGE, ""), cmd.getChannel());
            }
            
        }else if(i.getName().equals("event info")){
            try{
                Class event = Class.forName(Commons.EVENT_PACKAGE+i.getValue("event"));
                
                cmd.getStream().send(toString()+" Information about "+i.getValue("event")+":", cmd.getChannel());
                if(event.getAnnotation(Info.class) != null){
                    cmd.getStream().send(((Info)event.getAnnotation(Info.class)).txt(), cmd.getChannel());
                }
                for(Constructor constructor : event.getConstructors()){
                    StringBuilder args = new StringBuilder();int j=0;
                    for(Class c : constructor.getParameterTypes()){
                        args.append(c.getSimpleName());
                        if(j<constructor.getParameterTypes().length-1)args.append(", ");
                        j++;
                    }
                    cmd.getStream().send("  "+i.getValue("event")+"("+args+")", cmd.getChannel());
                }
            }catch(ClassNotFoundException ex){
                cmd.getStream().send(toString()+" Event '"+i.getValue("event")+"' not found.", cmd.getChannel());
            }
            
        }else if(i.getName().equals("event invoke")){
            try{
                cmd.getStream().send(toString()+" Attempting to invoke Event "+i.getValue("event")+"...", cmd.getChannel());
                Class event = Class.forName(Commons.EVENT_PACKAGE+i.getValue("event"));
                Object[] args = new Object[i.getAddPargs().length];
                Class[] cargs = new Class[i.getAddPargs().length];
                StringBuilder str = new StringBuilder();
                for(int j=0;j<args.length;j++){
                    DObject arg = DObject.parse(i.getAddPargs()[j]);
                    args[j] = arg.get();
                    switch(arg.getType()){
                        case DObject.TYPE_BOOLEAN:  cargs[j] = Boolean.TYPE;break;
                        case DObject.TYPE_DOUBLE:   cargs[j] = Double.TYPE;break;
                        case DObject.TYPE_INTEGER:  cargs[j] = Integer.TYPE;break;
                        case DObject.TYPE_LONG:     cargs[j] = Long.TYPE;break;
                        case DObject.TYPE_NULL:     cargs[j] = null;break;
                        case DObject.TYPE_OBJECT:   cargs[j] = Object.class;break;
                        case DObject.TYPE_STRING:   cargs[j] = String.class;break;
                    }
                    str.append(cargs[j].getSimpleName()).append(" ");
                }
                cmd.getStream().send("  Detected types: "+str+"", cmd.getChannel());
                
                Constructor constructor = event.getConstructor(cargs);
                Event e = (Event)constructor.newInstance(args);
                bot.event(e);
                cmd.getStream().send("  Invocation succeeded! Event: "+e, cmd.getChannel());
                
            }catch(InstantiationException ex){
                cmd.getStream().send("  Error during instantiation: "+ex.getMessage(), cmd.getChannel());
            }catch(IllegalAccessException ex){
                cmd.getStream().send("  Error during instantiation: "+ex.getMessage(), cmd.getChannel());
            }catch(IllegalArgumentException ex){
                cmd.getStream().send("  Error during instantiation: "+ex.getMessage(), cmd.getChannel());
            }catch(InvocationTargetException ex){
                cmd.getStream().send("  Error during instantiation: "+ex.getMessage(), cmd.getChannel());
            }catch(NoSuchMethodException ex){
                cmd.getStream().send("  No matching constructor found.", cmd.getChannel());
            }catch(SecurityException ex){
                cmd.getStream().send("  Constructor is not accessible.", cmd.getChannel());
            }catch(ClassNotFoundException ex){
                cmd.getStream().send("  Event '"+i.getValue("event")+"' not found.", cmd.getChannel());
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
                    Class bindC = Class.forName(Commons.EVENT_PACKAGE+i.getValue("event"));
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
                Class bindC = Class.forName(Commons.EVENT_PACKAGE+i.getValue("event"));
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
                            cmd.getStream().send(" * "+c.getSimpleName()+" "+b.getPriority()+" "+b.getListener()+":"+b.getMethod().getName(), cmd.getChannel());
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
            bot.getStream(i.getValue("stream")).send(i.getValue("message")+StringUtils.implode(i.getAddPargs(), " "), i.getValue("channel"));
            
        }else if(i.getName().equals("stream broadcast")){
            bot.broadcast(cmd.getArgs());
            
        }else if(i.getName().equals("stream close")){
            if(bot.getStream(i.getValue("stream")) == null){
                cmd.getStream().send(toString()+" No such stream '"+i.getValue("stream")+"'.", cmd.getChannel());
                return;
            }
            bot.getStream(i.getValue("stream")).close();
            
        }else if(i.getName().equals("stream remove")){
            if(bot.getStream(i.getValue("stream")) == null){
                cmd.getStream().send(toString()+" No such stream '"+i.getValue("stream")+"'.", cmd.getChannel());
                return;
            }
            bot.unregisterStream(i.getValue("stream"));
            
        }else if(i.getName().equals("stream list")){
            cmd.getStream().send(toString()+" Stream listing: ", cmd.getChannel());
            for(Stream s : bot.getStreams()){
                cmd.getStream().send(" * "+s.getID()+" ("+((s.isClosed())? "closed" : "open")+")", cmd.getChannel());
            }
            
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
