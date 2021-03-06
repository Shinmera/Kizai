package org.tymoonnext.bot.event.core;

import org.tymoonnext.bot.event.Event;
import org.tymoonnext.bot.meta.Arguments;
import org.tymoonnext.bot.stream.Stream;

/**
 * CommandEvent used to exchange user-issued commands between streams and
 * listeners.
 * @author Shinmera
 * @license GPLv3
 * @version 0.0.0
 */
public class CommandEvent extends Event{
    public static final String CMD_ANY="--any--";
    public static final String CMD_UNBOUND="--unbound--";
    
    private String user;
    private String channel;
    private String command;
    private String args;
    
    @Arguments({"origin", "command"})
    public CommandEvent(Stream origin, String command){this(origin, command, (String)null);}
    
    @Arguments({"origin", "command", "args"})
    public CommandEvent(Stream origin, String command, String args){this(origin, command, args, null);}
    
    @Arguments({"origin", "command", "args", "user"})
    public CommandEvent(Stream origin, String command, String args, String user){this(origin, command, args, user, user);}
    
    @Arguments({"origin", "command", "args", "user", "channel"})
    public CommandEvent(Stream origin, String command, String args, String user, String channel){
        super(origin);
        this.user=user;
        this.channel=channel;
        this.args=args;
        this.command=command.trim().toLowerCase();
    }
    
    @Arguments({"command", "evt"})
    public CommandEvent(String command, CommandEvent evt){
        this(evt.getStream(), command, evt.getArgs(), evt.getUser(), evt.getChannel());
    }
    
    public String getUser(){return user;}
    public String getChannel(){return channel;}
    public String getCommand(){return command;}
    public String getArgs(){return args;}
    
    public String toString(){
        return "<"+getClass().getSimpleName()+"|"+command+">";
    }
}
