package org.tymoonnext.bot.module.irc;

import java.io.File;
import org.tymoonnext.bot.Commons;
import org.tymoonnext.bot.Configuration;
import org.tymoonnext.bot.Kizai;
import org.tymoonnext.bot.event.CommandListener;
import org.tymoonnext.bot.event.EventListener;
import org.tymoonnext.bot.event.IRCBot.JoinEvent;
import org.tymoonnext.bot.event.core.CommandEvent;
import org.tymoonnext.bot.meta.Info;
import org.tymoonnext.bot.module.Module;
import org.tymoonnext.bot.module.core.ext.CommandModule;

/**
 *
 * @author Shinmera
 * @license GPLv3
 * @version 0.0.0
 */

@Info("Very simple IRC module that provides user greeting messages.")
public class Greeting extends Module implements EventListener, CommandListener{
    public static final File CONFIGFILE = new File(Commons.f_CONFIGDIR, "greetings.cfg");
    
    private Configuration config;
    
    public Greeting(Kizai bot) throws NoSuchMethodException{
        super(bot);
        
        config = new Configuration();
        config.load(CONFIGFILE);
        
        bot.bindEvent(JoinEvent.class, this, "onJoin");
        
        CommandModule.register(bot, "greeting", "message[]".split(" "), "Set an IRC greeting message. No message disables it.", this);
    }

    @Override
    public void shutdown() {
        bot.unbindAllEvents(this);
        config.save(CONFIGFILE);
    }
    
    public void onJoin(JoinEvent evt){
        if(config.has(evt.sender.toLowerCase())){
            if(!config.getS(evt.sender.toLowerCase()).isEmpty())
                evt.getStream().send(config.getS(evt.sender.toLowerCase()), evt.channel);
        }
    }

    @Override
    public void onCommand(CommandEvent cmd) {
        config.setS(cmd.getUser(), cmd.getArgs());
        if(cmd.getArgs() == null)   cmd.getStream().send("Greeting disabled.", cmd.getChannel());
        else                        cmd.getStream().send("Greeting set to: "+cmd.getArgs(), cmd.getChannel());
    }
}