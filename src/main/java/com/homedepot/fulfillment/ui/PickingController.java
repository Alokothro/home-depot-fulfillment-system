package com.homedepot.fulfillment.ui;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

/**
 * Controller for the warehouse picking screen: scan/confirm each item in an order,
 * then complete the pick (which packs the order via the fulfillment API).
 */
public class PickingController {

    private static final String BASE_URL = "http://localhost:8080";

    @FXML private Label orderTitleLabel;
    @FXML private Label customerLabel;
    @FXML private Label progressLabel;
    @FXML private Label scanFeedback;
    @FXML private TextField scanField;
    @FXML private VBox pickItemsContainer;
    @FXML private Button completeButton;

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    private final List<PickRow> rows = new ArrayList<>();
    private final Set<Long> orderIdsToPack = new LinkedHashSet<>();

    /** State + UI references for a single line item being picked. */
    private static class PickRow {
        final String sku;
        final String name;
        final int required;
        int picked = 0;
        VBox card;
        Label progress;
        Label badge;
        Button pickButton;

        PickRow(String sku, String name, int required) {
            this.sku = sku;
            this.name = name;
            this.required = required;
        }
    }

    // ----- Entry points -----

    /** Load a single order's pick list from the API. */
    public void initOrder(long orderId) {
        orderIdsToPack.add(orderId);
        CompletableFuture
            .supplyAsync(() -> httpGetNode("/api/fulfillment/order/" + orderId + "/pick-list"))
            .thenAccept(node -> Platform.runLater(() -> renderOrder(node)))
            .exceptionally(e -> {
                Platform.runLater(() -> showFeedback("Failed to load order: " + e.getMessage(), false));
                return null;
            });
    }

    /** Build a pick list from an already-loaded batch node (aggregates every customer's items). */
    public void initBatch(JsonNode batch) {
        String department = batch.path("department").asText("Batch");
        orderTitleLabel.setText(department + " Batch");
        int customers = batch.path("customerCount").asInt(0);
        customerLabel.setText(customers + " customer" + (customers != 1 ? "s" : ""));

        for (JsonNode customer : batch.path("customers")) {
            orderIdsToPack.add(customer.path("orderId").asLong());
            for (JsonNode item : customer.path("items")) {
                addRow(item.path("sku").asText(""),
                       item.path("productName").asText("Item"),
                       item.path("location").asText("—"),
                       item.path("quantity").asInt(1));
            }
        }
        finishRender();
    }

    private void renderOrder(JsonNode order) {
        orderTitleLabel.setText(order.path("orderNumber").asText("Order"));
        String customer = order.path("customerName").asText("");
        int totalItems = order.path("totalItems").asInt(0);
        customerLabel.setText(customer + "  •  " + totalItems + " item" + (totalItems != 1 ? "s" : ""));

        for (JsonNode line : order.path("lines")) {
            addRow(line.path("sku").asText(""),
                   line.path("productName").asText("Item"),
                   line.path("location").asText("—"),
                   line.path("quantity").asInt(1));
        }
        finishRender();
    }

    // ----- Rendering -----

    private void addRow(String sku, String name, String location, int quantity) {
        PickRow row = new PickRow(sku, name, quantity);

        VBox card = new VBox(6);
        card.getStyleClass().add("pick-item-card");
        card.setPadding(new Insets(14));

        // Row 1: name + status badge
        HBox top = new HBox(10);
        top.setAlignment(Pos.CENTER_LEFT);
        Label nameLabel = new Label(name);
        nameLabel.getStyleClass().add("warehouse-order-name");
        nameLabel.setFont(Font.font("System", 17));
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        Label badge = new Label("TO PICK");
        badge.getStyleClass().addAll("warehouse-badge", "warehouse-badge-inprogress");
        top.getChildren().addAll(nameLabel, spacer, badge);

        // Row 2: SKU + bin location
        HBox mid = new HBox(18);
        mid.setAlignment(Pos.CENTER_LEFT);
        Label skuLabel = new Label("SKU: " + sku);
        skuLabel.getStyleClass().add("warehouse-order-number");
        Label binLabel = new Label("Bin: " + location);
        binLabel.getStyleClass().add("warehouse-order-info");
        mid.getChildren().addAll(skuLabel, binLabel);

        // Row 3: progress + manual pick button
        HBox bottom = new HBox(10);
        bottom.setAlignment(Pos.CENTER_LEFT);
        Label progress = new Label("0 / " + quantity + " picked");
        progress.getStyleClass().add("warehouse-order-info");
        Region spacer2 = new Region();
        HBox.setHgrow(spacer2, Priority.ALWAYS);
        Button pickButton = new Button("Pick 1");
        pickButton.getStyleClass().add("pick-item-button");
        pickButton.setOnAction(e -> { pickOne(row); scanField.requestFocus(); });
        bottom.getChildren().addAll(progress, spacer2, pickButton);

        card.getChildren().addAll(top, mid, bottom);

        row.card = card;
        row.progress = progress;
        row.badge = badge;
        row.pickButton = pickButton;
        rows.add(row);
        pickItemsContainer.getChildren().add(card);
    }

    private void finishRender() {
        updateProgress();
        Platform.runLater(() -> scanField.requestFocus());
    }

    // ----- Scanning / picking -----

    @FXML
    private void onScan() {
        String code = scanField.getText() == null ? "" : scanField.getText().trim();
        scanField.clear();
        if (code.isEmpty()) return;

        // Find the first not-yet-complete row whose SKU matches (case-insensitive).
        PickRow match = null;
        for (PickRow row : rows) {
            if (row.sku.equalsIgnoreCase(code) && row.picked < row.required) {
                match = row;
                break;
            }
        }

        if (match == null) {
            boolean known = rows.stream().anyMatch(r -> r.sku.equalsIgnoreCase(code));
            showFeedback(known
                    ? "\"" + code + "\" is already fully picked."
                    : "\"" + code + "\" is not in this order.", false);
        } else {
            pickOne(match);
            showFeedback("Picked " + match.name + "  (" + match.picked + "/" + match.required + ")", true);
        }
        scanField.requestFocus();
    }

    private void pickOne(PickRow row) {
        if (row.picked >= row.required) return;
        row.picked++;
        row.progress.setText(row.picked + " / " + row.required + " picked");

        if (row.picked >= row.required) {
            row.badge.setText("✓ PICKED");
            row.badge.getStyleClass().removeAll("warehouse-badge-inprogress");
            row.badge.getStyleClass().add("pick-badge-done");
            if (!row.card.getStyleClass().contains("pick-item-done")) {
                row.card.getStyleClass().add("pick-item-done");
            }
            row.pickButton.setDisable(true);
            row.pickButton.setText("Done");
        }
        updateProgress();
    }

    private void updateProgress() {
        int picked = rows.stream().mapToInt(r -> r.picked).sum();
        int total = rows.stream().mapToInt(r -> r.required).sum();
        progressLabel.setText(picked + " / " + total + " picked");

        boolean allDone = total > 0 && picked >= total;
        completeButton.setDisable(!allDone);
    }

    private void showFeedback(String message, boolean success) {
        scanFeedback.setText(message);
        scanFeedback.getStyleClass().removeAll("pick-scan-ok", "pick-scan-err");
        scanFeedback.getStyleClass().add(success ? "pick-scan-ok" : "pick-scan-err");
    }

    // ----- Complete (pack) -----

    @FXML
    private void onComplete() {
        completeButton.setDisable(true);
        completeButton.setText("Packing…");

        CompletableFuture.runAsync(() -> {
            for (Long orderId : orderIdsToPack) {
                httpPut("/api/fulfillment/pack/" + orderId);
            }
        }).thenRun(() -> Platform.runLater(this::goToOrderList))
          .exceptionally(e -> {
              Platform.runLater(() -> {
                  showFeedback("Failed to pack order: " + e.getMessage(), false);
                  completeButton.setDisable(false);
                  completeButton.setText("COMPLETE PICK & PACK");
              });
              return null;
          });
    }

    @FXML
    private void onBackClick(MouseEvent event) {
        goToOrderList();
    }

    private void goToOrderList() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/order-list.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) scanField.getScene().getWindow();
            Scene scene = new Scene(root, 1400, 900);
            scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());
            stage.setScene(scene);
            stage.setTitle("Home Depot - Order Fulfillment");
        } catch (Exception e) {
            showFeedback("Failed to return to order list: " + e.getMessage(), false);
        }
    }

    // ----- HTTP helpers -----

    private JsonNode httpGetNode(String path) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + path))
                    .GET()
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            return objectMapper.readTree(response.body());
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    private void httpPut(String path) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + path))
                    .PUT(HttpRequest.BodyPublishers.noBody())
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 300) {
                throw new RuntimeException("HTTP " + response.statusCode() + ": " + response.body());
            }
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }
}
