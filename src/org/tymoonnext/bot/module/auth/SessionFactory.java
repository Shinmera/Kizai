package org.tymoonnext.bot.module.auth;

import org.tymoonnext.bot.Kizai;
import org.tymoonnext.bot.module.Module;

/**
 * 
 * @author Shinmera
 * @license GPLv3
 * @version 0.0.0
 */
public abstract class SessionFactory {
    private Kizai bot;
    
    public SessionFactory(Kizai bot){
        this.bot=bot;
    }
    
    public void addUser(User user){
        Module auth = bot.getModule("Auth");
        auth.invoke("addUser", user);
    }
    
    public User getUser(String ID){
        Module auth = bot.getModule("Auth");
        return (User)auth.invoke("getUser", ID);
    }
    
    public abstract void authenticate(String... userinf);
}
