package org.tymoonnext.bot.event.IRCBot;

import org.tymoonnext.bot.module.irc.IRC;

/**
 * 
 * @author Shinmera
 * @license GPLv3
 * @version 0.0.0
 */
public class DeopEvent extends IRCEvent{
    public String channel;
    public String sender;
    public String login;
    public String host;
    public String recipient;
    
    public DeopEvent(IRC bot, String sender, String channel, String recipient){this(bot, sender, channel, null, null, recipient);}
    public DeopEvent(IRC bot, String sender, String channel, String host, String login, String recipient){
        super(bot);
        this.channel=channel;
        this.sender=sender;
        this.host=host;
        this.login=login;
        this.recipient=recipient;
    }
}
