package org.tymoonnext.bot.module.cmd;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.regex.Pattern;

/**
 * 
 * @author Shinmera
 * @license GPLv3
 * @version 0.0.0
 */
public class CommandInstance {
    private static final Pattern KEYWORD = Pattern.compile("^[a-zA-Z0-9_-]+=");
    
    private Command base;
    private String name;
    private String description;
    private Argument[] chain;
    private String[] apargs;
    private HashMap<String, Argument> kchain;
    private HashMap<String, String> akargs;
    
    public CommandInstance(Command base){
        this.base = base;
        this.name = base.getName();
        this.description = base.getDescription();
        this.chain = base.getArguments();
        this.kchain = new HashMap<String, Argument>();
        this.apargs = new String[0];
        this.akargs = new HashMap<String, String>();
        for(Argument arg : chain){
            kchain.put(arg.getName(), arg);
        }
    }
    
    public CommandInstance(Command base, String args) throws ParseException{
        this(base);
        if(args != null)parse(args);
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
                nextSpace = argscall.indexOf(' ', pointer);
                nextStringStart = indexOfNextUnescapedChar(argscall, '"', pointer);
                
                System.out.println(">> "+nextSpace+" > "+nextStringStart);
                
                if(nextStringStart < nextSpace && nextStringStart != -1){
                    nextStringEnd = indexOfNextUnescapedChar(argscall, '"', nextStringStart+1);
                    System.out.println(">>> "+nextStringStart+" > "+nextStringEnd);
                    if(nextStringEnd == -1)
                        throw new ParseException("Expected \" but reached end of string.");
                    nextSpace = argscall.indexOf(' ', nextStringEnd+1);
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
                    val = val.substring(1, val.length()-1).replaceAll("\\\\\"", "\"");
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
        
        apargs = args.toArray(new String[args.size()]);
        akargs.putAll(kwargs);
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
            if(pos <= 0)break;
            if((pos > start) && (s.charAt(pos-1) != '\\'))break;
            else pos++;
        }
        return pos;
    }
    
    public Command getBase(){return base;}
    public String getName(){return name;}
    public String getDescription(){return description;}
    public Argument[] getArgs(){return chain;}
    public String[] getAddPargs(){return apargs;}
    public HashMap<String,String> getAddKargs(){return akargs;}
    
    /**
     * Returns the requested value by key from either the kwargs map. This
     * function returns a value for any key that exists in the parsed command,
     * regardless of whether an argument is defined for it or not.
     * @param name
     * @return 
     */
    public String getValue(String name){
        if(!kchain.containsKey(name))return akargs.get(name);
        else                         return kchain.get(name).toString();
    }
    
    /**
     * Returns the requested value by index from either the args list. This
     * function returns a value for any index that exists in the parsed command,
     * regardless of whether an argument is defined for it or not.
     * @param i
     * @return 
     */
    public String getValue(int i){
        if(i >= chain.length)       return apargs[i-chain.length];
        else                        return chain[i].toString();
    }
    
    /**
     * Returns the number of all arguments given, both positional and keyworded,
     * regardless of whether they have been explicitly defined or given through
     * additional positional/keyword arguments.
     * @return 
     */
    public int size(){return chain.length+apargs.length+akargs.size();}
    public int argsSize(){return chain.length;}
    public int addKargsSize(){return akargs.size();}
    public int addPargsSize(){return apargs.length;}
}
