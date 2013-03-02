package org.tymoonnext.bot.module.cmd;

import java.util.ArrayList;

/**
 * 
 * @author Shinmera
 * @license GPLv3
 * @version 0.0.0
 */
public class Command{    
    private String name;
    private String description;
    private ArrayList<Argument> chain;
    
    public Command(String name){this(name, null, null);}
    public Command(String name, String[] args){this(name, args, null);}
    public Command(String name, String desc){this(name, null, desc);}
    public Command(String name, String[] args, String desc){
        this.name=name;
        this.description=desc;
        this.chain = new ArrayList<Argument>();
        if(args!=null){
            for(String arg: args){
                add(arg);
            }
        }
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
     * @return The generated Argument
     * @see Argument#generate(java.lang.String) 
     */
    private Argument add(String arg){
        Argument a = Argument.generate(arg);
        if(a.getName().equals("ARG"))
            a.setName("ARG"+chain.size());
        chain.add(a);
        return a;
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
    public CommandInstance getInstance(){return new CommandInstance(this);}
    public CommandInstance getInstance(String args) throws ParseException{return new CommandInstance(this, args);}
    
    public String toString(){return "{"+this.getClass().getSimpleName()+"|"+name+"}";}
}
