package org.tymoonnext.bot.stream;

/**
 * Default stream that links everything to System.out
 * @author Shinmera
 * @license GPLv3
 * @version 0.0.0
 */
public class StdOut implements Stream{
    private boolean closed=false;
    
    public boolean isClosed(){return closed;}
    
    public void send(String msg, String dst){
        if(!closed)
            System.out.println(dst+"> "+msg);
    }

    public void close(){closed=true;}
    
    public String toString(){return "~StdOut~";}
    
    public String getID(){return "stdout";}
}
