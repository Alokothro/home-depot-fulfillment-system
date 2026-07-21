package com.homedepot.fulfillment.ui;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
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
import java.util.stream.Collectors;

public class PickingController {

    private static final String BASE_URL = "http://localhost:8080";

    @FXML private Label       orderTitleLabel;
    @FXML private Label       customerLabel;
    @FXML private Label       progressLabel;
    @FXML private Label       scanFeedback;
    @FXML private TextField   scanField;
    @FXML private VBox        pickItemsContainer;  // "To Pick" items
    @FXML private VBox        pickedSection;        // hidden header + pickedContainer
    @FXML private VBox        pickedContainer;      // "Picked" items
    @FXML private Button      completeButton;
    @FXML private ProgressBar progressBar;

    // Partial reason overlay
    @FXML private StackPane partialOverlay;
    @FXML private Label     partialSubLabel;
    @FXML private VBox      partialReasonList;
    @FXML private Button    partialConfirmBtn;
    private String selectedReason = null;

    private final HttpClient   httpClient   = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    private final List<PickRow> rows           = new ArrayList<>();
    private final Set<Long>     orderIdsToPack = new LinkedHashSet<>();

    private static final String COL_PENDING = "#94a3b8";
    private static final String COL_DONE    = "#16a34a";

    private static class PickRow {
        final long   productId;
        final String sku;
        final String name;
        final int    required;
        final int    originalPicked; // pickedQuantity when the order was opened — used for discard
        int    picked = 0;
        HBox   outerCard;
        Region strip;
        Label  progressLbl;
        Label  badge;
        Button pickBtn;
        Button pickAllBtn;
        TextField qtyField;

        PickRow(long productId, String sku, String name, int required, int originalPicked) {
            this.productId = productId;
            this.sku = sku; this.name = name; this.required = required;
            this.originalPicked = originalPicked;
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
                addRow(item.path("productId").asLong(0),
                       item.path("sku").asText(""),
                       item.path("productName").asText("Item"),
                       item.path("location").asText("—"),
                       item.path("quantity").asInt(1),
                       0, cName);
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
            addRow(line.path("productId").asLong(0),
                   line.path("sku").asText(""),
                   line.path("productName").asText("Item"),
                   line.path("location").asText("—"),
                   line.path("quantity").asInt(1),
                   line.path("pickedQuantity").asInt(0),
                   null);
        }
        finishRender();
    }

    // ── Card building ─────────────────────────────────────────────────────────

    private void addRow(long productId, String sku, String name,
                        String location, int quantity, int alreadyPicked,
                        String customerHint) {
        PickRow row = new PickRow(productId, sku, name, quantity, alreadyPicked);

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

        // Row 1 — name + badge
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

        // Row 2 — bin + SKU
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

        // Row 3 — progress + qty field + buttons
        HBox r3 = new HBox(8);
        r3.setAlignment(Pos.CENTER_LEFT);
        Label progressLbl = new Label("0 / " + quantity + " picked");
        progressLbl.setStyle("-fx-font-size: 13px; -fx-text-fill: #64748b;");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        TextField qtyField = new TextField();
        qtyField.setPromptText("Qty");
        qtyField.setPrefWidth(60); qtyField.setMaxWidth(60);
        qtyField.setStyle("-fx-font-size: 13px; -fx-padding: 7 8;"
                + " -fx-background-radius: 7; -fx-border-radius: 7;"
                + " -fx-border-color: #e2e8f0; -fx-border-width: 1.5;");

        Button pickNBtn = new Button("Pick N");
        pickNBtn.setStyle("-fx-background-color: #0f172a; -fx-text-fill: white;"
                + " -fx-background-radius: 7; -fx-font-weight: bold;"
                + " -fx-font-size: 13px; -fx-padding: 8 16; -fx-cursor: hand;");
        pickNBtn.setOnAction(e -> { handlePickN(row); scanField.requestFocus(); });
        qtyField.setOnAction(e  -> { handlePickN(row); scanField.requestFocus(); });

        Button pickAllBtn = null;
        if (quantity > 1) {
            pickAllBtn = new Button("Pick All (" + quantity + ")");
            pickAllBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #f97316;"
                    + " -fx-border-color: #f97316; -fx-border-width: 1.5;"
                    + " -fx-background-radius: 7; -fx-border-radius: 7;"
                    + " -fx-font-weight: bold; -fx-font-size: 13px; -fx-padding: 8 14; -fx-cursor: hand;");
            pickAllBtn.setOnAction(e -> { pickAll(row); scanField.requestFocus(); });
        }

        Button pickBtn = new Button("Pick 1");
        pickBtn.setStyle("-fx-background-color: #f97316; -fx-text-fill: white;"
                + " -fx-background-radius: 7; -fx-font-weight: bold;"
                + " -fx-font-size: 13px; -fx-padding: 8 20; -fx-cursor: hand;");
        pickBtn.setOnAction(e -> {
            pickOne(row);
            if (row.picked >= row.required) {
                showFeedback("✓ All " + row.required + " × " + row.name + " picked!", true);
            } else {
                showFeedback("Picked 1 × " + row.name + "  —  " + (row.required - row.picked) + " still needed", true);
            }
            scanField.requestFocus();
        });

        r3.getChildren().addAll(progressLbl, spacer, qtyField, pickNBtn);
        if (pickAllBtn != null) r3.getChildren().add(pickAllBtn);
        r3.getChildren().add(pickBtn);

        body.getChildren().addAll(r1, r2, r3);
        outer.getChildren().addAll(strip, body);

        row.outerCard  = outer;
        row.strip      = strip;
        row.progressLbl = progressLbl;
        row.badge      = badge;
        row.pickBtn    = pickBtn;
        row.pickAllBtn = pickAllBtn;
        row.qtyField   = qtyField;
        rows.add(row);

        if (alreadyPicked > 0) {
            // Restore persisted pick state
            row.picked = alreadyPicked;
            row.progressLbl.setText(row.picked + " / " + row.required + " picked");
            if (row.picked >= row.required) {
                // Fully done — put straight into Picked section
                markRowDoneVisuals(row);
                pickedContainer.getChildren().add(outer);
                pickedSection.setVisible(true);
                pickedSection.setManaged(true);
            } else {
                // Partially done — show in To Pick with amber state
                row.badge.setText(row.picked + "/" + row.required + " picked");
                row.badge.setStyle("-fx-background-color: #fef3c7; -fx-text-fill: #92400e;"
                        + " -fx-background-radius: 20; -fx-padding: 3 10;"
                        + " -fx-font-size: 11px; -fx-font-weight: bold;");
                row.strip.setStyle("-fx-background-color: #f59e0b; -fx-background-radius: 10 0 0 10;");
                pickItemsContainer.getChildren().add(outer);
            }
        } else {
            pickItemsContainer.getChildren().add(outer);
        }
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
            showFeedback(known ? "✓ " + code + " is already fully picked."
                               : code + " is not in this order — double-check the SKU.", false);
        } else {
            pickOne(match);
            if (match.picked >= match.required) {
                showFeedback("✓ All " + match.required + " × " + match.name + " picked!", true);
            } else {
                showFeedback("Picked 1 × " + match.name
                        + "  —  " + (match.required - match.picked) + " still needed", true);
            }
        }
        scanField.requestFocus();
    }

    private void pickOne(PickRow row) {
        if (row.picked >= row.required) return;
        row.picked++;
        syncRowDisplay(row);
        if (row.picked >= row.required) markRowDone(row);
        updateProgress();
    }

    private void pickAll(PickRow row) {
        if (row.picked >= row.required) return;
        int n = row.required - row.picked;
        row.picked = row.required;
        syncRowDisplay(row);
        markRowDone(row);
        showFeedback("Picked all " + n + " × " + row.name, true);
        updateProgress();
    }

    private void handlePickN(PickRow row) {
        String raw = row.qtyField.getText() == null ? "" : row.qtyField.getText().trim();
        row.qtyField.clear();
        if (raw.isEmpty()) { showFeedback("Enter a quantity in the Qty field first.", false); return; }

        // Strip any accidental decimal (e.g. "8.0" → "8")
        raw = raw.contains(".") ? raw.substring(0, raw.indexOf('.')) : raw;
        int qty;
        try { qty = Integer.parseInt(raw); }
        catch (NumberFormatException e) { showFeedback("Enter a whole number (e.g. 8).", false); return; }
        if (qty <= 0) { showFeedback("Quantity must be at least 1.", false); return; }

        int remaining = row.required - row.picked;
        if (remaining <= 0) { showFeedback(row.name + " is already fully picked.", false); return; }

        boolean clamped = qty > remaining;
        if (clamped) qty = remaining;

        row.picked += qty;
        syncRowDisplay(row);

        if (row.picked >= row.required) {
            markRowDone(row);
            showFeedback("Picked all " + row.required + " × " + row.name, true);
        } else {
            setPartialStyle(row);
            String clampNote = clamped ? " (adjusted — only " + qty + " needed)" : "";
            showFeedback("Picked " + qty + " × " + row.name
                    + "  —  " + (row.required - row.picked) + " still needed" + clampNote, true);
        }
        updateProgress();
    }

    /**
     * Sync the progress label and Pick All button text to reflect current row.picked.
     * Called after every pick action before checking for completion.
     */
    private void syncRowDisplay(PickRow row) {
        row.progressLbl.setText(row.picked + " / " + row.required + " picked");
        // Keep badge in sync when partially picked
        if (row.picked > 0 && row.picked < row.required) {
            row.badge.setText(row.picked + "/" + row.required + " picked");
            row.badge.setStyle("-fx-background-color: #fef3c7; -fx-text-fill: #92400e;"
                    + " -fx-background-radius: 20; -fx-padding: 3 10;"
                    + " -fx-font-size: 11px; -fx-font-weight: bold;");
            row.strip.setStyle("-fx-background-color: #f59e0b; -fx-background-radius: 10 0 0 10;");
        }
        // Update Pick All button to show how many are left
        if (row.pickAllBtn != null && !row.pickAllBtn.isDisabled()) {
            int left = row.required - row.picked;
            row.pickAllBtn.setText(left > 0 ? "Pick All (" + left + ")" : "Done");
        }
    }

    /** Apply amber partial styling (called from handlePickN for partial result). */
    private void setPartialStyle(PickRow row) {
        // syncRowDisplay already handled badge and strip; nothing extra needed
    }

    /** Apply done visuals without moving the card (used during restore). */
    private void markRowDoneVisuals(PickRow row) {
        row.strip.setStyle("-fx-background-color: " + COL_DONE + "; -fx-background-radius: 10 0 0 10;");
        row.badge.setText("✓ PICKED");
        row.badge.setStyle("-fx-background-color: #dcfce7; -fx-text-fill: #15803d;"
                + " -fx-background-radius: 20; -fx-padding: 3 10;"
                + " -fx-font-size: 11px; -fx-font-weight: bold;");
        row.pickBtn.setDisable(true);
        row.pickBtn.setText("Done");
        row.pickBtn.setStyle("-fx-background-color: #86efac; -fx-text-fill: white;"
                + " -fx-background-radius: 7; -fx-font-weight: bold;"
                + " -fx-font-size: 13px; -fx-padding: 8 20;");
        if (row.pickAllBtn != null) row.pickAllBtn.setDisable(true);
        row.qtyField.setDisable(true);
    }

    /** Apply done visuals AND move the card from To Pick → Picked section. */
    private void markRowDone(PickRow row) {
        markRowDoneVisuals(row);
        row.outerCard.setStyle("-fx-effect: dropshadow(gaussian, rgba(22,163,74,0.1), 8, 0, 0, 2);");

        // Move card
        pickItemsContainer.getChildren().remove(row.outerCard);
        pickedContainer.getChildren().add(row.outerCard);

        // Show Picked section if first done item
        if (!pickedSection.isVisible()) {
            pickedSection.setVisible(true);
            pickedSection.setManaged(true);
        }

        // Save this item's progress to the backend immediately
        saveSingleItemProgress(row);
    }

    private void updateProgress() {
        int picked = rows.stream().mapToInt(r -> r.picked).sum();
        int total  = rows.stream().mapToInt(r -> r.required).sum();
        progressLabel.setText(picked + " / " + total + " picked");
        progressBar.setProgress(total > 0 ? (double) picked / total : 0);

        boolean allDone = total > 0 && picked >= total;
        if (allDone) {
            completeButton.setDisable(false);
            completeButton.setText("Complete Pick & Pack");
            completeButton.setStyle("-fx-background-color: #16a34a; -fx-text-fill: white;"
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

    // ── Save progress to backend ──────────────────────────────────────────────

    /** Save a single fully-picked item immediately. */
    private void saveSingleItemProgress(PickRow row) {
        for (Long orderId : orderIdsToPack) {
            CompletableFuture.runAsync(() -> {
                try {
                    String body = String.format(
                        "{\"items\":[{\"productId\":%d,\"pickedQuantity\":%d}]}",
                        row.productId, row.picked);
                    httpPutWithBody("/api/fulfillment/order/" + orderId + "/pick-progress", body);
                } catch (Exception ignored) {}
            });
        }
    }

    /** Save the full current state of all rows (used before saving as partial). */
    private void saveAllProgress() {
        String items = rows.stream()
            .filter(r -> r.picked > 0)
            .map(r -> String.format("{\"productId\":%d,\"pickedQuantity\":%d}", r.productId, r.picked))
            .collect(Collectors.joining(","));
        if (items.isEmpty()) return;

        String body = "{\"items\":[" + items + "]}";
        for (Long orderId : orderIdsToPack) {
            try { httpPutWithBody("/api/fulfillment/order/" + orderId + "/pick-progress", body); }
            catch (Exception ignored) {}
        }
    }

    // ── Complete / Back ───────────────────────────────────────────────────────

    @FXML
    private void onComplete() {
        completeButton.setDisable(true);
        completeButton.setText("Packing…");
        CompletableFuture.runAsync(() -> {
            for (Long orderId : orderIdsToPack) httpPut("/api/fulfillment/pack/" + orderId);
        }).thenRun(() -> Platform.runLater(this::goToOrderList))
          .exceptionally(e -> {
              Platform.runLater(() -> { showFeedback("Failed: " + e.getMessage(), false); updateProgress(); });
              return null;
          });
    }

    @FXML
    private void onBackClick(MouseEvent event) {
        int picked = rows.stream().mapToInt(r -> r.picked).sum();
        int total  = rows.stream().mapToInt(r -> r.required).sum();

        if (total > 0 && picked >= total) {
            // All items fully picked — prompt to complete rather than leave it stuck
            showAllPickedDialog();
        } else if (picked > 0 && picked < total) {
            // Some picked but not all — partial reason dialog
            showPartialReasonDialog(picked, total);
        } else {
            // Nothing picked yet — just go back, nothing to undo
            goToOrderList();
        }
    }

    private void showAllPickedDialog() {
        // Reuse the overlay header — update title dynamically via subLabel
        partialSubLabel.setText("All " + rows.stream().mapToInt(r -> r.required).sum()
                + " items picked. Complete the order now or go back to finish later.");
        partialReasonList.getChildren().clear();
        selectedReason = null;

        // Reuse the overlay but with a simple message instead of reason buttons
        Label msg = new Label("Press \"Complete & Pack\" to finish the order,\nor go back to complete it later.");
        msg.setStyle("-fx-font-size: 14px; -fx-text-fill: #475569; -fx-wrap-text: true;");
        msg.setWrapText(true);
        partialReasonList.getChildren().add(msg);

        // Swap button labels for this context
        partialConfirmBtn.setText("Complete & Pack");
        partialConfirmBtn.setDisable(false); // no reason selection needed
        selectedReason = "__complete__";     // sentinel so onPartialConfirm knows what to do

        partialOverlay.setVisible(true);
    }

    // ── Partial reason overlay ────────────────────────────────────────────────

    private void showPartialReasonDialog(int picked, int total) {
        partialSubLabel.setText("You picked " + picked + " of " + total + " items — select a reason:");
        partialReasonList.getChildren().clear();
        selectedReason = null;
        partialConfirmBtn.setDisable(true);

        String[][] reasons = {
            {"shortage",    "Item Shortage",       "Not enough stock on the shelf"},
            {"not_found",   "Item Not Found",      "Could not locate the item in the store"},
            {"unavailable", "Item Unavailable",    "Damaged, recalled, or restricted"},
            {"oversize",    "Oversize / Too Heavy","Cannot be safely transported"},
            {"other",       "Other",               "Customer service will review"}
        };
        for (String[] r : reasons)
            partialReasonList.getChildren().add(buildReasonOption(r[0], r[1], r[2]));
        partialOverlay.setVisible(true);
    }

    private VBox buildReasonOption(String key, String title, String desc) {
        Label titleLbl = new Label(title);
        titleLbl.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #0f172a;");
        Label descLbl = new Label(desc);
        descLbl.setStyle("-fx-font-size: 12px; -fx-text-fill: #94a3b8;");
        VBox box = new VBox(3, titleLbl, descLbl);
        box.setCursor(Cursor.HAND);
        box.setStyle("-fx-background-color: white; -fx-border-color: #e2e8f0;"
                + " -fx-border-width: 1.5; -fx-border-radius: 10;"
                + " -fx-background-radius: 10; -fx-padding: 14 16;");
        box.setOnMouseClicked(e -> selectReason(key, box, titleLbl));
        return box;
    }

    private void selectReason(String key, VBox selected, Label titleLbl) {
        partialReasonList.getChildren().forEach(node -> {
            node.setStyle("-fx-background-color: white; -fx-border-color: #e2e8f0;"
                    + " -fx-border-width: 1.5; -fx-border-radius: 10;"
                    + " -fx-background-radius: 10; -fx-padding: 14 16;");
            if (node instanceof VBox vb && !vb.getChildren().isEmpty())
                ((Label) vb.getChildren().get(0)).setStyle(
                    "-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #0f172a;");
        });
        selected.setStyle("-fx-background-color: #fff7ed; -fx-border-color: #f97316;"
                + " -fx-border-width: 2; -fx-border-radius: 10;"
                + " -fx-background-radius: 10; -fx-padding: 14 16;");
        titleLbl.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #c2410c;");
        selectedReason = key;
        partialConfirmBtn.setDisable(false);
    }

    @FXML private void onPartialConfirm() {
        if (selectedReason == null) return;
        partialOverlay.setVisible(false);
        resetOverlayButtons();

        if ("__complete__".equals(selectedReason)) {
            // Pack the order then return to list
            completeButton.setDisable(true);
            completeButton.setText("Packing…");
            CompletableFuture.runAsync(() -> {
                for (Long orderId : orderIdsToPack) httpPut("/api/fulfillment/pack/" + orderId);
            }).thenRun(() -> Platform.runLater(this::goToOrderList))
              .exceptionally(e -> {
                  Platform.runLater(() -> {
                      showFeedback("Failed to pack: " + e.getMessage(), false);
                      updateProgress();
                  });
                  return null;
              });
        } else {
            // Save as partial with selected reason
            saveAllProgress();
            for (Long orderId : orderIdsToPack) httpPut("/api/fulfillment/partial/" + orderId);
            goToOrderList();
        }
    }

    private void resetOverlayButtons() {
        partialConfirmBtn.setText("Save as Partial");
        partialConfirmBtn.setDisable(true);
    }

    @FXML private void onPartialDiscard() {
        partialOverlay.setVisible(false);
        resetOverlayButtons();
        // Restore every item to the pick state it had when this session opened
        String items = rows.stream()
            .map(r -> String.format("{\"productId\":%d,\"pickedQuantity\":%d}", r.productId, r.originalPicked))
            .collect(Collectors.joining(","));
        String body = "{\"items\":[" + items + "]}";
        for (Long orderId : orderIdsToPack) {
            try { httpPutWithBody("/api/fulfillment/order/" + orderId + "/pick-progress", body); }
            catch (Exception ignored) {}
        }
        goToOrderList();
    }

    @FXML private void onPartialCancel() {
        partialOverlay.setVisible(false);
        resetOverlayButtons();
        selectedReason = null;
    }

    private void goToOrderList() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/order-list.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) scanField.getScene().getWindow();
            stage.getScene().setRoot(root);
            stage.setTitle("Home Depot - Store Fulfillment");
        } catch (Exception e) {
            showFeedback("Failed to return: " + e.getMessage(), false);
        }
    }

    // ── HTTP ──────────────────────────────────────────────────────────────────

    private JsonNode httpGetNode(String path) {
        try {
            HttpRequest req = HttpRequest.newBuilder().uri(URI.create(BASE_URL + path)).GET().build();
            return objectMapper.readTree(httpClient.send(req, HttpResponse.BodyHandlers.ofString()).body());
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

    private void httpPutWithBody(String path, String jsonBody) {
        try {
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + path))
                    .header("Content-Type", "application/json")
                    .PUT(HttpRequest.BodyPublishers.ofString(jsonBody)).build();
            httpClient.send(req, HttpResponse.BodyHandlers.ofString());
        } catch (Exception e) { throw new RuntimeException(e.getMessage(), e); }
    }
}
