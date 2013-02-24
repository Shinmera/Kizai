package org.tymoonnext.bot.event.IRCBot;

import org.tymoonnext.bot.module.irc.IRC;

/**
 * 
 * @author Shinmera
 * @license GPLv3
 * @version 0.0.0
 */
public class MessageEvent extends IRCEvent{
    public String message;
    public String sender;
    public String login;
    public String host;
    public String channel;
    
    public MessageEvent(String message, String channel){this(null, null, message, channel, null, null);}
    public MessageEvent(IRC bot, String sender, String message, String channel, String host, String login){
        super(bot);
        this.message=message;
        this.sender=sender;
        this.channel=channel;
        this.login=login;
        this.host=host;
    }
}
