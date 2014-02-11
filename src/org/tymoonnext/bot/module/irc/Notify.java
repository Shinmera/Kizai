package org.tymoonnext.bot.module.irc;

import NexT.db.mongo.DataModel;
import NexT.db.mongo.MongoException;
import NexT.util.StringUtils;
import com.mongodb.BasicDBObject;
import java.util.Date;
import java.util.logging.Level;
import org.tymoonnext.bot.Commons;
import org.tymoonnext.bot.Kizai;
import org.tymoonnext.bot.event.CommandListener;
import org.tymoonnext.bot.event.EventListener;
import org.tymoonnext.bot.event.IRCBot.MessageEvent;
import org.tymoonnext.bot.event.cmd.CommandInstanceEvent;
import org.tymoonnext.bot.event.core.CommandEvent;
import org.tymoonnext.bot.module.Module;
import org.tymoonnext.bot.module.cmd.CommandInstance;
import org.tymoonnext.bot.module.core.ext.CommandModule;

/**
 * 
 * @author Shinmera
 * @license GPLv3
 * @version 0.0.0
 */
public class Notify extends Module implements EventListener, CommandListener{
    
    public Notify(Kizai bot){
        super(bot);
        CommandModule.register(bot, "notify", "user message".split(" "), "", this);
        try{bot.bindEvent(MessageEvent.class, this, "onMessage");
        }catch(NoSuchMethodException ex){}
    }

    @Override
    public void shutdown() {
        bot.unregisterAllCommands(this);
    }
    
    @Override
    public void onCommand(CommandEvent cmd) {
        CommandInstance i = ((CommandInstanceEvent)cmd).get();
        try {
            DataModel mod = DataModel.getHull("notify");
            mod.set("source", cmd.getUser());
            mod.set("channel", cmd.getChannel());
            mod.set("target", i.getValue("user").toLowerCase());
            mod.set("time", new Date().getTime());
            mod.set("message", cmd.getArgs().substring(i.getValue("user").length()+1).trim());
            mod.insert();
            cmd.getStream().send(String.format("%s: Note for %s created. The note will be shown the next time he talks.", cmd.getUser(), i.getValue("user")), cmd.getChannel());
        } catch (MongoException ex) {
            Commons.log.log(Level.WARNING, "[Notify] Failed to create note. ", ex);
            cmd.getStream().send("Failed to create note: "+ex, cmd.getChannel());
        }
    }
    
    public void onMessage(MessageEvent ev){
        try{
            DataModel[] mods = DataModel.getData("notify", new BasicDBObject("target", ev.sender.toLowerCase()));
            if(mods != null){
                for(DataModel mod : mods){
                    ev.getStream().send(String.format("%s: %s wrote to you on %s: %s", ev.sender, mod.get("source"), StringUtils.toHumanTime((Long)mod.get("time")), mod.get("message")), ev.channel);
                    mod.delete();
                }
            }
        } catch (MongoException ex) {
            Commons.log.log(Level.WARNING, "[Notify] Failed to retrieve notes. ", ex);
        }
    }
}
