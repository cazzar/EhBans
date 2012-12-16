package com.ehaqui.EhBans.util;

import java.sql.*;
import java.util.HashMap;

public class EhDatabase {

    private Connection conn = null;
    private Statement statement;
    private HashMap<Integer, HashMap<String, Object>> rows = new HashMap<Integer, HashMap<String, Object>>();
    private int numRows 	= 0;
    
    String db_host;
    String db_port;
    
	String db_database;
	String db_user;
	String db_pass;
    
    public EhDatabase (String host, String port, String db, String user, String pass) 
    {
    	db_host 		= host;
    	db_port 		= port;
        
    	db_database 	= db;
    	db_user 		= user;
    	db_pass 		= pass;
    }
    
    
    public Connection getConnection() 
    {
        if (conn == null)
        {
            return open();
        }
        
        return conn;
    }
    
    public Connection open() 
    {
        try {
        	
        	Class.forName("com.mysql.jdbc.Driver").newInstance();
        	String url 		= "jdbc:mysql://"+ db_host +":"+ db_port +"/" + db_database;
        	
        	conn = DriverManager.getConnection(url, db_user, db_pass);

            return conn;
        } catch (Exception e) {
            log.aviso(e.getMessage());
        }
        return null;
    }
    
    public void close() 
    {
        if (conn != null) 
        {
            try {
                conn.close();
            } catch (Exception e) {
            	log.aviso(e.getMessage());
            }
        }
    }
    
    public boolean isClosed() 
    {
        try {
            
            if(conn.isClosed())
                return true;
            else
                return false;
                
        } catch (SQLException e) {
            return true;
        }
    }
    
    public boolean checkTable(String tableName) 
    {
        DatabaseMetaData dbm = null;
        
        try {
            dbm = this.open().getMetaData();
            ResultSet tables = dbm.getTables(null, null, tableName, null);
            
            if (tables.next())
                return true;
            else
                return false;
            
        } catch (Exception e) {
        	log.aviso(e.getMessage());
            return false;
        }
    }
    
    public boolean createTable(String tableName, String[] columns, String[] dims) 
    {
        try {
            statement = conn.createStatement();
            String query = "CREATE TABLE " + tableName + "(";
            
            for (int i = 0; i < columns.length; i++) 
            {
                if (i!=0) 
                {
                    query += ",";
                }
                
                query += columns[i] + " " + dims[i];
            }
            
            query += ")";
            statement.execute(query);
            
        } catch (Exception e) {
        	log.aviso(e.getMessage());
        }
        return true;
    }
    
    public ResultSet query(String query) 
    {
        try 
        {
            statement = conn.createStatement();
            ResultSet results = statement.executeQuery(query);
            return results;
            
        } catch (Exception e) {
            if (!e.getMessage().contains("not return ResultSet") || (e.getMessage().contains("not return ResultSet") && query.startsWith("SELECT"))) 
            {
            	log.aviso(e.getMessage());
            }
        }
        return null;
    }
    
    public boolean update(String query) 
    {
        try 
        {
            statement = conn.createStatement();
            statement.executeUpdate(query);
            
            return true;
            
        } catch (Exception e) {

            log.aviso(e.getMessage());
            return false;
        }
    }
    
    public void update(String query, boolean test) throws SQLException 
    {
        statement = conn.createStatement();
        statement.executeUpdate(query);
    }
    
    public HashMap<Integer, HashMap<String, Object>> select(String fields, String tableName, String where, String group, String order) 
    {
        if ("".equals(fields) || fields == null) 
        {
            fields = "*";
        }
        
        String query = "SELECT " + fields + " FROM " + tableName;
       
        try{
            statement = conn.createStatement();
            
            if (!"".equals(where) && where != null) 
            {
                query += " WHERE " + where;
            }
            
            if (!"".equals(group) && group != null) 
            {
                query += " GROUP BY " + group;
            }
            
            if (!"".equals(order) && order != null) 
            {
                query += " ORDER BY " + order;
            }
            rows.clear();
            numRows = 0;
            
            ResultSet results = statement.executeQuery(query);
            
            if (results != null) 
            {
                int columns = results.getMetaData().getColumnCount();
                String columnNames = "";
                
                for (int i = 1; i <= columns; i++) 
                {
                    if (!"".equals(columnNames)) 
                    {
                        columnNames += ",";
                    }
                    columnNames += results.getMetaData().getColumnName(i);
                }
                
                String[] columnArray = columnNames.split(",");
                numRows = 0;
                
                while (results.next()) 
                {
                    HashMap<String, Object> thisColumn = new HashMap<String, Object>();
                    
                    for (String columnName : columnArray) 
                    {
                        thisColumn.put(columnName, results.getObject(columnName));
                    }
                    rows.put(numRows, thisColumn);
                    numRows++;
                }
                
                results.close();
                
                return rows;
            } 
            else 
            {
                return null;
            }
        } catch (Exception e) {
            log.aviso(e.getMessage());
        }
        return null;
    }


    
    
}