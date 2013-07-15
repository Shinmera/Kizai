package org.tymoonnext.bot.module.clhs;

import NexT.db.mongo.DataModel;
import NexT.db.mongo.MongoException;
import NexT.db.mongo.MongoWrapper;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import java.util.logging.Level;
import java.util.regex.Pattern;
import org.tymoonnext.bot.Commons;
import org.tymoonnext.bot.Kizai;
import org.tymoonnext.bot.event.CommandListener;
import org.tymoonnext.bot.event.core.CommandEvent;
import org.tymoonnext.bot.module.Module;
import org.tymoonnext.bot.module.athenaeum.InexistentVolumeException;
import org.tymoonnext.bot.module.athenaeum.Result;
import org.tymoonnext.bot.module.athenaeum.ResultSet;
import org.tymoonnext.bot.module.athenaeum.Source;
import org.tymoonnext.bot.module.athenaeum.SourceException;
import org.tymoonnext.bot.module.core.ext.CommandModule;

/**
 * 
 * @author Shinmera
 * @license GPLv3
 * @version 0.0.0
 */
public class CLHS extends Module implements CommandListener, Source{

    public CLHS(Kizai bot){
        super(bot);
        CommandModule.register(bot, "clhs", "symbol".split(" "), "Search the Common Lisp Hyperspec for a symbol.", this);
        CommandModule.register(bot, "clhs-reindex", null, "Rebuild the index for the Common Lisp Hyperspec.", this);
        MongoWrapper.getInstance().getCollection("clhs").ensureIndex(new BasicDBObject("symbol", 1), new BasicDBObject("unique", true));
    }
    
    public Pattern regex(String regex){
        regex = regex.replaceAll("\\*", ".*").replaceAll("\\-","\\-");
        return Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
    }
    
    @Override
    public void shutdown() {
        bot.unregisterAllCommands(this);
    }

    @Override
    public void onCommand(CommandEvent cmd) {
        if(cmd.getCommand().equalsIgnoreCase("clhs")){
            try {
                DataModel[] mods = DataModel.getData("clhs", new BasicDBObject("symbol", regex(cmd.getArgs())));
                switch(mods.length){
                    case 0: break;
                    case 1: cmd.getStream().send((String)mods[0].get("header") + "   " + mods[0].get("link"), cmd.getChannel());
                    default: cmd.getStream().send("More results available.", cmd.getChannel()); break;
                }
            } catch (MongoException ex) {
                Commons.log.log(Level.WARNING, "[CLHS] Failed to retrieve record from the clhs collection!", ex);
                cmd.getStream().send("Failed to retrieve records: " + ex, cmd.getChannel());
            }
        }else if(cmd.getCommand().equalsIgnoreCase("clhs-reindex")){
            MongoWrapper.getInstance().getCollection("clhs").drop();
            MongoWrapper.getInstance().getCollection("clhs").ensureIndex(new BasicDBObject("symbol", 1), new BasicDBObject("unique", true));
            Indexer.buildSymbolIndex();
        }
    }

    @Override
    public ResultSet search(String query, int from, int to, String user) throws SourceException {
        try{
            DataModel[] mods = DataModel.getData("clhs", new BasicDBObject("symbol", regex(query)), from, to-from);
            if(mods == null)return new ResultSet(new Result[0], 0);
            
            Result[] res = new Result[mods.length];
            for(int i=0;i<res.length;i++){
                res[i]=new Result(mods[i].get("symbol").toString());
            }
            return new ResultSet(res); //@TODO Not actual maximum pages returned...
        }catch(MongoException ex){
            Commons.log.log(Level.WARNING, "[CLHS] Failed to retrieve record from the clhs collection!", ex);
            throw new SourceException("Error in MongoDB", ex);
        }
    }

    @Override
    public ResultSet get(String volume, int from, int to, String user) throws SourceException, InexistentVolumeException {
        try{
            DataModel mod = DataModel.getFirst("clhs", new BasicDBObject("symbol", volume));
            if(mod == null)throw new InexistentVolumeException("No results for '"+volume+"'.");
            BasicDBList pages = mod.get("pages");
            
            if(to > pages.size())to = pages.size();
            if(to == -1)to = pages.size();
            if(from > to)from = to;
            
            Result[] res = new Result[to-from];
            for(int i=0;i<res.length;i++){
                res[i]=new Result(pages.get(i+from).toString());
            }
            return new ResultSet(res, pages.size());
        }catch(MongoException ex){
            Commons.log.log(Level.WARNING, "[CLHS] Failed to retrieve record from the clhs collection!", ex);
            throw new SourceException("Error in MongoDB", ex);
        }
    }

    @Override
    public String getName() {return "clhs";}
}
