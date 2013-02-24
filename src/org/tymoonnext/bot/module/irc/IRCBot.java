package org.tymoonnext.bot.module.irc;

import NexT.data.DObject;
import NexT.data.DParse;
import NexT.util.Toolkit;
import java.io.File;
import java.io.IOException;
import org.jibble.pircbot.IrcException;
import org.tymoonnext.bot.Commons;
import org.tymoonnext.bot.ConfigLoader;
import org.tymoonnext.bot.Configuration;
import org.tymoonnext.bot.Kizai;
import org.tymoonnext.bot.event.EventListener;
import org.tymoonnext.bot.event.IRCBot.MessageEvent;
import org.tymoonnext.bot.event.core.CommandEvent;
import org.tymoonnext.bot.meta.required;
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
    
    public class C extends ConfigLoader{
        public String cmd = "./";
        @required public DObject server;
    }
    
    private C config = new C();
    private IRC irc;
    
    public IRCBot(Kizai bot) throws IOException, IrcException{
        super(bot);
        config.load(DParse.parse(CONFIGFILE));
        
        irc = new IRC(bot, config.server);
        bot.registerStream("irc", irc);
        try{bot.bindEvent(MessageEvent.class, this, "onMessage");}catch(NoSuchMethodException ex){}
    }

    public void shutdown(){
        bot.unbindAllEvents(this);
        Toolkit.saveStringToFile(DParse.parse(config.save(), true), CONFIGFILE);
    }
    
    public IRC getIRC(){return irc;}
    
    public void onMessage(MessageEvent ev){
        if(ev.message.startsWith(config.cmd)){
            String cmd,args;
            if(ev.message.contains(" ")){
                cmd = ev.message.substring(config.cmd.length(), ev.message.indexOf(" "));
                args = ev.message.substring(ev.message.indexOf(" ")+1);
            }else{
                cmd = ev.message.substring(config.cmd.length());
                args = null;
            }
            bot.event(new CommandEvent(irc, cmd, args, ev.sender, ev.channel));
        }
    }
}
