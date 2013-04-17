package NexT.db;

/**
 * 
 * @author Shinmera
 * @license GPLv3
 * @version 0.0.0
 */
public class DBException extends Exception{
    public DBException(String message){
        super(message);
    }
    public DBException(String message, Exception e){
        super(message, e);
    }
}

