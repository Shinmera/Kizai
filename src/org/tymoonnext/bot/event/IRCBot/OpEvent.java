package org.tymoonnext.bot.event.IRCBot;

import org.tymoonnext.bot.module.irc.IRC;

/**
 * 
 * @author Shinmera
 * @license GPLv3
 * @version 0.0.0
 */
public class OpEvent extends IRCEvent{
    public String channel;
    public String sender;
    public String login;
    public String host;
    public String recipient;
    
    public OpEvent(String channel, String recipient){this(null, null, channel, null, null, recipient);}
    public OpEvent(IRC bot, String sender, String channel, String host, String login, String recipient){
        super(bot);
        this.channel=channel;
        this.sender=sender;
        this.host=host;
        this.login=login;
        this.recipient=recipient;
    }
}
