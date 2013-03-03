package org.tymoonnext.bot.module.lookup;

import NexT.util.StringUtils;
import java.io.IOException;
import java.util.Hashtable;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.tymoonnext.bot.Commons;
import org.tymoonnext.bot.Kizai;
import org.tymoonnext.bot.event.CommandListener;
import org.tymoonnext.bot.event.EventListener;
import org.tymoonnext.bot.event.IRCBot.MessageEvent;
import org.tymoonnext.bot.event.cmd.CommandInstanceEvent;
import org.tymoonnext.bot.event.core.CommandEvent;
import org.tymoonnext.bot.module.Module;
import org.tymoonnext.bot.module.core.ext.CommandModule;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

/**
 * Module that looks up database entries from Wikipedia
 * @author Mithent
 * @license GPLv3
 * @version 0.0.0
 */
public class Lookup extends Module implements CommandListener, EventListener{
    private Hashtable<String, LookupProviderInterface> providers = new Hashtable<String, LookupProviderInterface>();
    
    public Lookup(Kizai bot)
    {
        super(bot);
        CommandModule.register(bot, "lookup", "wikipedia", "term".split(" "), "Look up a specified word on Wikipedia.", this);

        providers.put("Wikipedia", new Wikipedia());
    }

    public void shutdown()
    {
        bot.unbindAllEvents(this);
    }
    
    public void onCommand(CommandEvent cmd)
    {
        String[] cmdParts = cmd.getCommand().split(" ");
        String target = StringUtils.firstToUpper(cmdParts[1]);
        
        if (providers.containsKey(target))
        {
            cmd.getStream().send(providers.get(target).getDefinition(cmd.getArgs()), cmd.getCommand());
        }
        else
        {
            this.invoke("on"+target, cmd);
        }
    }

    public void onWikipedia(CommandInstanceEvent evt)
    {
        String searchTerm = evt.get().getValue("term");
        
    }
    
}
