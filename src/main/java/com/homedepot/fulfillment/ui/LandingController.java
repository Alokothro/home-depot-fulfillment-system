package com.homedepot.fulfillment.ui;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Controller for the landing page.
 */
public class LandingController {

    @FXML
    private void onAssociateClick(ActionEvent event) {
        try {
            // Load the order list view
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/order-list.fxml"));
            Parent root = loader.load();

            // Get the current stage and switch scenes
            Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root, 1400, 900);
            scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());

            stage.setScene(scene);
            stage.setTitle("Home Depot - Order Fulfillment");

        } catch (IOException e) {
            e.printStackTrace();
            showError("Failed to load Associate view: " + e.getMessage());
        }
    }

    @FXML
    private void onCustomerClick(ActionEvent event) {
        // TODO: Implement customer view
        System.out.println("Customer view not yet implemented");
    }

    private void showError(String message) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText("Navigation Error");
        alert.setContentText(message);
        alert.showAndWait();
    }
}
