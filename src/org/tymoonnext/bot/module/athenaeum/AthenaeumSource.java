package org.tymoonnext.bot.module.athenaeum;

import NexT.db.mongo.DataModel;
import NexT.db.mongo.MongoException;
import NexT.db.mongo.MongoWrapper;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.regex.Pattern;
import org.tymoonnext.bot.Commons;

/**
 * 
 * @author Shinmera
 * @license GPLv3
 * @version 0.0.0
 */
public class AthenaeumSource implements ModifiableSource{
    
    public AthenaeumSource(){
        MongoWrapper.getInstance().getCollection("athenaeum").ensureIndex(new BasicDBObject("title", 1), new BasicDBObject("unique", true));
    }
    
    public Pattern regex(String regex){
        regex = regex.replaceAll("\\*", ".*").replaceAll("\\-","\\-");
        return Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
    }
    
    public DataModel resolveModel(String volume) throws MongoException{
        DataModel mod = DataModel.getFirst("athenaeum", new BasicDBObject("title", volume));
        if (mod != null){
            String first = ((BasicDBList)mod.get("pages")).get(0).toString();
            if(first.startsWith("link to ")){
                mod = resolveModel(first.substring(7).trim());
            }
        }
        return mod;
    }

    @Override
    public ResultSet search(String query, int from, int to, String user) throws SourceException{
        try{
            DataModel[] mods = DataModel.getData("athenaeum", new BasicDBObject("title", regex(query)), from, to-from);
            if(mods == null)return new ResultSet(new Result[0], 0);
            
            Result[] res = new Result[mods.length];
            for(int i=0;i<res.length;i++){
                res[i]=new Result(mods[i].get("title").toString());
            }
            return new ResultSet(res); //@TODO Not actual maximum pages returned...
        }catch(MongoException ex){
            Commons.log.log(Level.WARNING, "Failed to retrieve record from the athenaeum collection!", ex);
            throw new SourceException("Error in MongoDB", ex);
        }
    }

    @Override
    public ResultSet get(String volume, int from, int to, String user) throws SourceException, InexistentVolumeException{
        try{
            DataModel mod = resolveModel(volume);
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
            Commons.log.log(Level.WARNING, "Failed to retrieve record from the athenaeum collection!", ex);
            throw new SourceException("Error in MongoDB", ex);
        }
    }

    @Override
    public Result modify(String volume, int from, int to, String[] data, String user) throws SourceException{
        try{
            Result res = new Result();
            res.changed=0;
            res.added  =0;
            res.removed=-1;
            DataModel mod = resolveModel(volume);
            if(mod == null){
                res.data = "Volume created";
                mod = DataModel.getHull("athenaeum");
                mod.set("title", volume);
                mod.set("creator", user);
                mod.set("pages", new BasicDBList());
                mod.insert();
            }
            
            BasicDBList pages = mod.get("pages");
            if(from==-1){
                pages.addAll(Arrays.asList(data));
                res.added = data.length;
            }else{
                for(int i=from;i<to;i++){
                    if(i>pages.size()){
                        pages.add(data[i-from]);
                        res.added++;
                    }else{
                        pages.set(i, data[i-from]);
                        res.changed++;
                    }
                }
            }
            
            mod.update();
            res.total=pages.size();
            return res;
        }catch(MongoException ex){
            Commons.log.log(Level.WARNING, "Failed to insert records to the athenaeum collection!", ex);
            throw new SourceException("Error in MongoDB", ex);
        }
    }

    @Override
    public Result remove(String volume, int from, int to, String user) throws SourceException, InexistentVolumeException{
        try{
            Result res = new Result();
            res.changed=-1;
            res.added  =-1;
            res.removed=0;
            DataModel mod = DataModel.getFirst("athenaeum", new BasicDBObject("title", volume));
            if(mod == null)throw new InexistentVolumeException("No results for '"+volume+"'.");
            
            BasicDBList pages = mod.get("pages");
            if(from==-1){
                res.removed = pages.size();
                res.total = -1;
                res.data = "Volume burned";
                mod.delete();
            }else{
                for(int i=to-1;i>=from;i--){
                    if(i<pages.size()){
                        pages.remove(i);
                        res.removed++;
                    }
                }
                res.total = pages.size();
            }
            
            mod.update();
            return res;
        }catch(MongoException ex){
            Commons.log.log(Level.WARNING, "Failed to insert records to the athenaeum collection!", ex);
            throw new SourceException("Error in MongoDB", ex);
        }
    }

    @Override
    public String getName(){return "athenaeum";}

}
