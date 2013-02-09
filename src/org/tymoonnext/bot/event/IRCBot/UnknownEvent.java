package org.tymoonnext.bot.event.IRCBot;

import org.tymoonnext.bot.module.irc.IRC;

/**
 * 
 * @author Shinmera
 * @license GPLv3
 * @version 0.0.0
 */
public class UnknownEvent extends IRCEvent{
    public String line;
    
    public UnknownEvent(IRC bot, String line){super(bot);this.line=line;}
}
