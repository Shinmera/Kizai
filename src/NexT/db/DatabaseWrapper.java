package NexT.db;

import com.mongodb.MongoException;


/**
 * 
 * @author Shinmera
 * @license GPLv3
 * @version 0.0.0
 */
public abstract class DatabaseWrapper {
    
    protected String user, pass, database, host;
    protected int port;
    
    public DatabaseWrapper(String user, String pass, String database, String host, int port) throws DBException{
        this.user=user;
        this.pass=pass;
        this.database=database;
        this.host=host;
        this.port=port;
    }
    
    public abstract void close();
}
