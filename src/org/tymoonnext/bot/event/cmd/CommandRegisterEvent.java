package org.tymoonnext.bot.event.cmd;

import org.tymoonnext.bot.Commons;
import org.tymoonnext.bot.event.Event;
import org.tymoonnext.bot.module.cmd.CommandChain;

/**
 * 
 * @author Shinmera
 * @license GPLv3
 * @version 0.0.0
 */
public class CommandRegisterEvent extends Event{
    private String command;
    private CommandChain args;
    private String help;
    
    public CommandRegisterEvent(String command){this(command, null, null);}
    public CommandRegisterEvent(String command, String help){this(command, help, null);}
    public CommandRegisterEvent(String command, CommandChain args){this(command, null, args);}
    public CommandRegisterEvent(String command, String help, CommandChain args){
        super(Commons.stdout);
        this.help=help;
        this.args=args;
    }
    
    public String getCommand(){return command;}
    public CommandChain getArgs(){return args;}
    public String getHelp(){return help;}
}