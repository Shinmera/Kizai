package org.tymoonnext.bot.event.IRCBot;

import org.tymoonnext.bot.module.irc.IRC;

/**
 * 
 * @author Shinmera
 * @license GPLv3
 * @version 0.0.0
 */
public class QuitEvent extends IRCEvent{
    public String sender;
    public String login;
    public String host;
    public String reason;
    
    public QuitEvent(){this(null);}
    public QuitEvent(String reason){this(null, null, reason, null, null);}
    public QuitEvent(IRC bot, String sender, String reason, String host, String login){
        super(bot);
        this.sender=sender;
        this.host=host;
        this.login=login;
        this.reason=reason;
    }
}
