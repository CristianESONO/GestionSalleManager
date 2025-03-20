package com.core;

import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import com.App;

public class MysqlDb implements IDatabase {
    protected Connection conn;
    protected PreparedStatement ps;

    @Override
    public void openConnexionBD() {
        try {
            Class.forName("org.sqlite.JDBC");
            //String dbPath = App.class.getResource("/com/gestionsalles.sqlite").getPath();
            String fileName = "gestionsalles.sqlite";
            String programFilesX86 = System.getenv("ProgramFiles(x86)");
            String appDataPath = programFilesX86 + "/GestionSalles";
            String dbPath = Paths.get(appDataPath,fileName).toString(); 
            dbPath = dbPath.replace("%20", " ");
            conn = DriverManager.getConnection("jdbc:sqlite:" + dbPath);
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
       
     
    }

    @Override
    public void closeConnexionBD() {
        if(conn!=null){
            try {
                conn.close();
            } catch (SQLException e) {
               
                e.printStackTrace();
            }
        }
        
    }
}
