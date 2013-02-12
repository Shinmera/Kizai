package org.tymoonnext.bot.event;

import org.tymoonnext.bot.stream.Stream;

/**
 * 
 * @author Shinmera
 * @license GPLv3
 * @version 0.0.0
 */
public abstract class Event {
    protected Stream origin;
    protected boolean cancelled = false;
    protected boolean halted = false;
    
    public Event(Stream origin){this.origin=origin;}
    
    public String toString(){
        return "<"+this.getClass().getSimpleName()+">";
    }
    
    public Stream getStream(){return origin;}
    public boolean isCancelled(){return cancelled;}
    public boolean isHalted(){return halted;}
    
    /**
     * Cancel the event. This is a flag that should be respected by module
     * implementations and should prevent any further standard actions by the
     * module that created the event.
     * 
     * Example: Module A issues the event of a ping and would by default, after
     * sending out the event, respond with a pong. Setting the event to
     * cancelled should prevent this action though.
     * @param c 
     */
    public void setCancelled(boolean c){cancelled=c;}
    
    /**
     * Halts further propagation of the event. After the halt is set, no other
     * module will receive this event, thus effectively making them oblivious
     * to it. Use this with care!
     * 
     * Note that this does not prevent the issuing module's standard action.
     * See setCancelled() for that.
     * @param c 
     */
    public void setHalted(boolean c){cancelled=c;}
}
