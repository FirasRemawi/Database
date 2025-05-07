package com.example.db;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.Stage;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Base64;

public class CinemaDatabaseController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Button loginButton;

    @FXML
    private Button signupButton;
    // Static field to store the logged-in user's email
    private static String loggedInUserEmail;

    /**
     * Sets the logged-in user's email.
     *
     * @param email The email of the logged-in user.
     */
    public static void setLoggedInUserEmail(String email) {
        loggedInUserEmail = email;
    }

    /**
     * Retrieves the logged-in user's email.
     *
     * @return The email of the currently logged-in user.
     */
    public static String getLoggedInUserEmail() {
        return loggedInUserEmail;
    }

    /**
     * Retrieves the logged-in user's details from the database.
     *
     * @return A User object containing the user's details, or null if not found.
     */
    public static User getLoggedInUserDetails() {
        String dbURL = "jdbc:mysql://localhost:3306/CinemaDB";
        String username = "root";
        String password = "1111";

        try (Connection conn = DriverManager.getConnection(dbURL, username, password)) {
            String query = "SELECT * FROM Customer WHERE Email = ?";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setString(1, loggedInUserEmail);

            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                int id = rs.getInt("CustomerID");
                String name = rs.getString("Name");
                String email = rs.getString("Email");
                String phone = rs.getString("ContactNumber");

                return new User(id, name, email, phone);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    @FXML
    public void initialize() {
        // Add event handlers for buttons
        loginButton.setOnAction(event -> handleLogin());
        signupButton.setOnAction(event -> handleSignUp());
    }

    public void handleLogin() {
        String email = usernameField.getText().trim();
        String password = passwordField.getText().trim();

        if (email.isEmpty() || password.isEmpty()) {
            showAlert(AlertType.ERROR, "Login Failed", "Email and password cannot be empty.");
            return;
        }

        try {
            // Authenticate the user with the database
            String role = authenticateUser(email, password);

            if (role == null) {
                showAlert(AlertType.ERROR, "Login Failed", "Invalid email or password.");
            } else if (role.equals("Admin")) {
                showAlert(AlertType.INFORMATION, "Login Successful", "Welcome, Admin!");
                redirectToScene("admin-dashboard.fxml", "Admin Dashboard");
            } else if (role.equals("User")) {
                showAlert(AlertType.INFORMATION, "Login Successful", "Welcome, User!");
                redirectToScene("user-dashboard.fxml", "User Dashboard");
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(AlertType.ERROR, "Login Failed", "An error occurred during login.");
        }
    }

    private String authenticateUser(String email, String password) throws Exception {
        Connection con = null;
        try {
            String dbURL = "jdbc:mysql://localhost:3306/CinemaDB";
            String dbUsername = "root";
            String dbPassword = "1111";
            Class.forName("com.mysql.cj.jdbc.Driver");
            con = DriverManager.getConnection(dbURL, dbUsername, dbPassword);

            String SQL = "SELECT Password, Email FROM Customer WHERE Email = ?";
            PreparedStatement pstmt = con.prepareStatement(SQL);
            pstmt.setString(1, email);

            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                String storedPassword = rs.getString("Password");
                String hashedPassword = hashPassword(password);

                if (storedPassword.equals(hashedPassword)) {
                    loggedInUserEmail = email; // Store the email globally
                    return email.endsWith("@admin.com") ? "Admin" : "User";
                }
            }
        } finally {
            if (con != null) con.close();
        }
        return null;
    }
    private String hashPassword(String plainPassword) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hashedBytes = md.digest(plainPassword.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hashedBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error hashing password", e);
        }
    }
    private void redirectToScene(String fxmlFile, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
            Parent root = loader.load();
            Scene scene = new Scene(root, 1910, 1050);
            Stage primaryStage = (Stage) loginButton.getScene().getWindow();
            primaryStage.setScene(scene);
            primaryStage.setTitle(title);
            primaryStage.setResizable(true);
            primaryStage.setMaximized(true);
            primaryStage.show();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(AlertType.ERROR, "Navigation Error", "Failed to load the next scene.");
        }
    }

    public void handleSignUp() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("sign-up.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root, 900, 600);
            Stage primaryStage = (Stage) signupButton.getScene().getWindow();
            primaryStage.setScene(scene);
            primaryStage.setResizable(true);
            primaryStage.setMaximized(true);
            primaryStage.show();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(AlertType.ERROR, "Navigation Error", "Failed to load the sign-up screen.");
        }
    }

    private void showAlert(AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
