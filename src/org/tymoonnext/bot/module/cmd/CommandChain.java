/**
 * Sample argument structure:
 * CMD ARGS1 {ARGS2-1|ARGS2-2} [ARGS3] ARGS** ARGS4=XXX KWARGS**
 *      |         |               |      |        |        |
 *      +- Required, positional, any     |        |        |
 *                |               |      |        |        |
 *                +- Required, positional, choice |        |
 *                                |      |        |        |
 *                                +- Optional, positional, any
 *                                       |        |        |
 *                                       +- Optional, positional, any
 *                                                |        |
 *                                                +- Required, keyword, any 
 *                                                         |
 *                                                         +- Optional, keyword, any
 */
package org.tymoonnext.bot.module.cmd;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.regex.Pattern;

/**
 * 
 * @author Shinmera
 * @license GPLv3
 * @version 0.0.0
 */
public class CommandChain extends ArrayList<Argument>{
    private static final Pattern KEYWORD = Pattern.compile("^[a-zA-Z0-9_-]+=");
    
    public CommandChain(){
    }
    
    public void parse(String argscall) throws ParseException{
        LinkedList<String> args = new LinkedList<String>();
        HashMap<String, String> kwargs = new HashMap<String, String>();
        
        int pointer=0, nextStringStart=0, nextStringEnd=0, nextSpace=0;
        String token, key, val;
        while(nextSpace >= 0){
            
            //Find next token end point
            nextStringStart = indexOfNextUnescapedChar(argscall, '"', pointer);            
            if(nextStringStart < nextSpace && nextStringStart != -1){
                nextStringEnd = indexOfNextUnescapedChar(argscall, '"', nextStringStart+1);
                if(nextStringEnd == -1)
                    throw new ParseException("Expected \" but reached end of string.");
                nextSpace = argscall.indexOf(' ', nextStringEnd+1);
            }else{
                nextSpace = argscall.indexOf(' ', pointer);
            }
            
            //Extract token
            if(nextSpace == -1)token = argscall.substring(pointer);
            else               token = argscall.substring(pointer, nextSpace);            
            token = token.trim();
            
            //See if it's a keyword match or not
            if(KEYWORD.matcher(token).find()){
                key = token.substring(0, token.indexOf('='));
                val = token.substring(token.indexOf('=')+1);
            }else{
                key = null;
                val = token;
            }
            
            //Remove string indicators
            if(val.startsWith("\"") && val.endsWith("\"")){
                val = val.substring(1, val.length()-1);
            }
            
            //Save to queue or dict
            if(key == null) args.add(val);
            else            kwargs.put(key, val);
            
            //Advance pointer
            pointer = nextSpace+1;
        }
        
        for(Argument arg : this){
            arg.parse(args, kwargs);
        }
    }
    
    private int indexOfNextUnescapedChar(String s, char c, int start){
        int pos = 0;
        while(true){
            pos = s.indexOf(c, start+pos);
            if(pos == -1)break;
            if(s.charAt(pos-1) != '\\')break;
            else pos++;
        }
        return pos;
    }
    
    public String toString(){
        StringBuilder s = new StringBuilder();
        
        for(Argument arg : this){
            s.append(arg.toDescriptiveString()).append(" ");
        }
        return s.toString();
    }
}