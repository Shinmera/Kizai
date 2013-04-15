package NexT.mongo;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

/**
 * 
 * @author Shinmera
 * @license GPLv3
 * @version 0.0.0
 */
public class DataModel{    
    private MongoWrapper wrapper;
    private DBCollection collection;
    private DBObject dataOrig;
    private DBObject data;
    
    private DataModel(DBCollection collection, DBObject data){
        this.wrapper = MongoWrapper.getInstance();
        this.collection = collection;
        this.dataOrig = data;
        if(data == null)    this.data = new BasicDBObject();
        else                this.data = data;
    }
    
    /**
     * Attempt to retrieve records from the specified collection using the
     * given query. If no records were found, null is returned instead.
     * @param collection The collection to query
     * @param query A DBObject representing the query parameters.
     * @return Null if no queries were found or an array of records.
     * @throws MongoException Thrown if the MongoWrapper has not been
     * initialized yet or if there was an error reading the objects from the db.
     */
    public static DataModel[] getData(String collection, DBObject query) throws MongoException{
        MongoWrapper wrapper = MongoWrapper.getInstance();
        if(wrapper == null) throw new MongoException("MongoWrapper has not been initiated.");
        DBCollection col = wrapper.getCollection(collection);
        DBCursor cursor = col.find(query);
        DataModel[] models = new DataModel[cursor.length()];
        try{
            for(int i=0;cursor.hasNext();i++) {
                models[i] = new DataModel(col, cursor.next());
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
     */
    public void set(String column, Object o) {data.put(column, o);}
    
    /**
     * Retrieve the object from a particular key. If the requested key does not
     * exist, null is returned. Note that this is no test for whether a field
     * already exists or not, as the value of an existing field may also be
     * null.
     * @param column
     * @return 
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
     */
    public void insert() throws MongoException{
        try{
            collection.insert(data);
            dataOrig=data;
        }catch(Exception ex){throw new MongoException("Insert failed!", ex);}
    }
    
    /**
     * Attempts to update the model in the database. This is only possible if
     * the object has been retrieved through getData or if the hull has been
     * inserted before.
     * @throws MongoException 
     * @see DataModel#insert() 
     */
    public void update() throws MongoException{
        if(dataOrig == null) throw new MongoException("Model is only a hull. Perform an insert first!");
        try{collection.update(dataOrig, data);}
        catch(Exception ex){throw new MongoException("Update failed!", ex);}
    }
    
    /**
     * Attempts to delete the model in the database. This is only possible if
     * the object has been retrieved through getData or if the hull has been
     * inserted before.
     * @throws MongoException 
     * @see DataModel#insert() 
     */
    public void delete() throws MongoException{
        if(dataOrig == null) throw new MongoException("Model is only a hull. Perform an insert first!");
        try{collection.remove(data);}
        catch(Exception ex){throw new MongoException("Delete failed!", ex);}
    }
}
