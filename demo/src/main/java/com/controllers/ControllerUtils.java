package com.controllers;


import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class ControllerUtils {

    public static void showErrorAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

      // Method for displaying an informational alert
    public static void showInfoAlert(String title, String message) {
        Alert alert = new Alert(AlertType.INFORMATION, message, ButtonType.OK);
        alert.setTitle(title);
        alert.showAndWait();

    }
    public static void closeWindow(TextField field) {
        Stage stage = (Stage) field.getScene().getWindow();
        stage.close();
    }

    public static void closeWindow(ComboBox field) {
        Stage stage = (Stage) field.getScene().getWindow();
        stage.close();
    }

    public static boolean isValidEmail(String email) {
        return email.matches("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$");
    }

    public static boolean isValidPhone(String phone) {
        return phone.matches("\\d{9}"); // Exemple pour un numéro à 9 chiffres
    }
}