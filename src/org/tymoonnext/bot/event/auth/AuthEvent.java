package org.tymoonnext.bot.event.auth;

import org.tymoonnext.bot.event.Event;
import org.tymoonnext.bot.event.core.CommandEvent;
import org.tymoonnext.bot.stream.Stream;

/**
 *
 * @author Shinmera
 * @license GPLv3
 * @version 0.0.0
 */
public class AuthEvent extends Event{
    private CommandEvent cmd;
    private boolean granted = false;
    
    public AuthEvent(Stream origin, CommandEvent cmd){
        super(origin);
        this.cmd = cmd;
    }
    
    public void setGranted(boolean g){granted=g;}
    
    public CommandEvent getCommand(){return cmd;}
    public boolean isGranted(){return granted;}
}
