package org.tymoonnext.bot.event;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.logging.Level;
import org.tymoonnext.bot.Commons;

/**
 * Event binding class that is used for the event system.
 * @author Shinmera
 * @license GPLv3
 * @version 0.0.0
 */
public class EventBind implements Comparable{
    private int priority;
    private EventListener listener;
    private Method fun;
    
    public EventBind(EventListener ev, String fun, int prio) throws NoSuchMethodException{
        this.priority = prio;
        this.listener = ev;
        Method method = ev.getClass().getMethod(fun, ev.getClass());
    }
    
    public int getPriority(){return priority;}
    public EventListener getListener(){return listener;}
    public Method getMethod(){return fun;}
    
    public void invoke(Event ev){
        try{
            fun.invoke(listener, ev);
        }catch(IllegalAccessException ex){
            Commons.log.log(Level.WARNING, toString()+" Failed to invoke method for "+ev, ex);
        }catch(IllegalArgumentException ex){
            Commons.log.log(Level.WARNING, toString()+" Failed to invoke method for "+ev, ex);
        }catch(InvocationTargetException ex){
            Commons.log.log(Level.WARNING, toString()+" Failed to invoke method for "+ev, ex);
        }catch(SecurityException ex){
            Commons.log.log(Level.WARNING, toString()+" Failed to invoke method for "+ev, ex);
        }
    }

    public int compareTo(Object o) {
        return priority - ((EventBind)o).getPriority();
    }
    
    public String toString(){
        return "{"+this.getClass().getSimpleName()+"|"+listener+":"+fun+"}";
    }
}
