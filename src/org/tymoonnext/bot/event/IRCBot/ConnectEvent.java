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
    
    public ConnectEvent(){this(null, null, -1, null);}
    public ConnectEvent(String host, int port, String pw){this(null, host, port, pw);}
    public ConnectEvent(IRC bot, String host, int port, String pw){
        super(bot);
        this.host=host;
        this.port=port;
        this.pw=pw;
    }
}
