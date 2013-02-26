package org.tymoonnext.bot.module.cmd;


/**
 * 
 * @author Shinmera
 * @license GPLv3
 * @version 0.0.0
 */
public class ParseException extends Exception{
    public ParseException(){super();}
    public ParseException(String args){super(args);}
    public ParseException(String args, Exception ex){super(args, ex);}
}
