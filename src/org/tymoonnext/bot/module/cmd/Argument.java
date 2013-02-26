package org.tymoonnext.bot.module.cmd;

import NexT.util.StringUtils;
import java.util.HashMap;
import java.util.LinkedList;

/**
 * 
 * @author Shinmera
 * @license GPLv3
 * @version 0.0.0
 */
public class Argument {
    private String name;
    private String[] choices;
    private ArgumentChecker check;
    private String defval;
    private String value;
    
    public Argument(String name){                                                            this(name, false,                   null,       null);}
    public Argument(String name, String[] choices){                                          this(name, false,                   choices,    null);}
    public Argument(String name, ArgumentChecker check){                                     this(name, false,                   null,       check);}
    public Argument(String name, boolean optional){                                          this(name, (optional)? "" : null,   null,       null);}
    public Argument(String name, boolean optional, ArgumentChecker check){                   this(name, (optional)? "" : null,   null,       check);}
    public Argument(String name, boolean optional, String[] choices){                        this(name, (optional)? "" : null,   choices,    null);}
    public Argument(String name, boolean optional, String[] choices, ArgumentChecker check){ this(name, (optional)? "" : null,   choices,    check);}
    public Argument(String name, String defval){                                             this(name, defval,                  null,       null);}
    public Argument(String name, String defval, ArgumentChecker check){                      this(name, defval,                  null,       check);}
    public Argument(String name, String defval, String[] choices){                           this(name, defval,                  choices,    null);}
    public Argument(String name, String defval, String[] choices, ArgumentChecker check){
        this.name=name;
        this.defval=defval;
        this.choices=choices;
        this.check=(check==null)? ArgumentChecker.ANY : check;
    }
    
    public void parse(LinkedList<String> args, HashMap<String,String> kwargs) throws ParseException{
        value = defval;
        
        //Empty check
        if(args.isEmpty() && kwargs.isEmpty()){
            if(value != null)return;
            else throw new ParseException("Argument '"+name+"' not satisfied.");
        }
        
        //Search for keyword. If found and not empty, swallow keyword.
        for(String key : kwargs.keySet()){
            if(key.equals(name)){
                if(!kwargs.get(key).isEmpty()){
                    value = kwargs.get(key);
                    args.remove(key);
                }else if(value == null)
                    throw new ParseException("Argument '"+name+"' not satisfied.");
            }
        }
        
        //No keyword found, simply swallow next positional argument.
        if(((value == null) || value.equals(defval)) && args.size() > 0){
            value = args.pop();
        }
        
        //No keyword args found, no positional args and no default. Fuck.
        if(value == null){
            throw new ParseException("Argument '"+name+"' not satisfied.");
        }
        
        //Check if contained in choices.
        if(choices != null){
            boolean contained=false;
            for(String choice : choices){
                if(value.equals(choice)){
                    contained=true;
                    break;
                }
            }
            if(!contained) throw new ParseException("Value '"+value+"' is not applicable for '"+name+"'.");
        }
        
        //Perform validity check.
        if(!check.isValid(value)) 
            throw new ParseException("Value '"+value+"' is not applicable for '"+name+"'.");
    }
    
    public String getName(){return name;}
    public boolean isOptional(){return (defval != null);}
    public String getDefault(){return defval;}
    public boolean hasChoices(){return choices != null;}
    public String[] getChoices(){return choices;}
    public ArgumentChecker getChecker(){return check;}
    public String getValue(){return value;}
    
    public String toString(){return value;}
    
    public String toDescriptiveString(){
        String str = "";
        if(choices != null){
            str = "{"+StringUtils.implode(choices, "|")+"}";
        }else{
            str = name;
        }
        if((defval != null) && (!defval.isEmpty())){
            str+= ":"+defval;
        }
        if(isOptional())return "["+str+"]";
        else            return str;
    }
}