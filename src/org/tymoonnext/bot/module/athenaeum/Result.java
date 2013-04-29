package org.tymoonnext.bot.module.athenaeum;

/**
 * 
 * @author Shinmera
 * @license GPLv3
 * @version 0.0.0
 */
public class Result {
    public String data = null;
    public int added = -1;
    public int removed = -1;
    public int changed = -1;
    public int total = 0;
    
    public Result(String data, int total){this(data, -1, -1, -1, total);}    
    public Result(String data, int added, int removed, int changed, int total){
        this.data=data;this.added=added;this.removed=removed;this.changed=changed;this.total=total;
    }
}
