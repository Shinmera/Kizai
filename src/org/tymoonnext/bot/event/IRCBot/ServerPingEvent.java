package org.tymoonnext.bot.event.IRCBot;

import org.tymoonnext.bot.module.irc.IRC;

/**
 * 
 * @author Shinmera
 * @license GPLv3
 * @version 0.0.0
 */
public class ServerPingEvent extends IRCEvent{
    public String response;
    
    public ServerPingEvent(IRC bot, String response){super(bot);this.response=response;}
}
