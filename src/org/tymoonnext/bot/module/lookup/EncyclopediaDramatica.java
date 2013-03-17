package org.tymoonnext.bot.module.lookup;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.tymoonnext.bot.module.cmd.ParseException;
import org.tymoonnext.bot.module.lookup.json.InvalidJSONException;
import org.tymoonnext.bot.module.lookup.json.JSONData;

/**
 * Looks up definitions from Encyclopedia Dramatica
 * @author Mithent
 */
public class EncyclopediaDramatica extends MediaWiki {
    private static final Pattern linkRegex = Pattern.compile("<a.*?>(.*)</a>");
    
    public EncyclopediaDramatica(){
        super("https://encyclopediadramatica.se/api.php");
    }
    
    @Override
    protected String getDefinitionForTerm(String page) throws ConnectionException, NoMatchException, ParseException{
        String text = super.getDefinitionForTerm(page);
        
        if (text.contains("REDIRECT")){
            return handleRedirect(text, page);
        }
        
        return getDescription(text, page);
    }

    @Override
    String getFirstSearchMatch(String term) throws ConnectionException, NoMatchException{
        String searchResults = readFromURL(apiURL + "?action=opensearch&limit=1&namespace=0&search=" + term);
        
        try {
            JSONData response = new JSONData(searchResults);
            JSONData firstMatchArray = response.getIndex(1);
            if (firstMatchArray == null) throw new NoMatchException("Couldn't find a match for " + term);
            JSONData firstMatch = firstMatchArray.getIndex(0);
            if (firstMatch == null) throw new NoMatchException("Couldn't find a match for " + term);
            Object firstMatchContent = firstMatch.getContent();
            if (firstMatchContent == null || !(firstMatchContent instanceof String)) throw new InvalidJSONException("Reply was not a string");
            return (String) firstMatchContent;
        }
        catch (InvalidJSONException ex)
        {
            throw new NoMatchException(ex);
        }
    }
    
    private String handleRedirect(String text, String page) throws ConnectionException, NoMatchException, ParseException{
        
        Matcher linkRegexMatcher = linkRegex.matcher(text);
        if (linkRegexMatcher.find()){
            String redirectTo = linkRegexMatcher.group(1).replace(' ', '_');
            return getDefinitionForTerm(redirectTo);
        }else{
            throw new ParseException("The page " + page + " seems to redirect somewhere else, but I couldn't find where.");
        }
    }

    private String getDescription(String text, String page) throws NoMatchException, ParseException{
        text = text.replace("\n", "").replace("\r", "");

        String description = null;
        int startAt = 0;
        
        do{
            int nextParagraphStarts = text.indexOf("<p>", startAt);
            int nextParagraphEnds = text.indexOf("</p>", startAt);
            
            if (nextParagraphStarts < 0 || nextParagraphEnds < 0) break;
            
            String nextParagraph = text.substring(nextParagraphStarts + 3, nextParagraphEnds);
            description = getPlaintext(nextParagraph);
            
            startAt = nextParagraphEnds + 4;
        }while (description.length() < page.length());
        
        if (description != null){
            return page + ": " + description;
        }else{
            throw new ParseException("I couldn't find a good description for " + page + ".");
        }
        
    }
    
    private String getPlaintext(String text){   
        text = removeSuperscriptText(text);
        text = removeErrors(text);
        return stripTags(text);
    }
    
    private String removeSuperscriptText(String html){
        return html.replaceAll("<sup.*?>.*?</sup>", "");
    }
    
    private String removeErrors(String html){
        return html.replaceAll("<strong class=\"error\">.*?</strong>", "");
    }
    
    private String readFromURL(String fromUrl) throws ConnectionException {
        BufferedReader reader = null;
        
        try {
            URL url = new URL(fromUrl);
            reader = new BufferedReader(new InputStreamReader(url.openStream()));

            String response = "";
            String line;
            while ((line = reader.readLine()) != null) {
                response = response.concat(line);
            }
            
            return response;
        } catch (Exception ex) {
            throw new ConnectionException(ex);
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            }
            catch (Exception ex) {
                throw new ConnectionException(ex);
            }
        }
    }
}
