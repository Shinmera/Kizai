package org.tymoonnext.bot.module.lookup;

import NexT.util.StringUtils;
import java.util.HashMap;
import java.util.logging.Level;
import org.tymoonnext.bot.Commons;
import org.tymoonnext.bot.Kizai;
import org.tymoonnext.bot.event.CommandListener;
import org.tymoonnext.bot.event.EventListener;
import org.tymoonnext.bot.event.cmd.CommandInstanceEvent;
import org.tymoonnext.bot.event.core.CommandEvent;
import org.tymoonnext.bot.meta.Info;
import org.tymoonnext.bot.module.Module;
import org.tymoonnext.bot.module.cmd.CommandInstance;
import org.tymoonnext.bot.module.cmd.ParseException;
import org.tymoonnext.bot.module.core.ext.CommandModule;

/**
 * Module that looks up database entries from Wikipedia etc.
 * @author Mithent
 * @license GPLv3
 * @version 0.0.0
 */

@Info("Performs various lookup actions for sites, such as wikipedia.")
public class Lookup extends Module implements CommandListener, EventListener{
    private HashMap<String, LookupProviderInterface> providers = new HashMap<String, LookupProviderInterface>();
    
    public Lookup(Kizai bot){
        super(bot);
        CommandModule.register(bot, "lookup", "wikipedia", "term".split(" "), "Look up a specified word on Wikipedia.", this);

        //I'd recommend putting all the providers into a sub-package.
        //You can then use reflection to automatically load all classes in that
        //package. There's an example of this in Core's INIT, but I'll explain
        //it further once you're there.
        providers.put("Wikipedia", new Wikipedia());
    }

    public void shutdown(){
        bot.unbindAllEvents(this);
    }
    
    public void onCommand(CommandEvent cmd){
        String target = cmd.getCommand().split(" ")[1];
        target = StringUtils.firstToUpper(target);
        
        if (providers.containsKey(target)){
            //Original code; Please remove after the adaptation is understood and accepted.
            //cmd.getStream().send(providers.get(target).getDefinition(cmd.getArgs()), cmd.getCommand());
            
            try{
                String response = providers.get(target).getDefinition(cmd.getArgs());
                cmd.getStream().send(response, cmd.getChannel());
                
            }catch(ConnectionException ex){
                Commons.log.log(Level.INFO, toString()+" Failed to perform lookup on '"+cmd.getArgs()+"' ("+target+")", ex);
                cmd.getStream().send("Failed to perform lookup: "+ex.getMessage(), cmd.getChannel());
            }catch(ParseException ex){
                Commons.log.log(Level.INFO, toString()+" Error parsing returned output for '"+cmd.getArgs()+"' ("+target+")", ex);
                cmd.getStream().send("Error parsing returned output: "+ex.getMessage(), cmd.getChannel());
            }
        }else{
            this.invoke("on"+target, cmd);
        }
    }
    
}