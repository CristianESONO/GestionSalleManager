package com.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;

import com.core.Fabrique;
import com.entities.*;

import java.util.Date;
import java.util.List;
import java.util.Random;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;

public class AddReservationController {

    @FXML
    private TextField nameField;

    @FXML
    private TextField phoneField;

    @FXML
    private DatePicker birthDateField;

    @FXML
    private TextField addressField;

    @FXML
    private TextField loyaltyPointsField;

    @FXML
    private ComboBox<String> roleComboBox;

    @FXML
    private ComboBox<Game> gameComboBox;

    @FXML
    private ComboBox<Poste> posteComboBox;

    @FXML
    private ComboBox<Integer> durationComboBox;

    @FXML
    private TextField codeParrainageField;

    @FXML
    private Button addButton;

    @FXML
    private Button cancelButton;

    @FXML
    public void initialize() {
        // Configurer le ComboBox pour la durée
        setupDurationComboBox();

        // Configurer le ComboBox des rôles
        setupRoleComboBox();

        // Charger les jeux et les postes
        loadGames();
        loadPostes();

        // Écouter les changements de sélection dans le ComboBox des jeux
        setupGameComboBoxListener();

        // Configurer les boutons
        addButton.setOnAction(event -> {
            try {
                addReservation();
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        });
        cancelButton.setOnAction(event -> cancel());
    }

    private void setupDurationComboBox() {
        // Ajouter des valeurs multiples de 15 minutes (15, 30, 45, 60, 75, etc.)
        for (int i = 15; i <= 180; i += 15) { // Limite à 180 minutes (3 heures)
            durationComboBox.getItems().add(i);
        }
        durationComboBox.getSelectionModel().selectFirst(); // Sélectionner la première valeur par défaut
    }

    private void setupRoleComboBox() {
        // Ajouter les rôles disponibles
        roleComboBox.getItems().add(Role.Client.name());
    }

    private void loadGames() {
        List<Game> games = Fabrique.getService().getAllGames();
        gameComboBox.getItems().addAll(games);
    }

    private void loadPostes() {
        List<Poste> postes = Fabrique.getService().getPostes();
        posteComboBox.getItems().addAll(postes);
    }

    private void setupGameComboBoxListener() {
        // Écouter les changements de sélection dans le ComboBox des jeux
        gameComboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldGame, newGame) -> {
            if (newGame != null) {
                // Charger les postes disponibles pour le jeu sélectionné
                loadPostesForGame(newGame);
            } else {
                // Effacer les postes si aucun jeu n'est sélectionné
                posteComboBox.getItems().clear();
            }
        });
    }

    private void loadPostesForGame(Game game) {
        // Récupérer les postes disponibles pour le jeu sélectionné
        List<Poste> postes = Fabrique.getService().getPostesForGame(game);
        posteComboBox.getItems().clear(); // Effacer les anciens postes
        posteComboBox.getItems().addAll(postes); // Ajouter les nouveaux postes
    }

    @FXML
    private void addReservation() throws Exception {
        // Récupérer les valeurs du formulaire client
        String name = nameField.getText().trim();
        String phone = phoneField.getText().trim();
        LocalDate birthDate = birthDateField.getValue();
        String address = addressField.getText().trim();
        int loyaltyPoints = Integer.parseInt(loyaltyPointsField.getText().trim());
        Role role = roleComboBox.getValue().equals(Role.Client.name()) ? Role.Client : Role.Admin;
        Date date = Date.from(birthDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
    
        // Récupérer les autres valeurs pour la réservation
        Game selectedGame = gameComboBox.getValue();
        Poste selectedPoste = posteComboBox.getValue();
        int durationInMinutes = durationComboBox.getValue();
        String codeParrainage = codeParrainageField.getText().trim();
    
        // Valider les champs obligatoires
        if (name.isEmpty() || phone.isEmpty() || birthDate == null || address.isEmpty() || selectedGame == null || selectedPoste == null) {
            ControllerUtils.showErrorAlert("Erreur", "Veuillez remplir tous les champs obligatoires.");
            return;
        }
    
        // Valider la durée
        if (durationInMinutes < 15 || durationInMinutes % 15 != 0) {
            ControllerUtils.showErrorAlert("Erreur", "La durée doit être un multiple de 15 minutes (15, 30, 45, etc.).");
            return;
        }
    
        // Vérifier si le client existe déjà en base de données
        Client client = Fabrique.getService().findByTel(phone);
    
        if (client == null) {
            // Créer un nouveau client s'il n'existe pas
            client = new Client();
            client.setName(name);
            client.setPhone(phone);
            client.setBirthDate(date);
            client.setAddress(address);
            client.setLoyaltyPoints(loyaltyPoints);
            client.setRole(role);
            client.setRegistrationDate(date);
    
            // Ajouter le nouveau client à la base de données
            Fabrique.getService().addClient(client);
        }
    
        // Créer la réservation
        LocalDateTime dateTime = LocalDateTime.now();
        Duration duration = Duration.ofMinutes(durationInMinutes);
        Reservation reservation = new Reservation(client, dateTime, duration, codeParrainage, selectedPoste, selectedGame);
    
        // Générer et définir le numéro de ticket
        String numeroTicket = reservation.generateTicketNumber();
        reservation.setNumeroTicket(numeroTicket);
    
        try {
            // Ajouter la réservation
            Fabrique.getService().insertReservation(reservation);

             // Générer une facture PDF (à implémenter)
             genererFacturePDF(reservation);
    
            ControllerUtils.showInfoAlert("Succès", "Réservation ajoutée avec succès.");
            ControllerUtils.closeWindow(durationComboBox); // Fermer la fenêtre après l'ajout
        } catch (Exception e) {
            ControllerUtils.showErrorAlert("Erreur", "Échec de l'ajout de la réservation : " + e.getMessage());
        }
    }



    public void genererFacturePDF(Reservation reservation) {
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
            contentStream.showText("FACTURE DE RÉSERVATION");
            contentStream.endText();
            yPosition -= lineHeight * 2;

            // Police et taille pour les informations de la facture
            contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 12);

            // Informations de la réservation
            contentStream.beginText();
            contentStream.newLineAtOffset(margin, yPosition);
            contentStream.showText("Numéro de réservation : " + reservation.getNumeroTicket());
            contentStream.endText();
            yPosition -= lineHeight;

            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");
            contentStream.beginText();
            contentStream.newLineAtOffset(margin, yPosition);
            contentStream.showText("Date de réservation : " + dateFormat.format(reservation.getReservationDate()));
            contentStream.endText();
            yPosition -= lineHeight;

            contentStream.beginText();
            contentStream.newLineAtOffset(margin, yPosition);
            contentStream.showText("Client : " + reservation.getClient().getName());
            contentStream.endText();
            yPosition -= lineHeight;

            contentStream.beginText();
            contentStream.newLineAtOffset(margin, yPosition);
            contentStream.showText("Jeu réservé : " + reservation.getGame().getName());
            contentStream.endText();
            yPosition -= lineHeight;

            contentStream.beginText();
            contentStream.newLineAtOffset(margin, yPosition);
            contentStream.showText("Poste réservé : " + reservation.getPoste().getName());
            contentStream.endText();
            yPosition -= lineHeight;

            contentStream.beginText();
            contentStream.newLineAtOffset(margin, yPosition);
            contentStream.showText("Durée : " + reservation.getDuration().toMinutes() + " minutes");
            contentStream.endText();
            yPosition -= lineHeight;

            contentStream.beginText();
            contentStream.newLineAtOffset(margin, yPosition);
            contentStream.showText("Code de parrainage : " + (reservation.getCodeParrainage() != null ? reservation.getCodeParrainage() : "N/A"));
            contentStream.endText();
            yPosition -= lineHeight * 2;

            // Pied de page
            contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 10);
            contentStream.beginText();
            contentStream.newLineAtOffset(margin, yPosition - lineHeight * 2);
            contentStream.showText("Merci pour votre réservation !");
            contentStream.endText();

            contentStream.beginText();
            contentStream.newLineAtOffset(margin, yPosition - lineHeight * 3);
            contentStream.showText("Contact : contact@votreentreprise.com | Tél : +221 77 137 45 53 26");
            contentStream.endText();
        }

        // Sauvegarder le document
        String filePath = "Facture_Reservation_" + reservation.getNumeroTicket() + ".pdf";
        document.save(filePath);
        System.out.println("Facture de réservation générée avec succès : " + filePath);
    } catch (IOException e) {
        e.printStackTrace();
    }
}

       /**
     * Génère un mot de passe aléatoire.
     * @return Un mot de passe aléatoire de 10 caractères.
     */
    private String generateRandomPassword() {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        Random random = new Random();
        StringBuilder password = new StringBuilder();

        for (int i = 0; i < 10; i++) {
            int index = random.nextInt(characters.length());
            password.append(characters.charAt(index));
        }

        return password.toString();
    }
    
    @FXML
    private void cancel() {
        ControllerUtils.closeWindow(durationComboBox); // Fermer la fenêtre lors de l'annulation
    }
}