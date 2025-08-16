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

import com.utils.ReceiptPrinter; // <-- AJOUTEZ CET IMPORT
import java.io.File; // Toujours nécessaire pour File dans Desktop.getDesktop()
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
    private String detailsProduits; // Pas directement utilisé pour le reçu, mais peut rester pour d'autres logs/usages
    private Runnable onPaymentSuccessCallback;
    private User connectedUser;

    public void setOnPaymentSuccessCallback(Runnable callback) {
        this.onPaymentSuccessCallback = callback;
    }

    @FXML
    public void initialize() {
        modePaiementComboBox.setItems(FXCollections.observableArrayList("Cash", "Wave", "Orange Money", "Mixx/free money", "Wizall Money", "Emoney"));
    }

    public void setConnectedUser(User user) 
    {
        this.connectedUser = user;
    }

    public void initializeData(List<Client> clients, double montantTotal, String detailsProduits, Map<Produit, Integer> produitsDansLePanier) {
        this.montantTotal = montantTotal;
        this.detailsProduits = detailsProduits;
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

        String numeroTicket = "TICKET-" + System.currentTimeMillis();

        // Vérification cruciale de l'utilisateur connecté
        if (connectedUser == null) {
            showAlert("Erreur", "Aucun utilisateur connecté détecté.");
            return;
        }

        Payment payment = new Payment(
            0,
            numeroTicket,
            new Date(),
            montantTotal,
            modePaiement,
            detailsProduits,
            connectedUser
        );

        try {
            enregistrerPaiement(payment);
            diminuerStockProduits();

            // --- NOUVELLE LOGIQUE D'IMPRESSION DU REÇU THERMIQUE ---
            String userName = Fabrique.getService().getCurrentUser().getName(); // Récupérer le nom de l'utilisateur connecté
            ReceiptPrinter receiptPrinter = new ReceiptPrinter(produitsDansLePanier, montantTotal, numeroTicket, userName);
            receiptPrinter.printReceipt(); // Lance l'impression sur l'imprimante sélectionnée par l'OS

            // Supprimez l'appel à genererFacturePDF(payment) si vous ne voulez plus de PDF A4
            // genererFacturePDF(payment);

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