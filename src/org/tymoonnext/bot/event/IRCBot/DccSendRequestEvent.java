package org.tymoonnext.bot.event.IRCBot;

import org.tymoonnext.bot.module.irc.IRC;

/**
 * 
 * @author Shinmera
 * @license GPLv3
 * @version 0.0.0
 */
public class DccSendRequestEvent extends IRCEvent{
    public String sender;
    public String login;
    public String host;
    public String file;
    public long address;
    public int port;
    public int size;
    
    public DccSendRequestEvent(IRC bot, String sender, String file, long address, int port, int size){this(bot, sender, file, null, null, address, port, size);}
    public DccSendRequestEvent(IRC bot, String sender, String file, String host, String login, long address, int port, int size){
        super(bot);
        this.sender=sender;
        this.host=host;
        this.login=login;
        this.file=file;
        this.address=address;
        this.port=port;
        this.size=size;
    }
}
