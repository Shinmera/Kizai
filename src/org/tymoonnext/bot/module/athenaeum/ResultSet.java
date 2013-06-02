package org.tymoonnext.bot.module.athenaeum;

/**
 * 
 * @author Shinmera
 * @license GPLv3
 * @version 0.0.0
 */
public class ResultSet {
    private Result[] results;
    private int queryableSize;
    
    public ResultSet(Result result){this(ResultSet.makeToArray(result));}
    public ResultSet(Result[] results){this(results, results.length);}
    public ResultSet(Result[] results, int queryableSize){
        this.results=results;
        this.queryableSize=queryableSize;
    }
    
    public Result[] results(){return results;}
    public int queryableSize(){return queryableSize;}
    
    private static Result[] makeToArray(Result result){
        Result[] results = new Result[1];
        results[0] = result;
        return results;
    }
}
