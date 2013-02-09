package org.tymoonnext.bot.event.IRCBot;

import org.tymoonnext.bot.module.irc.IRC;

/**
 * 
 * @author Shinmera
 * @license GPLv3
 * @version 0.0.0
 */
public class DccChatRequestEvent extends IRCEvent{
    public String sender;
    public String login;
    public String host;
    public long address;
    public int port;
    
    public DccChatRequestEvent(IRC bot, String sender, long address, int port){this(bot, sender, null, null, address, port);}
    public DccChatRequestEvent(IRC bot, String sender, String host, String login, long address, int port){
        super(bot);
        this.sender=sender;
        this.host=host;
        this.login=login;
        this.address=address;
        this.port=port;
    }
}
