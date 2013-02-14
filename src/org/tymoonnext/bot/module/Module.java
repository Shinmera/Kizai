package org.tymoonnext.bot.module;

import org.tymoonnext.bot.Commons;
import org.tymoonnext.bot.Kizai;

/**
 * Module base class.
 * @author Shinmera
 * @license GPLv3
 * @version 0.0.0
 */
public abstract class Module {
    protected Kizai bot;
    
    public Module(Kizai bot){this.bot=bot;}
    
    public abstract void shutdown();
    
    public String toString(){
        return "["+this.getClass().getSimpleName()+"]";
    }
    
    public final Object invoke(String func, Object... args){
        return Commons.invoke(this, func, args);
    }
}
