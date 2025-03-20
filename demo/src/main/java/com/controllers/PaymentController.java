package com.controllers;

import com.entities.Client;
import com.entities.Payment;
import com.entities.Produit;
import com.core.Fabrique;

import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.stage.Stage;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;

public class PaymentController {

    @FXML
    private ComboBox<Client> clientComboBox; // ComboBox pour sélectionner un client

    @FXML
    private ComboBox<String> modePaiementComboBox; // ComboBox pour sélectionner un mode de paiement

    private List<Client> clients; // Liste des clients disponibles
    private double montantTotal; // Montant total du panier
    private String detailsProduits; // Détails des produits du panier
    private Map<Produit, Integer> produitsDansLePanier; // Map pour stocker les produits et leur quantité

    // Méthode pour initialiser les données
    public void initializeData(List<Client> clients, double montantTotal, String detailsProduits, Map<Produit, Integer> produitsDansLePanier) {
        this.clients = clients;
        this.montantTotal = montantTotal;
        this.detailsProduits = detailsProduits;
        this.produitsDansLePanier = produitsDansLePanier;

        // Remplir les ComboBox
        clientComboBox.setItems(FXCollections.observableArrayList(clients));
        modePaiementComboBox.setItems(FXCollections.observableArrayList("Cash", "Wave Money", "Orange Money"));
    }

    // Méthode pour valider le paiement
    @FXML
    private void handleValider() throws Exception {
        Client clientSelectionne = clientComboBox.getSelectionModel().getSelectedItem();
        String modePaiement = modePaiementComboBox.getSelectionModel().getSelectedItem();

        if (clientSelectionne != null && modePaiement != null) {
            // Générer un numéro de ticket automatiquement
            String numeroTicket = "TICKET-" + System.currentTimeMillis();

            // Créer un objet Payment
            Payment payment = new Payment(
                    0, // L'ID sera généré automatiquement par la base de données
                    numeroTicket,
                    new Date(),
                    montantTotal,
                    modePaiement,
                    clientSelectionne,
                    detailsProduits
            );

            // Enregistrer le paiement (à implémenter)
            enregistrerPaiement(payment);

            // Générer une facture PDF (à implémenter)
            genererFacturePDF(payment);

            // Vider le panier
            viderPanier();

            // Diminuer le stock des produits
            diminuerStockProduits();

            // Fermer la fenêtre
            Stage stage = (Stage) clientComboBox.getScene().getWindow();
            stage.close();
        } else {
            System.out.println("Veuillez sélectionner un client et un mode de paiement.");
        }
    }

    // Méthode pour enregistrer le paiement (à implémenter)
    private void enregistrerPaiement(Payment payment) throws Exception {
        // Logique pour enregistrer le paiement dans la base de données
        Fabrique.getService().addPayment(payment);
    }

    // Méthode pour générer une facture PDF (à implémenter)
  public void genererFacturePDF(Payment payment) {
    try (PDDocument document = new PDDocument()) {
        PDPage page = new PDPage();
        document.addPage(page);

        try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
            // Définir les marges et les positions
            float margin = 50;
            float yStart = page.getMediaBox().getHeight() - margin;
            float yPosition = yStart;
            float lineHeight = 20;

            // Police et taille pour le titre
            contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 18);
            contentStream.beginText();
            contentStream.newLineAtOffset(margin, yPosition);
            contentStream.showText("FACTURE");
            contentStream.endText();
            yPosition -= lineHeight * 2;

            // Police et taille pour les informations de la facture
            contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 12);

            // Informations de la facture
            contentStream.beginText();
            contentStream.newLineAtOffset(margin, yPosition);
            contentStream.showText("Numéro de facture : " + payment.getNumeroTicket());
            contentStream.endText();
            yPosition -= lineHeight;

            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");
            contentStream.beginText();
            contentStream.newLineAtOffset(margin, yPosition);
            contentStream.showText("Date : " + dateFormat.format(payment.getDateHeure()));
            contentStream.endText();
            yPosition -= lineHeight;

            contentStream.beginText();
            contentStream.newLineAtOffset(margin, yPosition);
            contentStream.showText("Client : " + payment.getClient().getName());
            contentStream.endText();
            yPosition -= lineHeight;

            contentStream.beginText();
            contentStream.newLineAtOffset(margin, yPosition);
            contentStream.showText("Mode de paiement : " + payment.getModePaiement());
            contentStream.endText();
            yPosition -= lineHeight;

            contentStream.beginText();
            contentStream.newLineAtOffset(margin, yPosition);
            contentStream.showText("Montant total : " + payment.getMontantTotal() + " FCFA");
            contentStream.endText();
            yPosition -= lineHeight * 2;

            // Tableau pour les détails des produits
            contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 12);
            contentStream.beginText();
            contentStream.newLineAtOffset(margin, yPosition);
            contentStream.showText("Détails des produits :");
            contentStream.endText();
            yPosition -= lineHeight;

            // Séparateur
            contentStream.moveTo(margin, yPosition);
            contentStream.lineTo(page.getMediaBox().getWidth() - margin, yPosition);
            contentStream.stroke();
            yPosition -= lineHeight;

            // Détails des produits
            String[] produits = payment.getDetailsProduits().split(",");
            for (String produit : produits) {
                contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 10);
                contentStream.beginText();
                contentStream.newLineAtOffset(margin, yPosition);
                contentStream.showText("- " + produit.trim());
                contentStream.endText();
                yPosition -= lineHeight;
            }

            // Pied de page
            contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 10);
            contentStream.beginText();
            contentStream.newLineAtOffset(margin, yPosition - lineHeight * 2);
            contentStream.showText("Merci pour votre achat !");
            contentStream.endText();

            contentStream.beginText();
            contentStream.newLineAtOffset(margin, yPosition - lineHeight * 3);
            contentStream.showText("Contact : contact@votreentreprise.com | Tél : +221 77 137 45 53 26");
            contentStream.endText();
        }

        // Sauvegarder le document
        String filePath = "Facture_" + payment.getNumeroTicket() + ".pdf";
        document.save(filePath);
        System.out.println("Facture générée avec succès : " + filePath);
    } catch (IOException e) {
        e.printStackTrace();
    }
}

    // Méthode pour vider le panier
    private void viderPanier() {
        produitsDansLePanier.clear(); // Vider la Map du panier
    }

    // Méthode pour diminuer le stock des produits
    private void diminuerStockProduits() {
        for (Map.Entry<Produit, Integer> entry : produitsDansLePanier.entrySet()) {
            Produit produit = entry.getKey();
            int quantiteAchetee = entry.getValue();

            // Diminuer le stock du produit
            int nouveauStock = produit.getStock() - quantiteAchetee;
            produit.setStock(nouveauStock);

            // Mettre à jour le produit dans la base de données
            mettreAJourProduit(produit);
        }
        
    }

    // Méthode pour mettre à jour le produit dans la base de données
    private void mettreAJourProduit(Produit produit) {
        try {
            // Utiliser votre service ou DAO pour mettre à jour le produit
            Fabrique.getService().updateProduit(produit);
            System.out.println("Produit mis à jour : " + produit.getNom());
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Erreur lors de la mise à jour du produit : " + produit.getNom());
        }
    }
}