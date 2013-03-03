package org.tymoonnext.bot.module.lookup;

import java.io.IOException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

/**
 * The base implementation for looking up definitions using the MediaWiki API.
 * @author Mithent
 */
abstract public class MediaWiki implements LookupProviderInterface {
    protected String apiURL;
    
    public String getDefinition(String term)
    {
        try
        {
            String closestMatch = getFirstSearchMatch(term);
            return getDefinitionForTerm(closestMatch);
        }
        catch (ConnectionException e)
        {
            return "Could not connect to server (" + e.getMessage() + ")";
        }
        catch (NoMatchException e)
        {
            return "No matching term could be found.";
        }
    }
    
    protected String getFirstSearchMatch(String term) throws ConnectionException, NoMatchException
    {
        return getTextFromAPIResponse(apiURL + "?action=opensearch&limit=1&namespace=0&format=xml&search=" + term, "Text");
    }
    
    protected String getDefinitionForTerm(String page) throws ConnectionException, NoMatchException
    {
        return getTextFromAPIResponse(apiURL + "?action=parse&format=xml&prop=text&section=0&page=" + page, "text");
    }
    
    protected String getTextFromAPIResponse(String url, String tagName) throws ConnectionException, NoMatchException
    {
        Document searchResults = getXML(url);
        Node resultNode = searchResults.getElementsByTagName(tagName).item(0);
        
        if (resultNode == null)
        {
            throw new NoMatchException();
        }
        else
        {
            return resultNode.getTextContent();
        }
    }
    
    protected String replaceSpacesWithUnderscores(String input)
    {
        return input.replace(' ', '_');
    }
    
    protected Document getXML(String uri) throws ConnectionException
    {
        try
        {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder documentBuilder = dbFactory.newDocumentBuilder();
            return documentBuilder.parse(uri);
        }
        catch (Exception e)
        {
            throw new ConnectionException(e);
        }
    }
    
    protected String stripTags(String html)
    {
        return html.replaceAll("\\<.*?\\>", "");
    }
}
