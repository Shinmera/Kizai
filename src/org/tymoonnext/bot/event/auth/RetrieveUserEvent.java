package org.tymoonnext.bot.event.auth;

import org.tymoonnext.bot.event.Event;
import org.tymoonnext.bot.module.auth.User;
import org.tymoonnext.bot.stream.Stream;

/**
 *
 * @author Shinmera
 * @license GPLv3
 * @version 0.0.0
 */
public class RetrieveUserEvent extends Event{
    private String ident;
    private User user;
    
    public RetrieveUserEvent(Stream origin, String ident){
        super(origin);
        this.ident=ident;
    }
    
    public void setUser(User user){this.user=user;}
    public User getUser(){return user;}
    public String getIdent(){return ident;}
}
