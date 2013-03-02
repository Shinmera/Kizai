package org.tymoonnext.bot.module.cmd;

import NexT.util.StringUtils;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.tymoonnext.bot.Commons;

/**
 * 
 * @author Shinmera
 * @license GPLv3
 * @version 0.0.0
 */
public class Argument {
    private static final Pattern DEFINITION = Pattern.compile("^([a-zA-Z0-9-_]*)(\\[.*\\])?(\\{.*\\})?(\\([A-Z_]+\\))?");
    
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
        this.value=defval;
        this.choices=choices;
        this.check=(check==null)? ArgumentChecker.ANY : check;
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
     * If no name is given, the name is automatically set to "ARG".
     * 
     * @param arg 
     */
    public static Argument generate(String arg){
        Commons.log.fine("{Argument} Generating '"+arg+"'");
        String name = null;
        String defval = null;
        String[] choices = null;
        String check = "ANY";
        ArgumentChecker checker;

        Matcher m = DEFINITION.matcher(arg);
        if(!m.find()) throw new IllegalArgumentException("Input '"+arg+"' is not a valid definition!");

        String[] groups = {m.group(1), m.group(2), m.group(3), m.group(4)};
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
        else throw new IllegalArgumentException("Checker '"+check+"' not known.");

        if((name==null) || (name.isEmpty()))name = "ARGS";
        return new Argument(name, defval, choices, checker);
    }
    
    /**
     * Attempt to filter out the correct value for this argument. If any of the
     * checks fail (requirement, validity), a ParseException is thrown.
     * 
     * @param args A list of positional arguments. If this argument is retrieved
     * from the list, the element is removed from the list.
     * @param kwargs A map of keyword arguments. If this argument is retrieved
     * from the map, the element is removed from the map.
     * @throws ParseException 
     */
    public void parse(LinkedList<String> args, HashMap<String,String> kwargs) throws ParseException{
        Commons.log.fine("{Argument|"+name+"} Parsing ARGS: "+StringUtils.implode(args.toArray(), ", ")+"  KWARGS: "+StringUtils.implode(kwargs, ": ",", "));
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
                    Commons.log.finer("{Argument|"+name+"} Consuming "+key+":"+kwargs.get(key));
                    value = kwargs.get(key);
                    args.remove(key);
                }else if(value == null)
                    throw new ParseException("Argument '"+name+"' not satisfied.");
            }
        }
        
        //No keyword found, simply swallow next positional argument.
        if(((value == null) || value.equals(defval)) && args.size() > 0){
            value = args.pop();
            Commons.log.finer("{Argument|"+name+"} Consuming '"+value+"'");
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
    
    public void setName(String name){this.name=name;}
    /**
     * Sets whether the argument is optional or not. Note that this will
     * inevitably change the default value to either an empty string or null,
     * depending on the optionality ("" means optional, null means not).
     * @param optional 
     */
    public void setOptional(boolean optional){defval = (optional)? "" : null ;}
    /**
     * Sets the default value. Note that this also influences the optionality of
     * this argument (null means required, any string means optional).
     * @param defval 
     */
    public void setDefault(String defval){this.defval=defval;}
    public void setChoices(String[] chocies){this.choices=choices;}
    public void setChecker(ArgumentChecker check){this.check=check;}
    public void setValue(String value){this.value=value;}
    
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