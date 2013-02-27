package org.tymoonnext.bot.module.cmdgroup;

import java.util.TreeMap;
import org.tymoonnext.bot.Commons;
import org.tymoonnext.bot.Kizai;
import org.tymoonnext.bot.event.CommandListener;
import org.tymoonnext.bot.event.cmdgroup.GroupRegisterEvent;
import org.tymoonnext.bot.event.core.CommandEvent;

/**
 * 
 * @author Shinmera
 * @license GPLv3
 * @version 0.0.0
 */
public class CommandGroup implements CommandListener{
    private String name;
    private TreeMap<String, CommandListener> commands;
    
    public CommandGroup(Kizai bot, String group){
        name=group;
        commands = new TreeMap<String, CommandListener>();
        commands.put("help", this);
        
        bot.registerCommand(name, this);
    }
    
    public void onCommand(CommandEvent cmd){
        if(cmd.getCommand().equals(name)){
            Commons.log.fine(toString()+" Received group command ("+cmd.getArgs()+")");
            if((cmd.getArgs() == null) || (cmd.getArgs().trim().isEmpty()) || (cmd.getArgs().equalsIgnoreCase("help"))){
                onHelp(cmd);
            }else{ 
                String[] args = cmd.getArgs().split(" ");
                if(!commands.containsKey(args[0])){
                    cmd.getStream().send("No command called '"+args[0]+"' known. Try '"+name+" help' for a list of available commands.", cmd.getChannel());
                }else{
                    CommandEvent fin = new CommandEvent(cmd.getStream(), args[0], cmd.getArgs().substring(args[0].length()).trim(), cmd.getUser(), cmd.getChannel());
                    commands.get(cmd.getCommand()).onCommand(fin);
                }
            }
        }
    }
    
    public void onGroupRegisterEvent(GroupRegisterEvent evt){
        if(evt.getGroupName().equals(name)){
            Commons.log.info(toString()+evt.getListener()+" Registering sub-command "+evt.getSubCommand());
            if(commands.containsKey(evt.getSubCommand())){
                if(!evt.isForced()) throw new IllegalArgumentException(evt.getSubCommand()+" is already used!");
                else                Commons.log.warning(toString()+evt.getListener()+" is overriding "+commands.get(evt.getSubCommand())+".");
            }
            commands.put(evt.getSubCommand(), evt.getListener());
        }
    }
    
    public void onHelp(CommandEvent evt){
        evt.getStream().send("The following commands are available in this group:", evt.getChannel());
        for(String s : commands.keySet()){
            evt.getStream().send(" * "+name+" "+s, evt.getChannel());
        }
    }

    public String[] getCommands(){return commands.keySet().toArray(new String[commands.size()]);}
    
    public String toString(){return "["+getClass().getSimpleName()+"|"+name+"]";}
}
