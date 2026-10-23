
package com.onlinevoting;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class VoterService {

    public boolean registerVoter(
            String voterCardNo,
            String fullName,
            String username,
            String password) throws SQLException {

        String sql = """
            INSERT INTO voters
                (voter_card_no, full_name, username, password_hash)
            VALUES (?, ?, ?, ?)
            """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setString(1, voterCardNo.trim());
            statement.setString(2, fullName.trim());
            statement.setString(3, username.trim());
            statement.setString(4, hashPassword(password));

            return statement.executeUpdate() == 1;
        }
    }

    public boolean loginVoter(
            String username,
            String password) throws SQLException {

        String sql = """
            SELECT password_hash
            FROM voters
            WHERE username = ? AND is_active = TRUE
            """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setString(1, username.trim());

            try (ResultSet result = statement.executeQuery()) {
                if (!result.next()) {
                    return false;
                }

                String savedHash = result.getString("password_hash");
                return savedHash.equals(hashPassword(password));
            }
        }
    }

    public int getVoterId(String username) throws SQLException {
        String sql = """
            SELECT voter_id
            FROM voters
            WHERE username = ? AND is_active = TRUE
            """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setString(1, username.trim());

            try (ResultSet result = statement.executeQuery()) {
                if (result.next()) {
                    return result.getInt("voter_id");
                }
                return -1;
            }
        }
    }

    public boolean hasVoted(int voterId) throws SQLException {
        String sql = "SELECT 1 FROM votes WHERE voter_id = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setInt(1, voterId);

            try (ResultSet result = statement.executeQuery()) {
                return result.next();
            }
        }
    }

    public boolean castVote(
            int voterId,
            int candidateId) throws SQLException {

        String sql = """
            INSERT INTO votes (voter_id, candidate_id)
            SELECT ?, candidate_id
            FROM candidates
            WHERE candidate_id = ? AND is_active = TRUE
            """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, voterId);
            statement.setInt(2, candidateId);

            return statement.executeUpdate() == 1;

        } catch (SQLException e) {
            // MySQL error 1062 means a duplicate unique-key value.
            if (e.getErrorCode() == 1062) {
                throw new SQLException(
                        "This voter has already cast a vote.", e);
            }
            throw e;
        }
    }

    private String hashPassword(String password) {
        try {
            MessageDigest digest =
                    MessageDigest.getInstance("SHA-256");

            byte[] hash = digest.digest(
                    password.getBytes(
                            java.nio.charset.StandardCharsets.UTF_8));

            StringBuilder hex = new StringBuilder();

            for (byte b : hash) {
                hex.append(String.format("%02x", b & 0xff));
            }

            return hex.toString();

        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(
                    "Password hashing is unavailable.", e);
        }
    }
}
