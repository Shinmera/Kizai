package org.tymoonnext.bot.event.IRCBot;

import org.tymoonnext.bot.module.irc.IRC;

/**
 * 
 * @author Shinmera
 * @license GPLv3
 * @version 0.0.0
 */
public class ModeEvent extends IRCEvent{
    public String channel;
    public String sender;
    public String login;
    public String host;
    public String mode;
    
    public ModeEvent(String channel, String mode){this(null, null, channel, mode, null, null);}
    public ModeEvent(IRC bot, String sender, String channel, String mode, String host, String login){
        super(bot);
        this.channel=channel;
        this.sender=sender;
        this.host=host;
        this.login=login;
        this.mode=mode;
    }
}
