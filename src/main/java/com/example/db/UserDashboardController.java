package com.example.db;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class UserDashboardController {

    public Button back;
    @FXML
    private FlowPane movieContainer; // To hold the movie cards dynamically

    @FXML
    private ChoiceBox<String> genreFilter = new ChoiceBox<>();

    private final List<Movie> allMovies = new ArrayList<>();

    @FXML
    public void initialize() {
        loadGenres();
        loadMovies();

        genreFilter.setOnAction(event -> filterMovies());
        back.setOnAction(event -> back());
    }
    void back(){
        {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("hello-view.fxml"));
                Parent root = loader.load();
                Scene scene = new Scene(root, 900, 600);
                Stage primaryStage = (Stage) back.getScene().getWindow();
                primaryStage.setScene(scene);
                primaryStage.setMaximized(true);
                primaryStage.show();
            } catch (Exception e) {
                e.printStackTrace();
                showAlert( Alert.AlertType.ERROR,"Navigation Error", "Failed to load the next scene.");
            }
        }
    }
    private void loadGenres() {
        genreFilter.getItems().clear();
        genreFilter.getItems().add("All");
        try (Connection con = connectDB();
             PreparedStatement pstmt = con.prepareStatement("SELECT DISTINCT Genre FROM Movie");
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                genreFilter.getItems().add(rs.getString("Genre"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        genreFilter.setValue("All");
    }

    private void loadMovies() {
        allMovies.clear();
        try (Connection con = connectDB();
             PreparedStatement pstmt = con.prepareStatement("SELECT * FROM Movie");
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                allMovies.add(new Movie(
                        rs.getInt("MovieID"),
                        rs.getString("Title"),
                        rs.getString("Description"),
                        rs.getString("Genre"),
                        rs.getInt("Duration"),
                        rs.getInt("showId")
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        updateMovieCards(allMovies);
    }

    private void filterMovies() {
        String selectedGenre = genreFilter.getValue();
        if (selectedGenre.equals("All")) {
            updateMovieCards(allMovies);
        } else {
            List<Movie> filteredMovies = new ArrayList<>();
            for (Movie movie : allMovies) {
                if (movie.getGenre().equalsIgnoreCase(selectedGenre)) {
                    filteredMovies.add(movie);
                }
            }
            updateMovieCards(filteredMovies);
        }
    }

    private void updateMovieCards(List<Movie> movies) {
        movieContainer.getChildren().clear();

        for (Movie movie : movies) {
            VBox card = createMovieCard(movie);
            movieContainer.getChildren().add(card);
         }
    }

    private VBox createMovieCard(Movie movie) {
        VBox card = new VBox();
        card.setStyle("-fx-background-color: white; -fx-border-color: lightgray; -fx-padding: 10;");
        card.setPrefSize(200, 300);

        // Title
        Label title = new Label(movie.getTitle());
        title.setStyle("-fx-font-size: 16; -fx-font-weight: bold;");

        // Poster
        ImageView poster = new ImageView();
        poster.setFitWidth(180);
        poster.setFitHeight(240);
        try {
            poster.setImage(new Image(movie.getPosterPath()));
        } catch (Exception e) {
           // poster.setImage(new Image("path_to_images/placeholder.jpg")); // Fallback for missing posters
        }

        card.getChildren().addAll(poster, title);
        card.setOnMouseClicked(event -> redirectToBookingScene(movie));
        return card;
    }


    private void redirectToBookingScene(Movie movie) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("booking-scene.fxml"));
            Parent root = loader.load();

            // Get the controller instance
            BookingController controller = loader.getController();

            // Set the movie in the controller
            if (controller != null) {
                controller.setMovie(movie);
            } else {
                throw new NullPointerException("BookingController is null.");
            }

            // Set up the new scene
            Scene bookingScene = new Scene(root, 900, 600);
            Stage primaryStage = (Stage) movieContainer.getScene().getWindow();
            primaryStage.setMaximized(true);
            primaryStage.setResizable(true);
            primaryStage.setScene(bookingScene);
            primaryStage.setTitle("Book Ticket - " + movie.getTitle());
            primaryStage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Navigation Error", "Failed to load the booking scene.");
        }
    }


    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private Connection connectDB() throws Exception {
        String dbURL = "jdbc:mysql://localhost:3306/CinemaDB";
        String dbUsername = "root";
        String dbPassword = "1111";
        Class.forName("com.mysql.cj.jdbc.Driver");
        return DriverManager.getConnection(dbURL, dbUsername, dbPassword);
    }
}
