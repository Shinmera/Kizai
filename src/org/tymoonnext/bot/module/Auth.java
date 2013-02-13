package org.tymoonnext.bot.module;

import java.util.HashMap;
import org.tymoonnext.bot.Kizai;
import org.tymoonnext.bot.event.CommandEvent;
import org.tymoonnext.bot.event.EventListener;
import org.tymoonnext.bot.module.auth.User;

/**
 *
 * @author Shinmera
 * @license GPLv3
 * @version 0.0.0
 */
public class Auth extends Module implements EventListener{
    private HashMap<String, User> users;
    
    public Auth(Kizai bot){
        super(bot);
        //Believe me, we want to be the first with this.
        try{bot.bindEvent(CommandEvent.class, this, "checkCommand", Integer.MAX_VALUE);}catch(NoSuchMethodException ex){}
    }

    @Override
    public void shutdown() {
        bot.unbindAllEvents(this);
    }
    
    public void checkCommand(CommandEvent cmd){
        if(!users.get(cmd.getUser()).check("command."+cmd.getCommand())){
            cmd.setHalted(true);
        }
    }
}
