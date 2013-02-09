package org.tymoonnext.bot.event.IRCBot;

import org.tymoonnext.bot.module.irc.IRC;

/**
 * 
 * @author Shinmera
 * @license GPLv3
 * @version 0.0.0
 */
public class SendEvent extends IRCEvent{
    public String dest;
    public String message;
    
    public SendEvent(IRC bot, String dest, String message){
        super(bot);
        this.dest=dest;
        this.message=message;
    }
}
