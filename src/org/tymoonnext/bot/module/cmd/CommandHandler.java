package org.tymoonnext.bot.module.cmd;

import java.util.HashMap;
import org.tymoonnext.bot.Commons;
import org.tymoonnext.bot.Kizai;
import org.tymoonnext.bot.event.CommandListener;
import org.tymoonnext.bot.event.EventListener;
import org.tymoonnext.bot.event.cmd.CommandInstanceEvent;
import org.tymoonnext.bot.event.cmd.CommandRegisterEvent;
import org.tymoonnext.bot.event.core.CommandEvent;

/**
 * 
 * @author Shinmera
 * @license GPLv3
 * @version 0.0.0
 */
public class CommandHandler implements EventListener, CommandListener{
    private Kizai bot;
    private String name;
    private HashMap<String, Command> commands;
    private HashMap<String, CommandListener> listeners;
    
    public CommandHandler(Kizai bot, String ident){
        this.bot=bot;
        this.name=ident;
        commands = new HashMap<String, Command>();
        listeners = new HashMap<String, CommandListener>();
    }

    public void onCommandRegister(CommandRegisterEvent evt){
        Commons.log.info(toString()+evt.getListener()+" Registering new bind for '"+evt.getCommand()+"'");
        if(commands.containsKey(evt.getCommand().getName())){
            if(!evt.isForced()) throw new IllegalArgumentException(evt.getCommand()+" is already used!");
            else                Commons.log.warning(toString()+evt.getListener()+" is overriding "+commands.get(evt.getCommand())+".");
        }
        commands.put(evt.getCommand().getName(), evt.getCommand());
        listeners.put(evt.getCommand().getName(), evt.getListener());
        bot.registerCommand(evt.getCommand().getName(), this);
    }

    public void onCommand(CommandEvent cmd){
        if(commands.containsKey(cmd.getCommand())){
            Commons.log.fine(toString()+" Received event: "+cmd);
            Command command = commands.get(cmd.getCommand());
            try{
                CommandInstance instance = new CommandInstance(commands.get(cmd.getCommand()), cmd.getArgs());
                CommandInstanceEvent cmde = new CommandInstanceEvent(cmd, instance);
                listeners.get(cmd.getCommand()).onCommand(cmde);
            }catch(ParseException ex){
                Commons.log.info(toString()+command+" Failed to parse: "+ex.getMessage());
                cmd.getStream().send("Usage: "+command.toDescriptiveString(), cmd.getChannel());
            }
        }
    }
    
    public Command[] getCommands(){return commands.values().toArray(new Command[commands.size()]);}
    public CommandListener[] getListeners(){return listeners.values().toArray(new CommandListener[commands.size()]);}
    
    public Command getCommand(String cmd){return commands.get(cmd);}
    public CommandListener getListener(String cmd){return listeners.get(cmd);}
    
    public String toString(){return "["+getClass().getSimpleName()+"|"+name+"]";}
}
