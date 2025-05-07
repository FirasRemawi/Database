package com.example.db;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.sql.*;

public class ViewReportsController {

    public DatePicker startDatePicker;
    public DatePicker endDatePicker;
    public Button back;
    @FXML
    private TableView<ReportEntry> salesReportTable, noBookingsTable, availableSeatsTable, foodRevenueTable;

    @FXML
    private TableColumn<ReportEntry, String> colMovieTitle, colNoBookingMovieTitle, colAvailableSeatsMovie, colFoodItem;

    @FXML
    private TableColumn<ReportEntry, Integer> colTicketsSold, colAvailableSeats;

    @FXML
    private TableColumn<ReportEntry, Double> colRevenue, colFoodRevenue;
    @FXML
    private BarChart<String, Number> moviePerformanceChart;

    @FXML
    private PieChart customerActivityChart;

    private final ObservableList<ReportEntry> salesData = FXCollections.observableArrayList();
    private final ObservableList<ReportEntry> noBookingData = FXCollections.observableArrayList();
    private final ObservableList<ReportEntry> availableSeatsData = FXCollections.observableArrayList();
    private final ObservableList<ReportEntry> foodRevenueData = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // Initialize column bindings for each table
        colMovieTitle.setCellValueFactory(new PropertyValueFactory<>("title"));
        colTicketsSold.setCellValueFactory(new PropertyValueFactory<>("ticketsSold"));
        colRevenue.setCellValueFactory(new PropertyValueFactory<>("revenue"));

        colNoBookingMovieTitle.setCellValueFactory(new PropertyValueFactory<>("title"));

        colAvailableSeatsMovie.setCellValueFactory(new PropertyValueFactory<>("title"));
        colAvailableSeats.setCellValueFactory(new PropertyValueFactory<>("availableSeats"));
        back.setOnAction(e->back());

        // Load initial data for all tabs
        loadSalesReport();
        loadMoviesWithNoBookings();
        loadMoviePerformanceData();
        loadCustomerActivityData();
        loadAvailableSeats();
    }
    void back(){
        {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("admin-dashboard.fxml"));
                Parent root = loader.load();
                Scene scene = new Scene(root, 900, 600);
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
    @FXML
    private void loadSalesReport() {
        salesData.clear();
        String query = """
                SELECT Movie.Title, SUM(NumberOfSeats) AS TicketsSold, SUM(NumberOfSeats * 10) AS Revenue
                FROM Booking
                JOIN CinemaShow ON Booking.ShowID = CinemaShow.ShowID
                JOIN Movie ON CinemaShow.ShowID = Movie.ShowID
                GROUP BY Movie.Title
                """;

        try (Connection con = connectDB();
             PreparedStatement stmt = con.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                salesData.add(new ReportEntry(rs.getString("Title"), rs.getInt("TicketsSold"), rs.getDouble("Revenue")));
            }
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        salesReportTable.setItems(salesData);
    }

    @FXML
    private void exportSalesReport() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Sales Report");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        var file = fileChooser.showSaveDialog(null);
        if (file != null) {
            try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
                writer.println("Movie Title,Revenue");
                for (ReportEntry entry : salesData) {
                    writer.printf("%s,%.2f%n", entry.getTitle(), entry.getRevenue());
                }
                showAlert("Success", "Sales report exported successfully.");
            } catch (Exception e) {
                showAlert("Error", "Failed to export sales report.");
                e.printStackTrace();
            }
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    private void loadMoviesWithNoBookings() {
        noBookingData.clear();
        String query = """
                SELECT Title
                FROM Movie
                WHERE ShowID NOT IN (SELECT DISTINCT ShowID FROM Booking)
                """;

        try (Connection con = connectDB();
             PreparedStatement stmt = con.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                noBookingData.add(new ReportEntry(rs.getString("Title")));
            }
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        noBookingsTable.setItems(noBookingData);
    }


    @FXML
    private void loadMoviePerformanceData() {
        moviePerformanceChart.getData().clear(); // Clear any existing data
        String query = """
                SELECT Movie.Title AS MovieTitle, SUM(NumberOfSeats) AS TicketsSold
                FROM Booking
                JOIN CinemaShow ON Booking.ShowID = CinemaShow.ShowID
                JOIN Movie ON CinemaShow.ShowID = Movie.ShowID
                GROUP BY Movie.Title
                ORDER BY TicketsSold DESC
                """;

        try (Connection con = connectDB();
             PreparedStatement stmt = con.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Tickets Sold");

            while (rs.next()) {
                String movieTitle = rs.getString("MovieTitle");
                int ticketsSold = rs.getInt("TicketsSold");
                series.getData().add(new XYChart.Data<>(movieTitle, ticketsSold));
            }

            moviePerformanceChart.getData().add(series);
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void loadCustomerActivityData() {
        customerActivityChart.getData().clear(); // Clear any existing data
        String query = """
                SELECT Customer.Name AS CustomerName, COUNT(Booking.BookingID) AS TotalBookings
                FROM Booking
                JOIN Customer ON Booking.CustomerID = Customer.CustomerID
                GROUP BY Customer.Name
                ORDER BY TotalBookings DESC
                """;

        try (Connection con = connectDB();
             PreparedStatement stmt = con.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                String customerName = rs.getString("CustomerName");
                int totalBookings = rs.getInt("TotalBookings");
                customerActivityChart.getData().add(new PieChart.Data(customerName, totalBookings));
            }
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }


    @FXML
    private void loadAvailableSeats() {
        availableSeatsData.clear();
        String query = """
                SELECT DISTINCT Movie.Title, 
                       (50 - COALESCE(SUM(Booking.NumberOfSeats), 0)) AS AvailableSeats
                FROM Movie
                JOIN CinemaShow ON Movie.ShowID = CinemaShow.ShowID
                LEFT JOIN Booking ON CinemaShow.ShowID = Booking.ShowID
                GROUP BY Movie.Title
                """;

        try (Connection con = connectDB();
             PreparedStatement stmt = con.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                availableSeatsData.add(new ReportEntry(rs.getString("Title"), rs.getInt("AvailableSeats")));
            }
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        availableSeatsTable.setItems(availableSeatsData);
    }

    private Connection connectDB() throws SQLException, ClassNotFoundException {
        String dbURL = "jdbc:mysql://localhost:3306/CinemaDB";
        String dbUsername = "root";
        String dbPassword = "1111";
        Class.forName("com.mysql.cj.jdbc.Driver");
        return DriverManager.getConnection(dbURL, dbUsername, dbPassword);
    }
}
