/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package NexT.mongo;

public class MongoException extends Exception{
    public MongoException(String message){
        super(message);
    }
    public MongoException(String message, Exception e){
        super(message, e);
    }
}
