package org.tymoonnext.bot.stream;

/**
 * Default stream that links everything to System.out
 * @author Shinmera
 * @license GPLv3
 * @version 0.0.0
 */
public class StdOut implements Stream{
    public void send(String msg, String dst){
        System.out.println(dst+"> "+msg);
    }

    public void close(){}
}
