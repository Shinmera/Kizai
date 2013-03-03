package org.tymoonnext.bot.module.lookup;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Looks up definitions from Wikipedia
 * @author Mithent
 */
public class Wikipedia extends MediaWiki {
    public Wikipedia()
    {
        super.apiURL = "http://en.wikipedia.org/w/api.php";
    }
    
    @Override
    protected String getDefinitionForTerm(String page) throws ConnectionException, NoMatchException
    {
        String text = super.getDefinitionForTerm(page);
        
        if (text.contains("REDIRECT"))
        {
            return handleRedirect(text, page);
        }
        
        return getDescription(text, page);
    }

    private String handleRedirect(String text, String page) throws ConnectionException, NoMatchException{
        Pattern linkRegex = Pattern.compile("<a.*?>(.*)</a>");
        Matcher linkRegexMatcher = linkRegex.matcher(text);
        if (linkRegexMatcher.find())
        {
            String redirectTo = replaceSpacesWithUnderscores(linkRegexMatcher.group(1));
            return getDefinitionForTerm(redirectTo);
        }
        else
        {
            return "The page " + page + " seems to redirect somewhere else, but I couldn't find where.";
        }
    }

    private String getDescription(String text, String page){
        text = text.replace("\n", "").replace("\r", "");

        String description = null;
        int startAt = 0;
        
        do
        {
            int nextParagraphStarts = text.indexOf("<p>", startAt);
            int nextParagraphEnds = text.indexOf("</p>", startAt);
            
            if (nextParagraphStarts < 0 || nextParagraphEnds < 0) break;
            
            String nextParagraph = text.substring(nextParagraphStarts + 3, nextParagraphEnds);
            description = getPlaintext(nextParagraph);
            
            startAt = nextParagraphEnds + 4;
        }
        while (description.length() < page.length());
        
        if (description != null)
        {
            return page + ": " + description;
        }
        else
        {
            return "I couldn't find a good description for " + page + ".";
        }
        
    }
    
    private String getPlaintext(String text)
    {   
        text = removeSuperscriptText(text);
        text = removeErrors(text);
        return stripTags(text);
    }
    
    private String removeSuperscriptText(String html)
    {
        return html.replaceAll("<sup.*?>.*?</sup>", "");
    }
    
    private String removeErrors(String html)
    {
        return html.replaceAll("<strong class=\"error\">.*?</strong>", "");
    }
}
