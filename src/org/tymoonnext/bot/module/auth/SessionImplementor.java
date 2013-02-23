package org.tymoonnext.bot.module.auth;

import java.util.logging.Level;
import org.tymoonnext.bot.Commons;
import org.tymoonnext.bot.Kizai;
import org.tymoonnext.bot.event.EventListener;
import org.tymoonnext.bot.event.auth.UserVerifyEvent;
import org.tymoonnext.bot.module.Module;

/**
 * Base class for authentication handlers. UserVerifyEvents will only be sent to
 * classes that extend this and are bound to the UserVerifyEvent in Kizai. A
 * child class of this should provide a mechanism to verify a user's session and
 * set the grant status on the UserVerifyEvent.
 * @author Shinmera
 * @license GPLv3
 * @version 0.0.0
 */
public abstract class SessionImplementor extends Module implements EventListener{
    
    public SessionImplementor(Kizai bot){this(bot, 0);}
    public SessionImplementor(Kizai bot, int priority){
        super(bot);
        try {
            bot.bindEvent(UserVerifyEvent.class, this, "onUserVerify", priority);
        } catch (NoSuchMethodException ex) {
            Commons.log.log(Level.SEVERE, toString()+" Idk wtf just happened.", ex);
        }
    }
    
    public abstract void onUserVerify(UserVerifyEvent evt);

    @Override
    public void shutdown(){
        bot.unbindAllEvents(this);
    }
}
