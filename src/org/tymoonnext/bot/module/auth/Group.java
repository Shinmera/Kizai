package org.tymoonnext.bot.module.auth;

/**
 *
 * @author Shinmera
 * @license GPLv3
 * @version 0.0.0
 */
public class Group {
    private String id;
    private PermissionTree perms;
    
    public Group(String id, PermissionTree perms){
        this.id=id;
        this.perms=perms;
    }
    
    public PermissionTree getPermissions(){return perms;}
    public String getID(){return id;}
    
    public boolean check(String branch){
        return perms.check(branch);
    }
    
    public String toString(){return "#"+getClass().getSimpleName()+":"+id+"#";}
}
