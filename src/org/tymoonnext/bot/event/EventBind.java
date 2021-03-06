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
    private Method method;
    
    public EventBind(EventListener listener, Class<? extends Event> ev, String fun, int priority) throws NoSuchMethodException{
        this.priority = priority;
        this.listener = listener;
        method = listener.getClass().getMethod(fun, ev);
    }
    
    public int getPriority(){return priority;}
    public EventListener getListener(){return listener;}
    public Method getMethod(){return method;}
    
    /**
     * Invoke this bind's method with the specified Event.
     * @param ev The event to pass.
     */
    public void invoke(Event ev){
        try{
            boolean wasAccessible = method.isAccessible();
            method.setAccessible(true);
            method.invoke(listener, ev);
            method.setAccessible(wasAccessible);
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

    /**
     * Comparator to enable sorting by priority.
     * @param o
     * @return 
     */
    public int compareTo(Object o) {
        return ((EventBind)o).getPriority() - priority;
    }
    
    public String toString(){
        return "{"+this.getClass().getSimpleName()+"|"+listener+":"+method.getName()+"}";
    }
}
