package org.tymoonnext.bot.module.athenaeum;

import NexT.db.mongo.DataModel;
import NexT.db.mongo.MongoException;
import com.mongodb.BasicDBObject;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.tymoonnext.bot.Commons;
import org.tymoonnext.bot.Kizai;
import org.tymoonnext.bot.event.CommandListener;
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
    private static final Pattern getInfoRegex = Pattern.compile("(([a-z0-9_\\-\\|\\.]+)\\s)?((all|everything|something)\\s)?(about|of|on)\\s(.+)", Pattern.CASE_INSENSITIVE);
    private static final Pattern findInfoRegex= Pattern.compile("(out for|up|for)\\s((anything|things|whatever|stuff)\\s(about|like|fits|fitting|similar to)\\s)?(.+)", Pattern.CASE_INSENSITIVE);
    private static final Pattern addInfoRegex = Pattern.compile("((all|everything|nothing|something|this|the following)\\s)?(about|of|on)\\s(.+):\\s*(.+)", Pattern.CASE_INSENSITIVE);
    private static final Pattern delInfoRegex = Pattern.compile("((all|everything|nothing)\\s)?(about|of|on)\\s(.+)", Pattern.CASE_INSENSITIVE);

    public Athenaeum(Kizai bot){
        super(bot);
        
        CommandModule.register(bot, "tell", null, "Retrieve information from the athenaeum.", this);
        CommandModule.register(bot, "look", null, "Search the athenaeum for entries.", this);
        CommandModule.register(bot, "record", null, "Record a new entry in the athenaeum.", this);
        CommandModule.register(bot, "burn", null, "Burn an entry from the athenaeum.", this);
        CommandModule.register(bot, "forget", null, "Forget an entry from the athenaeum.", this);
        CommandModule.register(bot, "athenaeum", "action{clear|lock|unlock}".split(""), "Manage the athenaeum.", this);
    }

    public void shutdown(){
        bot.unregisterAllCommands(this);
    }

    public void onCommand(CommandEvent cmd){
        if(cmd.getCommand().equalsIgnoreCase("tell")){
            onGetInfo(cmd);
        }else if(cmd.getCommand().equalsIgnoreCase("look")){
            onFindInfo(cmd);
        }else if(cmd.getCommand().equalsIgnoreCase("record")){
            onAddInfo(cmd);
        }else if(cmd.getCommand().equalsIgnoreCase("burn") ||
                 cmd.getCommand().equalsIgnoreCase("forget")){
            onDelInfo(cmd);
        }
    }
    
    public void onGetInfo(CommandEvent cmd){
        Matcher m = getInfoRegex.matcher(cmd.getArgs());
        if(!m.matches()){
            cmd.getStream().send("Sorry, I could not understand your request.", cmd.getChannel());
            return;
        }
        
        String directedTo = (m.group(2) == null)? "" : 
                            (m.group(2).equalsIgnoreCase("me"))? cmd.getUser()+": " :
                            m.group(2)+": ";
        String entry = m.group(6);
        try{
            DataModel[] mods = DataModel.getData("athenaeum", new BasicDBObject("title", entry));
            for(DataModel mod : mods){
                if(mod == null){
                    cmd.getStream().send(cmd.getUser()+": Sorry, I found nothing about '"+entry+"' in the athenaeum.", cmd.getChannel());
                }else{
                    String text = mod.get("text").toString();
                    cmd.getStream().send(directedTo+text, cmd.getChannel());
                }
            }
            
        }catch(MongoException ex){
            Commons.log.log(Level.WARNING, "Failed to retrieve record from the athenaeum collection!", ex);
            cmd.getStream().send("Sorry, I could not retrieve this entry. Error: " + ex.getMessage(), cmd.getChannel());
        }
        
    }
    
    public void onFindInfo(CommandEvent cmd){
        Matcher m = findInfoRegex.matcher(cmd.getArgs());
        if(!m.matches()){
            cmd.getStream().send("Sorry, I could not understand your request.", cmd.getChannel());
            return;
        }
        
    }
    
    public void onAddInfo(CommandEvent cmd){
        Matcher m = addInfoRegex.matcher(cmd.getArgs());
        if(!m.matches()){
            cmd.getStream().send("Sorry, I could not understand your request.", cmd.getChannel());
            return;
        }
        
        String entry = m.group(4);
        String data = m.group(5);
        try{
            DataModel obj = DataModel.getFirst("athenaeum", new BasicDBObject("title", entry));
            if(obj == null){
                DataModel.getHull("athenaeum").set("title", entry).set("text", data).insert();
                cmd.getStream().send("Record for "+entry+" added to the athenaeum.", cmd.getChannel());
            }else{
                obj.set("text", data).update();
                cmd.getStream().send("Record for "+entry+" updated in the athenaeum.", cmd.getChannel());
            }
        }catch(MongoException ex){
            Commons.log.log(Level.WARNING, "Failed to insert record into athenaeum collection!", ex);
            cmd.getStream().send("Sorry, I could not record this entry. Error: " + ex.getMessage(), cmd.getChannel());
        }
    }
    
    public void onDelInfo(CommandEvent cmd){
        Matcher m = delInfoRegex.matcher(cmd.getArgs());
        if(!m.matches()){
            cmd.getStream().send("Sorry, I could not understand your request.", cmd.getChannel());
            return;
        }
        
        String entry = m.group(4);
        try{
            DataModel obj = DataModel.getFirst("athenaeum", new BasicDBObject("title", entry));
            if(obj == null){
                cmd.getStream().send(cmd.getUser()+": Sorry, I found nothing about '"+entry+"' in the athenaeum.", cmd.getChannel());
            }else{
                obj.delete();
                cmd.getStream().send("Record about "+entry+" removed from the athenaeum.", cmd.getChannel());
            }
        }catch(MongoException ex){
            Commons.log.log(Level.WARNING, "Failed to delete the record from the athenaeum collection!", ex);
            cmd.getStream().send("Sorry, I could not delete this entry. Error: " + ex.getMessage(), cmd.getChannel());
        }
    }
}
