package com.example.db;

import javafx.animation.*;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.*;
import java.util.Base64;

public class SignUpController {

    @FXML
    private VBox mainContainer;

    @FXML
    private Label titleLabel;

    @FXML
    private TextField nameField;

    @FXML
    private TextField contactNumberField;

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private PasswordField confirmPasswordField;

    @FXML
    private Button signupButton;

    @FXML
    private Button cancelButton;

    private Connection con;

    @FXML
    public void initialize() {
        // Add button event handlers
        signupButton.setOnAction(event -> handleSignUp());
        cancelButton.setOnAction(event -> {
            try {
                handleCancel();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        // Play animations for the screen
        playIntroAnimation();
    }

    private void handleSignUp() {
        String name = nameField.getText().trim();
        String contactNumber = contactNumberField.getText().trim();
        String email = emailField.getText().trim();
        String password = passwordField.getText().trim();
        String confirmPassword = confirmPasswordField.getText().trim();

        // Validate inputs
        if (name.isEmpty() || contactNumber.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            showAlert(AlertType.ERROR, "Sign-Up Failed", "All fields are required.");
            return;
        }

        if (!password.equals(confirmPassword)) {
            showAlert(AlertType.ERROR, "Sign-Up Failed", "Passwords do not match.");
            return;
        }

        if (!isValidEmail(email)) {
            showAlert(AlertType.ERROR, "Sign-Up Failed", "Invalid email format.");
            return;
        }

        if (!isUniqueEmail(email)) {
            showAlert(AlertType.ERROR, "Sign-Up Failed", "Email is already registered.");
            return;
        }

        // Hash the password for security
        String hashedPassword = hashPassword(password);

        // Save user details to the database
        boolean success = registerUser(name, contactNumber, email, hashedPassword);
        if (success) {
            showAlert(AlertType.INFORMATION, "Sign-Up Successful", "Your account has been created.");
            clearFields();
        } else {
            showAlert(AlertType.ERROR, "Sign-Up Failed", "An error occurred while creating your account.");
        }
    }

    private void handleCancel() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("hello-view.fxml"));
        Parent root = loader.load();
        Scene scene = new Scene(root, 900, 600);
        Stage primaryStage = (Stage) signupButton.getScene().getWindow();
        primaryStage.setScene(scene);
        primaryStage.setResizable(true);
        primaryStage.setMaximized(true);
        primaryStage.show();
    }

    private boolean registerUser(String name, String contactNumber, String email, String hashedPassword) {
        try {
            connectDB();

            // Prepare SQL Insert Statement
            String SQL = "INSERT INTO Customer (Name, ContactNumber, Email, Password) VALUES (?, ?, ?, ?)";
            PreparedStatement pstmt = con.prepareStatement(SQL);

            pstmt.setString(1, name);
            pstmt.setString(2, contactNumber);
            pstmt.setString(3, email);
            pstmt.setString(4, hashedPassword);

            int rowsAffected = pstmt.executeUpdate();
            pstmt.close();
            con.close();

            return rowsAffected > 0;
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean isUniqueEmail(String email) {
        try {
            connectDB();

            // Prepare SQL Query to Check for Duplicate Email
            String SQL = "SELECT COUNT(*) FROM Customer WHERE Email = ?";
            PreparedStatement pstmt = con.prepareStatement(SQL);

            pstmt.setString(1, email);
            ResultSet rs = pstmt.executeQuery();

            rs.next();
            boolean isUnique = rs.getInt(1) == 0;

            rs.close();
            pstmt.close();
            con.close();

            return isUnique;
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean isValidEmail(String email) {
        return email.matches("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}");
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

    private void connectDB() throws SQLException, ClassNotFoundException {
        String dbURL = "jdbc:mysql://localhost:3306/CinemaDB";
        String dbUsername = "root";
        String dbPassword = "1111";
        Class.forName("com.mysql.cj.jdbc.Driver");
        con = DriverManager.getConnection(dbURL, dbUsername, dbPassword);
    }

    private void clearFields() {
        nameField.clear();
        contactNumberField.clear();
        emailField.clear();
        passwordField.clear();
        confirmPasswordField.clear();
    }

    private void showAlert(AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void playIntroAnimation() {
        // Fade in the entire screen
        FadeTransition fadeIn = new FadeTransition(Duration.seconds(2), mainContainer);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);

        // Slide down the title
        TranslateTransition slideTitle = new TranslateTransition(Duration.seconds(1), titleLabel);
        slideTitle.setFromY(-50);
        slideTitle.setToY(0);

        // Play both animations in parallel
        ParallelTransition introAnimation = new ParallelTransition(fadeIn, slideTitle);
        introAnimation.play();
    }
}
