package org.tymoonnext.bot.module.auth;

import org.tymoonnext.bot.Commons;
import org.tymoonnext.bot.Kizai;
import org.tymoonnext.bot.event.EventListener;
import org.tymoonnext.bot.event.auth.AuthEvent;
import org.tymoonnext.bot.event.core.CommandEvent;
import org.tymoonnext.bot.module.Module;

/**
 * Very simple authentication Module that issues AuthEvents on CommandEvents
 * and halts their execution in case no authentication succeeds.
 * @author Shinmera
 * @license GPLv3
 * @version 0.0.0
 */
public class Auth extends Module implements EventListener{
    
    public Auth(Kizai bot){
        super(bot);
        try{bot.bindEvent(CommandEvent.class, this, "onCommandEvent");}catch(NoSuchMethodException ex){}
    }

    @Override
    public void shutdown() {
        bot.unbindAllEvents(this);
    }
    
    public void onCommandEvent(CommandEvent evt){
        AuthEvent auth = new AuthEvent(evt.getStream(), evt);
        bot.event(auth);
        if(!auth.isGranted()){
            Commons.log.warning(toString()+" Not permitting "+evt+".");
            evt.setHalted(true);
        }
    }
}
