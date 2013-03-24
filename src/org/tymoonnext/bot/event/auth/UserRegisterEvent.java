package org.tymoonnext.bot.event.auth;

import NexT.data.DObject;
import org.tymoonnext.bot.Commons;
import org.tymoonnext.bot.event.Event;
import org.tymoonnext.bot.meta.Arguments;
import org.tymoonnext.bot.module.auth.User;
import org.tymoonnext.bot.stream.Stream;

/**
 *
 * @author Shinmera
 * @license GPLv3
 * @version 0.0.0
 */
public class UserRegisterEvent extends Event{
    private User user;
    
    @Arguments({"username"})
    public UserRegisterEvent(String name){this(Commons.stdout, name);}
    
    @Arguments({"origin", "username"})
    public UserRegisterEvent(Stream origin, String name){
        super(origin);
        DObject conf = new DObject();
        conf.set("name", name);
        this.user = new User(conf);
    }
    
    @Arguments({"user"})
    public UserRegisterEvent(User user){this(Commons.stdout, user);}
    
    @Arguments({"origin", "user"})
    public UserRegisterEvent(Stream origin, User user){
        super(origin);
        this.user=user;
    }
    
    public User getUser(){return user;}
}

