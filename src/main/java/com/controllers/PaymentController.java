package com.controllers;

import com.core.Fabrique;
import com.entities.Client;
import com.entities.Payment;
import com.entities.Produit;
import com.entities.User;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import com.utils.ReceiptPrinter;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class PaymentController {
    @FXML private ComboBox<String> modePaiementComboBox;
    @FXML private Text montantTotalText;
    private Map<Produit, Integer> produitsDansLePanier;
    private double montantTotal;
    private String detailsProduits;
    private String detailReservations; // Nouveau champ pour les détails des réservations
    private Runnable onPaymentSuccessCallback;
    private User connectedUser;

    public void setOnPaymentSuccessCallback(Runnable callback) {
        this.onPaymentSuccessCallback = callback;
    }

    @FXML
    public void initialize() {
        modePaiementComboBox.setItems(FXCollections.observableArrayList("En Espèce", "Wave", "Orange Money", "free money", "Wizall Money"));
    }

    public void setConnectedUser(User user) {
        this.connectedUser = user;
    }

    // Mise à jour : ajout du paramètre detailReservations
    public void initializeData(List<Client> clients, double montantTotal, String detailsProduits, String detailReservations, Map<Produit, Integer> produitsDansLePanier) {
        this.montantTotal = montantTotal;
        this.detailsProduits = detailsProduits;
        this.detailReservations = detailReservations; // Initialisation du nouveau champ
        this.produitsDansLePanier = produitsDansLePanier;
        montantTotalText.setText(String.format("%.2f FCFA", montantTotal));
    }

    @FXML
    private void handleValider() throws Exception {
        String modePaiement = modePaiementComboBox.getSelectionModel().getSelectedItem();
        if (modePaiement == null) {
            showAlert("Erreur de sélection", "Veuillez sélectionner un mode de paiement.");
            return;
        }

        String numeroTicket = "TICKET-" + String.format("%06d", (int)(System.currentTimeMillis() % 1000000));

        if (connectedUser == null) {
            showAlert("Erreur", "Aucun utilisateur connecté détecté.");
            return;
        }

        // Mise à jour : création de l'objet Payment avec detailReservations
        Payment payment = new Payment(
            0,
            numeroTicket,
            new Date(),
            montantTotal,
            modePaiement,
            detailsProduits,
            detailReservations, // Passage du nouveau champ
            connectedUser
        );

        try {
            enregistrerPaiement(payment);
            diminuerStockProduits();

            String userName = Fabrique.getService().getCurrentUser().getName();
            ReceiptPrinter receiptPrinter = new ReceiptPrinter(produitsDansLePanier, montantTotal, numeroTicket, userName, modePaiement);
            receiptPrinter.printReceipt();

            if (onPaymentSuccessCallback != null) {
                onPaymentSuccessCallback.run();
            }

            Stage stage = (Stage) modePaiementComboBox.getScene().getWindow();
            stage.close();
            showAlert("Paiement réussi", "La transaction a été validée et le reçu imprimé.");
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur", "Une erreur est survenue lors du traitement du paiement : " + e.getMessage());
        }
    }

    private void enregistrerPaiement(Payment payment) throws Exception {
        Fabrique.getService().addPayment(payment);
    }

    private void diminuerStockProduits() throws Exception {
        if (produitsDansLePanier == null || produitsDansLePanier.isEmpty()) {
            return;
        }
        Fabrique.getService().updateProduitStocks(produitsDansLePanier);
    }

    private void showAlert(String title, String message) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    private void cancel(ActionEvent event) {
        Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
        stage.close();
    }
}
