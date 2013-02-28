package org.tymoonnext.bot.event.cmd;

import org.tymoonnext.bot.event.core.CommandEvent;
import org.tymoonnext.bot.module.cmd.CommandInstance;


/**
 * 
 * @author Shinmera
 * @license GPLv3
 * @version 0.0.0
 */
public class CommandInstanceEvent extends CommandEvent{
    private CommandInstance instance;

    public CommandInstanceEvent(CommandEvent base, CommandInstance instance){
        super(base.getStream(), base.getCommand(), base.getArgs(), base.getUser(), base.getChannel());
    }
    
    public CommandInstance get(){return instance;}
}
