package org.tymoonnext.bot.module.lookup;

/**
 * Thrown because no matching term could be found.
 * @author Mithent
 */
public class NoMatchException extends Exception{
    
    public NoMatchException(String s){super(s);}
    public NoMatchException(Exception ex){super(ex);}
    public NoMatchException(String s, Exception ex){super(s, ex);}
}
