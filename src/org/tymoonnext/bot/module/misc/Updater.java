package org.tymoonnext.bot.module.misc;

import NexT.data.ConfigLoader;
import NexT.data.required;
import NexT.util.Toolkit;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import org.tymoonnext.bot.Commons;
import org.tymoonnext.bot.Kizai;
import org.tymoonnext.bot.event.CommandListener;
import org.tymoonnext.bot.event.cmd.CommandInstanceEvent;
import org.tymoonnext.bot.event.core.CommandEvent;
import org.tymoonnext.bot.module.Module;
import org.tymoonnext.bot.module.cmd.CommandInstance;
import org.tymoonnext.bot.module.core.ext.CommandModule;

/**
 * 
 * @author Shinmera
 * @license GPLv3
 * @version 0.0.0
 */
public class Updater extends Module implements CommandListener{
    
    protected class C extends ConfigLoader{
        @required public String url;
        @required public String cmd;
    };
    protected Updater.C conf = new Updater.C();

    public Updater(Kizai bot){
        super(bot);
        conf.load(bot.getConfig().get("modules").get("misc.Updater"));
        CommandModule.register(bot, "update", "url[] cmd[]".split(" "), "Attempts to update and reboot Kizai.", this);
    }
    
    @Override
    public void shutdown() {}

    @Override
    public void onCommand(CommandEvent cmd) {
        CommandInstance i = ((CommandInstanceEvent)cmd).get();
        try{        
            final String    start   = (i.getValue("cmd").isEmpty()) ? conf.cmd                      : i.getValue("cmd");            
            final File      file    = new File(Commons.f_BASEDIR, "Kizai.jar");
            
            if(!i.getValue("url").equalsIgnoreCase("none")){
                final URL       url     = (i.getValue("url").isEmpty()) ? new URL("http://"+conf.url)   : new URL(i.getValue("url")); 

                Commons.log.info("[Updater] Downloading new JAR from "+url+" to "+file.getAbsolutePath()+"...");
                cmd.getStream().send("Downloading update...", cmd.getChannel());
                boolean success = Toolkit.downloadFile(url, file);
                
                if(!success){
                    Commons.log.info("[Updater] Download failed!");
                    cmd.getStream().send("Download failed!", cmd.getChannel());
                    return;
                }
            }
            
            Commons.log.info("[Updater] Shutting down everything else...");
            cmd.getStream().send("Shutting down all modules and restarting...", cmd.getChannel());
            bot.shutdown(false);

            Commons.log.info("[Updater] Restarting...");
            Runtime.getRuntime().addShutdownHook(new Thread() {
                public void run() {
                    try{Runtime.getRuntime().exec(start);}
                    catch(IOException e){e.printStackTrace();}
                }
            });
            System.exit(0);
            
        }catch(MalformedURLException ex){
            Commons.log.log(Level.WARNING, "[Updater] URL forming failed.", ex);
            cmd.getStream().send("Failed to create url: "+ex.getMessage(), cmd.getChannel());
        }
    }

}
