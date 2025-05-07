package com.example.db;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.stage.Stage;

import java.sql.*;

public class ManageUsers {

    public Button backbt;
    @FXML
    private TableView<User> usersTable;

    @FXML
    private TableColumn<User, Integer> colUserID;

    @FXML
    private TableColumn<User, String> colName;

    @FXML
    private TableColumn<User, String> colEmail;

    @FXML
    private TableColumn<User, String> colContactNumber;

    @FXML
    private Button deleteButton;

    @FXML
    private Button refreshButton;

    private final ObservableList<User> data = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // Initialize table columns
        colUserID.setCellValueFactory(new PropertyValueFactory<>("userId"));
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colContactNumber.setCellValueFactory(new PropertyValueFactory<>("contactNumber"));

        // Make columns editable
        usersTable.setEditable(true);
        colName.setCellFactory(TextFieldTableCell.forTableColumn());
        colName.setOnEditCommit(t -> updateUserField(t.getRowValue().getUserId(), "Name", t.getNewValue()));

        colEmail.setCellFactory(TextFieldTableCell.forTableColumn());
        colEmail.setOnEditCommit(t -> updateUserField(t.getRowValue().getUserId(), "Email", t.getNewValue()));

        colContactNumber.setCellFactory(TextFieldTableCell.forTableColumn());
        colContactNumber.setOnEditCommit(t -> updateUserField(t.getRowValue().getUserId(), "ContactNumber", t.getNewValue()));

        // Load initial data
        loadUsers();

        // Button actions
        deleteButton.setOnAction(this::handleDeleteUser);
        refreshButton.setOnAction(e -> loadUsers());
        backbt.setOnAction(e -> back());
    }
    @FXML
    void back(){
        {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("admin-dashboard.fxml"));
                Parent root = loader.load();
                Scene scene = new Scene(root);
                Stage primaryStage = (Stage) backbt.getScene().getWindow();
                primaryStage.setScene(scene);
                primaryStage.setResizable(true);
                primaryStage.setMaximized(true);
                primaryStage.show();
            } catch (Exception e) {
                 showAlert( "Navigation Error", "Failed to load the next scene.");
            }
        }
    }
    private void loadUsers() {
        data.clear();
        try (Connection con = connectDB();
             PreparedStatement stmt = con.prepareStatement("SELECT * FROM Customer ORDER BY CustomerID");
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                data.add(new User(
                        rs.getInt("CustomerID"),
                        rs.getString("Name"),
                        rs.getString("Email"),
                        rs.getString("ContactNumber")
                ));
            }
        } catch (SQLException | ClassNotFoundException e) {
            showAlert("Error", "Failed to load users. Please check your database connection.");
        }
        usersTable.setItems(data);
    }

    private void handleDeleteUser(ActionEvent event) {
        User selectedUser = usersTable.getSelectionModel().getSelectedItem();
        if (selectedUser != null) {
            deleteUser(selectedUser.getUserId());
            data.remove(selectedUser);
        } else {
            showAlert("No Selection", "Please select a user to delete.");
        }
    }

    private Connection connectDB() throws SQLException, ClassNotFoundException {
        String dbURL = "jdbc:mysql://localhost:3306/CinemaDB";
        String dbUsername = "root";
        String dbPassword = "1111";
        Class.forName("com.mysql.cj.jdbc.Driver");
        return DriverManager.getConnection(dbURL, dbUsername, dbPassword);
    }

    private void updateUserField(int userId, String field, Object value) {
        try (Connection con = connectDB();
             PreparedStatement stmt = con.prepareStatement("UPDATE Customer SET " + field + " = ? WHERE CustomerID = ?")) {
            stmt.setObject(1, value);
            stmt.setInt(2, userId);
            stmt.executeUpdate();
            showAlert("Success", field + " updated successfully for User ID: " + userId);
        } catch (SQLException | ClassNotFoundException e) {
            showAlert("Error", "Failed to update user in the database.");
        }
    }

    private void deleteUser(int userId) {
        try (Connection con = connectDB();
             PreparedStatement stmt = con.prepareStatement("DELETE FROM Customer WHERE CustomerID = ?")) {
            stmt.setInt(1, userId);
            stmt.executeUpdate();
            showAlert("Success", "User with ID: " + userId + " deleted successfully.");
        } catch (SQLException | ClassNotFoundException e) {
            showAlert("Error", "Failed to delete user from the database.");
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
