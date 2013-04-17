package NexT.db.mongo;

import NexT.db.DBException;
import NexT.db.DatabaseWrapper;
import NexT.err.NLogger;
import java.util.logging.Logger;
import com.mongodb.Mongo;
import com.mongodb.MongoException;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import java.net.UnknownHostException;
import java.util.Set;
import java.util.logging.Level;

/**
 * 
 * @author Shinmera
 * @license GPLv3
 * @version 0.0.0
 */
public class MongoWrapper extends DatabaseWrapper{
    public static MongoWrapper INSTANCE;
    public static final Logger LOG = NLogger.get("MongoDB");
    
    private Mongo client;
    private DB db;
    
    public static MongoWrapper getInstance(){return MongoWrapper.INSTANCE;}
    public MongoWrapper(String database) throws DBException{this(database, "localhost");}
    public MongoWrapper(String database, String host) throws DBException{this(database, host, 27017);}
    public MongoWrapper(String database, String host, int port) throws DBException{this(null, null, database, host, port);}
    public MongoWrapper(String user, String pass, String database, String host, int port) throws DBException{
        super(user, pass, database, host, port);
        try{
            LOG.log(Level.INFO, "[MongoDB] Connecting.");
            client = new Mongo(host, port);
            db = client.getDB(database);
            
            if(user != null && pass != null){
                if(!db.authenticate(user, pass.toCharArray())){
                    LOG.log(Level.SEVERE, "[MongoDB] Authentication with user "+user+" failed!");
                    throw new MongoException("Failed to authenticate");
                }else{
                    LOG.log(Level.INFO, "[MongoDB] Authenticaton with user "+user+" successful.");
                }
            }
            
            MongoWrapper.INSTANCE = this;
        }catch(UnknownHostException ex){
            LOG.log(Level.SEVERE, "[MongoDB] Failed to start connection.", ex);
            throw new MongoException("Failed to start connection", ex);
        }
    }
    
    public void close(){
        LOG.log(Level.INFO, "[MongoDB] Closing connection.");
        client.close();
    }
    
    public String[] getCollections(){
        Set<String> set = db.getCollectionNames();
        return set.toArray(new String[set.size()]);
    }
    
    public DBCollection getCollection(String collection){
        return db.getCollection(collection);
    }    
    
    public Mongo getClient(){return client;}
    public DB getDatabase(){return db;}
}
