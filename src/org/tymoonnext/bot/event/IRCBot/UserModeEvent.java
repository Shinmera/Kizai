package org.tymoonnext.bot.event.IRCBot;

import org.tymoonnext.bot.module.irc.IRC;

/**
 * 
 * @author Shinmera
 * @license GPLv3
 * @version 0.0.0
 */
public class UserModeEvent extends IRCEvent{
    public String recipient;
    public String sender;
    public String login;
    public String host;
    public String mode;
    
    public UserModeEvent(String recipient, String mode){this(null, null, recipient, mode, null, null);}
    public UserModeEvent(IRC bot, String sender, String recipient, String mode, String host, String login){
        super(bot);
        this.recipient=recipient;
        this.sender=sender;
        this.host=host;
        this.login=login;
        this.mode=mode;
    }
}
