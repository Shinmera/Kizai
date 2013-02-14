package org.tymoonnext.bot.module.auth;

/**
 *
 * @author Shinmera
 * @license GPLv3
 * @version 0.0.0
 */
public class Session {
    public static int SESSION_TIMEOUT = 60*2;
    
    long lastAuth;
    
    public Session(){}
    
    public void invalidate(){lastAuth=0;}
    public void makeValid(){lastAuth=System.currentTimeMillis()/1000;}
    
    public boolean isValid(){
        if(lastAuth < System.currentTimeMillis()/1000 - SESSION_TIMEOUT){
            return false;
        }else{
            makeValid();
            return true;
        }
    }
    
    public String toString(){return "~"+getClass().getSimpleName()+"~";}
}
