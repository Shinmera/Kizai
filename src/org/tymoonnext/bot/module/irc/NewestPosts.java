package org.tymoonnext.bot.module.irc;

import com.sun.syndication.feed.synd.SyndEntryImpl;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.io.FeedException;
import com.sun.syndication.io.SyndFeedInput;
import com.sun.syndication.io.XmlReader;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.logging.Level;
import org.tymoonnext.bot.Commons;
import org.tymoonnext.bot.Kizai;
import org.tymoonnext.bot.event.EventListener;
import org.tymoonnext.bot.event.core.ModuleLoadEvent;
import org.tymoonnext.bot.module.TimedModule;

/**
 * Newest posts module that fetches the RSS feed from TyNET/Purplish.
 * @author Shinmera
 * @license GPLv3
 * @version 0.0.0
 * @todo Very static, should be extended for general purpose usage.
 * @todo remove IRC deps.
 */
public class NewestPosts extends TimedModule implements EventListener{
    private String lastTitle = "";
    private IRC irc;
    
    public NewestPosts(Kizai bot){
        super(bot);
        try{bot.bindEvent(ModuleLoadEvent.class, this, "onModuleLoad");}catch(NoSuchMethodException ex){}
    }
    
    public void onModuleLoad(ModuleLoadEvent ev){
        if(IRCBot.class.getName().equals(ev.getModule().getClass().getName())){
            try{
                Method m = ev.getModule().getClass().getMethod("getIRC");
                irc = (IRC)m.invoke(ev.getModule());
                schedule((Integer)bot.getConfig().get("modules").get("irc.NewestPosts").get("interval").get());
            }catch(Exception ex){
                Commons.log.log(Level.WARNING, toString()+" Failed to hook into IRC. Module is useless!", ex);
                bot.unloadModule("irc.NewestPosts");
            }
        }
    }
    
    public void shutdown(){
        super.shutdown();
        bot.unbindAllEvents(this);
    }

    public void run(){
        try {
            SyndFeedInput input = new SyndFeedInput();
            SyndFeed feed = input.build(new XmlReader(new URL("http://api.tymoon.eu/chan/rss")));

            SyndEntryImpl entry = ((SyndEntryImpl)feed.getEntries().get(0));
            if(!lastTitle.equals(entry.getTitle())){
                lastTitle=entry.getTitle();
                irc.sendToAll("[!] New post: "+((entry.getAuthor().trim().isEmpty()) ? "Anonymous" : entry.getAuthor().trim())+
                       ": "+entry.getTitle()+
                       " ( "+entry.getLink()+" )");
            }
        } catch (IOException ex) {
            Commons.log.log(Level.WARNING,toString()+" Failed to load RSS Feed.", ex);
        } catch (IllegalArgumentException ex) {
            Commons.log.log(Level.WARNING,toString()+" Failed to load RSS Feed.", ex);
        } catch (FeedException ex) {
            Commons.log.log(Level.WARNING,toString()+" Failed to load RSS Feed.", ex);
        }
    }
}
