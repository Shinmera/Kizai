package org.tymoonnext.bot.event.IRCBot;

import org.tymoonnext.bot.module.irc.IRC;

/**
 * 
 * @author Shinmera
 * @license GPLv3
 * @version 0.0.0
 */
public class ActionEvent extends IRCEvent{
    public String action;
    public String sender;
    public String login;
    public String host;
    public String recipient;
    
    public ActionEvent(String action, String recipient){this(null, null, action, recipient, null, null);}
    public ActionEvent(IRC bot, String sender, String action, String recipient, String host, String login){
        super(bot);
        this.action=action;
        this.sender=sender;
        this.host=host;
        this.login=login;
        this.recipient=recipient;
    }
}
