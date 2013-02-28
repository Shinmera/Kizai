package org.tymoonnext.bot.module.core;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.tymoonnext.bot.Kizai;
import org.tymoonnext.bot.event.cmd.CommandRegisterEvent;
import org.tymoonnext.bot.event.core.CommandEvent;
import org.tymoonnext.bot.module.Module;
import org.tymoonnext.bot.module.cmd.CommandHandler;


/**
 * 
 * @author Shinmera
 * @license GPLv3
 * @version 0.0.0
 */
public class CommandModule extends Module{
    private CommandHandler handler;

    public CommandModule(Kizai bot){
        super(bot);
        
        handler = new CommandHandler("Core");
        
        try{bot.bindEvent(CommandRegisterEvent.class, handler, "onCommandRegister");}catch(NoSuchMethodException ex){}
        try{bot.bindEvent(CommandEvent.class, handler, "onCommand");}catch(NoSuchMethodException ex){}
    }
    
    @Override
    public void shutdown(){
        bot.unbindAllEvents(handler);
        bot.unregisterAllCommands(handler);
    }
}
