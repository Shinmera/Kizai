package org.tymoonnext.bot.event.IRCBot;

import org.tymoonnext.bot.event.Event;
import org.tymoonnext.bot.module.irc.IRC;

/**
 * 
 * @author Shinmera
 * @license GPLv3
 * @version 0.0.0
 */
public abstract class IRCEvent extends Event{
    public IRCEvent(IRC bot){super(bot);}
    
    public IRC getIRC(){return (IRC)origin;}
}
