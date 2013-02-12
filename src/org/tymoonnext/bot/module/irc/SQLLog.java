package org.tymoonnext.bot.module.irc;

import NexT.mysql.DataModel;
import NexT.mysql.NSQLException;
import NexT.mysql.SQLWrapper;
import java.util.logging.Level;
import org.tymoonnext.bot.Commons;
import org.tymoonnext.bot.Kizai;
import org.tymoonnext.bot.event.CommandEvent;
import org.tymoonnext.bot.event.CommandListener;
import org.tymoonnext.bot.event.EventListener;
import org.tymoonnext.bot.event.IRCBot.ActionEvent;
import org.tymoonnext.bot.event.IRCBot.JoinEvent;
import org.tymoonnext.bot.event.IRCBot.KickEvent;
import org.tymoonnext.bot.event.IRCBot.MessageEvent;
import org.tymoonnext.bot.event.IRCBot.ModeEvent;
import org.tymoonnext.bot.event.IRCBot.NickEvent;
import org.tymoonnext.bot.event.IRCBot.PartEvent;
import org.tymoonnext.bot.event.IRCBot.QuitEvent;
import org.tymoonnext.bot.event.IRCBot.SendEvent;
import org.tymoonnext.bot.event.IRCBot.TopicEvent;
import org.tymoonnext.bot.module.Module;

/**
 * Logging class that saves everything to an SQL table.
 * @author Shinmera
 * @license GPLv3
 * @version 0.0.0
 * @todo load db stuff from config
 */
public class SQLLog extends Module implements EventListener, CommandListener{
    public static final char TYPE_MESSAGE= 'm';
    public static final char TYPE_TOPIC  = 't';
    public static final char TYPE_MODE   = 'o';
    public static final char TYPE_NICK   = 'n';
    public static final char TYPE_JOIN   = 'j';
    public static final char TYPE_PART   = 'p';
    public static final char TYPE_KICK   = 'k';
    public static final char TYPE_QUIT   = 'q';
    public static final char TYPE_ACTION = 'a';
    public static final char TYPE_SEND   = 's';
    
    private String sqlhost;
    private int sqlport = 3306;
    private String sqldb;
    private String sqluser;
    private String sqlpw;
    private String sqltable;
    private SQLWrapper wrapper;
    
    public SQLLog(Kizai bot){
        super(bot);
        try{
            bot.bindEvent(MessageEvent.class, this, "onMessage");
            bot.bindEvent(TopicEvent.class, this, "onTopic");
            bot.bindEvent(ModeEvent.class, this, "onMode");
            bot.bindEvent(NickEvent.class, this, "onNick");
            bot.bindEvent(JoinEvent.class, this, "onJoin");
            bot.bindEvent(KickEvent.class, this, "onKick");
            bot.bindEvent(PartEvent.class, this, "onPart");
            bot.bindEvent(QuitEvent.class, this, "onQuit");
            bot.bindEvent(ActionEvent.class, this, "onAction");
            bot.bindEvent(SendEvent.class, this, "onSend");
        }catch(NoSuchMethodException ex){}
    }

    @Override
    public void shutdown(){
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void onCommand(CommandEvent cmd){
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void insertUpdate(char type, String channel, String sender, String message){
        try{
            int timestamp = (int)(System.currentTimeMillis() / 1000L);
            DataModel model = DataModel.getHull(sqltable);
            model.set("channel", channel);
            model.set("user", sender);
            model.set("action", type+"");
            model.set("text", message);
            model.set("time", timestamp);
            model.insert();
        }catch(NSQLException ex){
            Commons.log.log(Level.WARNING, toString()+" Failed to log entry.", ex);
        }
    }
    
    public void onMessage(MessageEvent ev){
        insertUpdate(TYPE_MESSAGE, ev.channel, ev.sender, ev.message);
    }
    
    public void onTopic(TopicEvent ev){
        insertUpdate(TYPE_TOPIC, ev.channel, ev.sender, ev.topic);
    }
    
    public void onMode(ModeEvent ev){
        insertUpdate(TYPE_MODE, ev.channel, ev.sender, ev.mode);
    }
    
    public void onNickChange(NickEvent ev){
        for(String channel : ev.getIRC().getChannels())
            insertUpdate(TYPE_NICK, channel, ev.sender, ev.newNick);
    }
    
    public void onJoin(JoinEvent ev){
        insertUpdate(TYPE_JOIN, ev.channel, ev.sender, "");
    }
    
    public void onKick(KickEvent ev){
        insertUpdate(TYPE_KICK, ev.channel, ev.sender, ev.reason);
    }
    
    public void onPart(PartEvent ev){
        insertUpdate(TYPE_PART, ev.channel, ev.sender, "");
    }
    
    public void onQuit(QuitEvent ev){
        for(String channel : ev.getIRC().getChannels())
            insertUpdate(TYPE_QUIT, channel, ev.sender, ev.reason);
    }
    
    public void onAction(ActionEvent ev){
        insertUpdate(TYPE_ACTION, ev.target, ev.sender, ev.action);
    }
    
    public void onSend(SendEvent ev){
        insertUpdate(TYPE_SEND, ev.dest, ev.getIRC().getNick(), ev.message);
    }
}
