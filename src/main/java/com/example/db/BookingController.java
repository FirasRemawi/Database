package com.example.db;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class BookingController {

    public Button back;
    @FXML
    private Label movieTitleLabel;

    @FXML
    private Label movieGenreLabel;

    @FXML
    private Label movieDurationLabel;

    @FXML
    private TextArea movieDescriptionArea;

    @FXML
    private ImageView moviePoster;

    @FXML
    private GridPane seatGrid;

    @FXML
    private VBox foodMenuPanel;

    @FXML
    private VBox cartPanel;

    @FXML
    private Label countdownTimerLabel;

    @FXML
    private Button confirmBookingButton;

    private Movie selectedMovie;
    private final Set<String> selectedSeats = new HashSet<>();
    private final Map<String, Double> cartItems = new HashMap<>();
    private int countdownTime = 300; // 5 minutes
    private Timeline countdown;

    @FXML
    public void initialize() {
         confirmBookingButton.setOnAction(event ->{
            if(selectedSeats.isEmpty()){
                showAlert(Alert.AlertType.ERROR,"Error","Please select a seat");
                return;
            }
            handleConfirmBooking();
            back.setOnAction(e->back());
        });
    }
    void back(){
        {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("user-dashboard.fxml"));
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
    public void setMovie(Movie movie) {
        this.selectedMovie = movie;

        // Set movie details
        movieTitleLabel.setText(movie.getTitle());
        movieGenreLabel.setText("Genre: " + movie.getGenre());
        movieDurationLabel.setText("Duration: " + movie.getDuration() + " mins");
        movieDescriptionArea.setText(movie.getDescription());
        moviePoster.setImage(new Image(movie.getPosterPath()));

        setupSeatGrid(); // Ensure the seat grid is displayed
        loadFoodMenuFromDatabase();
        startCountdownTimer();
    }

    private void setupSeatGrid() {
        int rows = 5;
        int cols = 10;

        // Clear the grid to avoid duplicates
        seatGrid.getChildren().clear();

        try (Connection conn = connectToDatabase()) {
            // Fetch reserved seats for the selected movie's show ID
            String query = "SELECT SeatPosition FROM ReservedSeats WHERE ShowID = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, selectedMovie.getShowId());
            ResultSet rs = stmt.executeQuery();

            Set<String> reservedSeats = new HashSet<>();
            while (rs.next()) {
                reservedSeats.add(rs.getString("SeatPosition"));
            }

            // Populate the grid with seat rectangles
            for (int row = 0; row < rows; row++) {
                for (int col = 0; col < cols; col++) {
                    Rectangle seat = new Rectangle(30, 30, Color.GREEN);
                    seat.setArcWidth(10);
                    seat.setArcHeight(10);

                    String seatPosition = row + "-" + col;

                    // Disable reserved seats
                    if (reservedSeats.contains(seatPosition)) {
                        seat.setFill(Color.RED); // Mark as reserved
                        seat.setDisable(true);
                    } else {
                        seat.setOnMouseEntered(event -> {
                            if (!selectedSeats.contains(seatPosition)) {
                                seat.setFill(Color.LIGHTGREEN); // Hover color
                            }
                        });
                        seat.setOnMouseExited(event -> {
                            if (!selectedSeats.contains(seatPosition)) {
                                seat.setFill(Color.GREEN);
                            }
                        });
                        seat.setOnMouseClicked(event -> toggleSeatSelection(seat, seatPosition));
                    }

                    seatGrid.add(seat, col, row);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void toggleSeatSelection(Rectangle seat, String seatPosition) {
        if (selectedSeats.contains(seatPosition)) {
            selectedSeats.remove(seatPosition);
            seat.setFill(Color.GREEN);
        } else {
            selectedSeats.add(seatPosition);
            seat.setFill(Color.BLUE); // Selected color
        }
    }

    private void addToCart(String itemName, double price) {
        cartItems.put(itemName, price);
        updateCartDisplay();
    }

    private void loadFoodMenuFromDatabase() {
        try (Connection conn = connectToDatabase();
             PreparedStatement stmt = conn.prepareStatement("SELECT Name, Price FROM FoodItem");
             ResultSet rs = stmt.executeQuery()) {

            foodMenuPanel.getChildren().clear(); // Clear old items

            while (rs.next()) {
                String foodName = rs.getString("Name");
                double price = rs.getDouble("Price");

                HBox foodItem = new HBox(10);
                Label foodLabel = new Label(foodName);
                foodLabel.setPrefWidth(150);
                Label priceLabel = new Label("$" + price);
                Button addButton = new Button("Add");

                addButton.setOnAction(event -> addToCart(foodName, price));

                foodItem.getChildren().addAll(foodLabel, priceLabel, addButton);
                foodMenuPanel.getChildren().add(foodItem);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateCartDisplay() {
        cartPanel.getChildren().clear();

        for (Map.Entry<String, Double> entry : cartItems.entrySet()) {
            String itemName = entry.getKey();
            Double price = entry.getValue();

            HBox cartItem = new HBox(10);
            Label itemLabel = new Label(itemName);
            Label priceLabel = new Label("$" + price);
            Button removeButton = new Button("Remove");

            removeButton.setOnAction(event -> {
                cartItems.remove(itemName);
                updateCartDisplay();
            });

            cartItem.getChildren().addAll(itemLabel, priceLabel, removeButton);
            cartPanel.getChildren().add(cartItem);
        }
    }

    private void startCountdownTimer() {
        countdown = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
            if (countdownTime > 0) {
                countdownTime--;
                countdownTimerLabel.setText("Time Remaining: " + countdownTime / 60 + ":" + String.format("%02d", countdownTime % 60));
            } else {
                countdown.stop();
                Alert timeoutAlert = new Alert(Alert.AlertType.ERROR);
                timeoutAlert.setTitle("Session Expired");
                timeoutAlert.setHeaderText(null);
                timeoutAlert.setContentText("Your session has expired. Please restart the booking process.");
                timeoutAlert.showAndWait();
            }
        }));
        countdown.setCycleCount(Timeline.INDEFINITE);
        countdown.play();
    }

    @FXML
    private void handleConfirmBooking() {
        User loggedInUser = CinemaDatabaseController.getLoggedInUserDetails();
        if (loggedInUser == null) {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to retrieve user details.");
            return;
        }

        try (Connection conn = connectToDatabase()) {
            String selectedSeatsString = String.join(", ", selectedSeats);
            double totalPrice = cartItems.values().stream().mapToDouble(Double::doubleValue).sum();

            String bookingSQL = "INSERT INTO Booking (BookingDate, NumberOfSeats, CustomerID, ShowID) VALUES (?, ?, ?, ?)";
            PreparedStatement pstmt = conn.prepareStatement(bookingSQL);

            pstmt.setString(1, LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
            pstmt.setInt(2, selectedSeats.size());
            pstmt.setInt(3, loggedInUser.getUserId());
            pstmt.setInt(4, selectedMovie.getShowId());

            pstmt.executeUpdate();

            // Reserve seats
            String reserveSeatSQL = "INSERT INTO ReservedSeats (ShowID, SeatPosition) VALUES (?, ?)";
            PreparedStatement reserveStmt = conn.prepareStatement(reserveSeatSQL);

            for (String seat : selectedSeats) {
                reserveStmt.setInt(1, selectedMovie.getShowId());
                reserveStmt.setString(2, seat);
                reserveStmt.addBatch();
            }
            reserveStmt.executeBatch();

            // Load confirmation scene
            FXMLLoader loader = new FXMLLoader(getClass().getResource("confirmation-scene.fxml"));
            Parent root = loader.load();

            ConfirmationController controller = loader.getController();
            controller.setConfirmationDetails(selectedSeatsString, totalPrice, selectedMovie.getTitle(), loggedInUser.getName(),selectedMovie.getPosterPath());

            Stage stage = (Stage) confirmBookingButton.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Booking Confirmation");

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to confirm booking.");
        }
    }

    private Connection connectToDatabase() throws Exception {
        String dbURL = "jdbc:mysql://localhost:3306/CinemaDB";
        String username = "root";
        String password = "1111";
        return DriverManager.getConnection(dbURL, username, password);
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
