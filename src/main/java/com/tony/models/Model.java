package com.tony.models;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.tony.annotations.orm.Column;
import com.tony.annotations.orm.Id;
import com.tony.db.DBConnection;
import com.tony.db.DBException;

public abstract class Model {

    private static final SQLGenerator SQL_OBJECT = new SQLGenerator();

    protected static <T extends Model> T findById(Class<T> clazz, int id) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        final String QUERY = SQL_OBJECT.generateFindByIdSQL(clazz, id);
        try {
            conn = DBConnection.getConnection();
            pstmt = conn.prepareStatement(QUERY);
            pstmt.setInt(1, id);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                return createInstanceByRs(rs, clazz);
            }
            return null;
        } catch (SQLException e) {
            throw new DBException(e.getMessage());
        } finally {
            DBConnection.closeConnection(conn);
            DBConnection.closeResultSet(rs);
            DBConnection.closeStatement(pstmt);
        }
    }

    protected static <T extends Model> void save(Class<T> instance) throws IllegalAccessException, SQLException {
        Class<T> clazz = instance;
        Field[] fields = clazz.getDeclaredFields();
        Connection conn = null;
        PreparedStatement pstmt = null;
        final String QUERY = SQL_OBJECT.generateSaveSQL(clazz);
        try {
            conn = DBConnection.getConnection();
            pstmt = conn.prepareStatement(QUERY, Statement.RETURN_GENERATED_KEYS);
            int paramIndex = 1;
            for (Field field : fields) {
                if (!field.isAnnotationPresent(Id.class) && field.isAnnotationPresent(Column.class)) {
                    field.setAccessible(true); // Garante acesso ao campo
                    if (field.getType() == String.class) {
                        pstmt.setString(paramIndex, (String) field.get(instance));
                    } else if (field.getType() == int.class || field.getType() == Integer.class) {
                        pstmt.setInt(paramIndex, field.getInt(instance));
                    }
                    // Adicione mais condições conforme necessário para outros tipos de dados
                    paramIndex++;
                }
            }
            pstmt.executeUpdate();
        }catch(IllegalAccessException | SQLException e){
            throw new DBException(e.getMessage());
        } 
        finally {
            // Fechar os recursos usando try-with-resources ou manualmente
            DBConnection.closeStatement(pstmt);
            DBConnection.closeConnection(conn);
        }
    }
    
    
    protected static <T extends Model> T createInstanceByRs(ResultSet rs, Class<T> clazz) {
        try {
            T instance = clazz.getDeclaredConstructor().newInstance(); // Cria uma instância da classe

            for (Field field : clazz.getDeclaredFields()) {
                if (field.isAnnotationPresent(Column.class)) {
                    Column column = field.getAnnotation(Column.class);
                    Object value = rs.getObject(column.name());
                    field.setAccessible(true); // Torna o campo acessível
                    field.set(instance, value); // Define o valor do campo na instância
                }
            }
            return instance; // Retorna a instância preenchida
        } catch (Exception e) {
            throw new RuntimeException("Não foi possível mapear o ResultSet", e);
        }
    }

    private static final class SQLGenerator {

        private String getTableNameByClass(Class<? extends Model> clazz) {
            return clazz.getSimpleName().toLowerCase() + "s"; // Simplificado para demonstração
        }

        private String generateFindByIdSQL(Class<? extends Model> clazz, int id) {
            String tableName = getTableNameByClass(clazz);
            String idName = getIdColumnName(clazz);

            return "SELECT * FROM " + tableName + " WHERE " + idName + " = ?";
        }

        private String getIdColumnName(Class<? extends Model> clazz) {
            for (Field field : clazz.getDeclaredFields()) {
                if (field.isAnnotationPresent(Id.class)) {
                    return field.getAnnotation(Id.class).name();
                }
            }
            throw new IllegalStateException("Classe " + clazz.getSimpleName() + " não possui campo anotado com @Id");
        }

        private <T extends Model> String generateSaveSQL(Class<T> clazz) {
            String tableName = getTableNameByClass(clazz);

            StringBuilder sb = new StringBuilder();
            sb.append("INSERT INTO ").append(tableName).append("(");

            Field[] fields = clazz.getDeclaredFields();
            boolean isFirstField = true;
            for (Field field : fields) {
                if (!field.isAnnotationPresent(Id.class)) {
                    if (isFirstField) {
                        isFirstField = false;
                    } else {
                        sb.append(", ");
                    }
                    sb.append(field.getName());
                }
            }

            sb.append(") VALUES(");
            isFirstField = true;
            for (Field field : fields) {
                if (!field.isAnnotationPresent(Id.class)) {
                    if (isFirstField) {
                        isFirstField = false;
                    } else {
                        sb.append(", ");
                    }
                    sb.append("?");
                }
            }

            sb.append(")");
            return sb.toString();
        }

    }
}
