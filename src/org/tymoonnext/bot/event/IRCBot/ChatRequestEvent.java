package org.tymoonnext.bot.event.IRCBot;

import org.jibble.pircbot.DccChat;
import org.tymoonnext.bot.module.irc.IRC;

/**
 * 
 * @author Shinmera
 * @license GPLv3
 * @version 0.0.0
 */
public class ChatRequestEvent extends IRCEvent{
    public DccChat chat;
    
    public ChatRequestEvent(IRC bot, DccChat chat){super(bot);this.chat=chat;}
}
