package org.tymoonnext.bot.event.IRCBot;

import org.tymoonnext.bot.module.irc.IRC;

/**
 * 
 * @author Shinmera
 * @license GPLv3
 * @version 0.0.0
 */
public class VersionEvent extends IRCEvent{
    public String sender;
    public String login;
    public String host;
    public String recipient;
    
    public VersionEvent(String recipient){this(null, null, recipient, null, null);}
    public VersionEvent(IRC bot, String sender, String recipient, String host, String login){
        super(bot);
        this.sender=sender;
        this.recipient=recipient;
        this.login=login;
        this.host=host;
    }
}
