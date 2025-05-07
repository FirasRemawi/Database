package com.example.db;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CinemaDatabase {

    private static final String DB_URL = "jdbc:mysql://localhost:3306/CinemaDB";
    private static final String DB_USERNAME = "root";
    private static final String DB_PASSWORD = "1111";

    private Connection connection;

    // Constructor: Establishes connection
    public CinemaDatabase() throws SQLException, ClassNotFoundException {
        Class.forName("com.mysql.cj.jdbc.Driver");
        connection = DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD);
    }

    // Close connection
    public void close() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }

    // Authenticate admin credentials
    public boolean authenticateAdmin(String username, String password) throws SQLException {
        String query = "SELECT * FROM Admin WHERE Username = ? AND Password = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, username);
            stmt.setString(2, password);
            ResultSet rs = stmt.executeQuery();
            return rs.next();
        }
    }

    // Authenticate user credentials
    public boolean authenticateUser(String username, String password) throws SQLException {
        String query = "SELECT * FROM Users WHERE Username = ? AND Password = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, username);
            stmt.setString(2, password);
            ResultSet rs = stmt.executeQuery();
            return rs.next();
        }
    }

    // Fetch all movies from the database
    public List<Movie> getAllMovies() throws SQLException {
        List<Movie> movies = new ArrayList<>();
        String query = "SELECT * FROM Movie ORDER BY MovieID";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                movies.add(new Movie(
                        rs.getInt("MovieID"),
                        rs.getString("Title"),
                        rs.getString("Description"),
                        rs.getString("Genre"),
                        rs.getInt("Duration"),
                        rs.getInt("showID")
                ));
            }
        }
        return movies;
    }

    // Add a new movie
    public boolean addMovie(Movie movie) throws SQLException {
        String query = "INSERT INTO Movie (MovieID, Title, Description, Genre, Duration) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, movie.getMovieId());
            stmt.setString(2, movie.getTitle());
            stmt.setString(3, movie.getDescription());
            stmt.setString(4, movie.getGenre());
            stmt.setInt(5, movie.getDuration());
            return stmt.executeUpdate() > 0;
        }
    }

    // Update an existing movie
    public boolean updateMovie(Movie movie) throws SQLException {
        String query = "UPDATE Movie SET Title = ?, Description = ?, Genre = ?, Duration = ? WHERE MovieID = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, movie.getTitle());
            stmt.setString(2, movie.getDescription());
            stmt.setString(3, movie.getGenre());
            stmt.setInt(4, movie.getDuration());
            stmt.setInt(5, movie.getMovieId());
            return stmt.executeUpdate() > 0;
        }
    }

    // Delete a movie
    public boolean deleteMovie(int movieId) throws SQLException {
        String query = "DELETE FROM Movie WHERE MovieID = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, movieId);
            return stmt.executeUpdate() > 0;
        }
    }
}
