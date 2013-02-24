package org.tymoonnext.bot.event.IRCBot;

import org.tymoonnext.bot.module.irc.IRC;

/**
 * 
 * @author Shinmera
 * @license GPLv3
 * @version 0.0.0
 */
public class ServerResponseEvent extends IRCEvent{
    public int code;
    public String line;
    
    public ServerResponseEvent(int code, String line){this(null, code, line);}
    public ServerResponseEvent(IRC bot, int code, String line){
        super(bot);
        this.line=line;
        this.code=code;
    }
}
