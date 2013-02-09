package org.tymoonnext.bot.event.IRCBot;

import org.tymoonnext.bot.module.irc.IRC;

/**
 * 
 * @author Shinmera
 * @license GPLv3
 * @version 0.0.0
 */
public class PrivateMessageEvent extends IRCEvent{
    public String message;
    public String sender;
    public String login;
    public String host;
    
    public PrivateMessageEvent(IRC bot, String sender, String message){this(bot, sender, message, null, null);}
    public PrivateMessageEvent(IRC bot, String sender, String message, String host, String login){
        super(bot);
        this.message=message;
        this.sender=sender;
        this.host=host;
        this.login=login;
    }
}
