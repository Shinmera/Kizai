/**********************\
  file: NSQLException.java
  package: NexT.sql
  author: Shinmera
  team: NexT
  license: -
\**********************/

package NexT.db.mysql;

import NexT.db.DBException;

public class NSQLException extends DBException{
    public NSQLException(String message){
        super(message);
    }
    public NSQLException(String message, Exception e){
        super(message, e);
    }
}
