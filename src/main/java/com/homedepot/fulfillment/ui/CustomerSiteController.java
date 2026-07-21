package com.homedepot.fulfillment.ui;

import javafx.application.Platform;
import javafx.concurrent.Worker;
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

public class CustomerSiteController {

    private static final String CUSTOMER_SITE_URL = "http://localhost:8080/index.html";
    private static final int    MAX_RETRIES       = 30;
    private static final int    RETRY_DELAY_MS    = 1000;

    @FXML private WebView webView;

    private final HttpClient httpClient = HttpClient.newHttpClient();

    @FXML
    public void initialize() {
        // JS → Java channel: intercept alert() calls to save/clear session
        webView.getEngine().setOnAlert(event -> {
            String data = event.getData();
            if (data != null && data.startsWith("__session__:")) {
                // Format: __session__:customerId:firstName:lastName
                String[] parts = data.split(":", 4);
                if (parts.length == 4) {
                    try {
                        CustomerSession.set(Long.parseLong(parts[1]), parts[2], parts[3]);
                    } catch (NumberFormatException ignored) {}
                }
            } else if ("__clear_session__".equals(data)) {
                CustomerSession.clear();
            }
            // All other alert() calls: silently drop (we use in-DOM UI instead)
        });

        // Java → JS channel: restore session after page finishes loading
        webView.getEngine().getLoadWorker().stateProperty().addListener((obs, old, state) -> {
            if (state == Worker.State.SUCCEEDED && CustomerSession.hasSession()) {
                Platform.runLater(() -> {
                    String js = String.format(
                        "if(typeof restoreSession==='function') restoreSession(%d,'%s','%s');",
                        CustomerSession.getCustomerId(),
                        escapeJs(CustomerSession.getFirstName()),
                        escapeJs(CustomerSession.getLastName())
                    );
                    webView.getEngine().executeScript(js);
                });
            }
        });

        waitForServer().thenRun(() -> Platform.runLater(this::loadWebsite));
    }

    private void loadWebsite() {
        try {
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
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/landing.fxml"));
            Parent root = loader.load();
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
                    if (httpClient.send(request, HttpResponse.BodyHandlers.ofString()).statusCode() == 200)
                        return;
                } catch (Exception ignored) {}
                try { Thread.sleep(RETRY_DELAY_MS); }
                catch (InterruptedException e) { Thread.currentThread().interrupt(); break; }
            }
            Platform.runLater(() -> showError("Spring Boot server failed to start"));
        });
    }

    private String escapeJs(String s) {
        return s == null ? "" : s.replace("\\", "\\\\").replace("'", "\\'");
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
