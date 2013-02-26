package org.tymoonnext.bot.module.core;

import org.tymoonnext.bot.Kizai;
import org.tymoonnext.bot.event.CommandListener;
import org.tymoonnext.bot.event.EventListener;
import org.tymoonnext.bot.event.cmd.CommandRegisterEvent;
import org.tymoonnext.bot.event.core.CommandEvent;
import org.tymoonnext.bot.module.Module;

/**
 * 
 * @author Shinmera
 * @license GPLv3
 * @version 0.0.0
 */
public class CommandHandler extends Module implements EventListener, CommandListener{
    
    public CommandHandler(Kizai bot){
        super(bot);
        
        try{bot.bindEvent(CommandRegisterEvent.class, this, "onCommandRegister");}catch(NoSuchMethodException ex){}
    }

    @Override
    public void shutdown(){
        bot.unbindAllEvents(this);
    }

    public void onCommandRegister(CommandRegisterEvent evt){
        
    }

    public void onCommand(CommandEvent cmd){
        
    }
}
