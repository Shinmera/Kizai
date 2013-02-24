package org.tymoonnext.bot.event.IRCBot;

import org.tymoonnext.bot.module.irc.IRC;

/**
 * 
 * @author Shinmera
 * @license GPLv3
 * @version 0.0.0
 */
public class PartEvent extends IRCEvent{
    public String channel;
    public String sender;
    public String login;
    public String host;
    
    public PartEvent(String channel){this(null, null, channel, null, null);}
    public PartEvent(IRC bot, String sender, String channel, String host, String login){
        super(bot);
        this.channel=channel;
        this.sender=sender;
        this.host=host;
        this.login=login;
    }
}
