package org.tymoonnext.bot.event.IRCBot;

import org.tymoonnext.bot.module.irc.IRC;

/**
 * 
 * @author Shinmera
 * @license GPLv3
 * @version 0.0.0
 */
public class NickEvent extends IRCEvent{
    public String sender;
    public String login;
    public String host;
    public String newNick;
    
    public NickEvent(String newNick){this(null, null, newNick, null, null);}
    public NickEvent(IRC bot, String sender, String newNick, String host, String login){
        super(bot);
        this.sender=sender;
        this.host=host;
        this.login=login;
        this.newNick=newNick;
    }
}
