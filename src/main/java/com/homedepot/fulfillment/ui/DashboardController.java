package com.homedepot.fulfillment.ui;

import javafx.application.Platform;
import javafx.concurrent.Worker;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TabPane;
import javafx.scene.input.MouseEvent;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

import java.util.concurrent.CompletableFuture;

/**
 * Controller for the main dashboard UI.
 */
public class DashboardController {

    private static final String BASE_URL = "http://localhost:8080";
    private static final int MAX_RETRIES = 30;
    private static final int RETRY_DELAY_MS = 1000;

    @FXML
    private TabPane mainTabPane;

    @FXML
    private Label statusLabel;

    @FXML
    private WebView swaggerWebView;

    @FXML
    private WebView h2WebView;

    @FXML
    private WebView productsWebView;

    @FXML
    private WebView ordersWebView;

    @FXML
    private WebView inventoryWebView;

    @FXML
    public void initialize() {
        // Wait for Spring Boot to start, then load WebViews
        waitForServer().thenRun(() -> Platform.runLater(this::loadAllWebViews));
    }

    private CompletableFuture<Void> waitForServer() {
        return CompletableFuture.runAsync(() -> {
            for (int i = 0; i < MAX_RETRIES; i++) {
                try {
                    java.net.URL url = new java.net.URL(BASE_URL + "/api/warehouses");
                    java.net.HttpURLConnection connection = (java.net.HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");
                    connection.setConnectTimeout(2000);
                    connection.setReadTimeout(2000);

                    int responseCode = connection.getResponseCode();
                    if (responseCode == 200) {
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
            Platform.runLater(() -> {
                statusLabel.setText("⚠ Server Failed to Start");
                showError("Spring Boot server failed to start after " + MAX_RETRIES + " seconds");
            });
        });
    }

    private void loadAllWebViews() {
        // Load Swagger UI
        loadUrl(swaggerWebView, BASE_URL + "/swagger-ui.html");

        // Load H2 Console
        loadUrl(h2WebView, BASE_URL + "/h2-console");

        // Load API endpoints
        loadUrl(productsWebView, BASE_URL + "/api/products");
        loadUrl(ordersWebView, BASE_URL + "/api/orders");
        loadUrl(inventoryWebView, BASE_URL + "/api/inventory/low-stock");
    }

    private void loadUrl(WebView webView, String url) {
        WebEngine engine = webView.getEngine();

        // Enable JavaScript
        engine.setJavaScriptEnabled(true);

        // Handle loading errors
        engine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
            if (newState == Worker.State.FAILED) {
                System.err.println("Failed to load: " + url);
            }
        });

        engine.load(url);
    }

    // Card click handlers
    @FXML
    private void openProducts(MouseEvent event) {
        mainTabPane.getSelectionModel().select(3); // Products tab
    }

    @FXML
    private void openOrders(MouseEvent event) {
        mainTabPane.getSelectionModel().select(4); // Orders tab
    }

    @FXML
    private void openInventory(MouseEvent event) {
        mainTabPane.getSelectionModel().select(5); // Inventory tab
    }

    @FXML
    private void openWarehouses(MouseEvent event) {
        openUrlInBrowser(BASE_URL + "/api/warehouses");
    }

    @FXML
    private void openCustomers(MouseEvent event) {
        openUrlInBrowser(BASE_URL + "/api/customers");
    }

    @FXML
    private void openSwagger(MouseEvent event) {
        mainTabPane.getSelectionModel().select(1); // Swagger tab
    }

    private void openUrlInBrowser(String url) {
        try {
            java.awt.Desktop.getDesktop().browse(new java.net.URI(url));
        } catch (Exception e) {
            showError("Failed to open browser: " + e.getMessage());
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText("Application Error");
        alert.setContentText(message);
        alert.showAndWait();
    }
}
