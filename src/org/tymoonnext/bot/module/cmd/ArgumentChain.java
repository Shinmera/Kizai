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
 * KWARGS are never positional. They are not inside the positional sequence.
 * As such, the following two usages are recognized as the same:
 *   CMD ARGS1 ARGS3=VAL ARGS2
 *   CMD ARGS1 ARGS2 ARGS3=VAL
 * 
 * Defining an arguments list by the constructor can be done in the following
 * syntax:
 *   Named argument:    ARGSNAME
 *   Optional argument: ARGSNAME[]
 *   Dafulted argument: ARGSNAME[VALUE]
 *   Choice argument:   {CHOICE1|CHOICE2|..}
 *   Checked argument:  (INTEGER)
 *   All combined:      ARGSNAME[CHOICE1]{CHOICE1|CHOICE2|..}(INTEGER)
 * None of the names or values can contain {,},|,[ or ] names should match
 * [a-zA-Z0-9_-]. All values and names are automatically trimmed.
 */
package org.tymoonnext.bot.module.cmd;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 
 * @author Shinmera
 * @license GPLv3
 * @version 0.0.0
 */
public class ArgumentChain extends ArrayList<Argument>{
    private static final Pattern KEYWORD = Pattern.compile("^[a-zA-Z0-9_-]+=");
    private static final Pattern DEFINITION = Pattern.compile("^([a-zA-Z0-9-_]*)(\\[.*\\])?(\\{.*\\})?(\\([A-Z_]+\\))?");
    
    public static void main(String[] nobodycares) throws ParseException{
        String[] args = {"FIRST", "{ONE|TWO|THREE}", "THIRD[]", "FOURTH[4th]", "FIFTH[0](NUMERIC)"};
        ArgumentChain chain = new ArgumentChain(args);
        System.out.println(chain);
        chain.parse("LOL TWO FIFTH=12.1");
    }
    
    public ArgumentChain(){}
    public ArgumentChain(String[] args){
        int i=1;
        for(String arg : args){
            String name = null;
            String defval = null;
            String[] choices = null;
            String check = "ANY";
            ArgumentChecker checker;
            
            Matcher m = DEFINITION.matcher(arg);
            if(!m.find()) throw new IllegalArgumentException("Input '"+arg+"' is not a valid definition!");
            
            String[] groups = {m.group(0), m.group(1), m.group(2), m.group(3)};
            for(String grp : groups){
                if((grp != null) && (!grp.isEmpty())){
                    switch(grp.charAt(0)){
                        case '[':defval=grp.substring(1,grp.length()-1);break;
                        case '{':choices=grp.substring(1,grp.length()-1).split("\\|");break;
                        case '(':check=grp.substring(1,grp.length()-1);break;
                        default:name=grp;break;
                    }
                }
            }
            
            if(check.equals("ANY"))         checker = ArgumentChecker.ANY;
            else if(check.equals("NONE"))   checker = ArgumentChecker.NONE;
            else if(check.equals("INTEGER"))checker = ArgumentChecker.INTEGER;
            else if(check.equals("NUMERIC"))checker = ArgumentChecker.NUMERIC;
            else if(check.equals("ALPHA"))  checker = ArgumentChecker.ALPHA;
            else throw new IllegalArgumentException("Checker '"+check+"' not knoqn.");
            
            if((name==null) || (name.isEmpty()))name = "ARGS"+i;
            
            add(new Argument(name, defval, choices, checker));
            i++;
        }
    }
    
    public void parse(String argscall) throws ParseException{
        argscall = argscall.trim();
        if(argscall.isEmpty())return;
        
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