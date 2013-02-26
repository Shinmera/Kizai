package org.tymoonnext.bot.event.cmd;

import java.util.HashMap;
import org.tymoonnext.bot.event.core.CommandEvent;
import org.tymoonnext.bot.module.cmd.Argument;


/**
 * Extension of the CommandEvent in CORE for the Commands extension.
 * Extensions!
 * @author Shinmera
 * @license GPLv3
 * @version 0.0.0
 */
public class CommandEventExtra extends CommandEvent{
    private Argument[] args;
    private HashMap<String,Argument> kwargs;
    
    public CommandEventExtra(CommandEvent base, Argument[] args){
        super(base.getStream(), base.getCommand(), base.getArgs(), base.getUser(), base.getChannel());
        this.args = args;
        kwargs = new HashMap<String, Argument>();
        for(Argument arg: args){
            kwargs.put(arg.getName(), arg);
        }
    }
    
    public Argument getArg(int i){return args[i];}
    public Argument getArg(String name){return kwargs.get(name);}
    public String getVal(int i){return args[i].getValue();}
    public String getVal(String name){return kwargs.get(name).getValue();}
}
