package org.tymoonnext.bot.event.IRCBot;

import org.tymoonnext.bot.module.irc.IRC;

/**
 * 
 * @author Shinmera
 * @license GPLv3
 * @version 0.0.0
 */
public class KickEvent extends IRCEvent{
    public String channel;
    public String sender;
    public String login;
    public String host;
    public String recipient;
    public String reason;
    
    public KickEvent(IRC bot, String sender, String channel, String recipient){this(bot, sender, channel, null, null, recipient, null);}
    public KickEvent(IRC bot, String sender, String channel, String host, String login, String recipient, String reason){
        super(bot);
        this.channel=channel;
        this.sender=sender;
        this.host=host;
        this.login=login;
        this.recipient=recipient;
        this.reason=reason;
    }
}
