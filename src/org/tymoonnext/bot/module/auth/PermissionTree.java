package org.tymoonnext.bot.module.auth;

/**
 *
 * @author Shinmera
 * @license GPLv3
 * @version 0.0.0
 */
public class PermissionTree {
    String[] perms;
    
    public PermissionTree(String[] perms){this.perms=perms;}
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
    
    /**
     * Check whether a certain branch is accessible in this tree. Branch leaves
     * are separated by dots.
     * 
     * Example branch: commands.IRC.send
     * 
     * @param branch The branch to check against.
     * @return True if the check succeeded and the branch is accessible.
     */
    public boolean check(String branch){
        branch=branch.trim().toLowerCase();
        if(branch.isEmpty())return true;
        
        String[] leaves = branch.split("\\.");
        
        for(String _branch : perms){
            String[] _leaves = _branch.split("\\.");
            
            for(int i=0;i<leaves.length;i++){
                if(leaves[i].equals("*"))return true;   //Allow all below this.
                if(_leaves[i].equals("*"))return true;  //Search for any subleaf.
                if(!leaves[i].equals(_leaves[i]))break; //Mismatch on this leaf.
                if(i==leaves.length-1)return true;      //We reached the end of this branch, so it has to be correct.
            }
        }
        
        return false;
    }
}
