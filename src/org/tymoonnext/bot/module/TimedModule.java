package org.tymoonnext.bot.module;

import java.util.Timer;
import java.util.TimerTask;
import org.tymoonnext.bot.Commons;
import org.tymoonnext.bot.Kizai;

/**
 * Module extension that provides a timed module, which runs its function in a
 * fixed interval. Useful for polling and other services.
 * @author Shinmera
 * @license GPLv3
 * @version 0.0.0
 */
public abstract class TimedModule extends Module{
    protected InnerTimer timerTask;
    protected Timer timer;
    
    public TimedModule(Kizai bot){
        super(bot);
        timerTask = new InnerTimer(this);
        timer = new Timer();
    }
    public TimedModule(Kizai bot, long period){
        this(bot);
        schedule(period);
    }
    
    public void schedule(long period){
        timer.schedule(timerTask, period, period);
    }
    
    public void shutdown(){
        Commons.log.info(toString()+" Cancelling timer...");
        timer.cancel();
    }
    
    public abstract void run();
    
    private class InnerTimer extends TimerTask{
        private TimedModule outer;
        public InnerTimer(TimedModule outer){this.outer=outer;}
        
        public void run(){
            outer.run();
        }
    }
}
