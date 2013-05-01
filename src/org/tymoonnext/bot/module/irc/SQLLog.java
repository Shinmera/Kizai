package org.tymoonnext.bot.module.irc;

import NexT.db.mysql.DataModel;
import NexT.db.mysql.NSQLException;
import NexT.db.mysql.MySQLWrapper;
import java.util.logging.Level;
import org.tymoonnext.bot.Commons;
import NexT.data.ConfigLoader;
import NexT.data.required;
import org.tymoonnext.bot.Kizai;
import org.tymoonnext.bot.event.CommandListener;
import org.tymoonnext.bot.event.EventListener;
import org.tymoonnext.bot.event.IRCBot.*;
import org.tymoonnext.bot.event.core.CommandEvent;
import org.tymoonnext.bot.meta.Info;
import org.tymoonnext.bot.module.Module;

/**
 * Logging class that saves everything to an SQL table.
 * @author Shinmera
 * @license GPLv3
 * @version 0.0.0
 */

@Info("IRC logging module that writes all IRC messages to a remote SQL server.")
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
    
    class C extends ConfigLoader{
        @required public String table;
    };
    private C conf = new C();
    private MySQLWrapper wrapper;
    
    public SQLLog(Kizai bot) throws NSQLException{
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
        conf.load(bot.getConfig().get("modules").get("irc.SQLLog"));
    }

    public void shutdown(){
        bot.unbindAllEvents(this);
    }

    public void onCommand(CommandEvent cmd){
    }

    public void insertUpdate(char type, String channel, String sender, String message){
        try{
            int timestamp = (int)(System.currentTimeMillis() / 1000L);
            DataModel model = DataModel.getHull(conf.table);
            model.set("channel", channel);
            model.set("user", sender);
            model.set("action", type+"");
            model.set("text", message);
            model.set("time", timestamp);
            model.insert();
            Commons.log.log(Level.FINE, toString()+" Logging #"+channel+" "+sender+" "+type+": "+message);
        }catch(NSQLException ex){
            Commons.log.log(Level.WARNING, toString()+" Failed to log entry.", ex);
        }
    }
    
    public void onMessage(MessageEvent ev){
        insertUpdate(TYPE_MESSAGE, ev.channel, ev.sender, ev.message);
    }
    
    public void onTopic(TopicEvent ev){
        String sender = ev.sender;
        if(sender.contains("!"))sender = sender.substring(0, sender.indexOf('!'));
        insertUpdate(TYPE_TOPIC, ev.channel, sender, ev.topic);
    }
    
    public void onMode(ModeEvent ev){
        insertUpdate(TYPE_MODE, ev.channel, ev.sender, ev.mode);
    }
    
    public void onNick(NickEvent ev){
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
        insertUpdate(TYPE_ACTION, ev.recipient, ev.sender, ev.action);
    }
    
    public void onSend(SendEvent ev){
        insertUpdate(TYPE_SEND, ev.dest, ev.getIRC().getNick(), ev.message);
    }
}
