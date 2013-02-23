package org.tymoonnext.bot.module.irc;

import NexT.util.StringUtils;
import org.tymoonnext.bot.Kizai;
import org.tymoonnext.bot.event.CommandListener;
import org.tymoonnext.bot.event.EventListener;
import org.tymoonnext.bot.event.core.CommandEvent;
import org.tymoonnext.bot.event.group.GroupRegisterEvent;
import org.tymoonnext.bot.module.Module;

/**
 * 
 * @author Shinmera
 * @license GPLv3
 * @version 0.0.0
 */
public class Essentials extends Module implements CommandListener, EventListener{    
    public Essentials(Kizai bot){
        super(bot);
        
        bot.event(new GroupRegisterEvent("irc", "join", this));
        bot.event(new GroupRegisterEvent("irc", "part", this));
        bot.event(new GroupRegisterEvent("irc", "say", this));
        bot.event(new GroupRegisterEvent("irc", "msg", this));
        bot.event(new GroupRegisterEvent("irc", "send", this));
        bot.event(new GroupRegisterEvent("irc", "quit", this));
        bot.event(new GroupRegisterEvent("irc", "connect", this));
    }

    @Override
    public void shutdown(){
        bot.unregisterAllCommands(this);
        bot.unbindAllEvents(this);
    }

    public void onCommand(CommandEvent cmd){
        this.invoke("on"+StringUtils.firstToUpper(cmd.getCommand()), cmd);
    }

    public void onJoin(CommandEvent evt){
        
    }

    public void onPart(CommandEvent evt){
        
    }

    public void onSay(CommandEvent evt){onSend(evt);}
    public void onMsg(CommandEvent evt){onSend(evt);}
    public void onSend(CommandEvent evt){
        
    }
    
    public void onQuit(CommandEvent evt){
        
    }
    
    public void onConnect(CommandEvent evt){
        
    }
}
