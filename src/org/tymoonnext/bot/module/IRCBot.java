package org.tymoonnext.bot.module;

import NexT.data.DObject;
import java.io.IOException;
import org.jibble.pircbot.IrcException;
import org.tymoonnext.bot.Kizai;
import org.tymoonnext.bot.event.CommandEvent;
import org.tymoonnext.bot.event.CommandListener;
import org.tymoonnext.bot.event.EventListener;
import org.tymoonnext.bot.event.IRCBot.MessageEvent;
import org.tymoonnext.bot.module.irc.IRC;

/**
 * IRCBot Module that provides an interface to an IRC server. See
 * org.tymoonnext.bot.module.irc.IRC for main IRC funcs.
 * @author Shinmera
 * @license GPLv3
 * @version 0.0.0
 */
public class IRCBot extends Module implements CommandListener,EventListener{
    private IRC irc;
    private DObject config;
    
    public IRCBot(Kizai bot) throws IOException, IrcException{
        super(bot);
        config = bot.getConfig().get("IRCBot");
        
        irc = new IRC(bot, config);
        bot.registerStream("irc", irc);
        try{bot.bindEvent(MessageEvent.class, this, "onMessage");}catch(NoSuchMethodException ex){}
    }

    public void shutdown(){
        bot.unbindAllEvents(this);
        bot.unregisterAllCommands(this);
        bot.unregisterStream("irc");
    }
    
    public IRC getIRC(){return irc;}

    public void onCommand(CommandEvent cmd){
    }
    
    public void onMessage(MessageEvent ev){
        if(ev.message.startsWith(config.get("cmd").toString())){
            String cmd,args;
            if(ev.message.contains(" ")){
                cmd = ev.message.substring(config.get("cmd").toString().length(), ev.message.indexOf(" "));
                args = ev.message.substring(ev.message.indexOf(" ")+1);
            }else{
                cmd = ev.message.substring(config.get("cmd").toString().length());
                args = null;
            }
            bot.command(new CommandEvent(irc, cmd, args, ev.sender, ev.channel));
        }
    }
}
