package org.tymoonnext.bot.module.core;

import org.tymoonnext.bot.Kizai;
import org.tymoonnext.bot.event.EventListener;
import org.tymoonnext.bot.module.Module;


/**
 * 
 * @author Shinmera
 * @license GPLv3
 * @version 0.0.0
 */
public class CommandModule extends Module implements EventListener{

    public CommandModule(Kizai bot){
        super(bot);
    }
    
    @Override
    public void shutdown(){
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
