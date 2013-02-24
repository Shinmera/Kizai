package org.tymoonnext.bot.event.IRCBot;

import org.tymoonnext.bot.module.irc.IRC;

/**
 * 
 * @author Shinmera
 * @license GPLv3
 * @version 0.0.0
 */
public class PingEvent extends IRCEvent{
    public String sender;
    public String login;
    public String host;
    public String recipient;
    public String ping;
    
    public PingEvent(String recipient, String ping){this(null, null, recipient, ping, null, null);}
    public PingEvent(IRC bot, String sender, String recipient, String ping, String host, String login){
        super(bot);
        this.sender=sender;
        this.recipient=recipient;
        this.login=login;
        this.host=host;
        this.ping=ping;
    }
}
