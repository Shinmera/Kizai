package org.tymoonnext.bot.module.lookup;

import NexT.util.StringUtils;
import java.io.IOException;
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

    public Lookup(Kizai bot){
        super(bot);
        CommandModule.register(bot, "lookup", "wikipedia", "term".split(" "), "Look up a specified word on Wikipedia.", this);
    }

    public void shutdown(){
        bot.unbindAllEvents(this);
    }
    
    public void onCommand(CommandEvent cmd) {
        this.invoke("on"+StringUtils.firstToUpper(cmd.getCommand().split(" ")[1]), cmd);
    }

    public void onWikipedia(CommandInstanceEvent evt){
        String searchTerm = evt.get().getValue("term");
        try
        {
            Document wikipediaResponse = getXML("http://en.wikipedia.org/w/api.php?action=parse&page=" + searchTerm + "&format=xml&prop=text&section=0");

            Node textNode = wikipediaResponse.getElementsByTagName("text").item(0);

            String text = textNode.getTextContent();
            text = text.replace("\n", "").replace("\r", "");

            String firstParagraph = text.substring(text.indexOf("<p>") + 3, text.indexOf("</p>"));
            firstParagraph = firstParagraph.replaceAll("<sup.*?>.*?</sup>", "").replaceAll("\\<.*?\\>", "");

            evt.getStream().send(firstParagraph, evt.getChannel());
        }
        catch (Exception e)
        {
            evt.getStream().send(e.getMessage(), evt.getChannel());
        }
    }
    
    private Document getXML(String uri) throws ParserConfigurationException, SAXException, IOException
    {
        try
        {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder documentBuilder = dbFactory.newDocumentBuilder();
            return documentBuilder.parse(uri);
        }
        catch(ParserConfigurationException e)
        {
            throw e;
        }
        catch(SAXException e)
        {
            throw e;
        }
        catch(IOException e)
        {
            throw e;
        }
        
    }
}
