package org.tymoonnext.bot.module.cmd;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.tymoonnext.bot.event.CommandListener;

/**
 * 
 * @author Shinmera
 * @license GPLv3
 * @version 0.0.0
 */
public class Command{
    private static final Pattern KEYWORD = Pattern.compile("^[a-zA-Z0-9_-]+=");
    private static final Pattern DEFINITION = Pattern.compile("^([a-zA-Z0-9-_]*)(\\[.*\\])?(\\{.*\\})?(\\([A-Z_]+\\))?");
    
    private String name;
    private String description;
    private ArrayList<Argument> chain;
    
    public Command(String name){this(name, null, null);}
    public Command(String name, String[] args){this(name, args, null);}
    public Command(String name, String desc){this(name, null, desc);}
    public Command(String name, String[] args, String desc){
        if(args!=null){
            for(String arg: args){
                add(arg);
            }
        }
        this.name=name;
        this.description=desc;
    }
    
    /**
     * Add a new argument using the string notation:
     *   Named argument:    ARGSNAME
     *   Optional argument: ARGSNAME[]
     *   Dafulted argument: [DEFAULT]
     *   Choice argument:   {CHOICE1|CHOICE2|..}
     *   Checked argument:  (CHECKER)
     *   All combined:      ARGSNAME[DEFAULT]{CHOICE1|CHOICE2|..}(CHECKER)
     * Example:
     *   channel[#opers]{#opers|#help|#bots}(CHANNEL)
     * If no name is given, the name is automatically chosen by position in the
     * argslist.
     * 
     * @param arg 
     */
    private void add(String arg){
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

        if((name==null) || (name.isEmpty()))name = "ARGS"+chain.size();

        chain.add(new Argument(name, defval, choices, checker));
    }
    
    /**
     * Parses a given argument string into the keys and values and pass them to
     * the matching Argument containers. The syntax here is as follows:
     * 
     *   value value2 key=valueX value3="An \" encapsed string"
     * 
     * Keyword arguments are never positional. This means that they do not take
     * up space in the positional order and as such can appear anywhere in no
     * particular order. The example below is equivalent to the one above:
     * 
     *   key=valueX value value3="An \" enxapsed string" value2
     * 
     * @param argscall
     * @throws ParseException 
     */
    public void parse(String argscall) throws ParseException{
        argscall = argscall.trim();
        
        LinkedList<String> args = new LinkedList<String>();
        HashMap<String, String> kwargs = new HashMap<String, String>();
        
        if(!argscall.isEmpty()){
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
        }
        
        for(Argument arg : chain){
            arg.parse(args, kwargs);
        }
    }
    
    /**
     * Finds the index of the next, non-escaped character in a string.
     * @param s The string to search in.
     * @param c The character to search for.
     * @param start An offset in the string.
     * @return The positon of the next match or -1 if not found.
     */
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
    
    /**
     * Returns a neatly formatted representation of this command
     * @return 
     */
    public String toDescriptiveString(){
        StringBuilder s = new StringBuilder();
        s.append(name);
        for(Argument arg : chain){
            s.append(" ").append(arg.toDescriptiveString());
        }
        return s.toString();
    }
    
    public String getName(){return name;}
    public String getDescription(){return description;}
    public Argument[] getArguments(){return chain.toArray(new Argument[chain.size()]);}
    public String toString(){
        return "{"+this.getClass().getSimpleName()+"|"+name+"}";
    }
}
