
package com.onlinevoting;

import java.sql.Connection;
import java.sql.DatabaseMetaData;

public class DatabaseTest {
    public static void main(String[] args) {
        try (Connection connection = DBConnection.getConnection()) {
            DatabaseMetaData metadata = connection.getMetaData();

            System.out.println("Database connection successful!");
            System.out.println("Database: " + connection.getCatalog());
            System.out.println("MySQL version: " + metadata.getDatabaseProductVersion());

        } catch (Exception e) {
            System.out.println("Database connection failed.");
            System.out.println("Reason: " + e.getMessage());
        }
    }
}
