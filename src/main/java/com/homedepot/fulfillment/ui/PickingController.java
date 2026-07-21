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
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
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

public class PickingController {

    private static final String BASE_URL = "http://localhost:8080";

    @FXML private Label       orderTitleLabel;
    @FXML private Label       customerLabel;
    @FXML private Label       progressLabel;
    @FXML private Label       scanFeedback;
    @FXML private TextField   scanField;
    @FXML private VBox        pickItemsContainer;
    @FXML private Button      completeButton;
    @FXML private ProgressBar progressBar;

    private final HttpClient   httpClient   = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    private final List<PickRow> rows           = new ArrayList<>();
    private final Set<Long>     orderIdsToPack = new LinkedHashSet<>();

    private static final String COL_PENDING = "#94a3b8";
    private static final String COL_DONE    = "#16a34a";

    private static class PickRow {
        final String sku;
        final String name;
        final int    required;
        int    picked = 0;
        HBox   outerCard;
        Region strip;
        Label  progressLbl;
        Label  badge;
        Button pickBtn;
        Button pickAllBtn;
        TextField qtyField;   // for "enter quantity" flow

        PickRow(String sku, String name, int required) {
            this.sku = sku; this.name = name; this.required = required;
        }
    }

    // ── Entry points ──────────────────────────────────────────────────────────

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

    public void initBatch(JsonNode batch) {
        String department = batch.path("department").asText("Batch");
        orderTitleLabel.setText(department + " — Batch Pick");
        int customers = batch.path("customerCount").asInt(0);
        customerLabel.setText(customers + " customer" + (customers != 1 ? "s" : "") + "  •  Batch");

        for (JsonNode customer : batch.path("customers")) {
            orderIdsToPack.add(customer.path("orderId").asLong());
            String cName = customer.path("customerName").asText("");
            for (JsonNode item : customer.path("items")) {
                addRow(item.path("sku").asText(""),
                       item.path("productName").asText("Item"),
                       item.path("location").asText("—"),
                       item.path("quantity").asInt(1),
                       cName);
            }
        }
        finishRender();
    }

    private void renderOrder(JsonNode order) {
        orderTitleLabel.setText(order.path("orderNumber").asText("Order"));
        customerLabel.setText(order.path("customerName").asText("") + "  •  "
                + order.path("shippingMethod").asText("Standard") + "  •  "
                + order.path("totalItems").asInt(0) + " items");

        for (JsonNode line : order.path("lines")) {
            addRow(line.path("sku").asText(""),
                   line.path("productName").asText("Item"),
                   line.path("location").asText("—"),
                   line.path("quantity").asInt(1),
                   null);
        }
        finishRender();
    }

    // ── Card building ─────────────────────────────────────────────────────────

    private void addRow(String sku, String name, String location, int quantity, String customerHint) {
        PickRow row = new PickRow(sku, name, quantity);

        HBox outer = new HBox(0);
        outer.getStyleClass().add("pick-card");

        Region strip = new Region();
        strip.setPrefWidth(6);
        strip.setMinWidth(6);
        strip.setStyle("-fx-background-color: " + COL_PENDING + "; -fx-background-radius: 10 0 0 10;");

        VBox body = new VBox(10);
        body.setPadding(new Insets(14, 16, 14, 14));
        body.setStyle("-fx-background-color: white; -fx-background-radius: 0 10 10 0;");
        HBox.setHgrow(body, Priority.ALWAYS);

        // Row 1 ── name + badge
        HBox r1 = new HBox(10);
        r1.setAlignment(Pos.CENTER_LEFT);
        Label nameLabel = new Label(name);
        nameLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #0f172a;");
        nameLabel.setWrapText(true);
        HBox.setHgrow(nameLabel, Priority.ALWAYS);
        Label badge = new Label("TO PICK");
        badge.setStyle("-fx-background-color: #dbeafe; -fx-text-fill: #1e40af;"
                + " -fx-background-radius: 20; -fx-padding: 3 10;"
                + " -fx-font-size: 11px; -fx-font-weight: bold;");
        r1.getChildren().addAll(nameLabel, badge);

        // Row 2 ── bin + SKU + optional customer
        HBox r2 = new HBox(10);
        r2.setAlignment(Pos.CENTER_LEFT);
        Label binLabel = new Label("BIN  " + location);
        binLabel.setStyle("-fx-background-color: #fff7ed; -fx-text-fill: #c2410c;"
                + " -fx-background-radius: 6; -fx-padding: 5 14;"
                + " -fx-font-size: 14px; -fx-font-weight: bold;");
        Label skuLabel = new Label(sku);
        skuLabel.setStyle("-fx-background-color: #f1f5f9; -fx-text-fill: #64748b;"
                + " -fx-background-radius: 6; -fx-padding: 5 12; -fx-font-size: 13px;");
        r2.getChildren().addAll(binLabel, skuLabel);
        if (customerHint != null && !customerHint.isEmpty()) {
            Label cLabel = new Label("For: " + customerHint);
            cLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #94a3b8; -fx-font-style: italic;");
            r2.getChildren().add(cLabel);
        }

        // Row 3 ── progress + qty field + buttons
        HBox r3 = new HBox(8);
        r3.setAlignment(Pos.CENTER_LEFT);

        Label progressLbl = new Label("0 / " + quantity + " picked");
        progressLbl.setStyle("-fx-font-size: 13px; -fx-text-fill: #64748b;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Qty field + "Pick N" button
        TextField qtyField = new TextField();
        qtyField.setPromptText("Qty");
        qtyField.setPrefWidth(60);
        qtyField.setMaxWidth(60);
        qtyField.setStyle("-fx-font-size: 13px; -fx-padding: 7 8;"
                + " -fx-background-radius: 7; -fx-border-radius: 7;"
                + " -fx-border-color: #e2e8f0; -fx-border-width: 1.5;");

        Button pickNBtn = new Button("Pick N");
        pickNBtn.setStyle("-fx-background-color: #0f172a; -fx-text-fill: white;"
                + " -fx-background-radius: 7; -fx-font-weight: bold;"
                + " -fx-font-size: 13px; -fx-padding: 8 16; -fx-cursor: hand;");
        pickNBtn.setOnAction(e -> { handlePickN(row); scanField.requestFocus(); });
        qtyField.setOnAction(e -> { handlePickN(row); scanField.requestFocus(); });

        // Pick All (only when qty > 1)
        Button pickAllBtn = null;
        if (quantity > 1) {
            pickAllBtn = new Button("Pick All (" + quantity + ")");
            pickAllBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #f97316;"
                    + " -fx-border-color: #f97316; -fx-border-width: 1.5;"
                    + " -fx-background-radius: 7; -fx-border-radius: 7;"
                    + " -fx-font-weight: bold; -fx-font-size: 13px; -fx-padding: 8 14; -fx-cursor: hand;");
            pickAllBtn.setOnAction(e -> { pickAll(row); scanField.requestFocus(); });
        }

        // Pick 1
        Button pickBtn = new Button("Pick 1");
        pickBtn.setStyle("-fx-background-color: #f97316; -fx-text-fill: white;"
                + " -fx-background-radius: 7; -fx-font-weight: bold;"
                + " -fx-font-size: 13px; -fx-padding: 8 20; -fx-cursor: hand;");
        pickBtn.setOnAction(e -> { pickOne(row); scanField.requestFocus(); });

        r3.getChildren().addAll(progressLbl, spacer, qtyField, pickNBtn);
        if (pickAllBtn != null) r3.getChildren().add(pickAllBtn);
        r3.getChildren().add(pickBtn);

        body.getChildren().addAll(r1, r2, r3);
        outer.getChildren().addAll(strip, body);

        row.outerCard   = outer;
        row.strip       = strip;
        row.progressLbl = progressLbl;
        row.badge       = badge;
        row.pickBtn     = pickBtn;
        row.pickAllBtn  = pickAllBtn;
        row.qtyField    = qtyField;
        rows.add(row);
        pickItemsContainer.getChildren().add(outer);
    }

    private void finishRender() {
        updateProgress();
        Platform.runLater(() -> scanField.requestFocus());
    }

    // ── Picking actions ───────────────────────────────────────────────────────

    @FXML
    private void onScan() {
        String code = scanField.getText() == null ? "" : scanField.getText().trim();
        scanField.clear();
        if (code.isEmpty()) return;

        PickRow match = null;
        for (PickRow row : rows) {
            if (row.sku.equalsIgnoreCase(code) && row.picked < row.required) {
                match = row; break;
            }
        }
        if (match == null) {
            boolean known = rows.stream().anyMatch(r -> r.sku.equalsIgnoreCase(code));
            showFeedback(known ? "\"" + code + "\" is already fully picked."
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
        row.progressLbl.setText(row.picked + " / " + row.required + " picked");
        if (row.picked >= row.required) markRowDone(row);
        updateProgress();
    }

    private void pickAll(PickRow row) {
        if (row.picked >= row.required) return;
        int n = row.required - row.picked;
        row.picked = row.required;
        row.progressLbl.setText(row.picked + " / " + row.required + " picked");
        markRowDone(row);
        showFeedback("Picked all " + n + " × " + row.name, true);
        updateProgress();
    }

    private void handlePickN(PickRow row) {
        String text = row.qtyField.getText() == null ? "" : row.qtyField.getText().trim();
        row.qtyField.clear();
        if (text.isEmpty()) return;

        int qty;
        try {
            qty = Integer.parseInt(text);
        } catch (NumberFormatException e) {
            showFeedback("Enter a whole number in the quantity field.", false);
            return;
        }
        if (qty <= 0) { showFeedback("Quantity must be greater than 0.", false); return; }

        int remaining = row.required - row.picked;
        if (qty > remaining) {
            showFeedback("Only " + remaining + " more needed for " + row.name + ". Adjusted.", true);
            qty = remaining;
        }

        row.picked += qty;
        row.progressLbl.setText(row.picked + " / " + row.required + " picked");

        if (row.picked >= row.required) {
            markRowDone(row);
            showFeedback("Picked all " + row.required + " × " + row.name, true);
        } else {
            // Partially picked this line — update badge to show progress
            row.badge.setText(row.picked + "/" + row.required + " picked");
            row.badge.setStyle("-fx-background-color: #fef3c7; -fx-text-fill: #92400e;"
                    + " -fx-background-radius: 20; -fx-padding: 3 10;"
                    + " -fx-font-size: 11px; -fx-font-weight: bold;");
            row.strip.setStyle("-fx-background-color: #f59e0b; -fx-background-radius: 10 0 0 10;");
            showFeedback("Picked " + qty + " of " + row.required + " × " + row.name
                    + "  (" + (row.required - row.picked) + " short)", true);
        }
        updateProgress();
    }

    private void markRowDone(PickRow row) {
        row.strip.setStyle("-fx-background-color: " + COL_DONE + "; -fx-background-radius: 10 0 0 10;");
        row.badge.setText("✓ PICKED");
        row.badge.setStyle("-fx-background-color: #dcfce7; -fx-text-fill: #15803d;"
                + " -fx-background-radius: 20; -fx-padding: 3 10;"
                + " -fx-font-size: 11px; -fx-font-weight: bold;");
        row.outerCard.setStyle("-fx-effect: dropshadow(gaussian, rgba(22,163,74,0.15), 10, 0, 0, 2);");
        row.pickBtn.setDisable(true);
        row.pickBtn.setText("Done");
        row.pickBtn.setStyle("-fx-background-color: #86efac; -fx-text-fill: white;"
                + " -fx-background-radius: 7; -fx-font-weight: bold;"
                + " -fx-font-size: 13px; -fx-padding: 8 20;");
        if (row.pickAllBtn != null) row.pickAllBtn.setDisable(true);
        row.qtyField.setDisable(true);
    }

    private void updateProgress() {
        int picked = rows.stream().mapToInt(r -> r.picked).sum();
        int total  = rows.stream().mapToInt(r -> r.required).sum();
        progressLabel.setText(picked + " / " + total + " picked");
        progressBar.setProgress(total > 0 ? (double) picked / total : 0);

        boolean allDone    = total > 0 && picked >= total;
        boolean somePickedNotAll = picked > 0 && !allDone;

        if (allDone) {
            completeButton.setDisable(false);
            completeButton.setText("Complete Pick & Pack");
            completeButton.setStyle("-fx-background-color: #16a34a; -fx-text-fill: white;"
                    + " -fx-background-radius: 8; -fx-font-weight: bold; -fx-cursor: hand;");
        } else if (somePickedNotAll) {
            completeButton.setDisable(false);
            completeButton.setText("Complete as Partial  (" + picked + " / " + total + ")");
            completeButton.setStyle("-fx-background-color: #f59e0b; -fx-text-fill: white;"
                    + " -fx-background-radius: 8; -fx-font-weight: bold; -fx-cursor: hand;");
        } else {
            completeButton.setDisable(true);
            completeButton.setText("Complete Pick & Pack");
            completeButton.setStyle(null);
        }
    }

    private void showFeedback(String message, boolean success) {
        scanFeedback.setText(message);
        scanFeedback.getStyleClass().removeAll("pick-scan-ok", "pick-scan-err");
        scanFeedback.getStyleClass().add(success ? "pick-scan-ok" : "pick-scan-err");
    }

    // ── Complete / Back ───────────────────────────────────────────────────────

    @FXML
    private void onComplete() {
        int picked = rows.stream().mapToInt(r -> r.picked).sum();
        int total  = rows.stream().mapToInt(r -> r.required).sum();
        boolean isPartial = picked < total;

        completeButton.setDisable(true);
        completeButton.setText(isPartial ? "Saving partial…" : "Packing…");

        CompletableFuture.runAsync(() -> {
            String endpoint = isPartial ? "/api/fulfillment/partial/" : "/api/fulfillment/pack/";
            for (Long orderId : orderIdsToPack) httpPut(endpoint + orderId);
        }).thenRun(() -> Platform.runLater(this::goToOrderList))
          .exceptionally(e -> {
              Platform.runLater(() -> {
                  showFeedback("Failed: " + e.getMessage(), false);
                  updateProgress(); // re-enable button
              });
              return null;
          });
    }

    @FXML
    private void onBackClick(MouseEvent event) {
        int picked = rows.stream().mapToInt(r -> r.picked).sum();
        int total  = rows.stream().mapToInt(r -> r.required).sum();

        if (picked > 0 && picked < total) {
            // Some picks done — ask what to do
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Save Progress");
            alert.setHeaderText("You've picked " + picked + " of " + total + " items");
            alert.setContentText("Save this as a partial order (shortage will be flagged for customer service)?");

            ButtonType savePartial  = new ButtonType("Save as Partial");
            ButtonType discardBack  = new ButtonType("Discard & Go Back");
            ButtonType cancelReturn = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
            alert.getButtonTypes().setAll(savePartial, discardBack, cancelReturn);

            alert.showAndWait().ifPresent(result -> {
                if (result == savePartial) {
                    for (Long orderId : orderIdsToPack) httpPut("/api/fulfillment/partial/" + orderId);
                    goToOrderList();
                } else if (result == discardBack) {
                    goToOrderList();
                }
                // cancel → stay on picking screen
            });
        } else {
            goToOrderList();
        }
    }

    private void goToOrderList() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/order-list.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) scanField.getScene().getWindow();
            Scene scene = new Scene(root, 1400, 900);
            scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());
            stage.setScene(scene);
            stage.setTitle("Home Depot - Store Fulfillment");
        } catch (Exception e) {
            showFeedback("Failed to return: " + e.getMessage(), false);
        }
    }

    // ── HTTP ──────────────────────────────────────────────────────────────────

    private JsonNode httpGetNode(String path) {
        try {
            HttpRequest req = HttpRequest.newBuilder().uri(URI.create(BASE_URL + path)).GET().build();
            HttpResponse<String> resp = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
            return objectMapper.readTree(resp.body());
        } catch (Exception e) { throw new RuntimeException(e.getMessage(), e); }
    }

    private void httpPut(String path) {
        try {
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + path))
                    .PUT(HttpRequest.BodyPublishers.noBody()).build();
            HttpResponse<String> resp = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() >= 300)
                throw new RuntimeException("HTTP " + resp.statusCode() + ": " + resp.body());
        } catch (Exception e) { throw new RuntimeException(e.getMessage(), e); }
    }
}
