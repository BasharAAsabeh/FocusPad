package com.example;

import java.sql.Date;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Database {
    private static final String URL = "jdbc:mysql://localhost:3306/focuspad";
    private static final String USER = "focususer";
    private static final String PASSWORD = "1234";

    public record UserRecord(String username, String email, String password, String avatarPath) {
    }

    public record TaskRecord(int id, String title, String priority, LocalDate deadline, boolean completed) {
    }

    public record StudySessionRecord(int id, String title, String details) {
    }

    private Database() {
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    public static void initializeSchema() throws SQLException {
        try (Connection connection = getConnection(); Statement statement = connection.createStatement()) {
            statement.executeUpdate("""
                CREATE TABLE IF NOT EXISTS users (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    username VARCHAR(100) NOT NULL,
                    email VARCHAR(150) NOT NULL UNIQUE,
                    password VARCHAR(255) NOT NULL,
                    avatar_path VARCHAR(255)
                )
                """);

            ensureColumnExists(connection, "users", "username", "VARCHAR(100) NOT NULL DEFAULT 'User'");
            ensureColumnExists(connection, "users", "avatar_path", "VARCHAR(255) NULL");
            ensureColumnExists(connection, "users", "password", "VARCHAR(255) NOT NULL");
            ensureColumnExists(connection, "users", "email", "VARCHAR(150) NOT NULL");

            statement.executeUpdate("""
                CREATE TABLE IF NOT EXISTS tasks (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    user_email VARCHAR(150) NOT NULL,
                    title VARCHAR(255) NOT NULL,
                    priority VARCHAR(20) NOT NULL,
                    deadline DATE NOT NULL,
                    completed BOOLEAN NOT NULL DEFAULT FALSE,
                    FOREIGN KEY (user_email) REFERENCES users(email) ON DELETE CASCADE
                )
                """);

            ensureColumnExists(connection, "tasks", "user_email", "VARCHAR(150) NOT NULL DEFAULT 'bashar@example.com'");
            ensureColumnExists(connection, "tasks", "title", "VARCHAR(255) NOT NULL DEFAULT 'Untitled task'");
            ensureColumnExists(connection, "tasks", "priority", "VARCHAR(20) NOT NULL DEFAULT 'Medium'");
            ensureColumnExists(connection, "tasks", "deadline", "DATE NOT NULL DEFAULT '2026-06-06'");
            ensureColumnExists(connection, "tasks", "completed", "BOOLEAN NOT NULL DEFAULT FALSE");

            statement.executeUpdate("""
                CREATE TABLE IF NOT EXISTS study_sessions (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    user_email VARCHAR(150) NOT NULL,
                    title VARCHAR(255) NOT NULL,
                    details VARCHAR(255) NOT NULL,
                    FOREIGN KEY (user_email) REFERENCES users(email) ON DELETE CASCADE
                )
                """);

            ensureColumnExists(connection, "study_sessions", "user_email", "VARCHAR(150) NOT NULL DEFAULT 'bashar@example.com'");
            ensureColumnExists(connection, "study_sessions", "title", "VARCHAR(255) NOT NULL DEFAULT 'Study session'");
            ensureColumnExists(connection, "study_sessions", "details", "VARCHAR(255) NOT NULL DEFAULT ''");
        }
    }

    private static void ensureColumnExists(Connection connection, String tableName, String columnName, String definition) throws SQLException {
        String sql = """
                SELECT COUNT(*)
                FROM INFORMATION_SCHEMA.COLUMNS
                WHERE TABLE_SCHEMA = DATABASE()
                  AND TABLE_NAME = ?
                  AND COLUMN_NAME = ?
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, tableName);
            statement.setString(2, columnName);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next() && resultSet.getInt(1) == 0) {
                    try (Statement alter = connection.createStatement()) {
                        alter.executeUpdate("ALTER TABLE " + tableName + " ADD COLUMN " + columnName + " " + definition);
                    }
                }
            }
        }
    }

    public static UserRecord findUserByEmail(String email) throws SQLException {
        String sql = "SELECT username, email, password, avatar_path FROM users WHERE email = ?";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, email);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return new UserRecord(
                            resultSet.getString("username"),
                            resultSet.getString("email"),
                            resultSet.getString("password"),
                            resultSet.getString("avatar_path")
                    );
                }
            }
        }
        return null;
    }

    public static boolean userExists(String email) throws SQLException {
        String sql = "SELECT 1 FROM users WHERE email = ?";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, email);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        }
    }

    public static void insertUser(String username, String email, String password) throws SQLException {
        String sql = "INSERT INTO users (username, email, password) VALUES (?, ?, ?)";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, username);
            statement.setString(2, email);
            statement.setString(3, password);
            statement.executeUpdate();
        }
    }

    public static List<TaskRecord> findTasksByUserEmail(String email) throws SQLException {
        String sql = """
                SELECT id, title, priority, deadline, completed
                FROM tasks
                WHERE user_email = ?
                ORDER BY id
                """;
        List<TaskRecord> tasks = new ArrayList<>();
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, email);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    tasks.add(new TaskRecord(
                            resultSet.getInt("id"),
                            resultSet.getString("title"),
                            resultSet.getString("priority"),
                            resultSet.getDate("deadline").toLocalDate(),
                            resultSet.getBoolean("completed")
                    ));
                }
            }
        }
        return tasks;
    }

    public static int insertTask(String email, String title, String priority, LocalDate deadline, boolean completed) throws SQLException {
        String sql = "INSERT INTO tasks (user_email, title, priority, deadline, completed) VALUES (?, ?, ?, ?, ?)";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, email);
            statement.setString(2, title);
            statement.setString(3, priority);
            statement.setDate(4, Date.valueOf(deadline));
            statement.setBoolean(5, completed);
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
            return 0;
        }
    }

    public static void updateTaskTitle(String email, int taskId, String title) throws SQLException {
        String sql = "UPDATE tasks SET title = ? WHERE id = ? AND user_email = ?";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, title);
            statement.setInt(2, taskId);
            statement.setString(3, email);
            statement.executeUpdate();
        }
    }

    public static void updateTaskCompleted(String email, int taskId, boolean completed) throws SQLException {
        String sql = "UPDATE tasks SET completed = ? WHERE id = ? AND user_email = ?";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setBoolean(1, completed);
            statement.setInt(2, taskId);
            statement.setString(3, email);
            statement.executeUpdate();
        }
    }

    public static void deleteTask(String email, int taskId) throws SQLException {
        String sql = "DELETE FROM tasks WHERE id = ? AND user_email = ?";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, taskId);
            statement.setString(2, email);
            statement.executeUpdate();
        }
    }

    public static List<StudySessionRecord> findStudySessionsByUserEmail(String email) throws SQLException {
        String sql = """
                SELECT id, title, details
                FROM study_sessions
                WHERE user_email = ?
                ORDER BY id
                """;
        List<StudySessionRecord> sessions = new ArrayList<>();
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, email);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    sessions.add(new StudySessionRecord(
                            resultSet.getInt("id"),
                            resultSet.getString("title"),
                            resultSet.getString("details")
                    ));
                }
            }
        }
        return sessions;
    }

    public static int insertStudySession(String email, String title, String details) throws SQLException {
        String sql = "INSERT INTO study_sessions (user_email, title, details) VALUES (?, ?, ?)";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, email);
            statement.setString(2, title);
            statement.setString(3, details);
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
            return 0;
        }
    }

    public static void updateUserProfile(String email, String username, String password, String avatarPath) throws SQLException {
        String sql = """
                UPDATE users
                SET username = ?, password = ?, avatar_path = ?
                WHERE email = ?
                """;
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, username);
            statement.setString(2, password);
            statement.setString(3, avatarPath);
            statement.setString(4, email);
            statement.executeUpdate();
        }
    }
}
