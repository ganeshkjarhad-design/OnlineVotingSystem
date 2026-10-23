
package com.onlinevoting;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;

public class AdminLoginWindow extends JFrame {

    private final JTextField usernameField = new JTextField(20);
    private final JPasswordField passwordField = new JPasswordField(20);

    private final Color navy = new Color(25, 43, 77);
    private final Color blue = new Color(45, 100, 210);
    private final Color background = new Color(245, 247, 252);

    public AdminLoginWindow() {
        setTitle("Online Voting System - Admin Login");
        setSize(450, 340);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel root = new JPanel(new BorderLayout(15, 15));
        root.setBackground(background);
        root.setBorder(new EmptyBorder(20, 25, 20, 25));

        JLabel heading = new JLabel("ADMIN LOGIN", SwingConstants.CENTER);
        heading.setFont(new Font("SansSerif", Font.BOLD, 24));
        heading.setForeground(navy);
        root.add(heading, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridLayout(4, 1, 6, 6));
        form.setBackground(background);

        form.add(new JLabel("Admin Username"));
        form.add(usernameField);
        form.add(new JLabel("Admin Password"));
        form.add(passwordField);

        root.add(form, BorderLayout.CENTER);

        JButton loginButton = new JButton("Login");
        loginButton.setFont(new Font("SansSerif", Font.BOLD, 15));
        loginButton.setBackground(blue);
        loginButton.setForeground(Color.WHITE);
        loginButton.setFocusPainted(false);

        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(e -> dispose());

        loginButton.addActionListener(e -> authenticate());
        passwordField.addActionListener(e -> authenticate());

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 5));
        buttons.setBackground(background);
        buttons.add(loginButton);
        buttons.add(cancelButton);

        root.add(buttons, BorderLayout.SOUTH);
        setContentPane(root);
    }

    private void authenticate() {
        String username = usernameField.getText().trim();
        char[] password = passwordField.getPassword();

        if (username.isEmpty() || password.length == 0) {
            Arrays.fill(password, '\0');
            JOptionPane.showMessageDialog(
                    this,
                    "Please enter both username and password.",
                    "Missing Information",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        try {
            String enteredHash = hashPassword(password);
            boolean authenticated = verifyAdmin(username, enteredHash);

            if (authenticated) {
                passwordField.setText("");

                JOptionPane.showMessageDialog(
                        this,
                        "Admin login successful!",
                        "Login Successful",
                        JOptionPane.INFORMATION_MESSAGE
                );

                dispose();
                new AdminDashboard(username).setVisible(true);

            } else {
                passwordField.setText("");

                JOptionPane.showMessageDialog(
                        this,
                        "Invalid admin username or password.",
                        "Login Failed",
                        JOptionPane.ERROR_MESSAGE
                );
            }

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(
                    this,
                    "Could not connect to the admin database.\n"
                            + ex.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE
            );
        } finally {
            Arrays.fill(password, '\0');
        }
    }

    private boolean verifyAdmin(String username, String passwordHash)
            throws SQLException {

        String sql = """
                SELECT admin_id
                FROM admins
                WHERE username = ? AND password_hash = ?
                LIMIT 1
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, username);
            statement.setString(2, passwordHash);

            try (ResultSet result = statement.executeQuery()) {
                return result.next();
            }
        }
    }

    private String hashPassword(char[] password) {
        byte[] bytes = new String(password).getBytes(StandardCharsets.UTF_8);

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(bytes);
            StringBuilder hex = new StringBuilder();

            for (byte b : hash) {
                hex.append(String.format("%02x", b & 0xff));
            }

            return hex.toString();

        } catch (Exception ex) {
            throw new IllegalStateException("Could not verify password.", ex);
        } finally {
            Arrays.fill(bytes, (byte) 0);
        }
    }

    private static class AdminDashboard extends JFrame {

        private final String adminUsername;
        private final JLabel statusLabel =
                new JLabel("Signed in as administrator");

        AdminDashboard(String adminUsername) {
            this.adminUsername = adminUsername;

            setTitle("Online Voting System - Admin Dashboard");
            setSize(850, 550);
            setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            setLocationRelativeTo(null);

            Color navy = new Color(25, 43, 77);
            Color background = new Color(245, 247, 252);

            JPanel root = new JPanel(new BorderLayout(18, 18));
            root.setBackground(background);
            root.setBorder(new EmptyBorder(22, 25, 22, 25));

            JPanel header = new JPanel(new GridLayout(2, 1, 5, 5));
            header.setBackground(navy);
            header.setBorder(new EmptyBorder(18, 20, 18, 20));

            JLabel title = new JLabel("ADMIN DASHBOARD");
            title.setForeground(Color.WHITE);
            title.setFont(new Font("SansSerif", Font.BOLD, 25));

            JLabel welcome = new JLabel(
                    "Welcome, " + adminUsername
            );
            welcome.setForeground(Color.WHITE);
            welcome.setFont(new Font("SansSerif", Font.PLAIN, 15));

            header.add(title);
            header.add(welcome);

            JButton resultsButton = new JButton("View Election Results");
            JButton refreshButton = new JButton("Refresh Results");
            JButton logoutButton = new JButton("Logout");

            resultsButton.addActionListener(e -> loadResults());
            refreshButton.addActionListener(e -> loadResults());

            logoutButton.addActionListener(e -> {
                dispose();
                new AdminLoginWindow().setVisible(true);
            });

            JPanel actions = new JPanel(
                    new FlowLayout(FlowLayout.LEFT, 10, 5)
            );
            actions.setBackground(background);
            actions.add(resultsButton);
            actions.add(refreshButton);
            actions.add(logoutButton);

            statusLabel.setFont(
                    new Font("SansSerif", Font.PLAIN, 13)
            );

            JPanel footer = new JPanel(new BorderLayout());
            footer.setBackground(background);
            footer.add(statusLabel, BorderLayout.CENTER);
            footer.add(actions, BorderLayout.SOUTH);

            root.add(header, BorderLayout.NORTH);
            root.add(footer, BorderLayout.SOUTH);

            setContentPane(root);

            loadResults();
        }

        private void loadResults() {
            String sql = """
                    SELECT c.candidate_name,
                           c.party_name,
                           COUNT(v.vote_id) AS total_votes
                    FROM candidates c
                    LEFT JOIN votes v
                        ON c.candidate_id = v.candidate_id
                    GROUP BY c.candidate_id,
                             c.candidate_name,
                             c.party_name
                    ORDER BY total_votes DESC, c.candidate_id
                    """;

            String[] columns = {
                    "Candidate Name", "Party", "Total Votes"
            };

            DefaultTableModel model = new DefaultTableModel(columns, 0) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };

            try (Connection connection = DBConnection.getConnection();
                 PreparedStatement statement =
                         connection.prepareStatement(sql);
                 ResultSet result = statement.executeQuery()) {

                long totalVotes = 0;

                while (result.next()) {
                    long votes = result.getLong("total_votes");
                    totalVotes += votes;

                    model.addRow(new Object[]{
                            result.getString("candidate_name"),
                            result.getString("party_name"),
                            votes
                    });
                }

                JTable table = new JTable(model);
                table.setRowHeight(28);
                table.setFont(new Font("SansSerif", Font.PLAIN, 14));
                table.getTableHeader().setFont(
                        new Font("SansSerif", Font.BOLD, 14)
                );
                table.setAutoCreateRowSorter(true);

                JScrollPane scrollPane = new JScrollPane(table);

                getContentPane().removeAll();

                JPanel root = new JPanel(new BorderLayout(15, 15));
                root.setBorder(new EmptyBorder(20, 25, 20, 25));
                root.setBackground(new Color(245, 247, 252));

                JLabel heading = new JLabel(
                        "Election Results  |  Admin: " + adminUsername
                );
                heading.setFont(
                        new Font("SansSerif", Font.BOLD, 22)
                );

                JLabel summary = new JLabel(
                        "Candidates: " + model.getRowCount()
                                + "     Total votes: " + totalVotes
                );

                JButton refreshButton =
                        new JButton("Refresh Results");
                refreshButton.addActionListener(e -> loadResults());

                JButton logoutButton = new JButton("Logout");
                logoutButton.addActionListener(e -> {
                    dispose();
                    new AdminLoginWindow().setVisible(true);
                });

                JPanel top = new JPanel(new BorderLayout(5, 8));
                top.setBackground(new Color(245, 247, 252));
                top.add(heading, BorderLayout.NORTH);
                top.add(summary, BorderLayout.SOUTH);

                JPanel bottom = new JPanel(
                        new FlowLayout(FlowLayout.RIGHT)
                );
                bottom.setBackground(new Color(245, 247, 252));
                bottom.add(refreshButton);
                bottom.add(logoutButton);

                root.add(top, BorderLayout.NORTH);
                root.add(scrollPane, BorderLayout.CENTER);
                root.add(bottom, BorderLayout.SOUTH);

                setContentPane(root);
                revalidate();
                repaint();

                statusLabel.setText("Results loaded successfully.");

            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(
                        this,
                        "Could not load election results.\n"
                                + ex.getMessage(),
                        "Database Error",
                        JOptionPane.ERROR_MESSAGE
                );
            }
        }
    }
}
