
package org.tymoonnext.bot.module.misc;

import NexT.Commons;
import NexT.db.mongo.DataModel;
import NexT.db.mongo.MongoException;
import NexT.db.mongo.MongoWrapper;
import NexT.util.StringUtils;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.tymoonnext.bot.Kizai;
import org.tymoonnext.bot.event.CommandListener;
import org.tymoonnext.bot.event.cmd.CommandInstanceEvent;
import org.tymoonnext.bot.event.core.CommandEvent;
import org.tymoonnext.bot.module.Module;
import org.tymoonnext.bot.module.cmd.CommandInstance;
import org.tymoonnext.bot.module.core.ext.CommandModule;

/**
 * 
 * @author Shinmera
 * @license GPLv3
 * @version 0.0.0
 */
public class Medals extends Module implements CommandListener{
    
    public Medals(Kizai bot){
        super(bot);
        
        MongoWrapper.getInstance().getCollection("medals")
                .ensureIndex(new BasicDBObject("user", 1),
                             new BasicDBObject("unique", true));
        
        CommandModule.register(bot, "award", "username medal".split(" "), "Award a medal to someone.", this);
        CommandModule.register(bot, "medals", "username".split(" "), "View the medal collection of a user.", this);
    }

    @Override
    public void shutdown() {
    }

    @Override
    public void onCommand(CommandEvent cmd) {
        CommandInstance i = ((CommandInstanceEvent)cmd).get();
        String user = i.getValue("username").toLowerCase();
        if(cmd.getCommand().equals("award")){
            try {
                BasicDBObject key = new BasicDBObject().append("user", user);
                BasicDBObject val = new BasicDBObject().append("$push", 
                    new BasicDBObject().append("medals", i.getValue("medal")));
                MongoWrapper.getInstance().getCollection("medals").update(key, val, true, true);
                
                cmd.getStream().send(user+": You have been awarded the "+i.getValue("medal")+" medal.", cmd.getChannel());
                
            } catch (Exception ex) {
                Commons.log.log(Level.WARNING, toString()+" Error awarding medal for '"+user+"'", ex);
                cmd.getStream().send("Error awarding medal: "+ex.getMessage(), cmd.getChannel());
            }
            
        }else if(cmd.getCommand().equals("medals")){
            try {
                DataModel mod = DataModel.getFirst("medals", new BasicDBObject("user", user));
                
                if(mod == null) cmd.getStream().send(i.getValue("username")+" has no medals.", cmd.getChannel());
                else{
                    Object[] medals = ((BasicDBList)mod.get("medals")).toArray();
                    cmd.getStream().send("Medals for "+user+": "+StringUtils.implode(medals, ", "), cmd.getChannel());
                }
            } catch (MongoException ex) {
                Commons.log.log(Level.WARNING, toString()+" Error retrieving medals for '"+user+"'", ex);
                cmd.getStream().send("Error retrieving medals: "+ex.getMessage(), cmd.getChannel());
            }
        }
    }
    
}
