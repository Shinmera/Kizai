package org.tymoonnext.bot.event.cmd;

import org.tymoonnext.bot.Commons;
import org.tymoonnext.bot.event.CommandListener;
import org.tymoonnext.bot.event.Event;
import org.tymoonnext.bot.module.cmd.Command;

/**
 * 
 * @author Shinmera
 * @license GPLv3
 * @version 0.0.0
 */
public class CommandRegisterEvent extends Event{
    private Command command;
    private CommandListener listener;
    private boolean force = false;
    
    public CommandRegisterEvent(Command command, CommandListener listener){this(command, listener, false);}
    public CommandRegisterEvent(Command command, CommandListener listener, boolean force){
        super(Commons.stdout);
        this.command=command;
        this.listener=listener;
        this.force=force;
    }
    
    public Command getCommand(){return command;}
    public CommandListener getListener(){return listener;}
    public boolean isForced(){return force;}
}