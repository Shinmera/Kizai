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
    public String target;
    
    public ActionEvent(IRC bot, String sender, String action, String target){this(bot, sender, action, target, null, null);}
    public ActionEvent(IRC bot, String sender, String action, String target, String host, String login){
        super(bot);
        this.action=action;
        this.sender=sender;
        this.host=host;
        this.login=login;
        this.target=target;
    }
}
