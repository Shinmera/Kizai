package org.tymoonnext.bot.event.IRCBot;

import org.jibble.pircbot.User;
import org.tymoonnext.bot.module.irc.IRC;

/**
 * 
 * @author Shinmera
 * @license GPLv3
 * @version 0.0.0
 */
public class UserListEvent extends IRCEvent{
    public String channel;
    public User[] users;
    
    public UserListEvent(String channel){this(null, channel, null);}
    public UserListEvent(IRC bot, String channel, User[] users){
        super(bot);
        this.users=users;
        this.channel=channel;
    }
}
