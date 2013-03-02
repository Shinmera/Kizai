package org.tymoonnext.bot.module.irc;

import NexT.util.StringUtils;
import org.tymoonnext.bot.Commons;
import org.tymoonnext.bot.Kizai;
import org.tymoonnext.bot.event.CommandListener;
import org.tymoonnext.bot.event.EventListener;
import org.tymoonnext.bot.event.IRCBot.*;
import org.tymoonnext.bot.event.cmd.CommandInstanceEvent;
import org.tymoonnext.bot.event.core.CommandEvent;
import org.tymoonnext.bot.event.core.ModuleLoadEvent;
import org.tymoonnext.bot.event.core.ModuleUnloadEvent;
import org.tymoonnext.bot.module.Module;
import org.tymoonnext.bot.module.core.CommandModule;

/**
 * 
 * @author Shinmera
 * @license GPLv3
 * @version 0.0.0
 */
public class Essentials extends Module implements CommandListener, EventListener{   
    private Module ircbot;
    
    public Essentials(Kizai bot){
        super(bot);
        
        CommandModule.register(bot, "irc", "join",      "channel".split(" "),               "Let the bot join an irc channel.", this);
        CommandModule.register(bot, "irc", "part",      "channel".split(" "),               "Let the bot part an irc channel.", this);
        CommandModule.register(bot, "irc", "msg",       "channel message".split(" "),       "Let the bot send a message to a user or channel.", this);
        CommandModule.register(bot, "irc", "quit",      "message[]".split(" "),             "Make the bot quit.", this);
        CommandModule.register(bot, "irc", "connect",   "host[] port[6666](INTEGER) pass[]".split(" "), "(Re)connect the bot.", this);
        CommandModule.register(bot, "irc", "raw",       "line".split(" "),                  "Send a raw line over the bot.", this);
        try{bot.bindEvent(ModuleLoadEvent.class, this, "onModuleLoad");}catch(NoSuchMethodException ex){}
        try{bot.bindEvent(ModuleUnloadEvent.class, this, "onModuleUnload");}catch(NoSuchMethodException ex){}
    }

    @Override
    public void shutdown(){
        bot.unregisterAllCommands(this);
        bot.unbindAllEvents(this);
    }
    
    public void onModuleLoad(ModuleLoadEvent evt){
        if(evt.getModule().getClass().getSimpleName().equals("IRCBot")){
            Commons.log.info(toString()+" Grabbed IRCBot module.");
            ircbot = evt.getModule();
        }
    }
    
    public void onModuleUnload(ModuleUnloadEvent evt){
        if(evt.getModule().getClass().getSimpleName().equals("IRCBot")){
            Commons.log.info(toString()+" Releasing IRCBot module.");
            ircbot = null;
        }
    }

    public void onCommand(CommandEvent cmd){
        if(ircbot == null)ircbot = bot.getModule("irc.IRCBot");
        if(ircbot != null){
            this.invoke("on"+StringUtils.firstToUpper(cmd.getCommand().split(" ")[1]), cmd);
        }else{
            Commons.log.warning(toString()+" Skipping command "+cmd.getCommand()+"; IRCBot not available.");
        }
    }

    public void onJoin(CommandInstanceEvent evt){
        JoinEvent join = new JoinEvent(evt.getArgs());
        ircbot.invoke("onJoin", join);
    }

    public void onPart(CommandInstanceEvent evt){
        String channel;
        if(evt.get().getValue("channel").isEmpty()) channel = evt.getChannel();
        else                                        channel = evt.getArgs();
        PartEvent part = new PartEvent(channel);
        ircbot.invoke("onPart", part);
    }
    
    public void onMsg(CommandInstanceEvent evt){
        String dest = evt.get().getValue("channel");
        String text = evt.get().getValue("message")+" "+StringUtils.implode(evt.get().getAddPargs(), " ");
        if(dest.startsWith("#")){
            MessageEvent msg = new MessageEvent(text, dest);
            ircbot.invoke("onMessage", msg);
        }else{
            PrivateMessageEvent msg = new PrivateMessageEvent(text, dest);
            ircbot.invoke("onPrivateMessage", msg);
        }
    }
    
    public void onRaw(CommandInstanceEvent evt){
        ircbot.invoke("onUnknown", new UnknownEvent(evt.getArgs()));
    }
    
    public void onQuit(CommandInstanceEvent evt){
        ircbot.invoke("onQuit", new QuitEvent(evt.getArgs()));
    }
    
    public void onConnect(CommandInstanceEvent evt){
        ConnectEvent connect = new ConnectEvent(evt.get().getValue("host"),
                                                Integer.parseInt(evt.get().getValue("port")),
                                                evt.get().getValue("pw"));
        ircbot.invoke("onConnect", connect);
    }
}
