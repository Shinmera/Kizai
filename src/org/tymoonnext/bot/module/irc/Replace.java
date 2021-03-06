package org.tymoonnext.bot.module.irc;

import org.tymoonnext.bot.Kizai;
import org.tymoonnext.bot.event.EventListener;
import org.tymoonnext.bot.event.IRCBot.MessageEvent;
import org.tymoonnext.bot.meta.Info;
import org.tymoonnext.bot.module.Module;

/**
 * Very simple module that replaces the previous message it received.
 * @author Shinmera
 * @license GPLv3
 * @version 0.0.0
 */

@Info("Very simple IRC module that performs a replace on all messages of the pattern s/search/replace/g.")
public class Replace extends Module implements EventListener{
    private String previous = "";
    private String previousUser = "";

    public Replace(Kizai bot){
        super(bot);
        try{bot.bindEvent(MessageEvent.class, this, "onMessage");}catch(NoSuchMethodException ex){}
    }

    public void shutdown(){
        bot.unbindAllEvents(this);
    }

    public void onMessage(MessageEvent ev){
        if(ev.message.matches("s\\/.+\\/.+\\/g")){
            String[] parts = ev.message.split("/");
            ev.getStream().send("<"+previousUser+"> "+previous.replaceAll(parts[1], parts[2]), ev.channel);
        }else if(ev.message.matches("s\\,.+\\,.+\\,g")){
            String[] parts = ev.message.split(",");
            ev.getStream().send("<"+previousUser+"> "+previous.replaceAll(parts[1], parts[2]), ev.channel);
        }else{
            previous = ev.message;
            previousUser = ev.sender;
        }
    }
}
