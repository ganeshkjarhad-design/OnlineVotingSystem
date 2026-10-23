
package com.onlinevoting;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Main extends JFrame {

    private final Color navy = new Color(25, 43, 77);
    private final Color blue = new Color(45, 100, 210);
    private final Color green = new Color(31, 145, 94);
    private final Color background = new Color(245, 247, 252);

    public Main() {
        setTitle("Online Voting System");
        setSize(800, 580);
        setMinimumSize(new Dimension(650, 500));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel root = new JPanel(new BorderLayout(20, 25));
        root.setBackground(background);
        root.setBorder(new EmptyBorder(30, 45, 25, 45));

        JPanel header = new JPanel();
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setBackground(navy);
        header.setBorder(new EmptyBorder(30, 20, 30, 20));

        JLabel title = new JLabel("ONLINE VOTING SYSTEM");
        title.setFont(new Font("SansSerif", Font.BOLD, 28));
        title.setForeground(Color.WHITE);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitle = new JLabel(
                "A simple and secure voting management application"
        );
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 15));
        subtitle.setForeground(Color.WHITE);
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        header.add(title);
        header.add(Box.createVerticalStrut(12));
        header.add(subtitle);

        JPanel menu = new JPanel(new GridLayout(3, 1, 0, 18));
        menu.setBackground(background);

        JButton adminButton = createButton("Admin Login", navy);
        JButton voterButton = createButton("Voter Login / Registration", blue);
        JButton resultsButton = createButton("View Election Results", green);

        menu.add(adminButton);
        menu.add(voterButton);
        menu.add(resultsButton);

        // Opens the real administrator login window.
        adminButton.addActionListener(e -> {
            new AdminLoginWindow().setVisible(true);
        });

        // Opens the existing voter login and registration window.
        voterButton.addActionListener(e -> {
            new VoterWindow().setVisible(true);
        });

        // Loads election results directly from MySQL.
        resultsButton.addActionListener(e -> {
            showElectionResults();
        });

        JLabel footer = new JLabel(
                "Online Voting System | Java Swing and MySQL",
                SwingConstants.CENTER
        );
        footer.setForeground(new Color(95, 105, 120));
        footer.setFont(new Font("SansSerif", Font.PLAIN, 12));

        root.add(header, BorderLayout.NORTH);
        root.add(menu, BorderLayout.CENTER);
        root.add(footer, BorderLayout.SOUTH);

        setContentPane(root);
    }

    private JButton createButton(String text, Color color) {
        JButton button = new JButton(text);
        button.setFont(new Font("SansSerif", Font.BOLD, 17));
        button.setForeground(Color.WHITE);
        button.setBackground(color);
        button.setOpaque(true);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setCursor(
                Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
        );
        button.setPreferredSize(new Dimension(400, 60));
        return button;
    }

    private void showElectionResults() {
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
            table.setRowHeight(30);
            table.setFont(new Font("SansSerif", Font.PLAIN, 14));
            table.getTableHeader().setFont(
                    new Font("SansSerif", Font.BOLD, 14)
            );
            table.setAutoCreateRowSorter(true);

            JFrame resultsWindow = new JFrame("Election Results");
            resultsWindow.setSize(650, 420);
            resultsWindow.setLocationRelativeTo(this);
            resultsWindow.setDefaultCloseOperation(
                    JFrame.DISPOSE_ON_CLOSE
            );

            JPanel panel = new JPanel(new BorderLayout(15, 15));
            panel.setBorder(new EmptyBorder(20, 20, 20, 20));
            panel.setBackground(background);

            JLabel heading = new JLabel("ELECTION RESULTS");
            heading.setFont(
                    new Font("SansSerif", Font.BOLD, 23)
            );
            heading.setForeground(navy);

            JLabel summary = new JLabel(
                    "Candidates: " + model.getRowCount()
                            + "     Total votes: " + totalVotes
            );
            summary.setFont(
                    new Font("SansSerif", Font.PLAIN, 14)
            );

            JPanel top = new JPanel(new BorderLayout(5, 8));
            top.setBackground(background);
            top.add(heading, BorderLayout.NORTH);
            top.add(summary, BorderLayout.SOUTH);

            panel.add(top, BorderLayout.NORTH);
            panel.add(new JScrollPane(table), BorderLayout.CENTER);

            JButton closeButton = new JButton("Close");
            closeButton.addActionListener(e -> resultsWindow.dispose());

            JPanel bottom = new JPanel(
                    new FlowLayout(FlowLayout.RIGHT)
            );
            bottom.setBackground(background);
            bottom.add(closeButton);

            panel.add(bottom, BorderLayout.SOUTH);

            resultsWindow.setContentPane(panel);
            resultsWindow.setVisible(true);

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

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(
                        UIManager.getSystemLookAndFeelClassName()
                );
            } catch (Exception ignored) {
                // Use the default Swing appearance if unavailable.
            }

            new Main().setVisible(true);
        });
    }
}
