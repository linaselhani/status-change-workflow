package edu.gmu.cs321;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;


public class App {
    private static final String DB_HOST = "ep-soft-surf-a8kcx355-pooler.eastus2.azure.neon.tech";
    private static final String DB_PORT = "5432";
    private static final String DB_NAME = "group22-db";
    private static final String DB_USER = "neondb_owner";
    private static final String DB_PASSWORD = "npg_CDEzP50FBlKk";
    // private static final String DB_CONNECTION = "postgresql://neondb_owner:npg_CDEzP50FBlKk@ep-soft-surf-a8kcx355-pooler.eastus2.azure.neon.tech/neondb?sslmode=require&channel_binding=require";

    public static Connection getConnection() throws SQLException {
        String url = String.format(
            "jdbc:postgresql://%s:%s@%s/%s?sslmode=require&channel_binding=require&serverTimezone=UTC",
            DB_USER, DB_PASSWORD, DB_HOST, DB_NAME
        );

        System.out.println("DB CONNECTION SUCCESSFUL");

        return DriverManager.getConnection(url, DB_USER, DB_PASSWORD);
    }

    public static void main(String[] args) {
        try (Connection conn = getConnection()) {
            if (conn != null) {
                System.out.println("Connected to PostgreSQL successfully!");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
