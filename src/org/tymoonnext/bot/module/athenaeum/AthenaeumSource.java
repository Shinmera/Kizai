package org.tymoonnext.bot.module.athenaeum;

import NexT.db.mongo.DataModel;
import NexT.db.mongo.MongoException;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import java.util.Arrays;
import java.util.logging.Level;
import org.tymoonnext.bot.Commons;

/**
 * 
 * @author Shinmera
 * @license GPLv3
 * @version 0.0.0
 */
public class AthenaeumSource implements ModifiableSource{

    @Override
    public ResultSet search(String query, int from, int to, String user) throws SourceException{
        return null;
    }

    @Override
    public ResultSet get(String volume, int from, int to, String user) throws SourceException, InexistentVolumeException{
        try{
            DataModel mod = DataModel.getFirst("athenaeum", new BasicDBObject("title", volume));
            if(mod == null)throw new InexistentVolumeException("No results for '"+volume+"'.");
            BasicDBList pages = mod.get("pages");
            
            if(to > pages.size())to = pages.size();
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
            DataModel mod = DataModel.getFirst("athenaeum", new BasicDBObject("title", volume));
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
