
package com.onlinevoting;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import javax.swing.JPasswordField;
import javax.swing.JOptionPane;

public class DBConnection {

    private static final String URL =
            "jdbc:mysql://localhost:3306/online_voting";
    private static final String USER = "root";

    private static String password;

    public static Connection getConnection() throws SQLException {
        if (password == null) {
            JPasswordField passwordField = new JPasswordField();

            int result = JOptionPane.showConfirmDialog(
                    null,
                    passwordField,
                    "Enter MySQL root password",
                    JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.QUESTION_MESSAGE
            );

            if (result != JOptionPane.OK_OPTION) {
                throw new SQLException("MySQL password entry cancelled.");
            }

            char[] chars = passwordField.getPassword();

            if (chars.length == 0) {
                throw new SQLException("MySQL password cannot be empty.");
            }

            password = new String(chars);
            java.util.Arrays.fill(chars, '\0');
        }

        try {
            return DriverManager.getConnection(URL, USER, password);
        } catch (SQLException e) {
            password = null;
            throw e;
        }
    }
}
