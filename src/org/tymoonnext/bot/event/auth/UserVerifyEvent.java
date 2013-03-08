package org.tymoonnext.bot.event.auth;

import org.tymoonnext.bot.event.Event;
import org.tymoonnext.bot.meta.Arguments;
import org.tymoonnext.bot.module.auth.User;
import org.tymoonnext.bot.stream.Stream;

/**
 * Event sent out by the UserDB that allows the AuthImplementors to perform
 * their checks on the requested user.
 * @author Shinmera
 * @license GPLv3
 * @version 0.0.0
 */
public class UserVerifyEvent extends Event{
    private User user;
    
    @Arguments({"origin", "user"})
    public UserVerifyEvent(Stream origin, User user){
        super(origin);
        this.user = user;
    }
    
    public User getUser(){return user;}
}
