/**********************\
  file: NSQLException.java
  package: NexT.sql
  author: Shinmera
  team: NexT
  license: -
\**********************/

package NexT.mysql;

public class NSQLException extends Exception{
    public NSQLException(String message){
        super(message);
    }
    public NSQLException(String message, Exception e){
        super(message, e);
    }
}
