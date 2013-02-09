package org.tymoonnext.bot.event.IRCBot;

import org.tymoonnext.bot.module.irc.IRC;

/**
 * 
 * @author Shinmera
 * @license GPLv3
 * @version 0.0.0
 */
public class ChannelInfoEvent extends IRCEvent{
    public String topic;
    public String channel;
    public int userCount;
    
    public ChannelInfoEvent(IRC bot, String channel, int userCount, String topic){
        super(bot);
        this.channel=channel;
        this.topic=topic;
        this.userCount=userCount;
    }
}
