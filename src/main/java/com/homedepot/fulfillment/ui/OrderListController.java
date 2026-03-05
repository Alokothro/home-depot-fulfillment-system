package com.homedepot.fulfillment.ui;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;

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

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @FXML
    public void initialize() {
        // Wait for Spring Boot to start, then load orders
        waitForServer().thenRun(() -> Platform.runLater(this::loadOrders));
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
                    .uri(URI.create(BASE_URL + "/api/orders"))
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
                    VBox orderCard = createOrderCard(order);
                    orderListContainer.getChildren().add(orderCard);
                }

            } catch (Exception e) {
                e.printStackTrace();
                showError("Failed to parse orders: " + e.getMessage());
            }
        });
    }

    private VBox createOrderCard(JsonNode order) {
        VBox card = new VBox(8);
        card.getStyleClass().add("warehouse-order-card");
        card.setPadding(new Insets(15));

        // Row 1: Customer name and arrow
        HBox row1 = new HBox(10);
        row1.setAlignment(Pos.CENTER_LEFT);

        Label customerName = new Label(getCustomerName(order));
        customerName.getStyleClass().add("warehouse-order-name");
        customerName.setFont(Font.font("System", 18));

        // Badge for priority/status
        if (isPriorityOrder(order)) {
            Label priorityBadge = new Label("⚡");
            priorityBadge.getStyleClass().add("warehouse-badge-priority");
            row1.getChildren().addAll(customerName, priorityBadge);
        } else {
            row1.getChildren().add(customerName);
        }

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label arrow = new Label("›");
        arrow.setFont(Font.font("System", 24));
        arrow.setStyle("-fx-text-fill: #9ca3af;");

        row1.getChildren().addAll(spacer, arrow);

        // Row 2: Order number, delivery type, item count
        HBox row2 = new HBox(15);
        row2.setAlignment(Pos.CENTER_LEFT);

        Label orderNumber = new Label("Order #" + order.get("orderId").asText());
        orderNumber.getStyleClass().add("warehouse-order-number");

        Label deliveryType = new Label(order.has("shippingMethod") ?
                order.get("shippingMethod").asText() : "Standard");
        deliveryType.getStyleClass().add("warehouse-order-info");

        int itemCount = order.has("orderItems") ? order.get("orderItems").size() : 0;
        Label items = new Label(itemCount + " Item" + (itemCount != 1 ? "s" : ""));
        items.getStyleClass().add("warehouse-order-info");

        row2.getChildren().addAll(orderNumber, deliveryType, items);

        // Row 3: Due date/time
        Label dueDate = new Label(formatDueDate(order));
        dueDate.getStyleClass().add("warehouse-order-due");

        // Add status badge if applicable
        HBox row3 = new HBox(10);
        row3.setAlignment(Pos.CENTER_LEFT);
        row3.getChildren().add(dueDate);

        String status = order.has("orderStatus") ? order.get("orderStatus").asText() : "PENDING";
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
        card.setOnMouseClicked(e -> handleOrderClick(order));

        return card;
    }

    private String getCustomerName(JsonNode order) {
        if (order.has("customer")) {
            JsonNode customer = order.get("customer");
            String firstName = customer.has("firstName") ? customer.get("firstName").asText() : "";
            String lastName = customer.has("lastName") ? customer.get("lastName").asText() : "";
            return (firstName + " " + lastName).trim();
        }
        return "Customer";
    }

    private boolean isPriorityOrder(JsonNode order) {
        // Mark as priority if due soon (within 2 hours)
        if (order.has("orderDate")) {
            // In a real app, calculate based on due date
            // For now, randomly mark some as priority
            return order.get("orderId").asInt() % 3 == 0;
        }
        return false;
    }

    private String formatDueDate(JsonNode order) {
        if (order.has("orderDate")) {
            try {
                String dateStr = order.get("orderDate").asText();
                LocalDateTime orderDate = LocalDateTime.parse(dateStr);
                LocalDateTime dueDate = orderDate.plusDays(1);

                long hoursUntilDue = java.time.Duration.between(LocalDateTime.now(), dueDate).toHours();

                if (hoursUntilDue < 24) {
                    return "Due in " + hoursUntilDue + " hour" + (hoursUntilDue != 1 ? "s" : "");
                } else {
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEEE h:mma");
                    return dueDate.format(formatter);
                }
            } catch (Exception e) {
                return "Due soon";
            }
        }
        return "Due soon";
    }

    private void handleOrderClick(JsonNode order) {
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
