/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package NexT.db.mongo;

import NexT.db.DBException;

public class MongoException extends DBException{
    public MongoException(String message){
        super(message);
    }
    public MongoException(String message, Exception e){
        super(message, e);
    }
}
