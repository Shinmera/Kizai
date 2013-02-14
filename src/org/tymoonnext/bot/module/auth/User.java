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
    private PermissionTree perms;
    private Group group;
    private Session session;
    
    public User(String id){
        this.id=id;
        session = new Session();
    }
    
    public void setPermissions(PermissionTree tree){perms=tree;}
    public void setGroup(Group group){this.group=group;}
    
    public String getID(){return id;}
    public Group getGroup(){return group;}
    public Session getSession(){return session;}
    
    public void login(){
        session = new Session();
    }
    
    public void logout(){
        session.invalidate();
    }
    
    public boolean check(String branch){
        if(!session.isValid())return false;
        if((group != null) && (group.check(branch)))return true;
        if((perms != null) && (perms.check(branch)))return true;
        return false;
    }
    
    public String toString(){return "/"+getClass().getSimpleName()+":"+id+"/";}
}
