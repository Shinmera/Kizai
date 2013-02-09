package org.tymoonnext.bot.event;

import org.tymoonnext.bot.Commons;
import org.tymoonnext.bot.stream.Stream;

/**
 * 
 * @author Shinmera
 * @license GPLv3
 * @version 0.0.0
 */
public abstract class Event {
    protected Stream origin;
    
    public Event(Stream origin){this.origin=origin;}
    
    public String toString(){
        return "<"+this.getClass().getSimpleName()+">";
    }
    
    public Stream getStream(){return origin;}
}
