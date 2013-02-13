package org.tymoonnext.bot.module.auth;

import org.tymoonnext.bot.Commons;

/**
 *
 * @author Shinmera
 * @license GPLv3
 * @version 0.0.0
 */
public class User {
    private String id;
    private String pwHash;
    private PermissionTree perms;
    private Group group;
    private Session session;
    
    public User(){
    }
    
    public String getID(){return id;}
    public String getPasswordHash(){return pwHash;}
    public Group getGroup(){return group;}
    public Session getSession(){return session;}
    
    public boolean authenticate(String pw){
        String hpw = Commons.hash(pw);
        if(pwHash.equals(hpw)){
            
            
            return true;
        }else{
            session = null;
            return false;
        }
    }
    
    public boolean check(String branch){
        return perms.check(branch) || group.check(branch);
    }
    
    public String toString(){return "/"+getClass().getSimpleName()+":"+id+"/";}
}
