package org.tymoonnext.bot.module.lookup.json;

/**
 * Thrown because the JSON string could not be parsed 
 * @author Mithent
 */
public class InvalidJSONException extends Exception{
    public InvalidJSONException(String s){super(s);}
    public InvalidJSONException(Exception ex){super(ex);}
    public InvalidJSONException(String s, Exception ex){super(s, ex);}    
}
