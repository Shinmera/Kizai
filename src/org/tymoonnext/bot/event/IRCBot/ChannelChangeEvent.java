package org.tymoonnext.bot.event.IRCBot;

import org.tymoonnext.bot.module.irc.IRC;

/**
 * 
 * @author Shinmera
 * @license GPLv3
 * @version 0.0.0
 */
public class ChannelChangeEvent extends IRCEvent{
    public static final int TYPE_SET_INVITE_ONLY = 0;
    public static final int TYPE_REMOVE_INVITE_ONLY = 1;
    public static final int TYPE_SET_MODERATED = 2;
    public static final int TYPE_REMOVE_MODERATED = 3;
    public static final int TYPE_SET_NO_EXTERNAL_MESSAGES = 4;
    public static final int TYPE_REMOVE_NO_EXTERNAL_MESSAGES = 5;
    public static final int TYPE_SET_PRIVATE = 6;
    public static final int TYPE_REMOVE_PRIVATE = 7;
    public static final int TYPE_SET_SECRET = 8;
    public static final int TYPE_REMOVE_SECRET = 9;
    public static final int TYPE_SET_TOPIC_PROTECTION = 10;
    public static final int TYPE_REMOVE_TOPIC_PROTECTION = 11;
    public static final int TYPE_SET_VOICE = 12;
    public static final int TYPE_REMOVE_VOICE = 13;
    public static final int TYPE_SET_BAN = 14;
    public static final int TYPE_REMOVE_BAN = 15;
    public static final int TYPE_SET_KEY = 16;
    public static final int TYPE_REMOVE_KEY = 17;
    public static final int TYPE_SET_LIMIT = 18;
    public static final int TYPE_REMOVE_LIMIT = 19;
    
    public String sender;
    public String login;
    public String host;
    public String channel;
    public int change;
    public String args;
    
    public ChannelChangeEvent(String channel, int change){this(channel, change, null);}
    public ChannelChangeEvent(String channel, int change, String args){this(null, channel, change, args, null, null, null);}
    public ChannelChangeEvent(IRC bot, String channel, int change, String sender, String host, String login){this(bot, channel, change, null, sender, host, login);}
    public ChannelChangeEvent(IRC bot, String channel, int change, String args, String sender, String host, String login){
        super(bot);
        this.change=change;
        this.sender=sender;
        this.channel=channel;
        this.login=login;
        this.host=host;
        this.args=args;
    }
}
