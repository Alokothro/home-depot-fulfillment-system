package com.homedepot.fulfillment.ui;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.input.MouseEvent;
import javafx.scene.web.WebView;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

/**
 * Controller for the customer shopping portal (embedded web view).
 */
public class CustomerSiteController {

    private static final String CUSTOMER_SITE_URL = "http://localhost:8080/index.html";
    private static final int MAX_RETRIES = 30;
    private static final int RETRY_DELAY_MS = 1000;

    @FXML
    private WebView webView;

    private final HttpClient httpClient = HttpClient.newHttpClient();

    @FXML
    public void initialize() {
        // Wait for Spring Boot server, then load the customer website
        waitForServer().thenRun(() -> javafx.application.Platform.runLater(this::loadWebsite));
    }

    private void loadWebsite() {
        try {
            // Cache-bust so the WebView always picks up the latest CSS/JS after a restart
            String url = CUSTOMER_SITE_URL + "?v=" + System.currentTimeMillis();
            webView.getEngine().load(url);
        } catch (Exception e) {
            e.printStackTrace();
            showError("Failed to load customer website: " + e.getMessage());
        }
    }

    @FXML
    private void onBackClick(MouseEvent event) {
        try {
            // Load the landing page
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/landing.fxml"));
            Parent root = loader.load();

            // Get the current stage and switch scenes
            Stage stage = (Stage) webView.getScene().getWindow();
            stage.getScene().setRoot(root);
            stage.setTitle("Home Depot Order Fulfillment System");

        } catch (IOException e) {
            e.printStackTrace();
            showError("Failed to return to landing page: " + e.getMessage());
        }
    }

    private java.util.concurrent.CompletableFuture<Void> waitForServer() {
        return java.util.concurrent.CompletableFuture.runAsync(() -> {
            for (int i = 0; i < MAX_RETRIES; i++) {
                try {
                    HttpRequest request = HttpRequest.newBuilder()
                            .uri(URI.create("http://localhost:8080/api/products"))
                            .GET()
                            .timeout(java.time.Duration.ofSeconds(2))
                            .build();

                    HttpResponse<String> response = httpClient.send(request,
                            HttpResponse.BodyHandlers.ofString());

                    if (response.statusCode() == 200) {
                        System.out.println("Spring Boot server is ready!");
                        return;
                    }
                } catch (Exception e) {
                    // Server not ready yet
                }

                try {
                    Thread.sleep(RETRY_DELAY_MS);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }

            // If we get here, server didn't start
            javafx.application.Platform.runLater(() ->
                showError("Spring Boot server failed to start after " + MAX_RETRIES + " seconds")
            );
        });
    }

    private void showError(String message) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                javafx.scene.control.Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText("Customer Portal Error");
        alert.setContentText(message);
        alert.showAndWait();
    }
}
