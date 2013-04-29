package org.tymoonnext.bot.module.athenaeum;

import NexT.util.StringUtils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.tymoonnext.bot.Commons;
import org.tymoonnext.bot.Kizai;
import org.tymoonnext.bot.event.CommandListener;
import org.tymoonnext.bot.event.cmd.CommandInstanceEvent;
import org.tymoonnext.bot.event.core.CommandEvent;
import org.tymoonnext.bot.module.Module;
import org.tymoonnext.bot.module.core.ext.CommandModule;


/**
 * 
 * @author Shinmera
 * @license GPLv3
 * @version 0.0.0
 */
public class Athenaeum extends Module implements CommandListener{
    private static final String subjectMatch = "( [a-z0-9\\*\\-_ ]+)?";
    private static final String recordsMatch = "( (everything|all|((page|record|chapter) ([0-9]+)( (-|to|up to) ([0-9]+))?)) (about|on|of))?";
    private static final String sourceMatch  = "(,? (using|from|in) ([a-z0-9]+))?";
    private static final String endMatch     = "(\\.)?";
    private static final Pattern tellRegex   = Pattern.compile("tell( (me|[a-z0-9\\-_]+))?( about | of| on)?"+recordsMatch+subjectMatch+sourceMatch+endMatch, Pattern.CASE_INSENSITIVE);
    private static final Pattern lookRegex   = Pattern.compile("look for"+recordsMatch+subjectMatch+sourceMatch+endMatch, Pattern.CASE_INSENSITIVE);
    private static final Pattern recordRegex = Pattern.compile("record( (this |the following )?(about|of|on))?"+recordsMatch+subjectMatch+sourceMatch+"(:)?(.*)", Pattern.CASE_INSENSITIVE);
    private static final Pattern burnRegex   = Pattern.compile("burn"+recordsMatch+subjectMatch+sourceMatch+endMatch, Pattern.CASE_INSENSITIVE);
    private HashMap<String,Source> sources;
    private int max_look_entries = 10;
    private int std_look_entries = 3;
    private int max_tell_entries = 4;

    public Athenaeum(Kizai bot){
        super(bot);
        sources = new HashMap<String,Source>();
        addSource(new AthenaeumSource());
        
        CommandModule.register(bot, "tell", null, "Retrieve information from the athenaeum.", this);
        CommandModule.register(bot, "look", null, "Search the athenaeum for entries.", this);
        CommandModule.register(bot, "record", null, "Record a new entry in the athenaeum.", this);
        CommandModule.register(bot, "burn", null, "Burn an entry from the athenaeum.", this);
        CommandModule.register(bot, "athenaeum", "action{source|set|get|close|open}".split(""), "Manage the athenaeum.", this);
    }

    public void shutdown(){
        bot.unregisterAllCommands(this);
    }
    
    public void addSource(Source s){
        sources.put(s.getName(), s);
    }

    public void onCommand(CommandEvent cmd){
        String txt = (cmd.getCommand()+((cmd.getArgs()==null)?"":" "+cmd.getArgs()));
        Pattern p = Commons.retrieve(this, cmd.getCommand()+"Regex");
        Matcher m = p.matcher(txt.trim());
        
        if(!m.matches()){
            cmd.getStream().send("Sorry, I could not understand your request.", cmd.getChannel());
            return;
        }
        
        Commons.invoke(this, "on"+StringUtils.firstToUpper(cmd.getCommand()), cmd, m);        
    }
    
    public void onTell(CommandInstanceEvent cmd, Matcher m){
        //@TODO fr & to sanitization
        int fr = (m.group(8)==null)? 0 : Integer.parseInt(m.group(8));
        int to = (m.group(11)==null)? 1 : Integer.parseInt(m.group(11));
        String entry = (m.group(13)==null)? "" : m.group(13).toLowerCase().trim();
        String source = (m.group(15)==null)? "athenaeum" : m.group(15).toLowerCase();
        String directedTo = (m.group(2) == null)? "" : 
                            (m.group(2).equalsIgnoreCase("me"))? cmd.getUser()+": " :
                            m.group(2)+": ";
        
        if(entry.isEmpty()){
            cmd.getStream().send("About what?", cmd.getChannel());
            return;
        }        
        if(!sources.containsKey(source)){
            cmd.getStream().send("I don't know that place.", cmd.getChannel());
        }
        
        if(fr<0)fr=0;
        if(to<0)to=1;
        if(to<fr)to=fr+1;
        if(to-fr>max_tell_entries){
            cmd.getStream().send("Cannot display more than "+max_tell_entries+" pages at once, use paginated tell instead.", cmd.getChannel());
            to = fr+max_tell_entries;
        }
        
        try{
            //@TODO implement the "more" term.  
            ResultSet r = sources.get(source).get(entry, fr, to, cmd.getUser());
            if(r.results().length>0){
                for(int i=0;i<r.results().length;i++){
                    String msg = directedTo+r.results()[i].data;
                    if(i==r.results().length-1)
                        msg += " (Page "+fr+"-"+to+" of "+r.queryableSize()+") ["+StringUtils.firstToUpper(source)+"]";
                    cmd.getStream().send(msg, cmd.getChannel());
                }
            }else{
                cmd.getStream().send("No pages exist for this volume!", cmd.getChannel());
            }
            
        }catch(SourceException ex){
            Commons.log.log(Level.WARNING, "Error in "+sources.get(source)+" while processing query: "+cmd.getCommand()+" "+cmd.getArgs(), ex);
            cmd.getStream().send("Some kind of error occurred in the source! ("+ex.getMessage()+")", cmd.getChannel());
        }catch(InexistentVolumeException ex){
            cmd.getStream().send("I couldn't find a volume like that.", cmd.getChannel());
        }
    }
    
    public void onLook(CommandInstanceEvent cmd, Matcher m){
        boolean everything = (m.group(2)!=null) && 
                                (m.group(2).equalsIgnoreCase("everything") ||
                                 m.group(2).equalsIgnoreCase("all"));
        int fr = (m.group(5)==null)? 0 :
                                     Integer.parseInt(m.group(5));
        int to = (m.group(8)==null)? (fr+((everything)?max_look_entries:std_look_entries)) :
                                     Integer.parseInt(m.group(8));
        String entry = (m.group(10)==null)? "" : m.group(10).toLowerCase().trim();
        String source = (m.group(12)==null)? null : m.group(12).toLowerCase();
        
        if(entry.isEmpty()){
            cmd.getStream().send("What? Where?!", cmd.getChannel());
            return;
        }        
        if(!sources.containsKey(source)){
            cmd.getStream().send("I can't see there.", cmd.getChannel());
            return;
        }
        
        if(fr<0)fr=0;
        if(to<0)to=std_look_entries;
        if(to<fr)to=fr+1;
        if(to-fr>max_look_entries){
            cmd.getStream().send("Cannot display more than "+max_look_entries+" results at once, use paginated look instead.", cmd.getChannel());
            to = fr+max_look_entries;
        }
        
        try{
            //@TODO implement the "more" term.  
            Source[] lsources;      
            if(source != null){
                lsources = new Source[1];
                lsources[0] = sources.get(source);
            }else{
                lsources = sources.values().toArray(new Source[sources.size()]);
            }
            
            for(Source s : lsources){
                ResultSet r = s.search(entry, fr, to, cmd.getUser());
                String[] t = new String[r.results().length];
                for(int i=0;i<t.length;i++){t[i]=r.results()[i].data;}
                cmd.getStream().send("["+s.getName()+"] "+
                                     StringUtils.implode(t, ", ")+
                                     "("+(fr+1)+"-"+to+" of "+r.queryableSize()+")", cmd.getChannel());
            }
            
        }catch(SourceException ex){
            Commons.log.log(Level.WARNING, "Error in "+sources.get(source)+" while processing query: "+cmd.getCommand()+" "+cmd.getArgs(), ex);
            cmd.getStream().send("Some kind of error occurred in the source! ("+ex.getMessage()+")", cmd.getChannel());
        }
    }
    
    public void onRecord(CommandInstanceEvent cmd, Matcher m){
        int fr = (m.group(8)==null)? -1 : Integer.parseInt(m.group(8));
        int to = (m.group(11)==null)? -1 : Integer.parseInt(m.group(11));
        String entry = (m.group(13)==null)? "" : m.group(13).toLowerCase().trim();
        String source = (m.group(15)==null)? "athenaeum" : m.group(15).toLowerCase();
        String data = m.group(17);
        if(to<fr)to=fr+1;
        
        if(entry.isEmpty()){
            cmd.getStream().send("Record what?", cmd.getChannel());
            return;
        }        
        if(!sources.containsKey(source)){
            cmd.getStream().send("I can't reach it.", cmd.getChannel());
            return;
        }
        if(!sources.get(source).getClass().isInstance(ModifiableSource.class)){
            cmd.getStream().send("I don't do graffiti.", cmd.getChannel());
            return;
        }
        
        try{
            //@TODO actually implement the continuous lines adding as well as the "more" term.
            Result r = ((ModifiableSource)sources.get(source)).modify(entry, fr, to, data.split("\\\\\\\\"), cmd.getUser());
            cmd.getStream().send(buildResponse(r), cmd.getChannel());
        }catch(SourceException ex){
            Commons.log.log(Level.WARNING, "Error in "+sources.get(source)+" while processing query: "+cmd.getCommand()+" "+cmd.getArgs(), ex);
            cmd.getStream().send("Some kind of error occurred in the source! ("+ex.getMessage()+")", cmd.getChannel());
        }
    }
    
    public void onBurn(CommandInstanceEvent cmd, Matcher m){
        int fr = (m.group(5)==null)? -1 : Integer.parseInt(m.group(5));
        int to = (m.group(8)==null)? -1 : Integer.parseInt(m.group(8));
        String entry = (m.group(10)==null)? "" : m.group(10).toLowerCase().trim();
        String source = (m.group(12)==null)? "athenaeum" : m.group(12).toLowerCase();
        if(to<fr)to=fr+1;
        
        if(entry.isEmpty()){
            cmd.getStream().send("Android hell's existence is unconfirmed.", cmd.getChannel());
            return;
        }        
        if(!sources.containsKey(source)){
            cmd.getStream().send("I don't know that place.", cmd.getChannel());
            return;
        }
        if(!sources.get(source).getClass().isInstance(ModifiableSource.class)){
            cmd.getStream().send("That place is fire-proof!", cmd.getChannel());
            return;
        }
        
        try{
            Result r = ((ModifiableSource)sources.get(source)).remove(entry, fr, to, cmd.getUser());
            cmd.getStream().send(buildResponse(r), cmd.getChannel());
        }catch(SourceException ex){
            Commons.log.log(Level.WARNING, "Error in "+sources.get(source)+" while processing query: "+cmd.getCommand()+" "+cmd.getArgs(), ex);
            cmd.getStream().send("Some kind of error occurred in the source! ("+ex.getMessage()+")", cmd.getChannel());
        }catch(InexistentVolumeException ex){
            cmd.getStream().send("I can't burn what doesn't exist.", cmd.getChannel());
        }
    }
    
    public String buildResponse(Result r){
        ArrayList<String> res = new ArrayList<String>();
        if(r.added > -1)    res.add(r.added+" added");
        if(r.removed> -1)   res.add(r.removed+" removed");
        if(r.changed > -1)  res.add(r.changed+" changed");
        if(r.total > -1)    res.add(r.total+" total");
        if((r.data != null) && (!r.data.isEmpty())) res.add(r.data);
        return StringUtils.implode(res.toArray(), ", ")+".";
    }
}
