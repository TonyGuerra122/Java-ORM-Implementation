package com.tony.db;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import com.tony.App;

public final class DBConnection {

    public static Connection getConnection() {
        try {
            Properties props = loadProperties();
            String dbUrl = props.getProperty("dbUrl");
            return DriverManager.getConnection(dbUrl);
        } catch (SQLException e) {
            throw new DBException(e.getMessage());
        }
    }

    private static Properties loadProperties() {
        try (InputStream fs = App.class.getClassLoader().getResourceAsStream("application.properties")) {
            Properties props = new Properties();
            props.load(fs);
            return props;
        } catch (IOException e) {
            throw new DBException(e.getMessage());
        }
    }

    public static void closeConnection(Connection conn) {
        try {
            if (conn != null && !conn.isClosed()) {
                conn.close();
            }
        } catch (SQLException e) {
            throw new DBException(e.getMessage());
        }
    }

    public static void closeResultSet(ResultSet rs) {
        try {
            if (rs != null && !rs.isClosed()) {
                rs.close();
            }
        } catch (SQLException e) {
            throw new DBException(e.getMessage());
        }
    }

    public static void closeStatement(Statement stmt) {
        try {
            if (stmt != null && !stmt.isClosed()) {
                stmt.close();
            }
        } catch (SQLException e) {
            throw new DBException(e.getMessage());
        }
    }
}
