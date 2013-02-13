package org.tymoonnext.bot.module.auth;

/**
 *
 * @author Shinmera
 * @license GPLv3
 * @version 0.0.0
 */
public class PermissionTree {
    String[] perms;
    
    public PermissionTree(String permTree){this(permTree.split("\n"));}
    public PermissionTree(String[] perms){this.perms=perms;}
    public PermissionTree(PermissionTree tree, String permTree){this(tree, permTree.split("\n"));}
    public PermissionTree(PermissionTree tree, String[] permTree){
        String[] A = permTree;
        String[] B = tree.getPermissions();
        perms = new String[A.length+B.length];
        System.arraycopy(A, 0, perms, 0, A.length);
        System.arraycopy(B, 0, perms, 0, B.length);
    }
    public PermissionTree(PermissionTree A, PermissionTree B){
        this(A, B.getPermissions());
    }
    
    public String[] getPermissions(){return perms;}
    
    public boolean check(String branch){
        
        
        return false;
    }
}
