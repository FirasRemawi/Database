package com.example.db;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class CinemaDatabaseApp extends Application {

	public static void main(String[] args) {
		launch(args); // JavaFX application entry point
	}

	@Override
	public void start(Stage primaryStage) {
		try {
			primaryStage.setTitle("Cinema Database - Login");

			// Load the FXML file
			FXMLLoader loader = new FXMLLoader(getClass().getResource("hello-view.fxml"));
			Parent root = loader.load();

			// Set up the scene
			Scene scene = new Scene(root, 900, 600);
			primaryStage.setScene(scene);
			primaryStage.setMaximized(true);
			primaryStage.setResizable(false); // Prevent resizing
			primaryStage.show(); // Display the stage
		} catch (Exception e) {
			e.printStackTrace(); // Log errors for debugging
		}
	}
}
