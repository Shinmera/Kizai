package org.tymoonnext.bot.module.misc;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import org.tymoonnext.bot.Commons;
import NexT.data.ConfigLoader;
import org.tymoonnext.bot.Kizai;
import org.tymoonnext.bot.event.CommandListener;
import org.tymoonnext.bot.event.EventListener;
import org.tymoonnext.bot.event.IRCBot.MessageEvent;
import org.tymoonnext.bot.event.core.CommandEvent;
import NexT.data.noload;
import NexT.data.nosave;
import org.tymoonnext.bot.meta.Info;
import org.tymoonnext.bot.module.Module;
import org.tymoonnext.bot.module.core.ext.CommandModule;

/**
 * 
 * @author Shinmera
 * @license GPLv3
 * @version 0.0.0
 */

@Info("General module that provides a few essential commands.")
public class Essentials extends Module implements CommandListener, EventListener{
    private class C extends ConfigLoader{
        @nosave @noload public String googleUrl = "http://www.google.com/search?btnI&q=";
        @nosave @noload public String userAgent = "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.28 (KHTML, like Gecko) Chrome/26.0.1398.0 Safari/537.28";
        @nosave public String dateFormat = "d.M.yyyy";
        @nosave public String timeFormat = "H:mm:ss z";
        public long longestUptime = 0;
    }
    private C config = new C();
    private SimpleDateFormat dateFormat;
    private SimpleDateFormat timeFormat;
    
    public Essentials(Kizai bot){
        super(bot);
        CommandModule.register(bot, "date", null,   "Show the current date.", this);
        CommandModule.register(bot, "time", null,   "Show the current local bot time.", this);
        CommandModule.register(bot, "uptime", null, "Show how long the bot has been running for.", this);
        CommandModule.register(bot, "google", "query".split(" "), "Perform a google query and retrieve the first result url.", this);
        try{bot.bindEvent(MessageEvent.class, this, "onMessage"); }catch(NoSuchMethodException ex){}
        config.load(bot.getConfig().get("modules").get("misc.Essentials"));
        dateFormat = new SimpleDateFormat(config.dateFormat);
        timeFormat = new SimpleDateFormat(config.timeFormat);
    }

    @Override
    public void shutdown(){
        long curUptime = System.currentTimeMillis() - Commons.STARTUP_TIME;
        if(curUptime > config.longestUptime)config.longestUptime = curUptime;
        config.save();
        bot.unbindAllEvents(this);
        bot.unregisterAllCommands(this);
    }

    public void onCommand(CommandEvent cmd){
        if(cmd.getCommand().equals("date")){
            cmd.getStream().send("It is now the "+dateFormat.format(new Date()), cmd.getChannel());
        }else if(cmd.getCommand().equals("time")){
            cmd.getStream().send("It is currently "+timeFormat.format(new Date()), cmd.getChannel());
        }else if(cmd.getCommand().equals("uptime")){
            long curUptime = System.currentTimeMillis() - Commons.STARTUP_TIME;
            int[] comps = toTimeComponents(curUptime);
            cmd.getStream().send("Uptime since "+dateFormat.format(new Date(Commons.STARTUP_TIME))+" "+
                                                 timeFormat.format(new Date(Commons.STARTUP_TIME))+": "+
                                                 comps[3]+"d "+comps[2]+"h "+comps[1]+"m "+comps[0]+"s.", cmd.getChannel());
            if(config.longestUptime > curUptime){
                comps = toTimeComponents(config.longestUptime);
                cmd.getStream().send("Longest recorded uptime: "+
                                                 comps[3]+"d "+comps[2]+"h "+comps[1]+"m "+comps[0]+"s.", cmd.getChannel());
            }
        }else if(cmd.getCommand().equals("google")){
            if(cmd.getArgs() == null){
                cmd.getStream().send("Please specify a search term.", cmd.getChannel());
                return;
            }
            try{
                String url = getRedirectURL(new URL(config.googleUrl+cmd.getArgs()));
                if(url == null){
                    cmd.getStream().send("Failed to perform search: Internal error.", cmd.getChannel());
                }else{
                    cmd.getStream().send(url, cmd.getChannel());
                }
            }catch(MalformedURLException ex){
                Commons.log.log(Level.WARNING, toString()+" URL Error.", ex);
                cmd.getStream().send("Failed to perform search: Internal error.", cmd.getChannel());
            }
        }
    }

    public void onMessage(MessageEvent evt){
        
    }
    
    public String getRedirectURL(URL url){
        try{
            HttpURLConnection con = (HttpURLConnection)url.openConnection();
            con.setRequestProperty("User-Agent", config.userAgent);
            con.setInstanceFollowRedirects(false);
            if(con.getResponseCode() < 300 || con.getResponseCode() >= 400){
                Commons.log.warning(toString()+" failed to retrieve redirect from '"+url+"': Error code "+con.getResponseCode());
            }
            return con.getHeaderField("Location");
        }catch(IOException ex){
            Commons.log.log(Level.WARNING, toString()+" failed to open URL '"+url+"'.", ex);
            return null;
        }
    }
    
    public int[] toTimeComponents(long timestamp){
        int[] comps = new int[4];
        comps[3] = (int)(TimeUnit.MILLISECONDS.toDays(timestamp));
        comps[2] = (int)(TimeUnit.MILLISECONDS.toHours(timestamp) - comps[3]*24);
        comps[1] = (int)(TimeUnit.MILLISECONDS.toMinutes(timestamp) - TimeUnit.MILLISECONDS.toHours(timestamp)*60);
        comps[0] = (int)(TimeUnit.MILLISECONDS.toSeconds(timestamp) - TimeUnit.MILLISECONDS.toMinutes(timestamp)*60);
        return comps;
    }
}
