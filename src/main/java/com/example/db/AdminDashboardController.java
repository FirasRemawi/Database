package com.example.db;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.stage.Stage;

import java.io.IOException;

public class AdminDashboardController {

    @FXML
    private Button manageMoviesButton;

    @FXML
    private Button manageUsersButton;

    @FXML
    private Button viewReportsButton;


    @FXML
    private Button logoutButton;

    @FXML
    public void initialize() {
        // Set button actions
        manageMoviesButton.setOnAction(event -> openManageMovies());
        manageUsersButton.setOnAction(event -> openManageUsers());
        viewReportsButton.setOnAction(event -> openViewReports());
        logoutButton.setOnAction(event -> logout());
    }

    private void openManageMovies() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("manage-movies.fxml"));
            Parent root = loader.load();

            // Set the new scene
            Stage stage = (Stage) manageMoviesButton.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setHeight(scene.getWindow().getHeight());
            stage.setWidth(scene.getWindow().getWidth());
            stage.setTitle("Manage Movies");
            stage.setMaximized(true);
            stage.setResizable(true);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(AlertType.ERROR, "Error", "Failed to load the Manage Movies screen.");
        }
    }

    private void openManageUsers() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("manage-users.fxml"));
            Parent root = loader.load();
            // Set the new scene
            Stage stage = (Stage) manageUsersButton.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("Manage Movies");
            stage.setMaximized(true);
            stage.setResizable(true);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(AlertType.ERROR, "Error", "Failed to load the Manage Movies screen.");
        }
    }

    private void openViewReports() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("view-reports.fxml"));
            Parent root = loader.load();

            // Set the new scene
            Stage stage = (Stage) manageUsersButton.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("Manage Movies");
            stage.setMaximized(true);
            stage.setResizable(true);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(AlertType.ERROR, "Error", "Failed to load the Manage Movies screen.");
        }
    }


    private void logout() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("hello-view.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) logoutButton.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Cinema Management System - Login");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(AlertType.ERROR, "Error", "An error occurred while logging out.");
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
