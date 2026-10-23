
package com.onlinevoting;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.SQLException;
import java.util.Arrays;

public class VoterWindow extends JFrame {

    private final VoterService voterService = new VoterService();

    private final Color navy = new Color(25, 43, 77);
    private final Color blue = new Color(45, 100, 210);
    private final Color background = new Color(245, 247, 252);

    public VoterWindow() {
        setTitle("Online Voting System | Voter Portal");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(760, 560);
        setMinimumSize(new Dimension(680, 500));
        setLocationRelativeTo(null);

        JPanel root = new JPanel(new BorderLayout(0, 0));
        root.setBackground(background);

        JPanel header = new JPanel();
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setBackground(navy);
        header.setBorder(new EmptyBorder(25, 30, 25, 30));

        JLabel title = new JLabel("ONLINE VOTING SYSTEM");
        title.setFont(new Font("SansSerif", Font.BOLD, 25));
        title.setForeground(Color.WHITE);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitle = new JLabel("Secure Voter Portal");
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 14));
        subtitle.setForeground(new Color(215, 225, 245));
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        header.add(title);
        header.add(Box.createVerticalStrut(8));
        header.add(subtitle);

        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(new Font("SansSerif", Font.BOLD, 14));
        tabs.addTab("Voter Login", createLoginPanel());
        tabs.addTab("New Voter Registration", createRegistrationPanel());
        tabs.setBorder(new EmptyBorder(12, 25, 20, 25));

        JLabel footer = new JLabel(
                "College Mini Project | Java Swing + MySQL",
                SwingConstants.CENTER
        );
        footer.setBorder(new EmptyBorder(10, 5, 15, 5));
        footer.setForeground(Color.GRAY);

        root.add(header, BorderLayout.NORTH);
        root.add(tabs, BorderLayout.CENTER);
        root.add(footer, BorderLayout.SOUTH);

        setContentPane(root);
    }

    private JPanel createLoginPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(background);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(9, 8, 9, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;
        gbc.gridx = 0;
        gbc.gridwidth = 2;

        JLabel heading = new JLabel("Welcome Back");
        heading.setFont(new Font("SansSerif", Font.BOLD, 22));
        heading.setForeground(navy);
        heading.setHorizontalAlignment(SwingConstants.CENTER);

        gbc.gridy = 0;
        panel.add(heading, gbc);

        JLabel info = new JLabel(
                "Sign in with your registered voter account"
        );
        info.setHorizontalAlignment(SwingConstants.CENTER);
        info.setForeground(Color.DARK_GRAY);

        gbc.gridy = 1;
        panel.add(info, gbc);

        gbc.gridwidth = 1;
        gbc.weightx = 0.25;

        JLabel usernameLabel = new JLabel("Username");
        gbc.gridx = 0;
        gbc.gridy = 2;
        panel.add(usernameLabel, gbc);

        JTextField usernameField = new JTextField(22);
        styleField(usernameField);
        gbc.gridx = 1;
        gbc.weightx = 0.75;
        panel.add(usernameField, gbc);

        JLabel passwordLabel = new JLabel("Password");
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.weightx = 0.25;
        panel.add(passwordLabel, gbc);

        JPasswordField passwordField = new JPasswordField(22);
        styleField(passwordField);
        gbc.gridx = 1;
        gbc.weightx = 0.75;
        panel.add(passwordField, gbc);

        JButton loginButton = createButton("Sign In");

        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        gbc.weightx = 1;
        panel.add(loginButton, gbc);

        loginButton.addActionListener(e -> {
            String username = usernameField.getText().trim();
            char[] passwordChars = passwordField.getPassword();

            if (username.isEmpty() || passwordChars.length == 0) {
                Arrays.fill(passwordChars, '\0');

                JOptionPane.showMessageDialog(
                        this,
                        "Enter your username and password.",
                        "Missing Details",
                        JOptionPane.WARNING_MESSAGE
                );
                return;
            }

            String password = new String(passwordChars);
            Arrays.fill(passwordChars, '\0');

            try {
                boolean loginSuccessful =
                        voterService.loginVoter(username, password);

                if (loginSuccessful) {
                    JOptionPane.showMessageDialog(
                            this,
                            "Login successful! Welcome, " + username + ".",
                            "Login Successful",
                            JOptionPane.INFORMATION_MESSAGE
                    );

                    passwordField.setText("");

                    // Open the dashboard after successful login.
                    VoterDashboard dashboard =
                            new VoterDashboard(username);
                    dashboard.setVisible(true);

                    // Close the voter login and registration window.
                    dispose();

                } else {
                    JOptionPane.showMessageDialog(
                            this,
                            "Invalid username or password.",
                            "Login Failed",
                            JOptionPane.ERROR_MESSAGE
                    );

                    passwordField.setText("");
                }

            } catch (SQLException ex) {
                showDatabaseError(ex);
            }
        });

        return panel;
    }

    private JPanel createRegistrationPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(background);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(7, 8, 7, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridwidth = 1;
        gbc.weightx = 0.35;

        JLabel heading = new JLabel("Create Voter Account");
        heading.setFont(new Font("SansSerif", Font.BOLD, 21));
        heading.setForeground(navy);
        heading.setHorizontalAlignment(SwingConstants.CENTER);

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        panel.add(heading, gbc);

        JTextField cardField = new JTextField(22);
        JTextField nameField = new JTextField(22);
        JTextField usernameField = new JTextField(22);
        JPasswordField passwordField = new JPasswordField(22);

        styleField(cardField);
        styleField(nameField);
        styleField(usernameField);
        styleField(passwordField);

        gbc.gridwidth = 1;
        gbc.weightx = 0.35;

        addFormRow(panel, gbc, 1, "Voter Card Number", cardField);
        addFormRow(panel, gbc, 2, "Full Name", nameField);
        addFormRow(panel, gbc, 3, "Choose Username", usernameField);
        addFormRow(panel, gbc, 4, "Choose Password", passwordField);

        JButton registerButton = createButton("Register Voter");

        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridwidth = 2;
        gbc.weightx = 1;
        panel.add(registerButton, gbc);

        registerButton.addActionListener(e -> {
            String card = cardField.getText().trim();
            String name = nameField.getText().trim();
            String username = usernameField.getText().trim();
            char[] passwordChars = passwordField.getPassword();

            if (card.isEmpty() || name.isEmpty()
                    || username.isEmpty()
                    || passwordChars.length == 0) {

                Arrays.fill(passwordChars, '\0');

                JOptionPane.showMessageDialog(
                        this,
                        "Please complete all fields.",
                        "Missing Details",
                        JOptionPane.WARNING_MESSAGE
                );
                return;
            }

            if (passwordChars.length < 8) {
                Arrays.fill(passwordChars, '\0');

                JOptionPane.showMessageDialog(
                        this,
                        "Use a password of at least 8 characters.",
                        "Weak Password",
                        JOptionPane.WARNING_MESSAGE
                );
                return;
            }

            String password = new String(passwordChars);
            Arrays.fill(passwordChars, '\0');

            try {
                boolean registered = voterService.registerVoter(
                        card, name, username, password
                );

                if (registered) {
                    JOptionPane.showMessageDialog(
                            this,
                            "Voter registration successful! You can now sign in.",
                            "Registration Successful",
                            JOptionPane.INFORMATION_MESSAGE
                    );

                    cardField.setText("");
                    nameField.setText("");
                    usernameField.setText("");
                    passwordField.setText("");
                }

            } catch (SQLException ex) {
                String message = ex.getMessage() == null
                        ? "" : ex.getMessage().toLowerCase();

                if (message.contains("duplicate entry")) {
                    JOptionPane.showMessageDialog(
                            this,
                            "That voter card number or username "
                                    + "is already registered.",
                            "Duplicate Details",
                            JOptionPane.WARNING_MESSAGE
                    );
                } else {
                    showDatabaseError(ex);
                }
            }
        });

        return panel;
    }

    private void addFormRow(
            JPanel panel,
            GridBagConstraints gbc,
            int row,
            String label,
            JComponent field) {

        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0.35;
        panel.add(new JLabel(label), gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.65;
        panel.add(field, gbc);
    }

    private void styleField(JComponent field) {
        field.setFont(new Font("SansSerif", Font.PLAIN, 14));
        field.setPreferredSize(new Dimension(250, 36));
    }

    private JButton createButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("SansSerif", Font.BOLD, 14));
        button.setBackground(blue);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setOpaque(true);
        button.setBorderPainted(false);
        button.setPreferredSize(new Dimension(200, 40));
        button.setCursor(
                Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
        );
        return button;
    }

    private void showDatabaseError(SQLException ex) {
        JOptionPane.showMessageDialog(
                this,
                "Could not complete the database operation.\n"
                        + ex.getMessage(),
                "Database Error",
                JOptionPane.ERROR_MESSAGE
        );
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(
                () -> new VoterWindow().setVisible(true)
        );
    }
}
