package org.tymoonnext.bot.module.lookup;

/**
 * Thrown because of a connection error
 * @author Mithent
 */
public class ConnectionException extends Exception{
    
    /**
     * For Exceptions, please always include a message constructor and pass
     * an appropriate exception message along. This is the preferred method 
     * over creating many exception classes.
     * Also, always use the super() constructors provided by the Exception
     * class.
     */
    
    public ConnectionException(String s){super(s);}
    public ConnectionException(Exception ex){super(ex);}
    public ConnectionException(String s, Exception ex){super(s, ex);}
    
}
