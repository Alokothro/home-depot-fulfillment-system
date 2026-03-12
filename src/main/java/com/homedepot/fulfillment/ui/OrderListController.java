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
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.CompletableFuture;

/**
 * Controller for the order list view (warehouse associate interface).
 */
public class OrderListController {

    private static final String BASE_URL = "http://localhost:8080";
    private static final int MAX_RETRIES = 30;
    private static final int RETRY_DELAY_MS = 1000;

    @FXML
    private VBox orderListContainer;

    @FXML
    private Label timeLabel;

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private Timeline timeline;

    @FXML
    public void initialize() {
        // Set current time
        updateTime();

        // Update time every minute
        timeline = new Timeline(new KeyFrame(Duration.seconds(60), e -> updateTime()));
        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.play();

        // Wait for Spring Boot to start, then load orders
        waitForServer().thenRun(() -> Platform.runLater(this::loadOrders));
    }

    private void updateTime() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("h:mm a");
        String currentTime = LocalDateTime.now().format(formatter);
        timeLabel.setText(currentTime);
    }

    @FXML
    private void onBackClick(MouseEvent event) {
        try {
            // Stop the timeline
            if (timeline != null) {
                timeline.stop();
            }

            // Load the landing page
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/landing.fxml"));
            Parent root = loader.load();

            // Get the current stage and switch scenes
            Stage stage = (Stage) timeLabel.getScene().getWindow();
            Scene scene = new Scene(root, 1400, 900);
            scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());

            stage.setScene(scene);
            stage.setTitle("Home Depot Order Fulfillment System");

        } catch (IOException e) {
            e.printStackTrace();
            showError("Failed to return to landing page: " + e.getMessage());
        }
    }

    private CompletableFuture<Void> waitForServer() {
        return CompletableFuture.runAsync(() -> {
            for (int i = 0; i < MAX_RETRIES; i++) {
                try {
                    HttpRequest request = HttpRequest.newBuilder()
                            .uri(URI.create(BASE_URL + "/api/warehouses"))
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
            Platform.runLater(() -> showError("Spring Boot server failed to start after " + MAX_RETRIES + " seconds"));
        });
    }

    private void loadOrders() {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/api/fulfillment/batched-orders"))
                    .GET()
                    .build();

            httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenApply(HttpResponse::body)
                    .thenAccept(this::displayOrders)
                    .exceptionally(e -> {
                        Platform.runLater(() -> showError("Failed to load orders: " + e.getMessage()));
                        return null;
                    });

        } catch (Exception e) {
            showError("Failed to load orders: " + e.getMessage());
        }
    }

    private void displayOrders(String jsonResponse) {
        Platform.runLater(() -> {
            try {
                JsonNode orders = objectMapper.readTree(jsonResponse);
                orderListContainer.getChildren().clear();

                for (JsonNode order : orders) {
                    boolean isBatched = order.has("batched") && order.get("batched").asBoolean();
                    VBox orderCard = isBatched ? createBatchedOrderCard(order) : createIndividualOrderCard(order);
                    orderListContainer.getChildren().add(orderCard);
                }

            } catch (Exception e) {
                e.printStackTrace();
                showError("Failed to parse orders: " + e.getMessage());
            }
        });
    }

    private VBox createBatchedOrderCard(JsonNode batch) {
        VBox card = new VBox(8);
        card.getStyleClass().add("warehouse-order-card");
        card.setPadding(new Insets(15));

        // Row 1: Department name and customer count
        HBox row1 = new HBox(10);
        row1.setAlignment(Pos.CENTER_LEFT);

        Label departmentName = new Label(batch.get("department").asText() + " Order");
        departmentName.getStyleClass().add("warehouse-order-name");
        departmentName.setFont(Font.font("System", 18));

        Label batchBadge = new Label("BATCH");
        batchBadge.getStyleClass().addAll("warehouse-badge", "warehouse-badge-priority");

        row1.getChildren().addAll(departmentName, batchBadge);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label arrow = new Label("›");
        arrow.setFont(Font.font("System", 24));
        arrow.setStyle("-fx-text-fill: #9ca3af;");

        row1.getChildren().addAll(spacer, arrow);

        // Row 2: Customer count and total items
        HBox row2 = new HBox(15);
        row2.setAlignment(Pos.CENTER_LEFT);

        int customerCount = batch.get("customerCount").asInt();
        int totalItems = batch.get("totalItems").asInt();

        Label customers = new Label(customerCount + " Customer" + (customerCount != 1 ? "s" : ""));
        customers.getStyleClass().add("warehouse-order-number");

        Label items = new Label(totalItems + " Total Item" + (totalItems != 1 ? "s" : ""));
        items.getStyleClass().add("warehouse-order-info");

        row2.getChildren().addAll(customers, items);

        // Row 3: Department indicator
        Label department = new Label("Department: " + batch.get("department").asText());
        department.getStyleClass().add("warehouse-order-due");

        card.getChildren().addAll(row1, row2, department);

        // Make card clickable
        card.setOnMouseClicked(e -> handleBatchClick(batch));

        return card;
    }

    private VBox createIndividualOrderCard(JsonNode order) {
        VBox card = new VBox(8);
        card.getStyleClass().add("warehouse-order-card");
        card.setPadding(new Insets(15));

        // Row 1: Customer name and arrow
        HBox row1 = new HBox(10);
        row1.setAlignment(Pos.CENTER_LEFT);

        String customerName = order.has("customerName") ? order.get("customerName").asText() : "Customer";
        Label customerLabel = new Label(customerName);
        customerLabel.getStyleClass().add("warehouse-order-name");
        customerLabel.setFont(Font.font("System", 18));

        row1.getChildren().add(customerLabel);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label arrow = new Label("›");
        arrow.setFont(Font.font("System", 24));
        arrow.setStyle("-fx-text-fill: #9ca3af;");

        row1.getChildren().addAll(spacer, arrow);

        // Row 2: Order number, delivery type, item count
        HBox row2 = new HBox(15);
        row2.setAlignment(Pos.CENTER_LEFT);

        String orderNum = order.has("orderNumber") ? order.get("orderNumber").asText() : "Order";
        Label orderNumber = new Label(orderNum);
        orderNumber.getStyleClass().add("warehouse-order-number");

        String shipping = order.has("shippingMethod") ? order.get("shippingMethod").asText() : "Standard";
        Label deliveryType = new Label(shipping);
        deliveryType.getStyleClass().add("warehouse-order-info");

        int totalItems = order.has("totalItems") ? order.get("totalItems").asInt() : 0;
        Label items = new Label(totalItems + " Item" + (totalItems != 1 ? "s" : ""));
        items.getStyleClass().add("warehouse-order-info");

        row2.getChildren().addAll(orderNumber, deliveryType, items);

        // Row 3: Due date/time
        String dueDateStr = order.has("dueDate") ? order.get("dueDate").asText() : "Due soon";
        Label dueDate = new Label(dueDateStr);
        dueDate.getStyleClass().add("warehouse-order-due");

        // Add status badge if applicable
        HBox row3 = new HBox(10);
        row3.setAlignment(Pos.CENTER_LEFT);
        row3.getChildren().add(dueDate);

        String status = order.has("status") ? order.get("status").asText() : "PENDING";
        if ("PROCESSING".equals(status)) {
            Label statusBadge = new Label("In Progress");
            statusBadge.getStyleClass().addAll("warehouse-badge", "warehouse-badge-inprogress");
            row3.getChildren().add(statusBadge);
        } else if ("PACKED".equals(status)) {
            Label statusBadge = new Label("Partial");
            statusBadge.getStyleClass().addAll("warehouse-badge", "warehouse-badge-partial");
            row3.getChildren().add(statusBadge);
        }

        card.getChildren().addAll(row1, row2, row3);

        // Make card clickable
        card.setOnMouseClicked(e -> handleIndividualOrderClick(order));

        return card;
    }

    private void handleBatchClick(JsonNode batch) {
        System.out.println("Clicked batch: " + batch.get("department").asText());
        // TODO: Navigate to batch picking view showing all customers and items
    }

    private void handleIndividualOrderClick(JsonNode order) {
        System.out.println("Clicked order: " + order.get("orderId").asText());
        // TODO: Navigate to order detail/picking view
    }

    private void showError(String message) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                javafx.scene.control.Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText("Order Loading Error");
        alert.setContentText(message);
        alert.showAndWait();
    }
}
