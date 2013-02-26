package org.tymoonnext.bot.module.irc;

import NexT.util.StringUtils;
import org.tymoonnext.bot.Commons;
import org.tymoonnext.bot.Kizai;
import org.tymoonnext.bot.event.CommandListener;
import org.tymoonnext.bot.event.EventListener;
import org.tymoonnext.bot.event.IRCBot.*;
import org.tymoonnext.bot.event.core.CommandEvent;
import org.tymoonnext.bot.event.core.ModuleLoadEvent;
import org.tymoonnext.bot.event.core.ModuleUnloadEvent;
import org.tymoonnext.bot.event.cmd.GroupRegisterEvent;
import org.tymoonnext.bot.module.Module;

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
        
        bot.event(new GroupRegisterEvent("irc", "join", this));
        bot.event(new GroupRegisterEvent("irc", "part", this));
        bot.event(new GroupRegisterEvent("irc", "say", this));
        bot.event(new GroupRegisterEvent("irc", "msg", this));
        bot.event(new GroupRegisterEvent("irc", "send", this));
        bot.event(new GroupRegisterEvent("irc", "quit", this));
        bot.event(new GroupRegisterEvent("irc", "connect", this));
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
            this.invoke("on"+StringUtils.firstToUpper(cmd.getCommand()), cmd);
        }else{
            Commons.log.warning(toString()+" Skipping command "+cmd.getCommand()+"; IRCBot not available.");
        }
    }

    public void onJoin(CommandEvent evt){
        if((evt.getArgs() == null) || (evt.getArgs().isEmpty())){
            evt.getStream().send("No channel specified. Usage: irc join #channel", evt.getChannel());
            return;
        }
        JoinEvent join = new JoinEvent(evt.getArgs());
        ircbot.invoke("onJoin", join);
    }

    public void onPart(CommandEvent evt){
        if(((evt.getArgs() == null) || (evt.getArgs().isEmpty())) && (evt.getChannel() != null)){
            evt.getStream().send("No channel specified. Usage: irc part #channel", evt.getChannel());
            return;
        }
        String channel = "";
        if((evt.getArgs() == null) || (evt.getArgs().isEmpty())) channel = evt.getChannel();
        else                                                     channel = evt.getArgs();
        PartEvent part = new PartEvent(channel);
        ircbot.invoke("onPart", part);
    }

    public void onSay(CommandEvent evt){onSend(evt);}
    public void onMsg(CommandEvent evt){onSend(evt);}
    public void onSend(CommandEvent evt){
        if((evt.getArgs() == null) || (evt.getArgs().isEmpty()) || (!evt.getArgs().contains(" "))){
            evt.getStream().send("Not enough arguments. Usage: irc msg destination message*", evt.getChannel());
            return;
        }
        String dest = evt.getArgs().substring(0, evt.getArgs().indexOf(' '));
        String text = evt.getArgs().substring(evt.getArgs().indexOf(' ')+1);
        if(dest.startsWith("#")){
            MessageEvent msg = new MessageEvent(text, dest);
            ircbot.invoke("onMessage", msg);
        }else{
            PrivateMessageEvent msg = new PrivateMessageEvent(text, dest);
            ircbot.invoke("onPrivateMessage", msg);
        }
    }
    
    public void onQuit(CommandEvent evt){
        QuitEvent quit = new QuitEvent(null, null, evt.getArgs(), null, null);
        ircbot.invoke("onQuit", quit);
    }
    
    public void onConnect(CommandEvent evt){
        ConnectEvent connect = new ConnectEvent();
        ircbot.invoke("onConnect", connect);
    }
}
