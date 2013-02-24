package org.tymoonnext.bot.event.IRCBot;

import org.tymoonnext.bot.module.irc.IRC;

/**
 * 
 * @author Shinmera
 * @license GPLv3
 * @version 0.0.0
 */
public class ConnectEvent extends IRCEvent{
    public String host;
    public String pw;
    public int port;
    
    public ConnectEvent(IRC bot){this(bot, null, -1, null);}
    public ConnectEvent(IRC bot, String host, int port, String pw){
        super(bot);
        this.host=host;
        this.port=port;
        this.pw=pw;
    }
}
