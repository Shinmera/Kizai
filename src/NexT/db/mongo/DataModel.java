package NexT.db.mongo;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import java.util.List;

/**
 * 
 * @author Shinmera
 * @license GPLv3
 * @version 0.0.0
 */
public class DataModel extends NexT.db.DataModel{    
    private MongoWrapper wrapper;
    private DBCollection collection;
    private DBObject dataOrig;
    private DBObject data;
    
    private DataModel(DBCollection collection, DBObject data){
        this.wrapper = MongoWrapper.getInstance();
        this.collection = collection;
        this.dataOrig = new BasicDBObject();
        this.dataOrig.putAll(data);
        if(data == null)    this.data = new BasicDBObject();
        else                this.data = data;
    }
    
    /**
     * Attempt to retrieve records from the specified collection using the
     * given query. If no records were found, null is returned instead.
     * @param collection The collection to query
     * @param query A DBObject representing the query parameters.
     * @return Null if no documents were found or an array of records.
     * @throws MongoException Thrown if the MongoWrapper has not been
     * initialized yet or if there was an error reading the objects from the db.
     */
    public static DataModel[] getData(String collection, DBObject query) throws MongoException{
        MongoWrapper wrapper = MongoWrapper.getInstance();
        if(wrapper == null) throw new MongoException("MongoWrapper has not been initiated.");
        DBCollection col = wrapper.getCollection(collection);
        DBCursor cursor = col.find(query);
        List<DBObject> modelsList;
        DataModel[] models;
        try{
            modelsList = cursor.toArray();
            models = new DataModel[modelsList.size()];
            for(int i=0;i<models.length;i++){
                models[i] = new DataModel(col, modelsList.get(i));
            }
        }catch(Exception ex){
            throw new MongoException("Error while retrieving Objects", ex);
        }finally{
            cursor.close();
        }
        if(models.length == 0)  return null;
        else                    return models;
    }
    
    
    /**
     * Attempt to retrieve one record from the specified collection using the
     * given query.
     * @param collection The collection to query
     * @param query A DBObject representing the query parameters.
     * @return Null if no documents were found or the first record.
     * @throws MongoException Thrown if the MongoWrapper has not been
     * initialized yet or if there was an error reading the objects from the db.
     */
    public static DataModel getFirst(String collection, DBObject query) throws MongoException{
        MongoWrapper wrapper = MongoWrapper.getInstance();
        if(wrapper == null) throw new MongoException("MongoWrapper has not been initiated.");
        DBCollection col = wrapper.getCollection(collection);
        DBObject obj = col.findOne(query);
        return (obj==null)? null : new DataModel(col, obj);
    }
    
    /**
     * Get an empty model for a given collection. This is mainly used to insert
     * new records into the database.
     * @param collection The name of the collection to use for this model.
     * @return Returns the new DataModel hull tied to the collection.
     * @throws MongoException Thrown if the MongoWrapper has not been
     * initialized yet.
     */
    public static DataModel getHull(String collection) throws MongoException{
        MongoWrapper wrapper = MongoWrapper.getInstance();
        if(wrapper == null) throw new MongoException("MongoWrapper has not been initiated.");
        return new DataModel(wrapper.getCollection(collection), null);
    }
    
    /**
     * Set a new or existing field to a given value. Objects can be nested by
     * storing DBObjects as a value.
     * @param column The key
     * @param o  The value
     * @return This object again so it can be used for chaining.
     */
    public DataModel set(String column, Object o) {data.put(column, o);return this;}
    
    /**
     * Retrieve the object from a particular key. If the requested key does not
     * exist, null is returned. Note that this is no test for whether a field
     * already exists or not, as the value of an existing field may also be
     * null.
     * @param column
     * @return The requested Object or null.
     */
    public Object get(String column) {return (data.containsField(column)) ? data.get(column) : null;}
    
    /**
     * Returns whether a given key exists in the model or not.
     * @param column
     * @return 
     */
    public boolean hasColumn(String column){return data.containsField(column);}
    
    /**
     * Inserts the data into the db as a new record and replaces the original
     * data in the model. Note that after performing this action, all update()
     * and delete() calls only affect the documents matching the latest insert.
     * @throws MongoException 
     * @return This object again so it can be used for chaining.
     */
    public DataModel insert() throws MongoException{
        try{
            collection.insert(data);
            dataOrig=data;
        }catch(Exception ex){throw new MongoException("Insert failed!", ex);}
        return this;
    }
    
    /**
     * Attempts to update the model in the database. This is only possible if
     * the object has been retrieved through getData or if the hull has been
     * inserted before.
     * @throws MongoException 
     * @return This object again so it can be used for chaining.
     * @see DataModel#insert() 
     */
    public DataModel update() throws MongoException{
        if(dataOrig == null) throw new MongoException("Model is only a hull. Perform an insert first!");
        try{
            int affected = collection.update(dataOrig, data).getN();
            MongoWrapper.LOG.info("Record matching "+dataOrig+" changed to "+data+". "+affected+" records affected.");
            dataOrig=data;
        }catch(Exception ex){throw new MongoException("Update failed!", ex);}
        return this;
    }
    
    /**
     * Attempts to delete the model in the database. This is only possible if
     * the object has been retrieved through getData or if the hull has been
     * inserted before.
     * @throws MongoException 
     * @return This object again so it can be used for chaining.
     * @see DataModel#insert() 
     */
    public DataModel delete() throws MongoException{
        if(dataOrig == null) throw new MongoException("Model is only a hull. Perform an insert first!");
        try{
            int affected = collection.remove(data).getN();
            MongoWrapper.LOG.info("Record matching "+data+" deleted. "+affected+" records affected.");
        }catch(Exception ex){throw new MongoException("Delete failed!", ex);}
        return this;
    }
}
