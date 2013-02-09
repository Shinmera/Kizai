package org.tymoonnext.bot.event.IRCBot;

import org.tymoonnext.bot.module.irc.IRC;

/**
 * 
 * @author Shinmera
 * @license GPLv3
 * @version 0.0.0
 */
public class TopicEvent extends IRCEvent{
    public String topic;
    public String sender;
    public String channel;
    public long date;
    public boolean changed;
    
    public TopicEvent(IRC bot, String channel, String topic){this(bot, channel, topic, null, -1, false);}
    public TopicEvent(IRC bot, String channel, String topic, String sender, long date, boolean changed){
        super(bot);
        this.topic=topic;
        this.sender=sender;
        this.channel=channel;
        this.date=date;
        this.changed=changed;
    }
}
