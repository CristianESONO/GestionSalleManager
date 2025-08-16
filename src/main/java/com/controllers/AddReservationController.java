package com.controllers;

import com.core.Fabrique;
import com.entities.Client;
import com.entities.Game;
import com.entities.Parrain;
import com.entities.Poste;
import com.entities.Promotion;
import com.entities.Reservation;
import com.entities.User;
import com.utils.ReservationReceiptPrinter;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class AddReservationController {

    @FXML
    private TextField nameField;
    @FXML
    private TextField phoneField;
    @FXML
    private TextField addressField;

    @FXML
    private ComboBox<Poste> posteComboBox;
    @FXML
    private ComboBox<Game> gameComboBox;

    @FXML
    private ComboBox<Integer> hoursComboBox;
    @FXML
    private ComboBox<Integer> minutesComboBox;

    @FXML
    private ComboBox<Parrain> parrainComboBox;
    @FXML
    private Label amountLabel;

    private ReservationController parentController;
    private String connectedUserName;
    private User connectedUser;

    // Cette liste contiendra TOUS les postes, avec leurs jeux associés correctement chargés
    private List<Poste> allPostes;

    @FXML
    public void initialize() 
    {
        // 1. Chargement initial de TOUS les postes
        // C'est crucial que getPostes() retourne des Postes avec leurs listes de Games remplies
        // grâce aux modifications dans PosteRepository.
        allPostes = Fabrique.getService().getPostes();
        
        // 2. Charger les jeux et les parrains en premier
        loadGames();
        loadParrains();
        setupHoursAndMinutesComboBoxes();

        // 3. Configurer le StringConverter pour posteComboBox AVANT de définir les items
        // Cela garantit que l'affichage est correct dès le premier chargement ou filtrage.
        posteComboBox.setConverter(new StringConverter<Poste>() {
            @Override
            public String toString(Poste poste) {
                // Assurez-vous que votre classe Poste a une méthode getName()
                // ou une propriété "name" si la colonne s'appelle "name" dans la DB.
                // Si votre poste n'a pas de nom mais juste un ID, affichez l'ID.
                return poste != null ? "Poste " + poste.getId() : ""; // Exemple: "Poste 1", "Poste 2"
            }

            @Override
            public Poste fromString(String string) {
                return null; // Pas nécessaire pour cette utilisation
            }
        });

        // 4. Mettre à jour le ComboBox des postes pour afficher TOUS les postes au démarrage
        // (avant qu'un jeu ne soit sélectionné). C'est l'état initial.
        posteComboBox.setItems(FXCollections.observableArrayList(allPostes));

        // 5. Listener pour gameComboBox: quand un nouveau jeu est sélectionné
        gameComboBox.valueProperty().addListener((obs, oldVal, newGame) -> {
            filterPostesByGame(newGame); // Filtrer les postes en fonction du jeu sélectionné
            calculateAndSetAmount(); // Recalculer le montant après changement du jeu/poste potentiel
        });

        // 6. Les autres listeners restent les mêmes
        posteComboBox.valueProperty().addListener((obs, oldVal, newVal) -> calculateAndSetAmount());
        hoursComboBox.valueProperty().addListener((obs, oldVal, newVal) -> calculateAndSetAmount());
        minutesComboBox.valueProperty().addListener((obs, oldVal, newVal) -> calculateAndSetAmount());

        // Calcul initial du montant
        calculateAndSetAmount();
    }

    public void setConnectedUser(User user) {
        this.connectedUser = user;
    }

    public void setParentController(ReservationController parentController) {
        this.parentController = parentController;
    }

    public void setConnectedUserName(String userName) {
        this.connectedUserName = userName;
    }

    // Cette méthode n'est plus utile, car `allPostes` est chargé une fois
    // et les postes sont filtrés par `filterPostesByGame`.
    // private void loadPostes() { }

    /**
     * Filtre la ComboBox des postes en fonction du jeu sélectionné.
     * @param selectedGame Le jeu sélectionné dans gameComboBox, ou null si aucun.
     */
    private void filterPostesByGame(Game selectedGame) {
        // Efface la sélection actuelle du poste pour éviter de conserver un poste
        // qui ne correspond plus au nouveau jeu sélectionné.
        posteComboBox.setValue(null); 
        
        if (selectedGame == null) {
            // Si aucun jeu n'est sélectionné, affiche tous les postes
            posteComboBox.setItems(FXCollections.observableArrayList(allPostes));
        } else {
            // Filtre les postes qui contiennent le jeu sélectionné.
            // C'est ici que la méthode equals() et hashCode() de Game est CRUCIALE.
            List<Poste> filteredPostes = allPostes.stream()
                                                    .filter(poste -> poste.getGames() != null && poste.getGames().contains(selectedGame))
                                                    .collect(Collectors.toList());
            posteComboBox.setItems(FXCollections.observableArrayList(filteredPostes));
        }
        
        // **IMPORTANT** : Le StringConverter pour posteComboBox DOIT être configuré
        // une seule fois, idéalement dans `initialize()`, et non ici à chaque filtrage.
        // J'ai déplacé ce bloc dans initialize().
    }

    private void loadGames() {
        List<Game> games = Fabrique.getService().getAllGames();
        gameComboBox.setItems(FXCollections.observableArrayList(games));
        gameComboBox.setConverter(new StringConverter<Game>() {
            @Override
            public String toString(Game game) {
                return game != null ? game.getName() : "";
            }

            @Override
            public Game fromString(String string) {
                return null;
            }
        });
    }

    private void loadParrains() {
        List<Parrain> parrains = Fabrique.getService().getAllParrains();
        parrainComboBox.setItems(FXCollections.observableArrayList(parrains));
        parrainComboBox.setConverter(new StringConverter<Parrain>() {
            @Override
            public String toString(Parrain parrain) {
                return parrain != null ? parrain.getCodeParrainage() : "";
            }

            @Override
            public Parrain fromString(String string) {
                return null;
            }
        });
    }

    /**
     * Configure les ComboBox pour la sélection des heures et des minutes.
     */
    private void setupHoursAndMinutesComboBoxes() {
        ObservableList<Integer> hours = FXCollections.observableArrayList();
        for (int i = 0; i <= 23; i++) {
            hours.add(i);
        }
        hoursComboBox.setItems(hours);
        hoursComboBox.setValue(0);

        ObservableList<Integer> minutes = FXCollections.observableArrayList(0, 15, 30, 45);
        minutesComboBox.setItems(minutes);
        minutesComboBox.setValue(0);
    }

    /**
     * Calcule la durée totale en minutes à partir des ComboBox Heures et Minutes.
     * @return La durée en minutes sous forme d'objet Duration.
     */
    private Duration getSelectedDuration() {
        Integer selectedHours = hoursComboBox.getValue();
        Integer selectedMinutes = minutesComboBox.getValue();

        if (selectedHours == null) {
            selectedHours = 0;
        }
        if (selectedMinutes == null) {
            selectedMinutes = 0;
        }

        long totalMinutes = selectedHours * 60 + selectedMinutes;
        return Duration.ofMinutes(totalMinutes);
    }

    private void calculateAndSetAmount() {
        Reservation tempReservation = new Reservation();
        Duration currentDuration = getSelectedDuration();

        tempReservation.setDuration(currentDuration); 
        // **Applique automatiquement la promotion active si elle existe**
        Optional<Promotion> activePromo = Fabrique.getService().getActivePromotionForToday();
        activePromo.ifPresent(tempReservation::setAppliedPromotion);

        double amount = tempReservation.calculatePriceBasedOnDuration();
        amountLabel.setText(String.format("Montant : %.2f FCFA", amount));
    }

    @FXML
    private void addReservation(ActionEvent event) throws Exception {
        String clientName = nameField.getText().trim();
        String clientPhone = phoneField.getText().trim();
        String clientAddress = addressField.getText().trim();

        Poste poste = posteComboBox.getValue();
        Game game = gameComboBox.getValue();
        Duration duration = getSelectedDuration();
        Parrain parrain = parrainComboBox.getValue();

        if (clientName.isEmpty() || clientPhone.isEmpty() || poste == null || game == null || duration.toMinutes() < 15) {
            ControllerUtils.showErrorAlert("Champs manquants ou durée invalide", "Veuillez remplir le nom du client, le numéro de téléphone, choisir un poste, un jeu et une durée valide (minimum 15 minutes).");
            return;
        }

        String amountText = amountLabel.getText().replace("Montant : ", "").replace(" FCFA", "").replace(",", ".");
        double totalPrice = 0.0;
        try {
            totalPrice = Double.parseDouble(amountText);
        } catch (NumberFormatException e) {
            ControllerUtils.showErrorAlert("Erreur de calcul du prix", "Le montant n'a pas pu être déterminé. Veuillez vérifier la durée saisie.");
            return;
        }

         // VÉRIFICATION CRUCIALE DE L'UTILISATEUR
        if (connectedUser == null) {
            connectedUser = Fabrique.getService().getCurrentUser();
            if (connectedUser == null) {
                ControllerUtils.showErrorAlert("Erreur d'authentification", 
                    "Aucun utilisateur connecté détecté. Veuillez vous reconnecter.");
                return;
            }
        }

        Client client = Fabrique.getService().findByTel(clientPhone);
        if (client == null) {
            client = new Client();
            client.setName(clientName);
            client.setPhone(clientPhone);
            client.setAddress(clientAddress.isEmpty() ? null : clientAddress);
            client.setLoyaltyPoints(0);
            client.setRegistrationDate(new java.util.Date());
            Fabrique.getService().addClient(client);
            ControllerUtils.showInfoAlert("Nouveau client", String.format("Le client '%s' a été ajouté avec succès avec le numéro %s.", client.getName(), client.getPhone()));
        } else {
            boolean updated = false;
            if (!clientName.isEmpty() && !client.getName().equals(clientName)) {
                client.setName(clientName);
                updated = true;
            }
            
            if (!clientAddress.isEmpty() && (client.getAddress() == null || !client.getAddress().equals(clientAddress))) {
                client.setAddress(clientAddress);
                updated = true;
            }
            if (updated) {
                Fabrique.getService().updateClient(client);
                ControllerUtils.showInfoAlert("Client mis à jour", String.format("Les informations du client '%s' ont été mises à jour.", client.getName()));
            }
        }

        // C'est ici que l'on met à jour les points du parrain
        if (parrain != null) {
            // Utilisation de la méthode addParrainagePoints() de votre classe Parrain
            parrain.addParrainagePoints(1);
            // Appel à la méthode de service pour mettre à jour l'objet Parrain dans la base de données
            Fabrique.getService().updateParrain(parrain);
            ControllerUtils.showInfoAlert("Points de parrainage", String.format("1 point a été ajouté au parrain %s.", parrain.getCodeParrainage()));
        }

        Reservation reservation = new Reservation();
        reservation.setNumeroTicket(Reservation.generateRandomTicketNumber());
        reservation.setClient(client);
        reservation.setPoste(poste);
        reservation.setGame(game);
        reservation.setDuration(duration);
        reservation.setReservationDate(LocalDateTime.now());
        reservation.setTotalPrice(totalPrice);
        reservation.setCodeParrainage(parrain != null ? parrain.getCodeParrainage() : null);
        reservation.setStatus("En attente");
        reservation.setCreatedBy(connectedUser); // Enregistre l'utilisateur connecté qui a créé la réservation

         // **1. Récupère la promotion active pour aujourd'hui**
        Optional<Promotion> activePromo = Fabrique.getService().getActivePromotionForToday();

        // **2. Si une promotion est active, l'appliquer à la réservation**
        activePromo.ifPresent(reservation::setAppliedPromotion);

        // **3. Calcule le prix (qui tiendra compte de la promotion si elle existe)**
        reservation.setTotalPrice(reservation.calculatePriceBasedOnDuration());

        try {
            Fabrique.getService().insertReservation(reservation);

            int pointsEarned = calculateLoyaltyPoints(duration);
            if (client != null && pointsEarned > 0) {
                client.setLoyaltyPoints(client.getLoyaltyPoints() + pointsEarned);
                Fabrique.getService().updateClient(client);
                ControllerUtils.showInfoAlert("Points de fidélité ajoutés",
                    String.format("Le client '%s' a gagné %d points de fidélité. Total actuel : %d points.",
                        client.getName(), pointsEarned, client.getLoyaltyPoints()));
            }

            ControllerUtils.showInfoAlert("Réservation ajoutée", "Réservation et session de jeu active ajoutées avec succès!");

            ReservationReceiptPrinter printer = new ReservationReceiptPrinter(reservation, connectedUserName);
            printer.printReceipt();

            if (parentController != null) {
                parentController.loadReservations();
            }

            Stage stage = (Stage) ((Button) event.getSource()).getScene().getWindow();
            stage.close();

        } catch (Exception e) {
            e.printStackTrace();
            ControllerUtils.showErrorAlert("Erreur", "Erreur lors de l'ajout de la réservation et de la session de jeu : " + e.getMessage());
        }
    }

    private int calculateLoyaltyPoints(Duration duration) {
        long totalMinutes = duration.toMinutes();
        if (totalMinutes <= 0) {
            return 0;
        }
        return (int) (totalMinutes / 15);
    }

    @FXML
    private void cancel(ActionEvent event) {
        Stage stage = (Stage) ((Button) event.getSource()).getScene().getWindow();
        stage.close();
    }
}