package org.example.config;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DatabaseConnection {
    private static Connection connection;

    private DatabaseConnection() {}

    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            Properties props = new Properties();

            try (InputStream in = DatabaseConnection.class
                    .getClassLoader()
                    .getResourceAsStream("db.properties")) {

                if (in == null) {
                    throw new RuntimeException("Файл db.properties не найден!");
                }
                props.load(in);

                connection = DriverManager.getConnection(
                        props.getProperty("db.url"),
                        props.getProperty("db.user"),
                        props.getProperty("db.password")
                );
            } catch (IOException e) {
                throw new RuntimeException("Ошибка при чтении настроек БД", e);
            }
        }
        return connection;
    }
}
