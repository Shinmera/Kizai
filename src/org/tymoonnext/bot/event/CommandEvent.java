package org.tymoonnext.bot.event;

import org.tymoonnext.bot.stream.Stream;

/**
 * 
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
    private boolean cancelled=false;
    
    public CommandEvent(Stream origin, String command){this(origin, command, (String)null);}
    public CommandEvent(Stream origin, String command, String args){this(origin, command, args, null);}
    public CommandEvent(Stream origin, String command, String args, String user){this(origin, command, args, user, user);}
    public CommandEvent(Stream origin, String command, String args, String user, String channel){
        super(origin);
        this.user=user;
        this.channel=channel;
        this.args=args;
        this.command=command.trim();
    }
    public CommandEvent(String command, CommandEvent evt){
        this(evt.getStream(), command, evt.getArgs(), evt.getUser(), evt.getChannel());
    }
    
    public String getUser(){return user;}
    public String getChannel(){return channel;}
    public String getCommand(){return command;}
    public String getArgs(){return args;}
    public Stream getStream(){return origin;}
    public boolean isCancelled(){return cancelled;}
    
    public void setCancelled(boolean c){cancelled=c;}
    
    public String toString(){
        return "<"+getClass().getSimpleName()+"|"+command+">";
    }
}
