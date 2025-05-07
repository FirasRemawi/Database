package com.example.db;

import javafx.collections.*;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.util.converter.IntegerStringConverter;

import java.sql.*;

public class ManageMoviesController {

    public Button back;
    public BorderPane bp;
    @FXML
    private TableView<Movie> moviesTable;

    @FXML
    private TableColumn<Movie, Integer> colMovieID;

    @FXML
    private TableColumn<Movie, String> colTitle;

    @FXML
    private TableColumn<Movie, String> colDescription;

    @FXML
    private TableColumn<Movie, String> colGenre;

    @FXML
    private TableColumn<Movie, Integer> colDuration;

    @FXML
    private TableColumn<Movie, Integer> colShowID;

    @FXML
    private TextField addTitle;

    @FXML
    private TextField addDescription;

    @FXML
    private TextField addGenre;

    @FXML
    private TextField addDuration;

    @FXML
    private TextField showID;

    @FXML
    private Button addButton;

    @FXML
    private Button deleteButton;

    @FXML
    private Button refreshButton;

    private final ObservableList<Movie> data = FXCollections.observableArrayList();
    private Connection con;

    @FXML
    public void initialize() {
        // Initialize table columns
        colMovieID.setCellValueFactory(new PropertyValueFactory<>("movieId"));
        colTitle.setCellValueFactory(new PropertyValueFactory<>("title"));
        colDescription.setCellValueFactory(new PropertyValueFactory<>("description"));
        colGenre.setCellValueFactory(new PropertyValueFactory<>("genre"));
        colDuration.setCellValueFactory(new PropertyValueFactory<>("duration"));
        colShowID.setCellValueFactory(new PropertyValueFactory<>("showId"));

        // Make columns editable
        moviesTable.setEditable(true);
        colTitle.setCellFactory(TextFieldTableCell.forTableColumn());
        colTitle.setOnEditCommit(t -> updateMovieField(t.getRowValue().getMovieId(), "Title", t.getNewValue()));

        colDescription.setCellFactory(TextFieldTableCell.forTableColumn());
        colDescription.setOnEditCommit(t -> updateMovieField(t.getRowValue().getMovieId(), "Description", t.getNewValue()));

        colGenre.setCellFactory(TextFieldTableCell.forTableColumn());
        colGenre.setOnEditCommit(t -> updateMovieField(t.getRowValue().getMovieId(), "Genre", t.getNewValue()));

        colDuration.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter()));
        colDuration.setOnEditCommit(t -> updateMovieField(t.getRowValue().getMovieId(), "Duration", t.getNewValue()));

        colShowID.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter()));
        colShowID.setOnEditCommit(t -> updateMovieField(t.getRowValue().getMovieId(), "ShowID", t.getNewValue()));

        // Load initial data
        loadMovies();

        // Button actions
        addButton.setOnAction(this::handleAddMovie);
        deleteButton.setOnAction(this::handleDeleteMovie);
        refreshButton.setOnAction(e -> loadMovies());
        back.setOnAction(e->back());
    }
    void back(){
        {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("admin-dashboard.fxml"));
                Parent root = loader.load();
                Scene scene = new Scene(root, 1910, 1050);
                Stage primaryStage = (Stage) back.getScene().getWindow();
                primaryStage.setScene(scene);
                 primaryStage.setMaximized(true);
                primaryStage.show();
            } catch (Exception e) {
                e.printStackTrace();
                showAlert( "Navigation Error", "Failed to load the next scene.");
            }
        }
    }
    void loadMovies() {
        data.clear();
        try (Connection con = connectDB();
             PreparedStatement stmt = con.prepareStatement("SELECT * FROM Movie ORDER BY MovieID");
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                data.add(new Movie(
                        rs.getInt("MovieID"),
                        rs.getString("Title"),
                        rs.getString("Description"),
                        rs.getString("Genre"),
                        rs.getInt("Duration"),
                        rs.getInt("ShowID")
                ));
            }
        } catch (SQLException | ClassNotFoundException e) {
            showAlert("Error", "Failed to load movies. Please check your database connection.");
        }
        moviesTable.setItems(data);
    }

    private void handleAddMovie(ActionEvent event) {
        try {
            String title = addTitle.getText();
            String description = addDescription.getText();
            String genre = addGenre.getText();
            int duration = Integer.parseInt(addDuration.getText());
            int showId = Integer.parseInt(showID.getText());

            if (title.isEmpty() || description.isEmpty() || genre.isEmpty()) {
                showAlert("Invalid Input", "All fields are required.");
                return;
            }

                int movieId = getNextMovieId();

            Movie movie = new Movie(movieId, title, description, genre, duration, showId);

            if (insertMovie(movie))
                data.add(movie);
            else
                return;
            addTitle.clear();
            addDescription.clear();
            addGenre.clear();
            addDuration.clear();
            showID.clear();

        } catch (NumberFormatException e) {
            showAlert("Invalid Input", "Please enter valid numbers for duration and show ID.");
        }
    }

    private void handleDeleteMovie(ActionEvent event) {
        Movie selectedMovie = moviesTable.getSelectionModel().getSelectedItem();
        if (selectedMovie != null) {
            deleteMovie(selectedMovie.getMovieId());
            data.remove(selectedMovie);
         } else {
            showAlert("No Selection", "Please select a movie to delete.");
        }
    }

    private Connection connectDB() throws SQLException, ClassNotFoundException {
        String dbURL = "jdbc:mysql://localhost:3306/CinemaDB";
        String dbUsername = "root";
        String dbPassword = "1111";
        Class.forName("com.mysql.cj.jdbc.Driver");
        return DriverManager.getConnection(dbURL, dbUsername, dbPassword);
    }

    private int getNextMovieId() {
        int nextId = 1;
        try (Connection con = connectDB();
             PreparedStatement stmt = con.prepareStatement("SELECT MAX(MovieID) FROM Movie");
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                nextId = rs.getInt(1) + 1;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return nextId;
    }

    private boolean insertMovie(Movie movie) {
        try (Connection con = connectDB();
             PreparedStatement stmt = con.prepareStatement("INSERT INTO Movie (MovieID, Title, Description, Genre, Duration, ShowID) VALUES (?, ?, ?, ?, ?, ?)")) {
            stmt.setInt(1, movie.getMovieId());
            stmt.setString(2, movie.getTitle());
            stmt.setString(3, movie.getDescription());
            stmt.setString(4, movie.getGenre());
            stmt.setInt(5, movie.getDuration());
            stmt.setInt(6, movie.getShowId());
            stmt.executeUpdate();
            return true;
        } catch (SQLException | ClassNotFoundException e) {
            showAlert("Error", "Failed to add movie to the database.");
            return false;
        }
    }

    private void updateMovieField(int movieId, String field, Object value) {
        try (Connection con = connectDB();
             PreparedStatement stmt = con.prepareStatement("UPDATE Movie SET " + field + " = ? WHERE MovieID = ?")) {
            stmt.setObject(1, value);
            stmt.setInt(2, movieId);
            stmt.executeUpdate();
        } catch (SQLException | ClassNotFoundException e) {
            showAlert("Error", "Failed to update movie in the database.");
        }
    }

    private void deleteMovie(int movieId) {
        try (Connection con = connectDB();
             PreparedStatement stmt = con.prepareStatement("DELETE FROM Movie WHERE MovieID = ?")) {
            stmt.setInt(1, movieId);
            stmt.executeUpdate();
            loadMovies();
        } catch (SQLException | ClassNotFoundException e) {
            showAlert("Error", "Failed to delete movie from the database.");
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
