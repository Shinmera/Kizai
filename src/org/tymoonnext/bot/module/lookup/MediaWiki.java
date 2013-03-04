package org.tymoonnext.bot.module.lookup;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * The base implementation for looking up definitions using the MediaWiki API.
 * @author Mithent
 */
abstract public class MediaWiki implements LookupProviderInterface {
    private String apiURL;
    
    //This is a mandatory thing. Enforce it through the constructor!
    public MediaWiki(String apiURL){
        this.apiURL=apiURL;
    }
    
    public String getDefinition(String term) throws ConnectionException{
        //Never, EVER return errors as strings. Even if throwing and recatching
        //Exceptions is a pain in the ass, evaluating stuff without a stack
        //stack trace is a billion times more so.
        //
        //Note my usage of IllegalArgumentException here. This is one of the few
        //unchecked exceptions in Java. I'm using it here to do a quick and
        //dirty job. It would be better if this function generally threw a
        //ParseException or something equally ambiguous that is appropriate for
        //the situation. I will leave this up to you though.
        try{
            String closestMatch = getFirstSearchMatch(term);
            return getDefinitionForTerm(closestMatch);
        }
        catch (NoMatchException e){
            throw new IllegalArgumentException("No matching term could be found.");
        }
    }
    
    protected String getFirstSearchMatch(String term) throws ConnectionException, NoMatchException{
        return getTextFromAPIResponse(apiURL + "?action=opensearch&limit=1&namespace=0&format=xml&search=" + term, "Text");
    }
    
    protected String getDefinitionForTerm(String page) throws ConnectionException, NoMatchException{
        return getTextFromAPIResponse(apiURL + "?action=parse&format=xml&prop=text&section=0&page=" + page, "text");
    }
    
    protected String getTextFromAPIResponse(String url, String tagName) throws ConnectionException, NoMatchException{
        Document searchResults = getXML(url);
        Node resultNode = searchResults.getElementsByTagName(tagName).item(0);
        
        if (resultNode == null){
            throw new NoMatchException("'"+tagName+"' could not be found in '"+url+"'");
        }else{
            return resultNode.getTextContent();
        }
    }
    
    //Generally the profit gained from the descriptor of a function is not
    //worth the extra writing in such a simple case. It would be better to
    //simply use the replace in-place.
    //
    //protected String replaceSpacesWithUnderscores(String input){
    //    return input.replace(' ', '_');
    //}
    
    protected Document getXML(String uri) throws ConnectionException{
        try{
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder documentBuilder = dbFactory.newDocumentBuilder();
            return documentBuilder.parse(uri);
        }
        catch (Exception e){
            throw new ConnectionException(e);
        }
    }
    
    protected String stripTags(String html){
        return html.replaceAll("\\<.*?\\>", "");
    }
}
