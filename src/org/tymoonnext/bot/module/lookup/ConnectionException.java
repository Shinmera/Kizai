package org.tymoonnext.bot.module.lookup;

/**
 * Thrown because of a connection error
 * @author Mithent
 */
public class ConnectionException extends Exception{
    public Exception innerException;
    
    ConnectionException(Exception e){
        innerException = e;
    }
    
}
