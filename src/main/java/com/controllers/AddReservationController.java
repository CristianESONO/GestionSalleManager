package com.controllers;

import com.core.Fabrique;
import com.entities.Client;
import com.entities.Game;
import com.entities.GameSession;
import com.entities.Parrain;
import com.entities.Payment;
import com.entities.Poste;
import com.entities.Promotion;
import com.entities.Reservation;
import com.entities.TypePromotion;
import com.entities.User;
import com.utils.ReservationReceiptPrinter;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class AddReservationController {
    @FXML private TextField nameField;
    @FXML private TextField phoneField;
    @FXML private TextField addressField;
    @FXML private ComboBox<Poste> posteComboBox;
    @FXML private ComboBox<Game> gameComboBox;
    @FXML private ComboBox<String> modePaiementComboBox;
    @FXML private ComboBox<Integer> hoursComboBox;
    @FXML private ComboBox<Integer> minutesComboBox;
    @FXML private ComboBox<Parrain> parrainComboBox;
    @FXML private Label amountLabel;
    @FXML private CheckBox temporaryClientCheckBox;
    @FXML private Button searchButton;

    private double calculatedAmount;
    private ReservationController parentController;
    private String connectedUserName;
    private User connectedUser;
    private List<Poste> allPostes;

    @FXML
    public void initialize() {
        try {
            checkFXMLFields();
            setupPhoneField();
            setupHoursAndMinutesComboBoxes();
            loadGames();
            loadParrains();
            if (modePaiementComboBox != null) {
                modePaiementComboBox.setItems(FXCollections.observableArrayList(
                    "En Espèce", "Wave", "Orange Money", "Free Money"
                ));
            }
            if (posteComboBox != null) {
                posteComboBox.setConverter(new StringConverter<Poste>() {
                    @Override
                    public String toString(Poste poste) {
                        if (poste == null) return "";
                        return "Poste " + poste.getId() + " - " + (poste.getName() != null ? poste.getName() : "");
                    }
                    @Override
                    public Poste fromString(String string) {
                        return null;
                    }
                });
            }
            loadPostes();
            if (gameComboBox != null) {
                gameComboBox.valueProperty().addListener((obs, oldVal, newGame) -> {
                    filterPostesByGame(newGame);
                    calculateAndSetAmount();
                });
            }
            if (posteComboBox != null) {
                posteComboBox.valueProperty().addListener((obs, oldVal, newVal) -> calculateAndSetAmount());
            }
            if (hoursComboBox != null) {
                hoursComboBox.valueProperty().addListener((obs, oldVal, newVal) -> calculateAndSetAmount());
            }
            if (minutesComboBox != null) {
                minutesComboBox.valueProperty().addListener((obs, oldVal, newVal) -> calculateAndSetAmount());
            }

            // Masquer les champs par défaut
            nameField.setVisible(false);
            addressField.setVisible(false);

            // Ajouter un listener pour la case à cocher "Client temporaire"
            temporaryClientCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal) {
                    // Désactiver les champs si "Client temporaire" est coché
                    nameField.setDisable(true);
                    addressField.setDisable(true);
                    nameField.clear();
                    addressField.clear();
                    phoneField.setDisable(true);
                    phoneField.clear();
                    // Désactiver le champ parrain
                    parrainComboBox.setDisable(true);
                    parrainComboBox.setValue(null);
                } else {
                    // Réactiver les champs si "Client temporaire" est décoché
                    nameField.setDisable(false);
                    addressField.setDisable(false);
                    phoneField.setDisable(false);
                    // Réactiver le champ parrain
                    parrainComboBox.setDisable(false);
                }
            });
            calculateAndSetAmount();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Erreur d'initialisation du contrôleur", e);
        }
    }

    private void checkFXMLFields() {
        if (nameField == null) System.err.println("ERREUR: nameField est null");
        if (phoneField == null) System.err.println("ERREUR: phoneField est null");
        if (addressField == null) System.err.println("ERREUR: addressField est null");
        if (posteComboBox == null) System.err.println("ERREUR: posteComboBox est null");
        if (gameComboBox == null) System.err.println("ERREUR: gameComboBox est null");
        if (modePaiementComboBox == null) System.err.println("ERREUR: modePaiementComboBox est null");
        if (hoursComboBox == null) System.err.println("ERREUR: hoursComboBox est null");
        if (minutesComboBox == null) System.err.println("ERREUR: minutesComboBox est null");
        if (parrainComboBox == null) System.err.println("ERREUR: parrainComboBox est null");
        if (amountLabel == null) System.err.println("ERREUR: amountLabel est null");
    }

    private void setupPhoneField() {
        phoneField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null || newValue.isEmpty()) return;
            String filteredValue = newValue.replaceAll("[^0-9]", "");
            if (filteredValue.length() > 9) filteredValue = filteredValue.substring(0, 9);
            if (!filteredValue.equals(newValue)) phoneField.setText(filteredValue);
        });
    }

    public void setClientInfo(Client client) {
        if (client != null) {
            phoneField.setText(client.getPhone());
            nameField.setText(client.getName());
            addressField.setText(client.getAddress() != null ? client.getAddress() : "");

            // Rendre les champs visibles et désactivés
            nameField.setVisible(true);
            addressField.setVisible(true);
            nameField.setDisable(true);
            addressField.setDisable(true);
            phoneField.setDisable(true);

            // Désactiver la case "Client temporaire"
            temporaryClientCheckBox.setSelected(false);
            temporaryClientCheckBox.setDisable(true);
        }
    }

    private void setupHoursAndMinutesComboBoxes() {
        ObservableList<Integer> hours = FXCollections.observableArrayList();
        for (int i = 0; i <= 23; i++) hours.add(i);
        hoursComboBox.setItems(hours);
        hoursComboBox.setValue(0);
        ObservableList<Integer> minutes = FXCollections.observableArrayList(0, 15, 30, 45);
        minutesComboBox.setItems(minutes);
        minutesComboBox.setValue(0);
    }

    private void loadGames() {
        List<Game> games = Fabrique.getService().getAllGames();
        gameComboBox.setItems(FXCollections.observableArrayList(games));
        gameComboBox.setConverter(new StringConverter<Game>() {
            @Override public String toString(Game game) { return game != null ? game.getName() : ""; }
            @Override public Game fromString(String string) { return null; }
        });
    }

    private void loadParrains() {
        List<Parrain> parrains = Fabrique.getService().getAllParrains();
        parrainComboBox.setItems(FXCollections.observableArrayList(parrains));
        parrainComboBox.setConverter(new StringConverter<Parrain>() {
            @Override public String toString(Parrain parrain) { return parrain != null ? parrain.getCodeParrainage() : ""; }
            @Override public Parrain fromString(String string) { return null; }
        });
    }

    private void loadPostes() {
        try {
            allPostes = Fabrique.getService().getPostes();
            if (allPostes == null) allPostes = new ArrayList<>();
            posteComboBox.setItems(FXCollections.observableArrayList(allPostes));
        } catch (Exception e) {
            e.printStackTrace();
            allPostes = new ArrayList<>();
            posteComboBox.setItems(FXCollections.observableArrayList());
            Platform.runLater(() -> ControllerUtils.showErrorAlert("Erreur de chargement", "Impossible de charger la liste des postes: " + e.getMessage()));
        }
    }

    private void filterPostesByGame(Game selectedGame) {
        try {
            posteComboBox.setValue(null);
            if (selectedGame == null) {
                posteComboBox.setItems(FXCollections.observableArrayList(allPostes));
            } else {
                List<Poste> filteredPostes = allPostes.stream()
                    .filter(poste -> poste.getGames() != null && poste.getGames().contains(selectedGame))
                    .collect(Collectors.toList());
                posteComboBox.setItems(FXCollections.observableArrayList(filteredPostes));
            }
        } catch (Exception e) {
            e.printStackTrace();
            posteComboBox.setItems(FXCollections.observableArrayList(allPostes));
        }
    }

    private Duration getSelectedDuration() {
        Integer selectedHours = hoursComboBox.getValue() != null ? hoursComboBox.getValue() : 0;
        Integer selectedMinutes = minutesComboBox.getValue() != null ? minutesComboBox.getValue() : 0;
        long totalMinutes = selectedHours * 60 + selectedMinutes;
        return Duration.ofMinutes(totalMinutes);
    }

    private void calculateAndSetAmount() {
        Reservation tempReservation = new Reservation();
        tempReservation.setDuration(getSelectedDuration());
        tempReservation.setReservationDate(LocalDateTime.now());
        Optional<Promotion> activePromo = Fabrique.getService().getBestActivePromotionForToday();
        activePromo.ifPresent(promo -> {
            if (promo.getTypePromotion() == TypePromotion.RESERVATION) {
                tempReservation.setAppliedPromotion(promo);
            }
        });
        calculatedAmount = tempReservation.calculatePriceBasedOnDuration();
        String promoInfo = activePromo.map(p -> " (Promo: " + p.getNom() + ")").orElse("");
        amountLabel.setText(String.format("Montant : %.2f FCFA%s", calculatedAmount, promoInfo));
    }

    @FXML
    private void searchClient(ActionEvent event) {
        String clientPhone = phoneField.getText().trim();
        if (clientPhone.isEmpty() || clientPhone.length() < 9) {
            ControllerUtils.showErrorAlert("Numéro invalide", "Veuillez entrer un numéro de téléphone valide (9 chiffres).");
            return;
        }
        Client client = Fabrique.getService().findByTel(clientPhone);
        if (client != null) {
            // Client trouvé : afficher et remplir les champs (désactivés)
            nameField.setVisible(true);
            nameField.setDisable(true);
            nameField.setText(client.getName());
            addressField.setVisible(true);
            addressField.setDisable(true);
            addressField.setText(client.getAddress() != null ? client.getAddress() : "");
            temporaryClientCheckBox.setVisible(false);
            temporaryClientCheckBox.setSelected(false);
            ControllerUtils.showInfoAlert("Client trouvé", "Les informations du client ont été chargées.");
        } else {
            // Client non trouvé : activer les champs pour création
            nameField.setVisible(true);
            nameField.setDisable(false);
            nameField.clear();
            addressField.setVisible(true);
            addressField.setDisable(false);
            addressField.clear();
            temporaryClientCheckBox.setVisible(true);
            ControllerUtils.showInfoAlert("Client non trouvé", "Ce numéro n'est pas enregistré. Veuillez remplir les informations du client ou cocher la case 'Client temporaire'.");
        }
    }

    private Duration getRemainingTimeForClient(Client client) {
        if (client == null) {
            return Duration.ZERO;
        }

        List<GameSession> sessions = Fabrique.getService().findGameSessionsByClientId(client.getId());
        Duration totalRemainingTime = Duration.ZERO;

        for (GameSession session : sessions) {
            if ("En pause".equals(session.getStatus())) {
                Duration remainingTime = session.getPausedRemainingTime();
                if (remainingTime != null && !remainingTime.isNegative() && !remainingTime.isZero()) {
                    totalRemainingTime = totalRemainingTime.plus(remainingTime);
                }
            }
        }

        return totalRemainingTime;
    }


   @FXML
private void addReservation(ActionEvent event) {
    try {
        String clientPhone = phoneField.getText().trim();
        Poste poste = posteComboBox.getValue();
        Game game = gameComboBox.getValue();
        Duration selectedDuration = getSelectedDuration(); // Durée sélectionnée par l'utilisateur
        String modePaiement = modePaiementComboBox.getSelectionModel().getSelectedItem();

        // Validation des champs obligatoires
        if (!temporaryClientCheckBox.isSelected() && (clientPhone.isEmpty() || clientPhone.length() < 9)) {
            ControllerUtils.showErrorAlert("Numéro invalide", "Veuillez entrer un numéro de téléphone valide (9 chiffres).");
            return;
        }
        if (poste == null || game == null || selectedDuration.toMinutes() < 15 || modePaiement == null) {
            ControllerUtils.showErrorAlert("Champs manquants", "Veuillez remplir tous les champs obligatoires.");
            return;
        }
        if (connectedUser == null) {
            connectedUser = Fabrique.getService().getCurrentUser();
            if (connectedUser == null) {
                ControllerUtils.showErrorAlert("Erreur d'authentification", "Aucun utilisateur connecté détecté.");
                return;
            }
        }

        // Gestion du client (temporaire ou permanent)
        final Client[] clientHolder = new Client[1];
        if (temporaryClientCheckBox.isSelected()) {
            clientHolder[0] = new Client();
            clientHolder[0].setName("Client temporaire");
            clientHolder[0].setPhone(null);
            clientHolder[0].setLoyaltyPoints(0);
            clientHolder[0].setRegistrationDate(new Date());
            Fabrique.getService().addClient(clientHolder[0]);
        } else {
            String clientName = nameField.getText().trim();
            if (clientName.isEmpty() && !temporaryClientCheckBox.isSelected()) {
                ControllerUtils.showErrorAlert("Nom manquant", "Veuillez entrer le nom du client.");
                return;
            }
            clientPhone = phoneField.getText().trim();
            clientHolder[0] = Fabrique.getService().findByTel(clientPhone);
            if (clientHolder[0] == null) {
                clientHolder[0] = new Client();
                clientHolder[0].setName(clientName);
                clientHolder[0].setPhone(clientPhone);
                clientHolder[0].setAddress(addressField.getText().trim().isEmpty() ? null : addressField.getText().trim());
                clientHolder[0].setLoyaltyPoints(0);
                clientHolder[0].setRegistrationDate(new Date());
                Fabrique.getService().addClient(clientHolder[0]);
                ControllerUtils.showInfoAlert("Nouveau client", String.format("Le client '%s' a été ajouté avec succès.", clientHolder[0].getName()));
            } else {
                clientHolder[0].setName(clientName);
                clientHolder[0].setAddress(addressField.getText().trim().isEmpty() ? clientHolder[0].getAddress() : addressField.getText().trim());
                Fabrique.getService().updateClient(clientHolder[0]);
            }
        }

        final Client client = clientHolder[0];
        Duration remainingTime = getRemainingTimeForClient(client);

        // Mettre à jour les anciennes sessions en pause pour ce client
        List<GameSession> pausedSessions = Fabrique.getService().getAllGameSessions().stream()
            .filter(session -> "En pause".equals(session.getStatus()) && session.getClient().getId() == client.getId())
            .collect(Collectors.toList());

        // Marquer les anciennes sessions en pause comme "Terminées"
        for (GameSession session : pausedSessions) {
            session.setPausedRemainingTime(Duration.ZERO);
            session.setStatus("Terminée");
            Fabrique.getService().updateGameSession(session);
            Reservation associatedReservation = session.getReservation();
            if (associatedReservation != null) {
                associatedReservation.setStatus("Terminée");
                Fabrique.getService().updateReservation(associatedReservation);
            }
        }

        // Calculer la durée totale (temps restant + temps sélectionné)
        Duration totalDuration = selectedDuration.plus(remainingTime);

        // Recalculer le montant à payer (uniquement pour le temps supplémentaire)
        double amountToPay = calculatedAmount;
        if (!remainingTime.isZero() && !remainingTime.isNegative()) {
            // Calculer le montant pour la durée supplémentaire (selectedDuration)
            Reservation tempReservation = new Reservation();
            tempReservation.setDuration(selectedDuration);
            amountToPay = tempReservation.calculatePriceBasedOnDuration();
        }

        // Créer la nouvelle réservation avec la durée totale
        Reservation reservation = new Reservation();
        reservation.setNumeroTicket(Reservation.generateRandomTicketNumber());
        reservation.setClient(client);
        reservation.setPoste(poste);
        reservation.setGame(game);
        reservation.setDuration(totalDuration);
        reservation.setReservationDate(LocalDateTime.now());
        reservation.setTotalPrice(amountToPay); // Montant ajusté
        reservation.setStatus("En attente");
        reservation.setCreatedBy(connectedUser);

        // Gestion du code de parrainage
        if (!temporaryClientCheckBox.isSelected() && parrainComboBox.getValue() != null) {
            reservation.setCodeParrainage(parrainComboBox.getValue().getCodeParrainage());
        } else {
            reservation.setCodeParrainage(null);
        }

        // Appliquer une promotion si disponible
        Optional<Promotion> activePromo = Fabrique.getService().getBestActivePromotionForToday();
        activePromo.ifPresent(promo -> {
            if (promo.getTypePromotion() == TypePromotion.RESERVATION) {
                reservation.setAppliedPromotion(promo);
            }
        });

        // Enregistrer la réservation et le paiement
        Fabrique.getService().insertReservation(reservation);
        String detailReservations = String.format(
            "Réservation pour %s (Poste %d) - Durée: %d minutes (dont %d minutes de temps restant)",
            game.getName(), poste.getId(), totalDuration.toMinutes(), remainingTime.toMinutes()
        );
        Payment payment = new Payment(
            reservation.getNumeroTicket(),
            new Date(),
            amountToPay,
            modePaiement,
            client,
            "",
            detailReservations,
            connectedUser
        );
        Fabrique.getService().addPayment(payment);

        // Mise à jour des points de fidélité (uniquement pour le temps payé)
        if (!temporaryClientCheckBox.isSelected() && selectedDuration.toMinutes() > 0) {
            int pointsEarned = (int) (selectedDuration.toMinutes() / 15);
            if (pointsEarned > 0) {
                Client freshClient = Fabrique.getService().findByTel(clientPhone);
                freshClient.setLoyaltyPoints(freshClient.getLoyaltyPoints() + pointsEarned);
                Fabrique.getService().updateClient(freshClient);
                ControllerUtils.showInfoAlert("Points de fidélité ajoutés",
                    String.format("Le client '%s' a gagné %d points de fidélité. Total actuel : %d points.",
                        freshClient.getName(), pointsEarned, freshClient.getLoyaltyPoints()));
            }
        }

        // Affichage du reçu et fermeture de la fenêtre
        ControllerUtils.showInfoAlert("Réservation ajoutée", "Réservation et paiement enregistrés avec succès!");
        ReservationReceiptPrinter printer = new ReservationReceiptPrinter(reservation, connectedUserName, modePaiement);
        printer.printReceipt();

        if (parentController != null) {
            parentController.loadReservations();
            parentController.loadActiveSessions();
        }

        Stage stage = (Stage) ((Button) event.getSource()).getScene().getWindow();
        stage.close();

    } catch (Exception e) {
        e.printStackTrace();
        ControllerUtils.showErrorAlert("Erreur", "Erreur lors de l'ajout de la réservation: " + e.getMessage());
    }
}



    @FXML
    private void cancel(ActionEvent event) {
        Stage stage = (Stage) ((Button) event.getSource()).getScene().getWindow();
        stage.close();
    }

    public void setParentController(ReservationController parentController) {
        this.parentController = parentController;
    }

    public void setConnectedUserName(String userName) {
        this.connectedUserName = userName;
    }

    public void setConnectedUser(User user) {
        this.connectedUser = user;
    }
}
