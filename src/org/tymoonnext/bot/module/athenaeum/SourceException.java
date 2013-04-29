package org.tymoonnext.bot.module.athenaeum;

/**
 * 
 * @author Shinmera
 * @license GPLv3
 * @version 0.0.0
 */
public class SourceException extends Exception{
    public SourceException(){super();}
    public SourceException(String msg){super(msg);}
    public SourceException(String msg, Throwable t){super(msg, t);}
}
