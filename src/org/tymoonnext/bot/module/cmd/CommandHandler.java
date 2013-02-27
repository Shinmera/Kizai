package org.tymoonnext.bot.module.cmd;

import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.tymoonnext.bot.Commons;
import org.tymoonnext.bot.Kizai;
import org.tymoonnext.bot.event.CommandListener;
import org.tymoonnext.bot.event.EventListener;
import org.tymoonnext.bot.event.cmd.CommandEventExtra;
import org.tymoonnext.bot.event.cmd.CommandRegisterEvent;
import org.tymoonnext.bot.event.core.CommandEvent;

/**
 * 
 * @author Shinmera
 * @license GPLv3
 * @version 0.0.0
 */
public class CommandHandler implements EventListener, CommandListener{
    private HashMap<String, Command> commands;
    private HashMap<String, CommandListener> listeners;
    
    public CommandHandler(Kizai bot){
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
    }

    public void onCommand(CommandEvent cmd){
        if(commands.containsKey(cmd.getCommand())){
            Command command = commands.get(cmd.getCommand());
            try{
                command.parse(cmd.getArgs());
                CommandEventExtra cmde = new CommandEventExtra(cmd, command.getArguments());
                listeners.get(cmd.getCommand()).onCommand(cmde);
            }catch(ParseException ex){
                Commons.log.log(Level.INFO, toString()+command+" Failed to parse.", ex);
                cmd.getStream().send("Usage: "+command.toDescriptiveString(), cmd.getChannel());
            }
        }
    }
}
