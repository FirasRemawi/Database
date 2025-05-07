package com.example.db;

import javafx.beans.property.*;

public class ReportEntry {

    private final StringProperty title;
    private final IntegerProperty ticketsSold;
    private final DoubleProperty revenue;
    private final IntegerProperty availableSeats;

    // Constructor for sales report
    public ReportEntry(String title, int ticketsSold, double revenue) {
        this.title = new SimpleStringProperty(title);
        this.ticketsSold = new SimpleIntegerProperty(ticketsSold);
        this.revenue = new SimpleDoubleProperty(revenue);
        this.availableSeats = new SimpleIntegerProperty(0); // Default to 0 for non-seat-related entries
    }

    // Constructor for movies with no bookings or available seats
    public ReportEntry(String title) {
        this.title = new SimpleStringProperty(title);
        this.ticketsSold = new SimpleIntegerProperty(0);
        this.revenue = new SimpleDoubleProperty(0.0);
        this.availableSeats = new SimpleIntegerProperty(0);
    }

    // Constructor for available seats
    public ReportEntry(String title, int availableSeats) {
        this.title = new SimpleStringProperty(title);
        this.ticketsSold = new SimpleIntegerProperty(0);
        this.revenue = new SimpleDoubleProperty(0.0);
        this.availableSeats = new SimpleIntegerProperty(availableSeats);
    }
    // Constructor for available seats
    public ReportEntry(String title, double revenue) {
        this.title = new SimpleStringProperty(title);
        this.ticketsSold = new SimpleIntegerProperty(0);
        this.revenue = new SimpleDoubleProperty(0.0);
        this.availableSeats = new SimpleIntegerProperty(0);
     }

    // Getters
    public String getTitle() {
        return title.get();
    }

    public int getTicketsSold() {
        return ticketsSold.get();
    }

    public double getRevenue() {
        return revenue.get();
    }

    public int getAvailableSeats() {
        return availableSeats.get();
    }

    // Property methods for JavaFX binding
    public StringProperty titleProperty() {
        return title;
    }

    public IntegerProperty ticketsSoldProperty() {
        return ticketsSold;
    }

    public DoubleProperty revenueProperty() {
        return revenue;
    }

    public IntegerProperty availableSeatsProperty() {
        return availableSeats;
    }
}
