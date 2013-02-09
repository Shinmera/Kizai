package org.tymoonnext.bot.event.IRCBot;

import org.tymoonnext.bot.module.irc.IRC;

/**
 * 
 * @author Shinmera
 * @license GPLv3
 * @version 0.0.0
 */
public class FingerEvent extends IRCEvent{
    public String sender;
    public String login;
    public String host;
    public String recipient;
    
    public FingerEvent(IRC bot, String sender, String recipient){this(bot, sender, recipient, null, null);}
    public FingerEvent(IRC bot, String sender, String recipient, String host, String login){
        super(bot);
        this.sender=sender;
        this.recipient=recipient;
        this.login=login;
        this.host=host;
    }
}
