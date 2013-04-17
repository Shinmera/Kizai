package NexT.db;

/**
 * 
 * @author Shinmera
 * @license GPLv3
 * @version 0.0.0
 */
public abstract class DataModel {

    public static DataModel[] getData(String query, Object args) throws DBException{return null;}
    public static DataModel getFirst(String query, Object args) throws DBException{return null;}    
    public static DataModel getHull(String table) throws DBException{return null;}
    
    public abstract DataModel set(String column, Object o);
    public abstract Object get(String column);
    
    public abstract DataModel insert() throws DBException;
    public abstract DataModel update() throws DBException;
    public abstract DataModel delete() throws DBException;
}
