package com.example.db;

public class Movie {
    private int movieId;
    private String title;
    private String description;
    private String genre;
    private int duration;
    private int showId; // Added showId field
    private String posterPath; // Add this field

    public Movie(String title, String genre, int duration) {
        this.title = title;
        this.genre = genre;
        this.duration = duration;
    }


    public Movie(int movieId, String title, String description, String genre, int duration, int showId) {
        this.movieId = movieId;
        this.title = title;
        this.description = description;
        this.genre = genre;
        this.duration = duration;
        this.showId = showId;
        this.posterPath = "C:\\Users\\ASUS\\IntelliJ\\DB\\src\\main\\resources\\images\\image_" + (movieId - 1) + ".jpg"; // Dynamically assign
    }

    // Add getter for posterPath
    public String getPosterPath() {
        return posterPath;
    }

    // Getters and Setters
    public int getMovieId() {
        return movieId;
    }

    public void setMovieId(int movieId) {
        this.movieId = movieId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getShowId() { // Getter for showId
        return showId;
    }

    public void setShowId(int showId) { // Setter for showId
        this.showId = showId;
    }
}
