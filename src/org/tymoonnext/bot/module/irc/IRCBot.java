package org.tymoonnext.bot.module.irc;

import java.io.File;
import java.io.IOException;
import org.jibble.pircbot.IrcException;
import org.tymoonnext.bot.Commons;
import org.tymoonnext.bot.Configuration;
import org.tymoonnext.bot.Kizai;
import org.tymoonnext.bot.event.EventListener;
import org.tymoonnext.bot.event.IRCBot.MessageEvent;
import org.tymoonnext.bot.event.core.CommandEvent;
import org.tymoonnext.bot.module.Module;

/**
 * IRCBot Module that provides an interface to an IRC server. See
 * org.tymoonnext.bot.module.irc.IRC for main IRC funcs.
 * @author Shinmera
 * @license GPLv3
 * @version 0.0.0
 */
public class IRCBot extends Module implements EventListener{
    public static final File CONFIGFILE = new File(Commons.f_CONFIGDIR, "irc.cfg");
    
    private IRC irc;
    private Configuration config;
    
    public IRCBot(Kizai bot) throws IOException, IrcException{
        super(bot);
        config = new Configuration();
        config.load(CONFIGFILE);
        
        irc = new IRC(bot, config.get());
        bot.registerStream("irc", irc);
        try{bot.bindEvent(MessageEvent.class, this, "onMessage");}catch(NoSuchMethodException ex){}
    }

    public void shutdown(){
        bot.unbindAllEvents(this);
        config.save(CONFIGFILE);
    }
    
    public IRC getIRC(){return irc;}
    
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
            bot.event(new CommandEvent(irc, cmd, args, ev.sender, ev.channel));
        }
    }
}
