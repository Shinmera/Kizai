package org.tymoonnext.bot.module.athenaeum;

/**
 * 
 * @author Shinmera
 * @license GPLv3
 * @version 0.0.0
 */
public class InexistentVolumeException extends Exception{
    public InexistentVolumeException(){super();}
    public InexistentVolumeException(String msg){super(msg);}
    public InexistentVolumeException(String msg, Throwable t){super(msg, t);}
}
