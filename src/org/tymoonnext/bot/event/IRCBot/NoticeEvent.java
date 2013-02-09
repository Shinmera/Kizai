package org.tymoonnext.bot.event.IRCBot;

import org.tymoonnext.bot.module.irc.IRC;

/**
 * 
 * @author Shinmera
 * @license GPLv3
 * @version 0.0.0
 */
public class NoticeEvent extends IRCEvent{
    public String sender;
    public String login;
    public String host;
    public String recipient;
    public String notice;
    
    public NoticeEvent(IRC bot, String sender, String recipient, String notice){this(bot, sender, recipient, notice, null, null);}
    public NoticeEvent(IRC bot, String sender, String recipient, String notice, String host, String login){
        super(bot);
        this.sender=sender;
        this.recipient=recipient;
        this.login=login;
        this.host=host;
        this.notice=notice;
    }
}
