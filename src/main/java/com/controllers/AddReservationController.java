package com.controllers;

import com.core.Fabrique;
import com.entities.Client;
import com.entities.Game;
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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
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
    private ComboBox<String> modePaiementComboBox;


    @FXML
    private ComboBox<Integer> hoursComboBox;
    @FXML
    private ComboBox<Integer> minutesComboBox;

    @FXML
    private ComboBox<Parrain> parrainComboBox;
    @FXML
    private Label amountLabel;
    private double calculatedAmount;


    private ReservationController parentController;
    private String connectedUserName;
    private User connectedUser;

    // Cette liste contiendra TOUS les postes, avec leurs jeux associés correctement chargés
    private List<Poste> allPostes;

    @FXML
public void initialize() {
    try {
        System.out.println("DEBUG: Début de l'initialisation de AddReservationController");
        
        // VÉRIFIER QUE TOUS LES CHAMPS FXML SONT INJECTÉS
        checkFXMLFields();
        
        // 1. Configurer d'abord les éléments simples
        setupHoursAndMinutesComboBoxes();
        System.out.println("DEBUG: ComboBox heures/minutes configurées");
        
        loadGames();
        System.out.println("DEBUG: Jeux chargés");
        
        loadParrains();
        System.out.println("DEBUG: Parrains chargés");

        // 2. Configurer le mode de paiement (MAINTENANT AVEC VÉRIFICATION NULL)
        if (modePaiementComboBox != null) {
            modePaiementComboBox.setItems(FXCollections.observableArrayList(
                "En Espèce", "Wave", "Orange Money", "free money"
            ));
            System.out.println("DEBUG: Mode de paiement configuré");
        } else {
            System.err.println("ERREUR: modePaiementComboBox est null");
        }

        // 3. Configurer le StringConverter pour posteComboBox
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
            System.out.println("DEBUG: StringConverter configuré");
        } else {
            System.err.println("ERREUR: posteComboBox est null");
        }

        // 4. Charger les postes EN DERNIER avec gestion d'erreur
        loadPostes();

        // 5. Listeners (uniquement si les ComboBox existent)
        if (gameComboBox != null) {
            gameComboBox.valueProperty().addListener((obs, oldVal, newGame) -> {
                System.out.println("DEBUG: Jeu sélectionné changé: " + (newGame != null ? newGame.getName() : "null"));
                filterPostesByGame(newGame);
                calculateAndSetAmount();
            });
        }

        if (posteComboBox != null) {
            posteComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
                System.out.println("DEBUG: Poste sélectionné changé: " + (newVal != null ? newVal.getId() : "null"));
                calculateAndSetAmount();
            });
        }

        if (hoursComboBox != null) {
            hoursComboBox.valueProperty().addListener((obs, oldVal, newVal) -> calculateAndSetAmount());
        }

        if (minutesComboBox != null) {
            minutesComboBox.valueProperty().addListener((obs, oldVal, newVal) -> calculateAndSetAmount());
        }

        calculateAndSetAmount();
        
        System.out.println("DEBUG: AddReservationController initialisé avec succès");

    } catch (Exception e) {
        System.err.println("ERREUR CRITIQUE lors de l'initialisation de AddReservationController:");
        e.printStackTrace();
        throw new RuntimeException("Erreur d'initialisation du contrôleur", e);
    }
}

/** Vérifie que tous les champs FXML sont correctement injectés */
private void checkFXMLFields() {
    System.out.println("DEBUG: Vérification des champs FXML...");
    
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
    
    System.out.println("DEBUG: Vérification des champs FXML terminée");
}

    private void loadPostes() {
        try {
            System.out.println("DEBUG: Tentative de chargement des postes...");
            allPostes = Fabrique.getService().getPostes();
            System.out.println("DEBUG: " + (allPostes != null ? allPostes.size() : "null") + " postes chargés");
            
            if (allPostes == null) {
                System.err.println("ERREUR: La liste des postes est null");
                allPostes = new ArrayList<>();
            }
            
            // Vérifier le chargement des jeux pour chaque poste
            for (Poste poste : allPostes) {
                System.out.println("DEBUG: Poste " + poste.getId() + 
                    " - Nom: " + poste.getName() + 
                    " - HorsService: " + poste.isHorsService() +
                    " - Jeux: " + (poste.getGames() != null ? poste.getGames().size() : "null"));
                
                if (poste.getGames() != null) {
                    for (Game game : poste.getGames()) {
                        System.out.println("DEBUG:   - Jeu: " + game.getName() + " (ID: " + game.getId() + ")");
                    }
                }
            }
            
            // Mettre à jour la ComboBox
            posteComboBox.setItems(FXCollections.observableArrayList(allPostes));
            System.out.println("DEBUG: ComboBox des postes mise à jour avec " + allPostes.size() + " éléments");
            
        } catch (Exception e) {
            System.err.println("ERREUR lors du chargement des postes: " + e.getMessage());
            e.printStackTrace();
            allPostes = new ArrayList<>();
            posteComboBox.setItems(FXCollections.observableArrayList());
            
            // Afficher une alerte à l'utilisateur
            Platform.runLater(() -> {
                ControllerUtils.showErrorAlert("Erreur de chargement", 
                    "Impossible de charger la liste des postes: " + e.getMessage());
            });
        }
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
    try {
        System.out.println("DEBUG: Filtrage des postes pour le jeu: " + 
            (selectedGame != null ? selectedGame.getName() + " (ID: " + selectedGame.getId() + ")" : "null"));
        
        posteComboBox.setValue(null);
        
        if (selectedGame == null) {
            // Afficher tous les postes
            posteComboBox.setItems(FXCollections.observableArrayList(allPostes));
            System.out.println("DEBUG: Affichage de tous les postes: " + allPostes.size());
        } else {
            // Filtrer les postes qui contiennent le jeu sélectionné
            List<Poste> filteredPostes = allPostes.stream()
                .filter(poste -> {
                    if (poste.getGames() == null) {
                        System.out.println("DEBUG: Poste " + poste.getId() + " - games list is null");
                        return false;
                    }
                    boolean containsGame = poste.getGames().contains(selectedGame);
                    System.out.println("DEBUG: Poste " + poste.getId() + " contient le jeu " + 
                        selectedGame.getName() + ": " + containsGame);
                    return containsGame;
                })
                .collect(Collectors.toList());
                
            posteComboBox.setItems(FXCollections.observableArrayList(filteredPostes));
            System.out.println("DEBUG: Postes filtrés: " + filteredPostes.size() + " postes correspondent au jeu");
        }
    } catch (Exception e) {
        System.err.println("ERREUR dans filterPostesByGame: " + e.getMessage());
        e.printStackTrace();
        // En cas d'erreur, afficher tous les postes
        posteComboBox.setItems(FXCollections.observableArrayList(allPostes));
    }
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
        tempReservation.setReservationDate(LocalDateTime.now()); // <-- Ajoutez cette ligne

        // Appliquer la promotion si elle est valide et de type "réservation"
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
private void addReservation(ActionEvent event) throws Exception {
    String clientName = nameField.getText().trim();
    String clientPhone = phoneField.getText().trim();
    String clientAddress = addressField.getText().trim();
    Poste poste = posteComboBox.getValue();
    Game game = gameComboBox.getValue();
    Duration duration = getSelectedDuration();
    Parrain parrain = parrainComboBox.getValue();

    if (clientName.isEmpty() || clientPhone.isEmpty() || poste == null || game == null || duration.toMinutes() < 15) {
        ControllerUtils.showErrorAlert("Champs manquants ou durée invalide",
            "Veuillez remplir le nom du client, le numéro de téléphone, choisir un poste, un jeu et une durée valide (minimum 15 minutes).");
        return;
    }

    String modePaiement = modePaiementComboBox.getSelectionModel().getSelectedItem();
    if (modePaiement == null) {
        ControllerUtils.showErrorAlert("Mode de paiement manquant",
            "Veuillez sélectionner un mode de paiement.");
        return;
    }

    if (connectedUser == null) {
        connectedUser = Fabrique.getService().getCurrentUser();
        if (connectedUser == null) {
            ControllerUtils.showErrorAlert("Erreur d'authentification",
                "Aucun utilisateur connecté détecté. Veuillez vous reconnecter.");
            return;
        }
    }

    // Calculer le montant total en utilisant une réservation temporaire
    Reservation tempReservation = new Reservation();
    tempReservation.setDuration(duration);
    tempReservation.setReservationDate(LocalDateTime.now());

    Optional<Promotion> activePromo = Fabrique.getService().getBestActivePromotionForToday();
    activePromo.ifPresent(promo -> {
        if (promo.getTypePromotion() == TypePromotion.RESERVATION) {
            tempReservation.setAppliedPromotion(promo);
        }
    });

    double totalPrice = tempReservation.calculatePriceBasedOnDuration();

    // --- Gestion du client ---
    Client client = Fabrique.getService().findByTel(clientPhone);
    if (client == null) {
        // Nouveau client : création et ajout
        client = new Client();
        client.setName(clientName);
        client.setPhone(clientPhone);
        client.setAddress(clientAddress.isEmpty() ? null : clientAddress);
        client.setLoyaltyPoints(0);
        client.setRegistrationDate(new java.util.Date());
        Fabrique.getService().addClient(client);
        ControllerUtils.showInfoAlert("Nouveau client",
            String.format("Le client '%s' a été ajouté avec succès avec le numéro %s.", client.getName(), client.getPhone()));
    } else {
        // Client existant : création d'une COPIE pour éviter les références partagées
        Client updatedClient = new Client();
        updatedClient.setId(client.getId());
        updatedClient.setName(clientName);
        updatedClient.setPhone(client.getPhone());
        updatedClient.setAddress(clientAddress.isEmpty() ? client.getAddress() : clientAddress);
        updatedClient.setLoyaltyPoints(client.getLoyaltyPoints());
        updatedClient.setRegistrationDate(client.getRegistrationDate());
        // Mise à jour de la COPIE
        Fabrique.getService().updateClient(updatedClient);
        // Rechargement du client depuis la base pour s'assurer de la cohérence
        client = Fabrique.getService().findByTel(clientPhone);
        ControllerUtils.showInfoAlert("Client mis à jour",
            String.format("Les informations du client '%s' ont été mises à jour.", client.getName()));
    }

    // --- Gestion du parrain ---
    if (parrain != null) {
        parrain.addParrainagePoints(1);
        Fabrique.getService().updateParrain(parrain);
        ControllerUtils.showInfoAlert("Points de parrainage",
            String.format("1 point a été ajouté au parrain %s.", parrain.getCodeParrainage()));
    }

    // --- Création de la réservation ---
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
    reservation.setCreatedBy(connectedUser);

    // Appliquer la promotion si elle est valide et de type "réservation"
    activePromo.ifPresent(promo -> {
        if (promo.getTypePromotion() == TypePromotion.RESERVATION) {
            reservation.setAppliedPromotion(promo);
        }
    });

    try {
        Fabrique.getService().insertReservation(reservation);

        // --- Création du paiement ---
        String detailReservations = String.format(
            "Réservation pour %s (Poste %d) - Durée: %d minutes",
            game.getName(),
            poste.getId(),
            duration.toMinutes()
        );

        Payment payment = new Payment(
            reservation.getNumeroTicket(),
            new Date(),
            reservation.getTotalPrice(),
            modePaiement,
            client,
            "",
            detailReservations,
            connectedUser
        );

        Fabrique.getService().addPayment(payment);

        // --- Gestion des points de fidélité ---
        int pointsEarned = calculateLoyaltyPoints(duration);
        if (client != null && pointsEarned > 0) {
            // Rechargement du client pour éviter les références partagées
            Client freshClient = Fabrique.getService().findByTel(clientPhone);
            freshClient.setLoyaltyPoints(freshClient.getLoyaltyPoints() + pointsEarned);
            Fabrique.getService().updateClient(freshClient);
            ControllerUtils.showInfoAlert("Points de fidélité ajoutés",
                String.format("Le client '%s' a gagné %d points de fidélité. Total actuel : %d points.",
                    freshClient.getName(), pointsEarned, freshClient.getLoyaltyPoints()));
        }

        ControllerUtils.showInfoAlert("Réservation ajoutée",
            "Réservation et paiement enregistrés avec succès!");

        // --- Impression du ticket ---
        ReservationReceiptPrinter printer = new ReservationReceiptPrinter(reservation, connectedUserName, modePaiement);
        printer.printReceipt();

        // --- Rafraîchissement des données ---
        if (parentController != null) {
            parentController.loadReservations();
            parentController.loadActiveSessions();
        }

        // --- Fermeture de la fenêtre ---
        Stage stage = (Stage) ((Button) event.getSource()).getScene().getWindow();
        stage.close();
    } catch (Exception e) {
        e.printStackTrace();
        ControllerUtils.showErrorAlert("Erreur",
            "Erreur lors de l'ajout de la réservation et du paiement : " + e.getMessage());
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