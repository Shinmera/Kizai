package org.tymoonnext.bot.event.IRCBot;

import org.tymoonnext.bot.module.irc.IRC;

/**
 * 
 * @author Shinmera
 * @license GPLv3
 * @version 0.0.0
 */
public class InviteEvent extends IRCEvent{
    public String sender;
    public String login;
    public String host;
    public String recipient;
    public String channel;
    
    public InviteEvent(String recipient, String channel){this(null, null, recipient, channel, null, null);}
    public InviteEvent(IRC bot, String sender, String recipient, String channel, String host, String login){
        super(bot);
        this.sender=sender;
        this.recipient=recipient;
        this.login=login;
        this.host=host;
        this.channel=channel;
    }
}
