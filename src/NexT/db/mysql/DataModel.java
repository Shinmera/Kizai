/**********************\
  file: DataModel.java
  package: NexT.sql
  author: Shinmera
  team: NexT
  license: -
\**********************/

package NexT.db.mysql;

import NexT.util.Toolkit;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;

public class DataModel extends NexT.db.DataModel{
    private MySQLWrapper wrapper;
    private String table;
    private HashMap<String,Object> data;
    private String[] columns;
    private String[] primary;
    
    private DataModel(String table, HashMap<String,Object> data){
        this.table = table;
        this.data = data;
        wrapper = MySQLWrapper.getInstance();
    }
    
    public static DataModel[] getData(String query) throws NSQLException{
        return getData(query, new Object[0]);
    }
    public static DataModel[] getData(String query, Object[] args) throws NSQLException{
        MySQLWrapper wrapper = MySQLWrapper.getInstance();
        if(wrapper == null) throw new NSQLException("SQLWrapper has not been initiated.");
        try{

            PreparedStatement st = wrapper.query(query, args);
            ResultSet rs = st.getResultSet();
            ResultSetMetaData meta = rs.getMetaData();
            String table = wrapper.getTableName(query);

            ArrayList<DataModel> data = new ArrayList<DataModel>();
            while(rs.next()){
                HashMap<String,Object> inner = new HashMap<String,Object>();
                for(int i=1;i<=meta.getColumnCount();i++){
                    inner.put(meta.getColumnName(i), rs.getObject(i));
                }
                data.add(new DataModel(table, inner));
            }
            
            if(data.isEmpty()) return null;
            return data.toArray(new DataModel[data.size()]);
        }catch(SQLException ex){
            MySQLWrapper.LOG.log(Level.WARNING, "Failed to build DataModel instances from query '"+query+"' ("+Toolkit.implode(args,", ") +"): " + ex.getMessage(), ex);
            throw new NSQLException("Failed to build DataModel instances from query '"+query+"' ("+Toolkit.implode(args,", ") +")", ex);
        }
    }
    
    public static DataModel getFirst(String query, Object[] args) throws NSQLException{
        return getData(query, args)[0];
    }
    
    public static DataModel getHull(String table) throws NSQLException{
        MySQLWrapper wrapper = MySQLWrapper.getInstance();
        if(wrapper == null) throw new NSQLException("SQLWrapper has not been initiated.");
        return new DataModel(table, new HashMap<String,Object>());
    }
    
    public DataModel set(String column, Object o) {data.put(column, o);return this;}
    public Object get(String column) {return (data.containsKey(column)) ? data.get(column) : null;}
    public String[] getColumns(){ 
        if(columns == null){
            try{columns = wrapper.getTableColumns(table);
            }catch(NSQLException ex){columns = new String[0];}
        } return columns;
    }
    public String[] getPrimaryKeys(){ 
        if(primary == null){
            try{primary = wrapper.getTablePrimaryKeys(table);
            }catch(NSQLException ex){primary = new String[0];}
        } return primary;
    }
    
    public DataModel insert() throws NSQLException{
        String query = "INSERT INTO "+table;
        ArrayList<Object> qdata = new ArrayList<Object>();
        query = composeSetPart(query, qdata);
        wrapper.query(query, qdata.toArray());
        //Set primary key id.
        //String[] primary = getPrimaryKeys();
        //if(primary.length > 0)data.put(primary[0], (Integer)wrapper.getLastInsertID());
        return this;
    }
    
    public DataModel update() throws NSQLException{
        String query = "UPDATE "+table;
        ArrayList<Object> qdata = new ArrayList<Object>();
        query = composeSetPart(query, qdata);
        query = composeWherePart(query+" ", qdata);
        wrapper.query(query, qdata.toArray());
        return this;
    }
    
    public DataModel delete() throws NSQLException{
        String query = "DELETE FROM "+table;
        ArrayList<Object> qdata = new ArrayList<Object>();
        query = composeWherePart(query, qdata);
        wrapper.query(query, qdata.toArray());
        return this;
    }
    
    private String composeSetPart(String query, ArrayList<Object> qdata){
        query+=" SET ";
        for(String column : data.keySet()){
            query+="`"+column+"`=?, ";
            qdata.add(data.get(column));
        }
        return query.substring(0, query.length()-2);
    }
    
    private String composeWherePart(String query, ArrayList<Object> qdata){
        String[] primary = getPrimaryKeys();
        query+=" WHERE ";
        for(String key : primary){
            query+="`"+key+"`=? AND ";
            qdata.add(data.get(key));
        }
        return query.substring(0, query.length()-5);
    }
}
