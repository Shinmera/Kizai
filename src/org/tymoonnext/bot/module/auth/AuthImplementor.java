package org.tymoonnext.bot.module.auth;

import java.util.logging.Level;
import org.tymoonnext.bot.Commons;
import org.tymoonnext.bot.Kizai;
import org.tymoonnext.bot.event.EventListener;
import org.tymoonnext.bot.event.auth.UserVerifyEvent;

/**
 *
 * @author Shinmera
 * @license GPLv3
 * @version 0.0.0
 */
public abstract class AuthImplementor implements EventListener{
    
    public AuthImplementor(Kizai bot){this(bot, 0);}
    public AuthImplementor(Kizai bot, int priority){
        try {
            bot.bindEvent(UserVerifyEvent.class, this, "onUserVerify", priority);
        } catch (NoSuchMethodException ex) {
            Commons.log.log(Level.SEVERE, toString()+" Idk wtf just happened.", ex);
        }
    }
    
    public abstract void onUserVerify(UserVerifyEvent evt);
}
