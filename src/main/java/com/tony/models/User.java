package com.tony.models;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import com.tony.annotations.orm.Column;
import com.tony.annotations.orm.Id;
import com.tony.db.DBConnection;
import com.tony.db.DBException;

public class User extends Model {

    @Id
    private int id;
    @Column(name = "name", type = "TEXT")
    private String name;

    public User() {
    }

    public User(String name){
        this.name = name;
    }

    public User(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public static void createModel() {
        Connection conn = null;
        Statement stmt = null;
        final String SQL = "CREATE TABLE IF NOT EXISTS users(id INTEGER PRIMARY KEY, name TEXT);";
        try {
            conn = DBConnection.getConnection();
            stmt = conn.createStatement();
            stmt.execute(SQL);
        } catch (SQLException e) {
            throw new DBException(e.getMessage());
        } finally {
            DBConnection.closeConnection(conn);
            DBConnection.closeStatement(stmt);
        }
    }

    public static User findById(int id) {
        return Model.findById(User.class, id);
    }

    public void save() throws IllegalAccessException, SQLException{
        Model.save(User.class);
    }
}
