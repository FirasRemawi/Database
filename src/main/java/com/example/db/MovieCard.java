package com.example.db;

import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class MovieCard extends StackPane {
    public MovieCard(Movie movie) {
        Rectangle background = new Rectangle(200, 300);
        background.setArcWidth(15);
        background.setArcHeight(15);
        background.setFill(Color.LIGHTGRAY);
        background.setEffect(new DropShadow(10, Color.BLACK));

        Label title = new Label(movie.getTitle());
        title.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-wrap-text: true;");

        getChildren().addAll(background, title);
    }
}
