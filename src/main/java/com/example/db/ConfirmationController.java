package com.example.db;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class ConfirmationController {

    public ImageView moviePoster;
    @FXML
    private Label movieTitleLabel;

    @FXML
    private Label selectedSeatsLabel;

    @FXML
    private Label totalPriceLabel;

    @FXML
    private Label bookingTimeLabel;

    @FXML
    private ImageView qrCodeImage;

    @FXML
    private Button backToDashboardButton;

    @FXML
    private Button exitButton;

    @FXML
    public void initialize() {
        backToDashboardButton.setOnAction(event -> handleBackToDashboard());
        exitButton.setOnAction(event -> handleExit());
    }

    public void setConfirmationDetails(String selectedSeats, double totalPrice, String movieTitle, String customerName, String posterPath) {
        String[]parts = selectedSeats.split(",");
        movieTitleLabel.setText("Movie: " + movieTitle);
        selectedSeatsLabel.setText("Selected Seats: " + selectedSeats);
        totalPriceLabel.setText("Total Price: $" + (totalPrice + 10.0*parts.length));
        bookingTimeLabel.setText("Customer: " + customerName);

        try {
            moviePoster.setImage(new Image(posterPath));
            generateQRCode(selectedSeats, totalPrice, movieTitle, customerName);
        } catch (Exception e) {
            // moviePoster.setImage(new Image("path_to_images/placeholder.jpg"));
        }
    }


    private void generateQRCode(String seats, double totalPrice, String movieTitle, String bookingTime) {
        String[]parts = seats.split(",");
        String qrData = "Movie: " + movieTitle + "\nSeats: " + seats + "\nPrice: $" + (totalPrice + 10.0*parts.length) + "\nBooking Time: " + bookingTime;

        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        int width = 300;
        int height = 300;

        try {
            BitMatrix bitMatrix = qrCodeWriter.encode(qrData, BarcodeFormat.QR_CODE, width, height);
            BufferedImage qrImage = toBufferedImage(bitMatrix);
            qrCodeImage.setImage(SwingFXUtils.toFXImage(qrImage, null));

            File qrFile = new File("ticket_qrcode.png");
            ImageIO.write(qrImage, "png", qrFile);

        } catch (WriterException | IOException e) {
            e.printStackTrace();
        }
    }

    private BufferedImage toBufferedImage(BitMatrix matrix) {
        int width = matrix.getWidth();
        int height = matrix.getHeight();
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                image.setRGB(x, y, matrix.get(x, y) ? 0xFF000000 : 0xFFFFFFFF);
            }
        }
        return image;
    }

    private void handleBackToDashboard() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("user-dashboard.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root, 900, 600);

            // Get the current stage
            Stage primaryStage = (Stage) backToDashboardButton.getScene().getWindow();

            primaryStage.setScene(scene);
            primaryStage.setMaximized(true);
            primaryStage.setTitle("User Dashboard");
            primaryStage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void handleExit() {
        Stage stage = (Stage) exitButton.getScene().getWindow();
        stage.close();
    }
}
