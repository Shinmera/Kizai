package org.tymoonnext.bot.module.lookup;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.tymoonnext.bot.module.cmd.ParseException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * The base implementation for looking up definitions using the MediaWiki API.
 * @author Mithent
 */
abstract public class MediaWiki implements LookupProviderInterface {
    String apiURL;
    
    public MediaWiki(String apiURL){
        this.apiURL=apiURL;
    }
    
    @Override
    public String getDefinition(String term) throws ConnectionException, ParseException{
        try{
            String closestMatch = getFirstSearchMatch(term);
            return getDefinitionForTerm(closestMatch);
        }
        catch (NoMatchException e){
            throw new ParseException("No matching term could be found.");
        }
    }
    
    String getFirstSearchMatch(String term) throws ConnectionException, NoMatchException{
        return getTextFromAPIResponse(apiURL + "?action=opensearch&limit=1&namespace=0&format=xml&search=" + term, "Text");
    }
    
    String getDefinitionForTerm(String page) throws ConnectionException, NoMatchException, ParseException{
        return getTextFromAPIResponse(apiURL + "?action=parse&format=xml&prop=text&section=0&page=" + page, "text");
    }
    
    String getTextFromAPIResponse(String url, String tagName) throws ConnectionException, NoMatchException{
        Document searchResults = getXML(url);
        Node resultNode = searchResults.getElementsByTagName(tagName).item(0);
        
        if (resultNode == null){
            throw new NoMatchException("'"+tagName+"' could not be found in '"+url+"'");
        }else{
            return resultNode.getTextContent();
        }
    }
    
    Document getXML(String uri) throws ConnectionException{
        try{
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder documentBuilder = dbFactory.newDocumentBuilder();
            return documentBuilder.parse(uri);
        }
        catch (Exception e){
            throw new ConnectionException(e);
        }
    }
    
    String stripTags(String html){
        return html.replaceAll("\\<.*?\\>", "");
    }
}
