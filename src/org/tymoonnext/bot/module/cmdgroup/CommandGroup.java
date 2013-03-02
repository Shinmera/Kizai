package org.tymoonnext.bot.module.cmdgroup;

import NexT.util.StringUtils;
import java.util.TreeMap;
import org.tymoonnext.bot.Commons;
import org.tymoonnext.bot.Kizai;
import org.tymoonnext.bot.event.CommandListener;
import org.tymoonnext.bot.event.cmdgroup.GroupRegisterEvent;
import org.tymoonnext.bot.event.core.CommandEvent;
import org.tymoonnext.bot.module.cmd.CommandHandler;

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
            if((cmd.getArgs() == null) || (cmd.getArgs().trim().isEmpty()) || (cmd.getArgs().toLowerCase().startsWith("help"))){
                onHelp(cmd);
            }else{ 
                String[] args = cmd.getArgs().split(" ");
                if(!commands.containsKey(args[0])){
                    cmd.getStream().send("No command called '"+args[0]+"' known. Try '"+name+" help' for a list of available commands.", cmd.getChannel());
                }else{
                    CommandEvent fin = new CommandEvent(cmd.getStream(), 
                                                        cmd.getCommand()+" "+args[0], 
                                                        cmd.getArgs().substring(args[0].length()).trim(), 
                                                        cmd.getUser(), 
                                                        cmd.getChannel());
                    commands.get(args[0]).onCommand(fin);
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
        if(evt.getArgs().toLowerCase().startsWith("help ")){
            String cmd = evt.getArgs().split(" ")[1];
            if(commands.containsKey(cmd)){
                if(commands.get(cmd) instanceof CommandHandler){
                    CommandHandler handler = ((CommandHandler)commands.get(cmd));
                    evt.getStream().send("Help for '"+name+" "+cmd+"':", evt.getChannel());
                    evt.getStream().send(" Usage: "+handler.getCommand(name+" "+cmd).toDescriptiveString(), evt.getChannel());
                    evt.getStream().send(" Description: "+handler.getCommand(name+" "+cmd).getDescription(), evt.getChannel());
                }else{
                    evt.getStream().send("No additional help recorded.", evt.getChannel());
                }
            }else{
                evt.getStream().send("No such command '"+cmd+"'.", evt.getChannel());
            }
        }else{
            evt.getStream().send("The following commands are available in this group:", evt.getChannel());
            for(String s : commands.keySet()){
                if(commands.get(s) instanceof CommandHandler){
                    evt.getStream().send(" * "+((CommandHandler)commands.get(s)).getCommand(name+" "+s).toDescriptiveString(), evt.getChannel());
                }else{
                    evt.getStream().send(" * "+name+" "+s, evt.getChannel());
                }
            }
            evt.getStream().send("More specific help for each command may be available with: "+name+" help commandname", evt.getChannel());
        }
    }

    public String[] getCommands(){return commands.keySet().toArray(new String[commands.size()]);}
    
    public String toString(){return "["+getClass().getSimpleName()+"|"+name+"]";}
}
