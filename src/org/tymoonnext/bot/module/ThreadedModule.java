package org.tymoonnext.bot.module;

import org.tymoonnext.bot.Commons;
import org.tymoonnext.bot.Kizai;

/**
 * 
 * @author Shinmera
 * @license GPLv3
 * @version 0.0.0
 */
public abstract class ThreadedModule extends Module implements Runnable{
    protected boolean interrupted=false;
    
    public ThreadedModule(Kizai bot){
        super(bot);
        Commons.log.info(toString()+" Launching thread...");
        Thread t = new Thread(this);
        t.setDaemon(true);
        t.start();
    }

    public void shutdown(){
        interrupt();
    }
    public abstract void run();
    
    public void interrupt(){interrupted=true;}
}