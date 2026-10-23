
package com.onlinevoting;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class VoterDashboard extends JFrame {

    private final String username;
    private final VoterService voterService = new VoterService();

    private final Color navy = new Color(25, 43, 77);
    private final Color blue = new Color(45, 100, 210);
    private final Color background = new Color(245, 247, 252);

    public VoterDashboard(String username) {
        this.username = username;

        setTitle("Online Voting System | Voter Dashboard");
        setSize(750, 520);
        setMinimumSize(new Dimension(650, 450));
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel root = new JPanel(new BorderLayout(20, 20));
        root.setBackground(background);
        root.setBorder(new EmptyBorder(25, 30, 25, 30));

        JPanel header = new JPanel();
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setBackground(navy);
        header.setBorder(new EmptyBorder(25, 15, 25, 15));

        JLabel title = new JLabel("VOTER DASHBOARD");
        title.setFont(new Font("SansSerif", Font.BOLD, 26));
        title.setForeground(Color.WHITE);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel welcome = new JLabel("Welcome, " + username + "!");
        welcome.setFont(new Font("SansSerif", Font.PLAIN, 16));
        welcome.setForeground(Color.WHITE);
        welcome.setAlignmentX(Component.CENTER_ALIGNMENT);

        header.add(title);
        header.add(Box.createVerticalStrut(10));
        header.add(welcome);

        JPanel menu = new JPanel(new GridLayout(3, 1, 0, 15));
        menu.setBackground(background);

        JButton candidatesButton = createButton("View Candidates");
        JButton voteButton = createButton("Cast Vote");
        JButton resultsButton = createButton("View Election Results");

        menu.add(candidatesButton);
        menu.add(voteButton);
        menu.add(resultsButton);

        candidatesButton.addActionListener(e -> viewCandidates());
        voteButton.addActionListener(e -> castVote());
        resultsButton.addActionListener(e -> viewResults());

        JButton logoutButton = new JButton("Logout");
        logoutButton.setFont(new Font("SansSerif", Font.BOLD, 14));
        logoutButton.setPreferredSize(new Dimension(130, 40));

        logoutButton.addActionListener(e -> {
            int choice = JOptionPane.showConfirmDialog(
                    this,
                    "Are you sure you want to logout?",
                    "Confirm Logout",
                    JOptionPane.YES_NO_OPTION
            );

            if (choice == JOptionPane.YES_OPTION) {
                dispose();
                new VoterWindow().setVisible(true);
            }
        });

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        footer.setBackground(background);
        footer.add(logoutButton);

        root.add(header, BorderLayout.NORTH);
        root.add(menu, BorderLayout.CENTER);
        root.add(footer, BorderLayout.SOUTH);

        setContentPane(root);
    }

    private void viewCandidates() {
        String sql = """
                SELECT candidate_id, candidate_name, party_name, manifesto
                FROM candidates
                WHERE is_active = TRUE
                ORDER BY candidate_id
                """;

        StringBuilder details = new StringBuilder();
        int count = 0;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {

            while (result.next()) {
                count++;

                details.append("Candidate ID: ")
                        .append(result.getInt("candidate_id"))
                        .append("\nName: ")
                        .append(result.getString("candidate_name"))
                        .append("\nParty: ")
                        .append(result.getString("party_name"))
                        .append("\nManifesto: ")
                        .append(result.getString("manifesto"))
                        .append("\n----------------------------------\n\n");
            }

            if (count == 0) {
                JOptionPane.showMessageDialog(
                        this,
                        "No active candidates are available.",
                        "Candidates",
                        JOptionPane.INFORMATION_MESSAGE
                );
                return;
            }

            JTextArea area = new JTextArea(details.toString());
            area.setEditable(false);
            area.setFont(new Font("Monospaced", Font.PLAIN, 14));
            area.setCaretPosition(0);
            area.setMargin(new Insets(12, 12, 12, 12));

            JScrollPane scroll = new JScrollPane(area);
            scroll.setPreferredSize(new Dimension(520, 350));

            JOptionPane.showMessageDialog(
                    this,
                    scroll,
                    "Available Candidates (" + count + ")",
                    JOptionPane.INFORMATION_MESSAGE
            );

        } catch (SQLException ex) {
            showError("Could not load candidates.", ex);
        }
    }

    private void castVote() {
        int voterId;

        try {
            voterId = voterService.getVoterId(username);

            if (voterId == -1) {
                JOptionPane.showMessageDialog(
                        this,
                        "Voter account was not found. Please log in again.",
                        "Account Error",
                        JOptionPane.ERROR_MESSAGE
                );
                return;
            }

            if (voterService.hasVoted(voterId)) {
                JOptionPane.showMessageDialog(
                        this,
                        "You have already cast your vote. "
                                + "A voter can vote only once.",
                        "Already Voted",
                        JOptionPane.WARNING_MESSAGE
                );
                return;
            }

        } catch (SQLException ex) {
            showError("Could not verify your voting status.", ex);
            return;
        }

        JComboBox<Candidate> candidateBox = new JComboBox<>();

        String sql = """
                SELECT candidate_id, candidate_name, party_name
                FROM candidates
                WHERE is_active = TRUE
                ORDER BY candidate_id
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {

            while (result.next()) {
                candidateBox.addItem(new Candidate(
                        result.getInt("candidate_id"),
                        result.getString("candidate_name"),
                        result.getString("party_name")
                ));
            }

        } catch (SQLException ex) {
            showError("Could not load candidates for voting.", ex);
            return;
        }

        if (candidateBox.getItemCount() == 0) {
            JOptionPane.showMessageDialog(
                    this,
                    "No active candidates are available for voting.",
                    "No Candidates",
                    JOptionPane.INFORMATION_MESSAGE
            );
            return;
        }

        JPanel panel = new JPanel(new GridLayout(2, 1, 8, 8));
        panel.add(new JLabel("Select your candidate:"));
        panel.add(candidateBox);

        int choice = JOptionPane.showConfirmDialog(
                this,
                panel,
                "Cast Your Vote",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );

        if (choice != JOptionPane.OK_OPTION) {
            return;
        }

        Candidate selected = (Candidate) candidateBox.getSelectedItem();

        if (selected == null) {
            return;
        }

        int confirmation = JOptionPane.showConfirmDialog(
                this,
                "You selected:\n" + selected.name
                        + "\n" + selected.party
                        + "\n\nSubmit your vote? This action cannot be undone.",
                "Confirm Vote",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (confirmation != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            boolean saved = voterService.castVote(
                    voterId, selected.id
            );

            if (saved) {
                JOptionPane.showMessageDialog(
                        this,
                        "Your vote has been recorded successfully!\n"
                                + "Thank you for participating.",
                        "Vote Submitted",
                        JOptionPane.INFORMATION_MESSAGE
                );
            } else {
                JOptionPane.showMessageDialog(
                        this,
                        "The vote could not be recorded. "
                                + "The candidate may no longer be active.",
                        "Vote Not Recorded",
                        JOptionPane.WARNING_MESSAGE
                );
            }

        } catch (SQLException ex) {
            String message = ex.getMessage() == null
                    ? "" : ex.getMessage();

            if (message.toLowerCase().contains("already cast")
                    || ex.getErrorCode() == 1062) {
                JOptionPane.showMessageDialog(
                        this,
                        "Your account has already submitted a vote.",
                        "Already Voted",
                        JOptionPane.WARNING_MESSAGE
                );
            } else {
                showError("Could not record your vote.", ex);
            }
        }
    }

    private void viewResults() {
        String sql = """
                SELECT c.candidate_name, c.party_name,
                       COUNT(v.vote_id) AS total_votes
                FROM candidates c
                LEFT JOIN votes v ON c.candidate_id = v.candidate_id
                WHERE c.is_active = TRUE
                GROUP BY c.candidate_id, c.candidate_name, c.party_name
                ORDER BY total_votes DESC, c.candidate_id
                """;

        StringBuilder details = new StringBuilder();
        int count = 0;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {

            details.append("ELECTION RESULTS\n");
            details.append("==============================\n\n");

            while (result.next()) {
                count++;

                details.append(count).append(". ")
                        .append(result.getString("candidate_name"))
                        .append("\n   Party: ")
                        .append(result.getString("party_name"))
                        .append("\n   Votes: ")
                        .append(result.getLong("total_votes"))
                        .append("\n\n");
            }

            if (count == 0) {
                details.append("No active candidates are available.");
            }

            JTextArea area = new JTextArea(details.toString());
            area.setEditable(false);
            area.setFont(new Font("Monospaced", Font.PLAIN, 14));
            area.setCaretPosition(0);
            area.setMargin(new Insets(12, 12, 12, 12));

            JScrollPane scroll = new JScrollPane(area);
            scroll.setPreferredSize(new Dimension(480, 320));

            JOptionPane.showMessageDialog(
                    this,
                    scroll,
                    "Election Results",
                    JOptionPane.INFORMATION_MESSAGE
            );

        } catch (SQLException ex) {
            showError("Could not load election results.", ex);
        }
    }

    private void showError(String message, SQLException ex) {
        JOptionPane.showMessageDialog(
                this,
                message + "\n" + ex.getMessage(),
                "Database Error",
                JOptionPane.ERROR_MESSAGE
        );
    }

    private JButton createButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("SansSerif", Font.BOLD, 16));
        button.setBackground(blue);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setOpaque(true);
        button.setBorderPainted(false);
        button.setCursor(
                Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
        );
        return button;
    }

    private static class Candidate {
        private final int id;
        private final String name;
        private final String party;

        private Candidate(int id, String name, String party) {
            this.id = id;
            this.name = name;
            this.party = party;
        }

        @Override
        public String toString() {
            return name + " — " + party;
        }
    }
}
