package com.homedepot.fulfillment.ui;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

public class OrderListController {

    private static final String BASE_URL = "http://localhost:8080";

    @FXML private FlowPane   orderGrid;
    @FXML private ScrollPane mainScroll;
    @FXML private Label      timeLabel;
    @FXML private Label      dateLabel;
    @FXML private Label      statsLabel;

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private Timeline clockTimeline;

    // BOPIS countdown: orderId → (deadline, countdown label)
    private final Map<Long, LocalDateTime> bopisDeadlines = new HashMap<>();
    private final Map<Long, Label>         bopisLabels    = new HashMap<>();
    private static final int BOPIS_MINUTES = 120;

    // Delivery method colors
    private static final String COLOR_BOPIS    = "#16a34a";
    private static final String COLOR_CAR      = "#2563eb";
    private static final String COLOR_VAN      = "#ea580c";
    private static final String COLOR_STANDARD = "#64748b";
    private static final String COLOR_BATCH    = "#7c3aed";

    // Avatar colors cycled by first letter
    private static final String[] AVATAR_COLORS = {
        "#f97316","#0ea5e9","#8b5cf6","#ec4899",
        "#10b981","#f59e0b","#ef4444","#6366f1"
    };

    @FXML
    public void initialize() {
        updateClock();
        clockTimeline = new Timeline(new KeyFrame(Duration.seconds(30), e -> updateClock()));
        clockTimeline.setCycleCount(Animation.INDEFINITE);
        clockTimeline.play();
        loadOrders();
    }

    private void updateClock() {
        LocalDateTime now = LocalDateTime.now();
        timeLabel.setText(now.format(DateTimeFormatter.ofPattern("h:mm a")));
        dateLabel.setText(now.format(DateTimeFormatter.ofPattern("EEEE, MMMM d")));
        updateBopisCountdowns();
    }

    private void updateBopisCountdowns() {
        LocalDateTime now = LocalDateTime.now();
        for (Map.Entry<Long, LocalDateTime> entry : bopisDeadlines.entrySet()) {
            Label lbl = bopisLabels.get(entry.getKey());
            if (lbl == null) continue;
            long remaining = ChronoUnit.MINUTES.between(now, entry.getValue());
            lbl.setText(formatCountdown(remaining));
            if (remaining <= 15) {
                lbl.setStyle("-fx-background-color: #fee2e2; -fx-text-fill: #dc2626;"
                        + " -fx-background-radius: 6; -fx-padding: 3 8; -fx-font-size: 12px; -fx-font-weight: bold;");
            } else if (remaining <= 30) {
                lbl.setStyle("-fx-background-color: #fef3c7; -fx-text-fill: #b45309;"
                        + " -fx-background-radius: 6; -fx-padding: 3 8; -fx-font-size: 12px; -fx-font-weight: bold;");
            }
        }
    }

    private String formatCountdown(long minutes) {
        if (minutes <= 0) return "Overdue — pick now";
        if (minutes >= 60) {
            long hrs  = minutes / 60;
            long mins = minutes % 60;
            return hrs + "hr " + mins + "min remaining";
        }
        return minutes + "min remaining";
    }

    @FXML
    private void onRefresh() {
        statsLabel.setText("Refreshing...");
        orderGrid.getChildren().clear();
        mainScroll.setContent(orderGrid);
        mainScroll.setFitToHeight(false);
        loadOrders();
    }

    @FXML
    private void onBackClick(MouseEvent event) {
        if (clockTimeline != null) clockTimeline.stop();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/landing.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) timeLabel.getScene().getWindow();
            Scene scene = new Scene(root, 1400, 900);
            scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());
            stage.setScene(scene);
            stage.setTitle("Home Depot Order Fulfillment System");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadOrders() {
        CompletableFuture.supplyAsync(() -> {
            try {
                HttpRequest req = HttpRequest.newBuilder()
                        .uri(URI.create(BASE_URL + "/api/fulfillment/batched-orders"))
                        .GET().build();
                HttpResponse<String> resp = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
                return resp.body();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }).thenAccept(body -> Platform.runLater(() -> displayOrders(body)))
          .exceptionally(e -> {
              Platform.runLater(() -> statsLabel.setText("Failed to load orders"));
              return null;
          });
    }

    private void displayOrders(String json) {
        try {
            JsonNode orders = objectMapper.readTree(json);
            orderGrid.getChildren().clear();
            bopisDeadlines.clear();
            bopisLabels.clear();

            int count = 0;
            AtomicInteger totalItems = new AtomicInteger(0);

            for (JsonNode order : orders) {
                boolean batched = order.path("batched").asBoolean(false);
                VBox card = batched ? createBatchCard(order) : createOrderCard(order);
                orderGrid.getChildren().add(card);
                count++;
                totalItems.addAndGet(order.path("totalItems").asInt(0));
            }

            if (count == 0) {
                mainScroll.setContent(buildEmptyState());
                mainScroll.setFitToHeight(true);
                statsLabel.setText("No pending orders");
            } else {
                mainScroll.setContent(orderGrid);
                mainScroll.setFitToHeight(false);
                statsLabel.setText(count + " order" + (count != 1 ? "s" : "") + " ready  •  "
                        + totalItems.get() + " item" + (totalItems.get() != 1 ? "s" : "") + " to pick");
            }

        } catch (Exception e) {
            e.printStackTrace();
            statsLabel.setText("Error loading orders");
        }
    }

    // ─── Individual order card ────────────────────────────────────────────────

    private VBox createOrderCard(JsonNode order) {
        String customerName  = order.path("customerName").asText("Customer");
        String orderNumber   = order.path("orderNumber").asText("Order");
        String shipping      = order.path("shippingMethod").asText("Standard");
        int    totalItems    = order.path("totalItems").asInt(0);
        String dueDate       = order.path("dueDate").asText("Due soon");
        String status        = order.path("status").asText("PENDING");
        long   orderId       = order.path("orderId").asLong(0);

        String accentColor  = cardAccentColor(shipping, status);
        String badgeBg      = deliveryBadgeBg(shipping);
        String badgeFg      = deliveryBadgeFg(shipping);
        String avatarColor  = avatarColor(customerName);
        String initials     = initials(customerName);

        VBox card = new VBox(0);
        card.getStyleClass().add("oc-card");
        card.setPrefWidth(420);
        card.setMaxWidth(420);

        // Top accent stripe
        Region stripe = new Region();
        stripe.setPrefHeight(5);
        stripe.setStyle("-fx-background-color: " + accentColor + "; -fx-background-radius: 10 10 0 0;");

        // Card body
        VBox body = new VBox(12);
        body.setPadding(new Insets(16, 16, 4, 16));

        // ── Row 1: avatar + name block + badge ──
        HBox row1 = new HBox(12);
        row1.setAlignment(Pos.CENTER_LEFT);

        Label avatar = new Label(initials);
        avatar.getStyleClass().add("oc-avatar");
        avatar.setStyle("-fx-background-color: " + avatarColor + ";");

        VBox nameBlock = new VBox(2);
        Label nameLabel = new Label(customerName);
        nameLabel.getStyleClass().add("oc-name");
        Label numLabel = new Label(orderNumber);
        numLabel.getStyleClass().add("oc-ordnum");
        nameBlock.getChildren().addAll(nameLabel, numLabel);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label badge = new Label(shipping);
        badge.setStyle("-fx-background-color: " + badgeBg + "; -fx-text-fill: " + badgeFg + ";"
                + " -fx-background-radius: 20; -fx-padding: 3 10; -fx-font-size: 11px; -fx-font-weight: bold;");

        row1.getChildren().addAll(avatar, nameBlock, spacer, badge);

        // ── Divider ──
        Region divider = new Region();
        divider.setPrefHeight(1);
        divider.setStyle("-fx-background-color: #f1f5f9;");
        VBox.setMargin(divider, new Insets(4, 0, 0, 0));

        // ── Row 2: info chips ──
        HBox row2 = new HBox(6);
        row2.setAlignment(Pos.CENTER_LEFT);
        row2.setPadding(new Insets(0, 0, 2, 0));

        Label itemsChip = makeChip(totalItems + (totalItems == 1 ? " item" : " items") + " to pick", "#f1f5f9", "#475569");
        Label dot = new Label("•");
        dot.setStyle("-fx-text-fill: #cbd5e1; -fx-font-size: 14px;");

        boolean isBopis = "BOPIS".equals(shipping);
        String orderDateIso = order.path("orderDateIso").asText(null);

        if (isBopis && orderDateIso != null) {
            LocalDateTime orderDate = LocalDateTime.parse(orderDateIso);
            LocalDateTime deadline  = orderDate.plusMinutes(BOPIS_MINUTES);
            long remaining = ChronoUnit.MINUTES.between(LocalDateTime.now(), deadline);

            Label countdownChip = makeChip(formatCountdown(remaining), "#dcfce7", "#15803d");
            if (remaining <= 15)
                countdownChip.setStyle("-fx-background-color: #fee2e2; -fx-text-fill: #dc2626;"
                        + " -fx-background-radius: 6; -fx-padding: 3 8; -fx-font-size: 12px; -fx-font-weight: bold;");
            else if (remaining <= 30)
                countdownChip.setStyle("-fx-background-color: #fef3c7; -fx-text-fill: #b45309;"
                        + " -fx-background-radius: 6; -fx-padding: 3 8; -fx-font-size: 12px; -fx-font-weight: bold;");

            bopisDeadlines.put(orderId, deadline);
            bopisLabels.put(orderId, countdownChip);

            row2.getChildren().addAll(itemsChip, dot, countdownChip);
        } else if ("PROCESSING".equals(status)) {
            Label dueChip = makeChip(dueDate, "#f1f5f9", "#475569");
            Label inProg  = makeChip("In Progress", "#dbeafe", "#1e40af");
            row2.getChildren().addAll(itemsChip, dot, dueChip, new Label("  "), inProg);
        } else if ("PARTIAL".equals(status)) {
            Label dueChip = makeChip(dueDate, "#f1f5f9", "#475569");
            Label partial = makeChip("Partial — Shortage", "#fef3c7", "#92400e");
            row2.getChildren().addAll(itemsChip, dot, dueChip, new Label("  "), partial);
        } else {
            Label dueChip = makeChip(dueDate, "#f1f5f9", "#475569");
            row2.getChildren().addAll(itemsChip, dot, dueChip);
        }

        body.getChildren().addAll(row1, divider, row2);

        // ── Footer: pick button ──
        HBox footer = new HBox();
        footer.setPadding(new Insets(10, 16, 14, 16));
        footer.setAlignment(Pos.CENTER_RIGHT);

        Button pickBtn = new Button("Start Picking  →");
        pickBtn.getStyleClass().add("oc-pick-btn");
        pickBtn.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(pickBtn, Priority.ALWAYS);
        pickBtn.setOnAction(e -> openPicking(ctrl -> ctrl.initOrder(orderId)));

        footer.getChildren().add(pickBtn);

        card.getChildren().addAll(stripe, body, footer);
        card.setOnMouseClicked(e -> openPicking(ctrl -> ctrl.initOrder(orderId)));

        return card;
    }

    // ─── Batch card ───────────────────────────────────────────────────────────

    private VBox createBatchCard(JsonNode batch) {
        String dept         = batch.path("department").asText("Batch");
        int    customers    = batch.path("customerCount").asInt(0);
        int    totalItems   = batch.path("totalItems").asInt(0);

        VBox card = new VBox(0);
        card.getStyleClass().add("oc-card");
        card.setPrefWidth(420);
        card.setMaxWidth(420);

        Region stripe = new Region();
        stripe.setPrefHeight(5);
        stripe.setStyle("-fx-background-color: " + COLOR_BATCH + "; -fx-background-radius: 10 10 0 0;");

        VBox body = new VBox(12);
        body.setPadding(new Insets(16, 16, 4, 16));

        // ── Row 1: batch icon + dept name + badge ──
        HBox row1 = new HBox(12);
        row1.setAlignment(Pos.CENTER_LEFT);

        Label avatar = new Label("B");
        avatar.getStyleClass().add("oc-avatar");
        avatar.setStyle("-fx-background-color: " + COLOR_BATCH + ";");

        VBox nameBlock = new VBox(2);
        Label nameLabel = new Label(dept + " Department");
        nameLabel.getStyleClass().add("oc-name");
        Label subLabel = new Label(customers + " customer" + (customers != 1 ? "s" : "") + " • batch pick");
        subLabel.getStyleClass().add("oc-ordnum");
        nameBlock.getChildren().addAll(nameLabel, subLabel);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label badge = new Label("BATCH");
        badge.setStyle("-fx-background-color: #ede9fe; -fx-text-fill: #6d28d9;"
                + " -fx-background-radius: 20; -fx-padding: 3 10; -fx-font-size: 11px; -fx-font-weight: bold;");

        row1.getChildren().addAll(avatar, nameBlock, spacer, badge);

        Region divider = new Region();
        divider.setPrefHeight(1);
        divider.setStyle("-fx-background-color: #f1f5f9;");

        HBox row2 = new HBox(6);
        row2.setAlignment(Pos.CENTER_LEFT);
        Label itemsChip = makeChip(totalItems + " total items", "#f1f5f9", "#475569");
        Label dot = new Label("•");
        dot.setStyle("-fx-text-fill: #cbd5e1; -fx-font-size: 14px;");
        Label custChip = makeChip(customers + " customers", "#ede9fe", "#6d28d9");
        row2.getChildren().addAll(itemsChip, dot, custChip);

        body.getChildren().addAll(row1, divider, row2);

        HBox footer = new HBox();
        footer.setPadding(new Insets(10, 16, 14, 16));
        footer.setAlignment(Pos.CENTER_RIGHT);

        Button pickBtn = new Button("Start Batch Pick  →");
        pickBtn.getStyleClass().add("oc-pick-btn");
        pickBtn.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(pickBtn, Priority.ALWAYS);
        pickBtn.setOnAction(e -> openPicking(ctrl -> ctrl.initBatch(batch)));

        footer.getChildren().add(pickBtn);
        card.getChildren().addAll(stripe, body, footer);
        card.setOnMouseClicked(e -> openPicking(ctrl -> ctrl.initBatch(batch)));

        return card;
    }

    // ─── Empty state ──────────────────────────────────────────────────────────

    private StackPane buildEmptyState() {
        Label icon = new Label("✓");
        icon.setStyle("-fx-font-size: 90px; -fx-text-fill: #10b981;");

        Label title = new Label("All caught up!");
        title.setStyle("-fx-font-size: 32px; -fx-font-weight: bold; -fx-text-fill: #0f172a;");

        Label sub = new Label("No pending orders right now.\nNew customer orders will appear here automatically.");
        sub.setStyle("-fx-font-size: 16px; -fx-text-fill: #64748b;");
        sub.setWrapText(true);
        sub.setTextAlignment(TextAlignment.CENTER);
        sub.setMaxWidth(460);

        VBox box = new VBox(20, icon, title, sub);
        box.setAlignment(Pos.CENTER);

        StackPane stack = new StackPane(box);
        stack.setAlignment(Pos.CENTER);
        return stack;
    }

    // ─── Navigation ───────────────────────────────────────────────────────────

    private void openPicking(java.util.function.Consumer<PickingController> init) {
        if (clockTimeline != null) clockTimeline.stop();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/picking.fxml"));
            Parent root = loader.load();
            PickingController ctrl = loader.getController();
            Stage stage = (Stage) orderGrid.getScene().getWindow();
            Scene scene = new Scene(root, 1400, 900);
            scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());
            stage.setScene(scene);
            stage.setTitle("Home Depot - Picking");
            init.accept(ctrl);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private Label makeChip(String text, String bg, String fg) {
        Label l = new Label(text);
        l.setStyle("-fx-background-color: " + bg + "; -fx-text-fill: " + fg + ";"
                + " -fx-background-radius: 6; -fx-padding: 3 8; -fx-font-size: 12px;");
        return l;
    }

    private String initials(String name) {
        String[] parts = name.trim().split("\\s+");
        if (parts.length >= 2 && !parts[0].isEmpty() && !parts[1].isEmpty())
            return ("" + parts[0].charAt(0) + parts[1].charAt(0)).toUpperCase();
        if (parts.length == 1 && !parts[0].isEmpty())
            return String.valueOf(parts[0].charAt(0)).toUpperCase();
        return "?";
    }

    private String avatarColor(String name) {
        int idx = name.isEmpty() ? 0 : Math.abs((int) name.charAt(0)) % AVATAR_COLORS.length;
        return AVATAR_COLORS[idx];
    }

    private String deliveryColor(String method) {
        if (method == null) return COLOR_STANDARD;
        return switch (method) {
            case "BOPIS"        -> COLOR_BOPIS;
            case "Car Delivery" -> COLOR_CAR;
            case "Van Delivery" -> COLOR_VAN;
            default             -> COLOR_STANDARD;
        };
    }

    private String cardAccentColor(String method, String status) {
        if ("PARTIAL".equals(status)) return "#f59e0b"; // amber for shortage
        return deliveryColor(method);
    }

    private String deliveryBadgeBg(String method) {
        if (method == null) return "#f1f5f9";
        return switch (method) {
            case "BOPIS"        -> "#dcfce7";
            case "Car Delivery" -> "#dbeafe";
            case "Van Delivery" -> "#fff7ed";
            default             -> "#f1f5f9";
        };
    }

    private String deliveryBadgeFg(String method) {
        if (method == null) return "#475569";
        return switch (method) {
            case "BOPIS"        -> "#15803d";
            case "Car Delivery" -> "#1e40af";
            case "Van Delivery" -> "#c2410c";
            default             -> "#475569";
        };
    }
}
