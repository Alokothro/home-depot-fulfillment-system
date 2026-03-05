package com.homedepot.fulfillment.ui;

import com.homedepot.fulfillment.FulfillmentApplication;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;

import java.io.IOException;

/**
 * Main JavaFX Desktop Application for Home Depot Order Fulfillment System.
 * Launches Spring Boot backend and JavaFX UI.
 */
public class FulfillmentDesktopApp extends Application {

    private ConfigurableApplicationContext springContext;
    private Stage primaryStage;

    @Override
    public void init() {
        // Start Spring Boot in background thread
        new Thread(() -> {
            try {
                springContext = SpringApplication.run(FulfillmentApplication.class);
            } catch (Exception e) {
                Platform.runLater(() -> showError("Failed to start Spring Boot backend", e));
            }
        }).start();
    }

    @Override
    public void start(Stage stage) {
        this.primaryStage = stage;

        try {
            // Load landing page FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/landing.fxml"));
            Scene scene = new Scene(loader.load(), 1400, 900);

            // Add CSS
            scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());

            // Configure stage
            stage.setTitle("Home Depot Order Fulfillment System");
            stage.setScene(scene);

            // Add application icon if available
            try {
                stage.getIcons().add(new Image(getClass().getResourceAsStream("/images/icon.png")));
            } catch (Exception e) {
                // Icon not found, continue without it
            }

            stage.setOnCloseRequest(event -> {
                shutdown();
            });

            stage.show();

        } catch (IOException e) {
            showError("Failed to load application UI", e);
        }
    }

    @Override
    public void stop() {
        shutdown();
    }

    private void shutdown() {
        if (springContext != null) {
            springContext.close();
        }
        Platform.exit();
        System.exit(0);
    }

    private void showError(String message, Exception e) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Application Error");
        alert.setHeaderText(message);
        alert.setContentText(e.getMessage());
        alert.showAndWait();
        Platform.exit();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
