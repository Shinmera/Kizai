package org.tymoonnext.bot.event.auth;

import org.tymoonnext.bot.event.Event;
import org.tymoonnext.bot.event.core.CommandEvent;
import org.tymoonnext.bot.meta.Arguments;
import org.tymoonnext.bot.stream.Stream;

/**
 * Authentication event issued by the Auth module on command receive.
 * This should optimally only be handled by user pools that can either handle
 * the authentication themselves or propagate it to appropriate handlers.
 * @author Shinmera
 * @license GPLv3
 * @version 0.0.0
 */
public class AuthEvent extends Event{
    private CommandEvent cmd;
    private boolean granted = false;
    
    @Arguments({"origin", "cmd"})
    public AuthEvent(Stream origin, CommandEvent cmd){
        super(origin);
        this.cmd = cmd;
    }
    
    /**
     * Sets the granted flag of the auth event. Once the event is granted, the
     * command will be propagated, otherwise it will be halted. Note that other
     * modules /might/ override your grant status.
     * @param g 
     */
    public void setGranted(boolean g){granted=g;}
    
    /**
     * Returns the CommandEvent that triggered the authentication check.
     * @return 
     */
    public CommandEvent getCommand(){return cmd;}
    
    public boolean isGranted(){return granted;}
}
