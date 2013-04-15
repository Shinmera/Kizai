/**********************\
  file: SQLWrapper.java
  package: stevenbot.modules
  author: Shinmera
  team: NexT
  license: -
\**********************/

package NexT.mysql;

import NexT.err.NLogger;
import NexT.util.Toolkit;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Savepoint;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SQLWrapper {
    private static final Pattern TABLE_QUERY_PATTERN = Pattern.compile("SELECT\\s+.+\\s+FROM\\s+([a-z_-]+)\\s*", Pattern.CASE_INSENSITIVE);
    public static final Logger LOG = NLogger.get("MySQL");
    private static SQLWrapper INSTANCE = null;
    private String sqlhost = "localhost";
    private int sqlport = 3306;
    private String sqldb;
    private String sqluser;
    private String sqlpw;
    private String sqlurl;
    private Connection con;
    private HashMap<String,String[]> tableColumns;
    private HashMap<String,String[]> tablePrimaryKeys;
    private int lastInsertID = -1;
    
    public static SQLWrapper getInstance(){return SQLWrapper.INSTANCE;}
    public SQLWrapper(String user, String pass, String db) throws NSQLException{this(user, pass, db, "localhost");}
    public SQLWrapper(String user, String pass, String db, String host) throws NSQLException{this(user, pass, db, host, 3306);}
    public SQLWrapper(String user, String pass, String db, String host, int port) throws NSQLException{
        tableColumns = new HashMap<String,String[]>();
        tablePrimaryKeys = new HashMap<String,String[]>();
        try{
            sqldb = db;sqluser = user; sqlpw = pass; sqlhost = host; sqlport = port;
            sqlurl = "jdbc:mysql://"+sqlhost+":"+sqlport+"/"+sqldb+"?useUnicode=true&characterEncoding=utf-8";
            con = DriverManager.getConnection(sqlurl, sqluser, sqlpw);
            setAutoCommit(true);
            SQLWrapper.INSTANCE = this;
        }catch(SQLException ex){
            LOG.log(Level.SEVERE, "[SQLWrapper] Failed to start connection.", ex);
            throw new NSQLException("Failed to start connection", ex);
        }
    }
    
    public void close() {
        try{if(con!= null)con.close();}
        catch(SQLException ex){LOG.log(Level.SEVERE, "[SQLWrapper] Failed to close connection.", ex);}
    }
    
    public boolean setAutoCommit(boolean on){
        try{con.setAutoCommit(on);return true;}
        catch(SQLException ex){LOG.log(Level.WARNING, "[SQLWrapper] Failed to change autocommit.", ex);return false;}
    }
    
    public boolean commit(){
        try{con.commit();return true;}
        catch(SQLException ex){LOG.log(Level.WARNING, "[SQLWrapper] Failed to commit.", ex);return false;}
    }
    
    public boolean rollback(){
        try{con.rollback();return true;}
        catch(SQLException ex){LOG.log(Level.WARNING, "[SQLWrapper] Failed to rollback.", ex);return false;}
    }
    
    public boolean rollback(Savepoint save){
        try{con.rollback(save);return true;}
        catch(SQLException ex){LOG.log(Level.WARNING, "[SQLWrapper] Failed to rollback.", ex);return false;}
    }
    
    public Savepoint saveState(){
        try{return con.setSavepoint();}
        catch(SQLException ex){LOG.log(Level.WARNING, "[SQLWrapper] Failed to commit.", ex);return null;}
    }
    
    public PreparedStatement insecureQuery(String query, Object[] args){
        try{return query(query, args);}catch(NSQLException ex){return null;}
    }
    
    public PreparedStatement query(String query, Object[] args) throws NSQLException{
        if(con!=null){
            try {
                query = query.trim();
                PreparedStatement ps = con.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
                LOG.log(Level.FINE, "[SQLWrapper] Running query: "+query+" ("+Toolkit.implode(args,", ") +").");
                int i = 1;
                for(Object o : args){
                    if(o==null){                      ps.setNull(i, Types.NULL);            LOG.log(Level.FINEST, "Adding value null (null)");
                    }else if(o instanceof String){    ps.setString(i, (String)o);           LOG.log(Level.FINEST, "Adding value "+o+" (String)");
                    }else if(o instanceof Integer){   ps.setInt(i,    (int)(Integer)o);     LOG.log(Level.FINEST, "Adding value "+o+" (Integer)");
                    }else if(o instanceof Float){     ps.setFloat(i,  (float)(Float)o);     LOG.log(Level.FINEST, "Adding value "+o+" (Float)");
                    }else if(o instanceof Double){    ps.setDouble(i, (double)(Double)o);   LOG.log(Level.FINEST, "Adding value "+o+" (Double)");
                    }else if(o instanceof Short){     ps.setShort(i,  (short)(Short)o);     LOG.log(Level.FINEST, "Adding value "+o+" (Short)");
                    }else if(o instanceof Long){      ps.setLong(i,   (long)(Long)o);       LOG.log(Level.FINEST, "Adding value "+o+" (Long)");
                    }else if(o instanceof Byte){      ps.setByte(i,   (byte)(Byte)o);       LOG.log(Level.FINEST, "Adding value "+o+" (Byte)");
                    }else if(o instanceof Boolean){   ps.setBoolean(i,(boolean)(Boolean)o); LOG.log(Level.FINEST, "Adding value "+o+" (Boolean)");
                    }else{                            ps.setObject(i, o);                   LOG.log(Level.FINEST, "Adding value "+o+" (Unknown)");
                    }i++;
                }
                if(query.startsWith("INSERT")){
                    lastInsertID = ps.executeUpdate();
                }else{
                    ps.execute();
                }
                return ps;
            } catch (SQLException ex) {
                LOG.log(Level.WARNING, "[SQLWrapper] Failed query '"+query+"' ("+Toolkit.implode(args,", ") +").", ex);
                throw new NSQLException("Failed query '"+query+"' ("+Toolkit.implode(args,", ") +")",ex);
            }
        }
        return null;
    }
    
    public String getTableName(String query) throws NSQLException{
        Matcher match = TABLE_QUERY_PATTERN.matcher(query);
        if(match.find())
            return match.group(1);
        else
            throw new NSQLException("Could not determine table name from query: " + query);
    }
    
    public int getLastInsertID(){return lastInsertID;}
    
    public String[] getTableColumns(String table) throws NSQLException{
        if(!tableColumns.containsKey(table)){
            try{
                String[] data = {table, sqldb};
                ResultSet rs = query("SELECT column_name,column_key FROM information_schema.columns WHERE table_name=? AND table_schema=?",data).getResultSet();
                ArrayList<String> columns = new ArrayList<String>();
                ArrayList<String> primary = new ArrayList<String>();
                while(rs.next()){
                    columns.add(rs.getString(1));
                    if(rs.getString(2).equals("PRI"))
                        primary.add(rs.getString(2));
                }
                tableColumns.put(table, columns.toArray(new String[columns.size()]));
                tablePrimaryKeys.put(table, primary.toArray(new String[columns.size()]));
            }catch(SQLException ex){
                LOG.log(Level.WARNING, "[SQLWrapper] Failed to retrieve columns for table "+table+".", ex);
                throw new NSQLException("Failed to retrieve columns for table "+table,ex);
            }
        }
        return tableColumns.get(table);
    }
    
    public String[] getTablePrimaryKeys(String table) throws NSQLException{
        if(!tablePrimaryKeys.containsKey(table)){
            try{
                String[] data = {table, sqldb, "PRI"};
                ResultSet rs = query("SELECT column_name FROM information_schema.columns WHERE table_name=? AND table_schema=? AND column_key=? ",data).getResultSet();
                ArrayList<String> primary = new ArrayList<String>();
                while(rs.next()){
                    primary.add(rs.getString(1));
                }
                tablePrimaryKeys.put(table, primary.toArray(new String[primary.size()]));
            }catch(SQLException ex){
                LOG.log(Level.WARNING, "[SQLWrapper] Failed to retrieve primary keys for table "+table+".", ex);
                throw new NSQLException("Failed to retrieve primary keys for table "+table,ex);
            }
        }
        return tablePrimaryKeys.get(table);
    }
    
}
