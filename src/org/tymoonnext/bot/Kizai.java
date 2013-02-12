package org.tymoonnext.bot;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeSet;
import java.util.logging.Level;
import org.tymoonnext.bot.event.CommandEvent;
import org.tymoonnext.bot.event.CommandListener;
import org.tymoonnext.bot.event.Event;
import org.tymoonnext.bot.event.EventBind;
import org.tymoonnext.bot.event.EventListener;
import org.tymoonnext.bot.event.ModuleLoadEvent;
import org.tymoonnext.bot.event.ModuleUnloadEvent;
import org.tymoonnext.bot.module.Module;
import org.tymoonnext.bot.stream.Stream;

/**
 * Main Bot class that handles all event, command and module capabilities.
 * @author Shinmera
 * @license GPLv3
 * @version 2.0.1
 */
public class Kizai{
    public static void main(String[] args){new Kizai();}
    
    static{
        Commons.log.info("[INIT] Starting up...");
    }
    
    private ClassLoader classLoader;
    private HashMap<String,Module> modules;
    private HashMap<String,ArrayList<CommandListener>> commands;
    private HashMap<Class<? extends Event>,TreeSet<EventBind>> events;
    private HashMap<String,Stream> streams;
    private Configuration conf;
    
    public Kizai(){
        conf = new Configuration();
        modules = new HashMap<String, Module>();
        commands = new HashMap<String, ArrayList<CommandListener>>();
        events = new HashMap<Class<? extends Event>,TreeSet<EventBind>>();
        streams = new HashMap<String, Stream>();
        classLoader = new ReloadingCapableClassLoader();
        
        conf.load(Commons.f_CONFIG);
        
        commands.put(CommandEvent.CMD_ANY, new ArrayList<CommandListener>());
        commands.put(CommandEvent.CMD_UNBOUND, new ArrayList<CommandListener>());
        events.put(Event.class, new TreeSet<EventBind>());
        streams.put("stdout", Commons.stdout);
        
        loadModule("Core");
    }
    
    /**
     * Shuts everything down.
     */
    public synchronized void shutdown(){
        try{
            Commons.log.info("[MAIN] Shutting down...");
            Commons.log.info("[MAIN] Shutting down modules.");
            for(Module mod : modules.values()){
                try{mod.shutdown();
                }catch(Throwable t){
                    Commons.log.log(Level.WARNING, "[MAIN]"+mod+" Failed to shutdown cleanly!", t);
                }
            }
            Commons.log.info("[MAIN] Closing streams.");
            for(Stream stream : streams.values()){
                try{stream.close();
                }catch(Throwable t){
                    Commons.log.log(Level.WARNING, "[MAIN]"+stream+" Failed to close cleanly!", t);
                }
            }
            Commons.log.info("[MAIN] Saving config.");
            conf.save(Commons.f_CONFIG);
        }finally{
            Commons.log.info("[MAIN] Goodbye!");
            System.exit(0);
        }
    }
    
    /**
     * Attempts to load the specified Module, creates a new Instance and adds
     * it to the module index.
     * @param module The Class name of the Module to load.
     * @return Whether the loading succeeded.
     */
    public synchronized boolean loadModule(String module){
        Commons.log.info("[MAIN] Attempting to load "+module);
        if(modules.containsKey(module)){
            Commons.log.warning("[MAIN] Module already loaded.");
            return false;
        }
        try{
            Class modClass = Class.forName(Commons.MODULE_PACKAGE+module, true, classLoader);
            Constructor[] modConstrs = modClass.getConstructors();
            Module modInstance =  (Module)modConstrs[0].newInstance(this);
            modules.put(module, modInstance);
            event(new ModuleLoadEvent(Commons.stdout, modInstance));
        }catch(IllegalArgumentException ex){
            Commons.log.log(Level.WARNING, "[MAIN] Failed to load module "+module+", failed to use constructor.", ex);
            return false;
        }catch(InvocationTargetException ex){
            Commons.log.log(Level.WARNING, "[MAIN] Failed to load module "+module+", failed to use constructor.", ex);
            return false;
        }catch(ClassNotFoundException ex){
            Commons.log.log(Level.WARNING, "[MAIN] Failed to load module "+module+", class not found.", ex);
            return false;
        }catch(IllegalAccessException ex){
            Commons.log.log(Level.WARNING, "[MAIN] Failed to load module "+module+", access denied.", ex);
            return false;
        }catch(InstantiationException ex){
            Commons.log.log(Level.WARNING, "[MAIN] Failed to load module "+module+", failed to instanciate.", ex);
            return false;
        }
        return true;
    }
    
    /**
     * Unloads the specified Module by calling its shutdown function. Note that
     * the EventListener and CommandListeners that have been registered by the
     * Module need to be unloaded in the shutdown function of the Module to
     * guarantee a proper unloading process.
     * @param module The Class name of the Module to unload.
     * @return Whether the unloading succeeded.
     */
    public synchronized boolean unloadModule(String module){
        Commons.log.info("[MAIN] Attempting to unload "+module);
        if(!modules.containsKey(module)){
            Commons.log.warning("[MAIN] Module not loaded.");
            return false;
        }
        Module mod = modules.get(module);
        event(new ModuleUnloadEvent(Commons.stdout, mod));
        mod.shutdown();
        modules.remove(module);
        return true;
    }
    
    /**
     * Unloads the specified Module, reloads its class file and loads the Module
     * anew.
     * @param module The Class name of the Module to reload.
     * @return Whether the loading succeeded.
     */
    public synchronized boolean reloadModule(String module){
        Commons.log.info("[MAIN] Attempting to reload "+module);        
        unloadModule(module);
        classLoader = new ReloadingCapableClassLoader();
        return loadModule(module);
    }
    
    /**
     * Registers a new Stream within the bot. Streams are used to output data.
     * @param id The identifier of the Stream.
     * @param stream The Stream itself.
     */
    public synchronized void registerStream(String id, Stream stream){
        Commons.log.info("[MAIN] Registering stream "+id);
        streams.put(id, stream);
    }
    
    /**
     * Bind the listener to the given command String. You can use the special
     * types in the CommandEvent class to register for either unbound or any
     * command.
     * @param cmd The command to listen for.
     * @param m The listener.
     */
    public synchronized void registerCommand(String cmd, CommandListener m){
        Commons.log.info("[MAIN] "+m+" Registering command "+cmd);
        if(!commands.containsKey(cmd))commands.put(cmd, new ArrayList<CommandListener>());
        commands.get(cmd).add(m);
    }
    
    /**
     * Bind the listener's function func to the Event Class. The function should
     * expect only one argument, the Event that is passed to it. You can listen
     * to any event if you register the general Event class.
     * @param evt The Class to listen for.
     * @param m The Listener.
     * @param func The function to call on the Listener.
     * @param priority The listener priority. The higher, the earlier the event
     * will arrive at the specified listener.
     * @throws NoSuchMethodException if the specified Method cannot be found
     * within the listener.
     */
    public synchronized void bindEvent(Class<? extends Event> evt, EventListener m, String func, int priority) throws NoSuchMethodException{
        Commons.log.info("[MAIN] "+m+" Binding event "+evt.getSimpleName()+" to function "+func);
        if(!events.containsKey(evt))events.put(evt, new TreeSet<EventBind>());
        events.get(evt).add(new EventBind(m, func, priority));
    }
    
    /**
     * Bind the listener's function func to the Event Class. The function should
     * expect only one argument, the Event that is passed to it. You can listen
     * to any event if you register the general Event class.
     * @param evt The Class to listen for.
     * @param m The Listener.
     * @param func The function to call on the Listener.
     * @throws NoSuchMethodException if the specified Method cannot be found
     * within the listener.
     */
    public synchronized void bindEvent(Class<? extends Event> evt, EventListener m, String func) throws NoSuchMethodException{bindEvent(evt, m, func);}
    
    /**
     * Unregister the given stream.
     * @param id The Stream to unregister.
     */
    public synchronized void unregisterStream(String id){streams.remove(id);}
    
    /**
     * Unregister a given listener from a specific command.
     * @param cmd The command to unregister from.
     * @param m The CommandListener to unbind.
     */
    public synchronized void unregisterCommand(String cmd, CommandListener m){
        if(commands.containsKey(cmd))commands.get(cmd).remove(m);
    }
    
    /**
     * Unbind a given listener from a specific event.
     * @param evt The Event Class to unbind from.
     * @param m The EventListener to unbind.
     */
    public synchronized void unbindEvent(Class<? extends Event> evt, EventListener m){
        if(events.containsKey(evt)){
            for(EventBind bind : events.get(evt)){
                if(bind.getListener() == m){
                    events.get(evt).remove(bind);
                    break;
                }
            }
        }
    }
    
    /**
     * Unregister all commands for the given listener.
     * @param m The CommandListener to unbind.
     */
    public synchronized void unregisterAllCommands(CommandListener m){
        for(String cmd : commands.keySet()){
            commands.get(cmd).remove(m);
        }
    }
    
    /**
     * Unbind all events for the given listener.
     * @param m The EventListener to unbind.
     */
    public synchronized void unbindAllEvents(EventListener m){
        for(Class ev : events.keySet()){
            unbindEvent(ev, m);
        }
    }
    
    /**
     * Triggers a bot command and relays it to the CommandListeners that
     * registered a handle for it.
     * @param ev The CommandEvent to relay.
     */
    public synchronized void command(CommandEvent ev){
        Commons.log.fine("[MAIN] Command "+ev);
        for(CommandListener m : commands.get(CommandEvent.CMD_ANY)){
            m.onCommand(ev);
        }
        
        if(!commands.containsKey(ev.getCommand())){
            if(!ev.getCommand().equals(CommandEvent.CMD_UNBOUND)){
                String args = ev.getCommand();
                if(ev.getArgs()!=null)args+=" "+ev.getArgs();
                command(new CommandEvent(ev.getStream(), 
                                        CommandEvent.CMD_UNBOUND,
                                        args,
                                        ev.getUser(), ev.getChannel()));
            }
        }else{
            for(CommandListener m : commands.get(ev.getCommand())){m.onCommand(ev);}
        }
    }
    
    /**
     * Triggers an event and propagates it to any EventListener that is bound to
     * the Event's Class.
     * @param ev The Event to trigger.
     */
    public synchronized void event(Event ev){
        Commons.log.fine("[MAIN] Event "+ev);
        for(Class<? extends Event> c : events.keySet()){
            if(c.isInstance(ev)){
                for(EventBind bind : events.get(c)){
                    if(!ev.isHalted())
                        bind.invoke(ev);
                }
            }
        }
    }
    
    public synchronized Module getModule(String name){return modules.get(name);}
    public synchronized Stream getStream(String name){return streams.get(name);}
    public synchronized Configuration getConfig(){return conf;}
    public synchronized CommandListener[] getCommandHandlers(String commandType){return commands.get(commandType).toArray(new CommandListener[commands.get(commandType).size()]);}
    public synchronized EventListener[] getEventListeners(Class<? extends Event> event){return events.get(event).toArray(new EventListener[events.get(event).size()]);}
}
