package org.tymoonnext.bot.module.lookup;

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
import org.tymoonnext.bot.module.core.ext.CommandModule;

/**
 * Module that looks up database entries from Wikipedia
 * @author Mithent
 * @license GPLv3
 * @version 0.0.0
 */

//This is an annotation used by the command system to show helpful information
//on a command. It is entirely optional, but nice to have anyway.
@Info("Performs various lookup actions for sites, such as wikipedia.")
public class Lookup extends Module implements CommandListener, EventListener{
    //Do not use Hashtables, use HashMaps instead.
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
        //Original code; Please remove after the adaptation is understood and accepted.
        //String[] cmdParts = cmd.getCommand().split(" ");
        //String target = StringUtils.firstToUpper(cmdParts[1]);
        
        //This allows you to retrieve the command's arguments more elegantly.
        CommandInstance i = ((CommandInstanceEvent)cmd).get();
        //Example usage: i.getValue("myarg")
        
        //The command group changes the command to be a combination of the
        //group and the sub-command, effectively making the command be:
        //"lookup wikipedia"
        String target = cmd.getCommand().split(" ")[1];
        
        if (providers.containsKey(target)){
            //Original code; Please remove after the adaptation is understood and accepted.
            //cmd.getStream().send(providers.get(target).getDefinition(cmd.getArgs()), cmd.getCommand());
            
            try{
                //I'm sure that was just a minor typo, but always respond to the commands channel.
                String response = providers.get(target).getDefinition(cmd.getArgs());
                cmd.getStream().send(response, cmd.getChannel());
                
            }catch(ConnectionException ex){
                //I'm using the INFO level here instead of WARNING (which would
                //be the default for exception logging) because this error is
                //of very, very minor concern to the bot operator and is most
                //likely either a temporal issue or a user fault.
                Commons.log.log(Level.INFO, toString()+" Failed to perform lookup on '"+cmd.getArgs()+"' ("+target+")", ex);
                cmd.getStream().send("Failed to perform lookup: "+ex.getMessage(), cmd.getChannel());
            }
        }else{
            this.invoke("on"+target, cmd);
        }
    }

    public void onWikipedia(CommandInstanceEvent evt){
        String searchTerm = evt.get().getValue("term");
        
    }
    
}

/**
 * GENERAL NOTES:
 * 
 * Please try to get used to using Java-Style braces. Which is to say, opening 
 * braces to not get their own lines. This is the style that is used throughout
 * most of Java projects and libraries and I'd like to keep it somewhat
 * consistent.
 * I've adapted the files accordingly. If you cannot live with this kind of
 * style, so be it and simply revert the changes in git.
 * 
 * The class structure seems alright so far, maybe rethink your use of
 * Exceptions though, as I've noted in the other files and places. I hope you
 * don't find my meddling too intrusive and instead take this as an opportunity
 * to learn. I'm sure most of this is just a matter of getting used to "The Java
 * Way" (or whatever pretentious thing you want to call it), so you'll be fine
 * in time.
 */