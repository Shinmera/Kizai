package org.tymoonnext.bot.module.cleverbot;

import org.tymoonnext.bot.event.CommandListener;
import org.tymoonnext.bot.event.EventListener;
import org.tymoonnext.bot.event.core.CommandEvent;
import org.tymoonnext.bot.module.Module;
import org.tymoonnext.bot.Kizai;
import org.tymoonnext.bot.event.cmd.CommandInstanceEvent;
import org.tymoonnext.bot.module.core.ext.CommandModule;

import com.google.code.chatterbotapi.*;
import org.tymoonnext.bot.Commons;
import org.tymoonnext.bot.meta.Info;

@Info("Replaces default unbound command handler with responses from Cleverbot")
public class Cleverbot extends Module implements CommandListener, EventListener{
    private ChatterBot cleverBot;
    private ChatterBotSession cleverBotSession = null;
    
    public Cleverbot(Kizai bot) {
        super(bot);

        ChatterBotFactory chatterbotFactory = new ChatterBotFactory();
        try {
            cleverBot = chatterbotFactory.create(ChatterBotType.CLEVERBOT);
        }
        catch (Exception e) {
            Commons.log.warning(toString()+" Failed to initialise: " + e.getMessage());
            return;
        }
        
        bot.unregisterCommand(CommandEvent.CMD_UNBOUND);
        bot.registerCommand(CommandEvent.CMD_UNBOUND, this);

    }
    
    public void shutdown(){
        bot.unbindAllEvents(this);
    }

    public void onCommand(CommandEvent cmd){
        if(cmd.getCommand().equals(CommandEvent.CMD_UNBOUND))
            try {
                if (cleverBotSession == null) {
                    cleverBotSession = cleverBot.createSession();
                }
                cmd.getStream().send(cleverBotSession.think(cmd.getArgs()), cmd.getChannel());
            }
            catch (Exception e) {
                cmd.getStream().send("Cleverbot error: " + e.getMessage(), cmd.getChannel());
            }

        return;
    }
}
