package org.tymoonnext.bot;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.logging.Level;
import org.tymoonnext.bot.event.CommandListener;
import org.tymoonnext.bot.event.Event;
import org.tymoonnext.bot.event.EventBind;
import org.tymoonnext.bot.event.EventListener;
import org.tymoonnext.bot.event.core.CommandEvent;
import org.tymoonnext.bot.event.core.ModuleLoadEvent;
import org.tymoonnext.bot.event.core.ModuleUnloadEvent;
import org.tymoonnext.bot.event.core.ShutdownEvent;
import org.tymoonnext.bot.module.Module;
import org.tymoonnext.bot.stream.Stream;
import sun.misc.Signal;
import sun.misc.SignalHandler;

/**
 * Main Bot class that handles all event, command and module capabilities.
 * @author Shinmera
 * @license GPLv3
 * @version 2.0.1
 */
public class Kizai implements SignalHandler{
    public static void main(String[] args){new Kizai();}
    
    static{
        Commons.log.info("[INIT] Starting up...");
    }
    
    private ClassLoader classLoader;
    private HashMap<String,Module> modules;
    private HashMap<String,CommandListener> commands;
    private HashMap<Class<? extends Event>,ArrayList<EventBind>> events;
    private HashMap<String,Stream> streams;
    private Configuration conf;
    private boolean shutdown = false;
    
    public Kizai(){
        conf = new Configuration();
        modules = new HashMap<String, Module>();
        commands = new HashMap<String, CommandListener>();
        events = new HashMap<Class<? extends Event>,ArrayList<EventBind>>();
        streams = new HashMap<String, Stream>();
        classLoader = new ReloadingCapableClassLoader();
        
        conf.load(Commons.f_CONFIG);
        
        events.put(Event.class, new ArrayList<EventBind>());
        streams.put("stdout", Commons.stdout);
        
        try{Signal.handle(new Signal("INT"), this);
        }catch(IllegalArgumentException ex){Commons.log.log(Level.WARNING, "[INIT] Failed to register INT signal handler.", ex);}
        try{Signal.handle(new Signal("TERM"), this);
        }catch(IllegalArgumentException ex){Commons.log.log(Level.WARNING, "[INIT] Failed to register TERM signal handler.", ex);}
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            public void run() {if(!shutdown)shutdown();}
        }));
        
        loadModule("core.Core");
        
        Commons.log.info("[INIT] Finished in "+((System.currentTimeMillis()-Commons.STARTUP_TIME)/1000.0)+"s");
        while(!shutdown){
            try{Thread.sleep(1000);}catch(Exception ex){/* Here be dragons */}
        }//KEEPALIVE TICK
    }
    
    /**
     * Handles SIGTERM and binds it to shutdown.
     * @param sig 
     */
    public void handle(Signal sig){
        if(sig.getName().equals("INT") || sig.getName().equals("TERM")){
            Commons.log.log(Level.SEVERE, "[MAIN] Received SIGTERM!");
            if(!shutdown)shutdown();
        }else{
            SignalHandler.SIG_DFL.handle(sig);
        }
    }
    
    /**
     * Shuts everything down.
     */
    public synchronized void shutdown(){shutdown(true);}
    public synchronized void shutdown(boolean exit){
        if(shutdown)return;
        shutdown = true;
        try{
            Commons.log.info("[MAIN] Shutting down...");
            event(new ShutdownEvent());
            Commons.log.info("[MAIN] Turning off modules...");
            for(Module mod : modules.values().toArray(new Module[modules.size()])){
                try{unloadModule(mod);
                }catch(Throwable t){
                    Commons.log.log(Level.WARNING, "[MAIN]"+mod+" Failed to shutdown cleanly!", t);
                }
            }
            Commons.log.info("[MAIN] Closing streams...");
            for(Stream stream : streams.values().toArray(new Stream[streams.size()])){
                try{unregisterStream(stream);
                }catch(Throwable t){
                    Commons.log.log(Level.WARNING, "[MAIN]"+stream+" Failed to close cleanly!", t);
                }
            }
            //Commons.log.info("[MAIN] Saving config.");
            //conf.save(Commons.f_CONFIG);
        }catch(Throwable t){
            Commons.log.log(Level.SEVERE, "[MAIN] WTF!", t);
            if(exit)System.exit(2);
        }finally{
            Commons.log.info("[MAIN] Goodbye!");
            if(exit)System.exit(0);
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
            return true;
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
     * Unloads the specified Module.
     * @param module The Class name of the Module to unload.
     * @return Whether the unloading succeeded.
     * @see Kizai#unloadModule(org.tymoonnext.bot.module.Module) 
     */
    public synchronized boolean unloadModule(String module){
        if(!modules.containsKey(module)){
            Commons.log.warning("[MAIN] Module not loaded.");
            return false;
        }
        return unloadModule(modules.get(module));
    }
    
    /**
     * Unloads the specified Module by calling its shutdown function. Note that
     * the EventListener and CommandListeners that have been registered by the
     * Module need to be unloaded in the shutdown function of the Module to
     * guarantee a proper unloading process.
     * @param mod The module instance to unload.
     * @return Whether the unloading succeeded.
     */
    public synchronized boolean unloadModule(Module mod){
        Commons.log.info("[MAIN] Attempting to unload "+mod);
        event(new ModuleUnloadEvent(Commons.stdout, mod));
        mod.shutdown();
        for(String key : modules.keySet().toArray(new String[modules.size()])){
            if(modules.get(key) == mod){
                modules.remove(key);
            }
        }
        return true;
    }
    
    /**
     * Unloads the specified Module, reloads its class file and loads the Module
     * anew.
     * @param module The Class name of the Module to reload.
     * @return Whether the loading succeeded.
     */
    public synchronized boolean reloadModule(Module mod){
        return reloadModule(mod.getClass().getName().replace(Commons.MODULE_PACKAGE, ""));
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
    public synchronized void registerStream(Stream stream){
        Commons.log.info("[MAIN] Registering stream "+stream);
        streams.put(stream.getID(), stream);
    }
    
    /**
     * Removes the stream by its ID.
     * @param id The ID of the stream to remove.
     * @see Kizai#unregisterStream(org.tymoonnext.bot.stream.Stream) 
     */
    public synchronized void unregisterStream(String id){
        unregisterStream(streams.get(id));
    }
    
    /**
     * Removes the Stream and calls its close function.
     * @param stream The stream to unregister.
     */
    public synchronized void unregisterStream(Stream stream){
        Commons.log.info("[MAIN] Unregistering stream "+stream);
        stream.close();
        streams.remove(stream);
    }
    
    /**
     * Bind the listener to the given command String. If the command is already
     * registered for another listener, an IllegalArgumentException is thrown.
     * @param cmd The command to listen for.
     * @param m The listener.
     */
    public synchronized void registerCommand(String cmd, CommandListener m){registerCommand(cmd, m, false);}
    
    /**
     * Bind the listener to the given command String.
     * @param cmd The command to listen for.
     * @param m The listener.
     * @param force Whether to override an existing listener.
     */
    public synchronized void registerCommand(String cmd, CommandListener m, boolean force){
        if(commands.containsKey(cmd)){
            if(!force)  throw new IllegalArgumentException(cmd+" is already used!");
            else        Commons.log.warning("[MAIN]"+m+" is overriding "+commands.get(cmd)+".");
        }
        Commons.log.fine("[MAIN]"+m+" Registering command "+cmd);
        commands.put(cmd, m);
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
        Commons.log.fine("[MAIN]"+m+" Binding event "+evt.getSimpleName()+" to function "+func);
        if(!events.containsKey(evt))events.put(evt, new ArrayList<EventBind>());
        else{
            for(EventBind bind : events.get(evt)){
                if(bind.getListener() == m && bind.getPriority() == priority)
                    throw new IllegalArgumentException("Listener "+m+" is already bound to "+evt.getSimpleName()+" with priority "+priority+".");
            }
        }
        
        EventBind bind = new EventBind(m, evt, func, priority);
        events.get(evt).add(bind);
        Collections.sort(events.get(evt));
    }
    
    /**
     * Bind the listener's function func to the Event Class. The function should
     * expect only one argument, the Event that is passed to it. You can listen
     * to any event if you register the general Event class. The priority is set
     * to 0 by default.
     * @param evt The Class to listen for.
     * @param m The Listener.
     * @param func The function to call on the Listener.
     * @throws NoSuchMethodException if the specified Method cannot be found
     * within the listener.
     */
    public synchronized void bindEvent(Class<? extends Event> evt, EventListener m, String func) throws NoSuchMethodException{bindEvent(evt, m, func, 0);}
    
    /**
     * Unregister a given listener from a specific command.
     * @param cmd The command to unregister from.
     * @param m The CommandListener to unbind.
     */
    public synchronized void unregisterCommand(String cmd){
        Commons.log.fine("[MAIN]"+commands.get(cmd) +" Unregistering command "+cmd);
        commands.remove(cmd);
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
                    Commons.log.fine("[MAIN]"+m+" Unbinding event "+evt);
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
        for(String cmd : commands.keySet().toArray(new String[commands.size()])){
            if(commands.get(cmd) == m)
                commands.remove(cmd);
        }
    }
    
    /**
     * Unbind all events for the given listener.
     * @param m The EventListener to unbind.
     */
    public synchronized void unbindAllEvents(EventListener m){
        for(Class ev : events.keySet().toArray(new Class[events.size()])){
            unbindEvent(ev, m);
        }
    }
    
    /**
     * Triggers a bot command and relays it to the CommandListeners that
     * registered a handle for it.
     * @param ev The CommandEvent to relay.
     */
    private synchronized CommandEvent command(CommandEvent ev){
        Commons.log.finer("[MAIN] Command "+ev);
        
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
            commands.get(ev.getCommand()).onCommand(ev);
        }
        return ev;
    }
    
    /**
     * Triggers an event and propagates it to any EventListener that is bound to
     * the Event's Class.
     * @param ev The Event to trigger.
     * @return Returns the Event again, to allow event chaining.
     * @see Kizai#event(org.tymoonnext.bot.event.Event, java.lang.Class) 
     */
    public synchronized Event event(Event ev){return event(ev, EventListener.class);}
    
    /**
     * Triggers an event and propagates it to any EventListener that is bound to
     * the Event's Class and extends the given listenerType class.
     * @param ev The Event to trigger.
     * @param listenerType A class type for to limit the propagation for 
     * specific listeners.
     * @return Returns the Event again, to allow event chaining.
     */
    public synchronized Event event(Event ev, Class<? extends EventListener> listenerType){
        Commons.log.finest("[MAIN] Event "+ev);
        for(Class<? extends Event> c : events.keySet()){
            if(c.isInstance(ev)){
                for(EventBind bind : events.get(c)){
                    if(listenerType.isInstance(bind.getListener())){
                        if(!ev.isHalted())
                            bind.invoke(ev);
                    }
                }
            }
        }
        return ev;
    }
    
    /**
     * Broadcast a message to all registered streams, on all destinations. Do 
     * note that the handling of the destinations is stream dependant.
     * @param message The message to broadcast.
     * @see Kizai#broadcast(java.lang.String, java.lang.String) 
     */
    public synchronized void broadcast(String message){broadcast(message, "*");}
    
    /**
     * Broadcast a message to all registered streams.
     * @param message The message to broadcast.
     * @param dest The destination to broadcast to. This is stream dependant.
     * Generally, a destination of "*" should tell the stream to broadcast to
     * any/all destinations it knows.
     */
    public synchronized void broadcast(String message, String dest){
        Commons.log.fine("[MAIN] Broadcast '"+message+"' to "+dest);
        for(Stream s : streams.values()){
            s.send(message, dest);
        }
    }
    
    /**
     * Returns the Module associated with this name.
     * @param name
     * @return 
     */
    public synchronized Module getModule(String name){return modules.get(name);}
    
    /**
     * Returns the Stream associated with this ID.
     * @param name
     * @return 
     * @see Stream#getID() 
     */
    public synchronized Stream getStream(String id){return streams.get(id);}
    
    /**
     * Returns the internal Configuration class.
     * @return 
     */
    public synchronized Configuration getConfig(){return conf;}
    
    /**
     * Returns the CommandListener associated with this command.
     * @param commandType
     * @return 
     */
    public synchronized CommandListener getCommandListener(String commandType){return commands.get(commandType);}
    
    /**
     * Returns all registered commands.
     * @return 
     */
    public synchronized String[] getRegisteredCommands(){return commands.keySet().toArray(new String[commands.size()]);}
    
    /**
     * Returns all the event binds bound to this specific event class.
     * Note that more listeners than this might receive calls from the specified
     * event class, as superclasses are not respected in this function.
     * @param event
     * @return 
     * @see Kizai#getAllEventBinds(java.lang.Class) 
     */
    public synchronized EventBind[] getEventBinds(Class<? extends Event> event){return events.get(event).toArray(new EventBind[events.get(event).size()]);}
    
    /**
     * Returns all the event binds bound to this event class and its
     * superclasses.
     * @param event
     * @return 
     * @see Kizai#getEventBinds(java.lang.Class) 
     */
    public synchronized EventBind[] getAllEventBinds(Class<? extends Event> event){
        ArrayList<EventBind> list = new ArrayList<EventBind>();
        for(Class<? extends Event> c : events.keySet()){
            if(c.isAssignableFrom(event))list.addAll(events.get(c));
        }
        return list.toArray(new EventBind[events.get(event).size()]);
    }
    
    /**
     * Returns all bound events.
     * @return 
     */
    public synchronized Class[] getBoundEvents(){return events.keySet().toArray(new Class[events.size()]);}
    
    /**
     * Returns all registered streams.
     * @return 
     */
    public synchronized Stream[] getStreams(){return streams.values().toArray(new Stream[streams.size()]);}
    
    /**
     * Returns all loaded modules.
     * @return 
     */
    public synchronized Module[] getModules(){return modules.values().toArray(new Module[modules.size()]);}
    
    /**
     * Returns whether a module with that name is registered.
     * @param module
     * @return 
     */
    public synchronized boolean hasModule(String module){return modules.containsKey(module);}
    
    /**
     * Returns whether a stream with that name is registered.
     * @param stream
     * @return 
     */
    public synchronized boolean hasStream(String stream){return streams.containsKey(stream);}
    
    /**
     * Returns whether a given command is registered.
     * @param command
     * @return 
     */
    public synchronized boolean isRegistered(String command){return commands.containsKey(command);}
    
    /**
     * Returns true if anything binds to this event or a superclass of this
     * event. Note that this is almost guaranteed to be always true, as there's
     * modules that bind to the general Event class.
     * @param evt The event to search for.
     * @return 
     * @see Kizai#isBound(java.lang.Class) 
     */
    public synchronized boolean isAnyBound(Class<? extends Event> evt){
        for(Class<? extends Event> c : events.keySet()){
            if(c.isAssignableFrom(evt))return true;
        }
        return false;
    }
    
    /**
     * Returns true if anything binds to this specific event. Note that this
     * excludes superclasses.
     * @param evt
     * @return
     * @see Kizai#isAnyBound(java.lang.Class) 
     */
    public synchronized boolean isBound(Class<? extends Event> evt){return events.containsKey(evt);}
    
    /**
     * Returns true if the bot is currently shutting down.
     * @return 
     */
    public synchronized boolean isShuttingDown(){return shutdown;}
}
