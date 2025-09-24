package com.controllers;

import com.entities.Client;
import com.entities.Game;
import com.entities.Poste;
import com.entities.Reservation;
import com.entities.Role;
import com.entities.User;
import com.utils.ReservationReceiptPrinter;
import com.entities.GameSession;
import com.core.Fabrique;
import com.core.WindowManager;
import com.entities.Parrain;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.util.Pair;
import javafx.geometry.Insets;
import javafx.geometry.Pos;

import java.io.IOException;
import java.net.URL;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class ReservationController implements Initializable {

    @FXML private TabPane mainTabPane;
    @FXML private Tab reservationsTab;
    @FXML private Tab activeSessionsTab;
    @FXML private TableView<Reservation> reservationTable;
    @FXML private TableColumn<Reservation, String> numeroTicketColumn;
    @FXML private TableColumn<Reservation, String> clientColumn;
    @FXML private TableColumn<Reservation, String> posteColumn;
    @FXML private TableColumn<Reservation, String> gameColumn;
    @FXML private TableColumn<Reservation, String> durationColumn;
    @FXML private TableColumn<Reservation, String> reservationDateColumn;
    @FXML private TableColumn<Reservation, String> totalPriceColumn;
    @FXML private TableColumn<Reservation, String> parrainCodeColumn;
    @FXML private TableColumn<Reservation, String> statusColumn;
    @FXML private TableColumn<Reservation, Void> actionColumn;
    @FXML private TextField searchReservationField;
    private ObservableList<Reservation> reservations;
    @FXML private TableView<GameSession> activeSessionsTable;
    @FXML private TableColumn<GameSession, String> sessionClientColumn;
    @FXML private TableColumn<GameSession, String> sessionPosteColumn;
    @FXML private TableColumn<GameSession, String> sessionGameColumn;
    @FXML private TableColumn<GameSession, String> sessionStartTimeColumn;
    @FXML private TableColumn<GameSession, String> sessionEndTimeColumn;
    @FXML private TableColumn<GameSession, String> sessionRemainingTimeColumn;
    @FXML private TableColumn<GameSession, String> sessionStatusColumn;
    @FXML private TableColumn<GameSession, Void> sessionActionsColumn;
    private ObservableList<GameSession> activeSessions;
    private String connectedUserName;
    private User connectedUser;
    private Set<Integer> notifiedSessions = new HashSet<>();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        SessionEndDialogController.setReservationController(this);
        setupReservationTable();
        loadReservations();
        setupActiveSessionsTable();
        loadActiveSessions();
        searchReservationField.textProperty().addListener((observable, oldValue, newValue) -> filterReservations(newValue));
        Timeline sessionChecker = new Timeline(new KeyFrame(javafx.util.Duration.minutes(1), e -> checkExpiredSessions()));
        sessionChecker.setCycleCount(Animation.INDEFINITE);
        sessionChecker.play();
    }

    public void setConnectedUserName(String userName) {
        this.connectedUserName = userName;
    }

    public void setConnectedUser() {
        this.connectedUser = Fabrique.getService().getCurrentUser();
    }

    private void setupReservationTable() {
        numeroTicketColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getNumeroTicket()));
        clientColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getClient() != null ? cellData.getValue().getClient().getName() : "N/A"));
        posteColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getPoste().getName()));
        gameColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getGame().getName()));
        reservationDateColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getReservationDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))));
        durationColumn.setCellValueFactory(cellData -> {
            Duration duration = cellData.getValue().getDuration();
            long totalMinutes = duration.toMinutes();
            long hours = totalMinutes / 60;
            long minutes = totalMinutes % 60;
            return new SimpleStringProperty(hours > 0 ? String.format("%dh %02dmin", hours, minutes) : String.format("%dmin", minutes));
        });
        totalPriceColumn.setCellValueFactory(cellData -> new SimpleStringProperty(String.format("%.2f FCFA", cellData.getValue().getTotalPrice())));
        parrainCodeColumn.setCellValueFactory(cellData -> {
            String codeParrainage = cellData.getValue().getCodeParrainage();
            if (codeParrainage != null && !codeParrainage.isEmpty()) {
                Parrain parrain = Fabrique.getService().getParrainByCodeParrainage(codeParrainage);
                return new SimpleStringProperty(parrain != null ? parrain.getName() : "N/A");
            }
            return new SimpleStringProperty("N/A");
        });
        statusColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getStatus()));
       actionColumn.setCellFactory(param -> new TableCell<>() {
            private final Button startButton = new Button("Démarrer");
            private final HBox pane = new HBox(5);
            {
                startButton.setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white; -fx-font-size: 11px; -fx-padding: 5px 8px; -fx-border-radius: 3px; -fx-background-radius: 3px; -fx-cursor: hand;");
                startButton.setEffect(new DropShadow(5, Color.web("#00000033")));
                startButton.setOnAction(event -> {
                    Reservation reservation = getTableView().getItems().get(getIndex());
                    startReservationSession(reservation);
                });
                pane.setAlignment(Pos.CENTER);
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Reservation reservation = getTableView().getItems().get(getIndex());
                    pane.getChildren().clear();
                    // Afficher le bouton "Démarrer" SI ET SEULEMENT si la réservation est "En attente"
                    if ("En attente".equals(reservation.getStatus())) {
                        Poste poste = reservation.getPoste();
                        GameSession activeSessionOnPoste = Fabrique.getService().getActiveSessionForPoste(poste);
                        // Le bouton est actif si le poste n'est pas hors service et n'a pas de session ACTIVE
                        if (!poste.isHorsService() && activeSessionOnPoste == null) {
                            startButton.setDisable(false);
                            pane.getChildren().addAll(startButton);
                        } else {
                            startButton.setDisable(true);
                        }
                        setGraphic(pane);
                    } else {
                        setGraphic(null);
                    }
                }
            }
        });

    }


    private void setupActiveSessionsTable() {
        sessionClientColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getClient() != null ? cellData.getValue().getClient().getName() : "N/A"));
        sessionPosteColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getPoste().getName()));
        sessionGameColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getGame().getName()));
        sessionStartTimeColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getStartTime().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))));
        sessionEndTimeColumn.setCellValueFactory(cellData -> {
            LocalDateTime endTime = cellData.getValue().getEndTime();
            if (endTime == null) {
                LocalDateTime startTime = cellData.getValue().getStartTime();
                Duration paidDuration = cellData.getValue().getPaidDuration();
                endTime = startTime.plus(paidDuration);
            }
            return new SimpleStringProperty(endTime.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
        });
        sessionRemainingTimeColumn.setCellValueFactory(cellData -> {
            Duration remaining = cellData.getValue().getRemainingTime();
            if (remaining == null || remaining.isNegative() || remaining.isZero()) {
                return new SimpleStringProperty("Terminée");
            } else {
                long totalSeconds = remaining.getSeconds();
                long hours = totalSeconds / 3600;
                long minutes = (totalSeconds % 3600) / 60;
                return new SimpleStringProperty(String.format("%d h %d min", hours, minutes));
            }
        });
        sessionStatusColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getStatus()));
        sessionActionsColumn.setCellFactory(new Callback<>() {
            @Override
            public TableCell<GameSession, Void> call(final TableColumn<GameSession, Void> param) {
                return new TableCell<>() {
                    private final Button stopButton = new Button("Arrêter");
                    private final Button extendButton = new Button("Prolonger");
                    private final Button pauseButton = new Button("Pause");
                    private final Button resumeButton = new Button("Reprendre");
                    private final HBox pane = new HBox(5);
                    {
                        pane.getChildren().addAll(stopButton, extendButton, pauseButton, resumeButton);
                        pane.setAlignment(Pos.CENTER);
                        stopButton.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-size: 11px; -fx-padding: 5px 8px; -fx-border-radius: 3px; -fx-background-radius: 3px; -fx-cursor: hand;");
                        extendButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-size: 11px; -fx-padding: 5px 8px; -fx-border-radius: 3px; -fx-background-radius: 3px; -fx-cursor: hand;");
                        pauseButton.setStyle("-fx-background-color: #f39c12; -fx-text-fill: white; -fx-font-size: 11px; -fx-padding: 5px 8px; -fx-border-radius: 3px; -fx-background-radius: 3px; -fx-cursor: hand;");
                        resumeButton.setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white; -fx-font-size: 11px; -fx-padding: 5px 8px; -fx-border-radius: 3px; -fx-background-radius: 3px; -fx-cursor: hand;");
                        stopButton.setEffect(new DropShadow(5, Color.web("#00000033")));
                        extendButton.setEffect(new DropShadow(5, Color.web("#00000033")));
                        pauseButton.setEffect(new DropShadow(5, Color.web("#00000033")));
                        resumeButton.setEffect(new DropShadow(5, Color.web("#00000033")));
                        stopButton.setOnAction(event -> {
                            GameSession session = getTableView().getItems().get(getIndex());
                            if (session != null && !"Terminée".equalsIgnoreCase(session.getStatus())) {
                                terminateGameSession(session);
                            }
                        });
                        extendButton.setOnAction(event -> {
                            GameSession session = getTableView().getItems().get(getIndex());
                            if (session != null) {
                                Duration remainingTime = session.getRemainingTime();
                                if (!"Terminée".equalsIgnoreCase(session.getStatus()) && !remainingTime.isNegative() && !remainingTime.isZero()) {
                                    promptForExtension(session);
                                }
                            }
                        });
                        pauseButton.setOnAction(event -> {
                            GameSession session = getTableView().getItems().get(getIndex());
                            if (session != null && "Active".equalsIgnoreCase(session.getStatus())) {
                                try {
                                    Fabrique.getService().pauseGameSession(session);
                                    loadActiveSessions();
                                    loadReservations();
                                    ControllerUtils.showInfoAlert("Session en pause", "La session et la réservation ont été mises en pause.");
                                } catch (Exception e) {
                                    ControllerUtils.showErrorAlert("Erreur", "Erreur lors de la mise en pause: " + e.getMessage());
                                }
                            }
                        });
                        resumeButton.setOnAction(event -> {
                            GameSession session = getTableView().getItems().get(getIndex());
                            if (session != null && "En pause".equalsIgnoreCase(session.getStatus())) {
                                try {
                                    Fabrique.getService().resumeGameSession(session);
                                    loadActiveSessions();
                                    loadReservations();
                                    ControllerUtils.showInfoAlert("Session reprise", "La session et la réservation ont été reprises.");
                                } catch (Exception e) {
                                    ControllerUtils.showErrorAlert("Erreur", "Erreur lors de la reprise: " + e.getMessage());
                                }
                            }
                        });
                    }
                    @Override
                    protected void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);
                        } else {
                            GameSession session = getTableView().getItems().get(getIndex());
                            if (session != null) {
                                pauseButton.setVisible("Active".equalsIgnoreCase(session.getStatus()));
                                resumeButton.setVisible("En pause".equalsIgnoreCase(session.getStatus()));
                                setGraphic(pane);
                            } else {
                                setGraphic(null);
                            }
                        }
                    }
                };
            }
        });
    }

    public void loadReservations() {
        User connectedUser = Fabrique.getService().getCurrentUser();
        if (connectedUser == null) {
            reservations = FXCollections.observableArrayList();
            reservationTable.setItems(reservations);
            return;
        }
        try {
            List<Reservation> reservationList = connectedUser.getRole() == Role.SuperAdmin
                    ? Fabrique.getService().findAllReservations()
                    : Fabrique.getService().findReservationsByUser(connectedUser);
            reservations = FXCollections.observableArrayList(reservationList);
            sortReservations();
            reservationTable.setItems(reservations);
        } catch (Exception e) {
            reservations = FXCollections.observableArrayList();
            reservationTable.setItems(reservations);
            ControllerUtils.showErrorAlert("Erreur", "Impossible de charger les réservations");
        }
    }

    private void sortReservations() {
        reservations.sort((r1, r2) -> {
            boolean isPending1 = "En attente".equals(r1.getStatus());
            boolean isPending2 = "En attente".equals(r2.getStatus());
            if (isPending1 != isPending2) return isPending1 ? -1 : 1;
            return r2.getReservationDate().compareTo(r1.getReservationDate());
        });
    }

    public void loadActiveSessions() {
        List<GameSession> sessionList = Fabrique.getService().getAllGameSessions()
                .stream()
                .filter(session -> "Active".equalsIgnoreCase(session.getStatus()) || "En pause".equalsIgnoreCase(session.getStatus()))
                .collect(Collectors.toList());
        activeSessions = FXCollections.observableArrayList(sessionList);
        activeSessionsTable.setItems(activeSessions);
    }

    private void filterReservations(String query) {
        if (query == null || query.isEmpty()) {
            reservationTable.setItems(reservations);
        } else {
            ObservableList<Reservation> filteredReservations = FXCollections.observableArrayList(
                    reservations.stream()
                            .filter(reservation -> {
                                Client client = reservation.getClient();
                                Game game = reservation.getGame();
                                Poste poste = reservation.getPoste();
                                double totalPrice = reservation.getTotalPrice();
                                String parrainCode = reservation.getCodeParrainage();
                                Parrain parrain = parrainCode != null && !parrainCode.isEmpty() ? Fabrique.getService().getParrainByCodeParrainage(parrainCode) : null;
                                boolean matchesClient = client != null && client.getName().toLowerCase().contains(query.toLowerCase());
                                boolean matchesTicket = reservation.getNumeroTicket().toLowerCase().contains(query.toLowerCase());
                                boolean matchesGame = game != null && game.getName().toLowerCase().contains(query.toLowerCase());
                                boolean matchesPoste = poste != null && poste.getName().toLowerCase().contains(query.toLowerCase());
                                boolean matchesPrice = String.format("%.2f", totalPrice).toLowerCase().contains(query.toLowerCase());
                                boolean matchesParrain = (parrain != null && parrain.getName().toLowerCase().contains(query.toLowerCase())) || (parrainCode != null && parrainCode.toLowerCase().contains(query.toLowerCase()));
                                boolean matchesStatus = reservation.getStatus().toLowerCase().contains(query.toLowerCase());
                                return matchesClient || matchesTicket || matchesGame || matchesPoste || matchesPrice || matchesParrain || matchesStatus;
                            })
                            .collect(Collectors.toList())
            );
            reservationTable.setItems(filteredReservations);
        }
    }

    private void startReservationSession(Reservation reservation) {
        if (!"En attente".equals(reservation.getStatus())) {
            ControllerUtils.showErrorAlert("Action impossible", "Cette réservation ne peut pas être démarrée.");
            return;
        }
        Poste poste = reservation.getPoste();
        if (poste.isHorsService()) {
            ControllerUtils.showErrorAlert("Poste hors service", "Le poste " + poste.getName() + " est hors service.");
            return;
        }
        // Vérifier qu'il n'y a pas de session ACTIVE sur ce poste
        GameSession activeSessionOnPoste = Fabrique.getService().getActiveSessionForPoste(poste);
        if (activeSessionOnPoste != null) {
            ControllerUtils.showErrorAlert("Poste occupé", "Le poste " + poste.getName() + " est occupé par une autre session active.");
            return;
        }
        // Vérifier qu'il n'y a pas déjà une session en pause pour cette réservation
        GameSession pausedSession = Fabrique.getService().getAllGameSessions().stream()
            .filter(s -> "En pause".equalsIgnoreCase(s.getStatus()) && s.getReservation().getId() == reservation.getId())
            .findFirst()
            .orElse(null);
        if (pausedSession != null) {
            ControllerUtils.showErrorAlert("Session en pause", "Une session est déjà en pause pour cette réservation. Veuillez la reprendre.");
            return;
        }
        // Créer une nouvelle session
        GameSession newSession = new GameSession();
        newSession.setReservation(reservation);
        newSession.setClient(reservation.getClient());
        newSession.setPoste(poste);
        newSession.setGame(reservation.getGame());
        newSession.setStartTime(LocalDateTime.now());
        newSession.setPaidDuration(reservation.getDuration());
        newSession.setStatus("Active");
        try {
            Fabrique.getService().addGameSession(newSession);
            reservation.setStatus("Active");
            Fabrique.getService().updateReservation(reservation);
            ControllerUtils.showInfoAlert("Session démarrée", String.format("Session pour le poste '%s' démarrée.", poste.getName()));
            loadReservations();
            loadActiveSessions();
            mainTabPane.getSelectionModel().select(activeSessionsTab);
        } catch (Exception e) {
            ControllerUtils.showErrorAlert("Erreur", "Erreur lors du démarrage: " + e.getMessage());
        }
    }



    private void cancelReservation(Reservation reservation) {
        if (!"En attente".equals(reservation.getStatus())) {
            ControllerUtils.showErrorAlert("Impossible d'annuler", "Seules les réservations 'En attente' peuvent être annulées.");
            return;
        }
        Alert confirmationAlert = new Alert(Alert.AlertType.CONFIRMATION, "Confirmer l'annulation de la réservation ?", ButtonType.YES, ButtonType.NO);
        confirmationAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                try {
                    reservation.setStatus("Annulée");
                    Fabrique.getService().updateReservation(reservation);
                    ControllerUtils.showInfoAlert("Réservation annulée", "Réservation annulée avec succès.");
                    loadReservations();
                } catch (Exception e) {
                    ControllerUtils.showErrorAlert("Erreur", "Erreur lors de l'annulation: " + e.getMessage());
                }
            }
        });
    }

    private List<String> getAvailablePaymentMethods() {
        // Récupération des moyens de paiement depuis la base de données ou une liste prédéfinie
        List<String> paymentMethods = new ArrayList<>();
        paymentMethods.add("En Espèce");
        paymentMethods.add("Wave");
        paymentMethods.add("Orange Money");
        paymentMethods.add("Free Money");
        paymentMethods.add("Wizall Money");
        paymentMethods.add("Carte Bancaire");

        return paymentMethods;
    }


   private void promptForExtension(GameSession session) {
    // Création du dialogue personnalisé
    Dialog<Pair<Integer, String>> dialog = new Dialog<>();
    dialog.setTitle("Prolonger la durée");
    dialog.setHeaderText("Prolonger la session pour le poste " + session.getPoste().getName());

    // Configuration des boutons
    ButtonType validerButtonType = new ButtonType("Valider", ButtonBar.ButtonData.OK_DONE);
    dialog.getDialogPane().getButtonTypes().addAll(validerButtonType, ButtonType.CANCEL);

    // Création du contenu du dialogue
    GridPane grid = new GridPane();
    grid.setHgap(10);
    grid.setVgap(10);
    grid.setPadding(new Insets(20, 150, 10, 10));

    // ComboBox pour les heures (0 à 24)
    ComboBox<Integer> hoursComboBox = new ComboBox<>();
    for (int i = 0; i <= 24; i++) {
        hoursComboBox.getItems().add(i);
    }
    hoursComboBox.getSelectionModel().select(0); // Sélectionne 0 par défaut

    // ComboBox pour les minutes (15, 30, 45)
    ComboBox<Integer> minutesComboBox = new ComboBox<>();
    minutesComboBox.getItems().addAll(15, 30, 45);
    minutesComboBox.getSelectionModel().select(0); // Sélectionne 15 par défaut

    // ComboBox pour les moyens de paiement
    ComboBox<String> paymentMethodComboBox = new ComboBox<>();
    List<String> paymentMethods = getAvailablePaymentMethods();
    paymentMethodComboBox.getItems().addAll(paymentMethods);
    paymentMethodComboBox.getSelectionModel().selectFirst(); // Sélectionne le premier élément par défaut

    // Ajout des éléments au grid
    grid.add(new Label("Heures:"), 0, 0);
    grid.add(hoursComboBox, 1, 0);
    grid.add(new Label("Minutes:"), 0, 1);
    grid.add(minutesComboBox, 1, 1);
    grid.add(new Label("Moyen de paiement:"), 0, 2);
    grid.add(paymentMethodComboBox, 1, 2);

    dialog.getDialogPane().setContent(grid);

    // Conversion du résultat
    dialog.setResultConverter(dialogButton -> {
        if (dialogButton == validerButtonType) {
            try {
                int hours = hoursComboBox.getSelectionModel().getSelectedItem();
                int minutes = minutesComboBox.getSelectionModel().getSelectedItem();
                int additionalMinutes = hours * 60 + minutes;

                if (additionalMinutes <= 0) {
                    ControllerUtils.showErrorAlert("Erreur de durée", "Veuillez sélectionner une durée valide.");
                    return null;
                }

                String selectedPaymentMethod = paymentMethodComboBox.getSelectionModel().getSelectedItem();
                if (selectedPaymentMethod == null || selectedPaymentMethod.isEmpty()) {
                    ControllerUtils.showErrorAlert("Erreur", "Veuillez sélectionner un moyen de paiement.");
                    return null;
                }

                return new Pair<>(additionalMinutes, selectedPaymentMethod);
            } catch (Exception e) {
                ControllerUtils.showErrorAlert("Erreur de saisie", "Veuillez sélectionner une durée et un moyen de paiement valides.");
                return null;
            }
        }
        return null;
    });

    // Affichage du dialogue
    Optional<Pair<Integer, String>> result = dialog.showAndWait();

    // Traitement du résultat
    result.ifPresent(pair -> {
        try {
            int additionalMinutes = pair.getKey();
            String selectedPaymentMethod = pair.getValue();
            // Appelle la méthode du service qui gère tout
            Fabrique.getService().extendGameSession(session, additionalMinutes, connectedUserName, selectedPaymentMethod);
            Platform.runLater(() -> {
                loadActiveSessions();
                loadReservations();
            });
        } catch (Exception e) {
            ControllerUtils.showErrorAlert("Erreur", "Échec de la prolongation : " + e.getMessage());
        }
    });
}




    private void terminateGameSession(GameSession session) {
        if (session == null) {
            ControllerUtils.showErrorAlert("Erreur", "Aucune session sélectionnée.");
            return;
        }

        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION, "Terminer la session ?", ButtonType.YES, ButtonType.NO);
        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                try {
                    // Mettre à jour le statut de la session
                    session.setStatus("Terminée");
                    session.setEndTime(LocalDateTime.now());
                    Fabrique.getService().updateGameSession(session);

                    // Mettre à jour le statut de la réservation associée
                    Reservation reservation = session.getReservation();
                    if (reservation != null) {
                        reservation.setStatus("Terminée");
                        Fabrique.getService().updateReservation(reservation);
                    }

                    ControllerUtils.showInfoAlert("Succès", "Session terminée avec succès.");
                    loadActiveSessions();
                    loadReservations();
                } catch (Exception e) {
                    ControllerUtils.showErrorAlert("Erreur", "Échec de la mise à jour: " + e.getMessage());
                }
            }
        });
    }


    private void updateReservationStatusForSession(GameSession session) throws Exception {
        Reservation reservation = session.getReservation();
        if (reservation != null) {
            reservation.setStatus(session.getStatus());
            Fabrique.getService().updateReservation(reservation);
        }
    }

    private void checkExpiredSessions() {
    List<GameSession> activeSessions = Fabrique.getService().getAllGameSessions()
            .stream()
            .filter(s -> "Active".equalsIgnoreCase(s.getStatus()) && !s.isPaused())
            .collect(Collectors.toList());

    boolean sessionsUpdated = false;
    for (GameSession session : activeSessions) {
        if (session.getStartTime() != null && session.getPaidDuration() != null) {
            LocalDateTime endTime = session.getStartTime().plus(session.getPaidDuration());
            if (LocalDateTime.now().isAfter(endTime)) {
                try {
                    // Ne pas marquer comme "Terminée" ici, mais ouvrir le dialogue
                    if (!notifiedSessions.contains(session.getId())) {
                        notifiedSessions.add(session.getId());
                        Platform.runLater(() -> openSessionEndDialog(session));
                    }
                    sessionsUpdated = true;
                } catch (Exception e) {
                    System.err.println("Erreur lors de la notification de la session " + session.getId() + ": " + e.getMessage());
                }
            }
        }
    }
    if (sessionsUpdated) {
        Platform.runLater(this::loadActiveSessions);
    }
}

private void openSessionEndDialog(GameSession session) {
    try {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/views/SessionEndDialog.fxml"));
        Scene scene = new Scene(loader.load());
        Stage stage = new Stage();
        SessionEndDialogController controller = loader.getController();
        controller.setSession(session);
        controller.setConnectedUserName(connectedUserName); // Passe le nom de l'utilisateur connecté
        stage.setScene(scene);
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.showAndWait();

        loadActiveSessions();
        loadReservations();
    } catch (IOException e) {
        ControllerUtils.showErrorAlert("Erreur", "Erreur lors de l'ouverture de la fenêtre de prolongation.");
    }
}





    @FXML
private void openAddReservationWindow() {
    try {
        System.out.println("DEBUG: Début de l'ouverture de AddReservationWindow");
        
        WindowManager.closeWindowsForView("AddReservationWindow");
        
        // Vérifier que le fichier FXML existe
        URL fxmlUrl = getClass().getResource("/com/views/AddReservationWindow.fxml");
        if (fxmlUrl == null) {
            System.err.println("ERREUR: Fichier FXML non trouvé");
            ControllerUtils.showErrorAlert("Erreur", "Fichier de vue non trouvé: AddReservationWindow.fxml");
            return;
        }
        
        System.out.println("DEBUG: Chargement du FXML...");
        FXMLLoader loader = new FXMLLoader(fxmlUrl);
        Scene scene = new Scene(loader.load());
        
        Stage stage = new Stage();
        AddReservationController addController = loader.getController();
        
        if (addController == null) {
            System.err.println("ERREUR: Controller non initialisé");
            ControllerUtils.showErrorAlert("Erreur", "Le contrôleur n'a pas été initialisé.");
            return;
        }
        
        addController.setParentController(this);
        addController.setConnectedUser(connectedUser);
        
        stage.setScene(scene);
        stage.initModality(Modality.APPLICATION_MODAL);
        WindowManager.register("AddReservationWindow", stage);
        stage.sizeToScene();
        stage.setResizable(false);
        stage.centerOnScreen();
        
        System.out.println("DEBUG: Affichage de la fenêtre...");
        stage.showAndWait();
        
        loadReservations();
        loadActiveSessions();
        
    } catch (Exception e) {
        System.err.println("ERREUR DÉTAILLÉE dans openAddReservationWindow:");
        e.printStackTrace();
        ControllerUtils.showErrorAlert("Erreur", "Erreur lors de l'ouverture de la fenêtre d'ajout: " + e.getMessage());
    }
}

    @FXML
    private void openEditReservationWindow() {
        Reservation selectedReservation = reservationTable.getSelectionModel().getSelectedItem();
        if (selectedReservation == null) {
            ControllerUtils.showErrorAlert("Aucune réservation sélectionnée", "Veuillez sélectionner une réservation.");
            return;
        }
        if ("Active".equals(selectedReservation.getStatus()) || "Terminée".equals(selectedReservation.getStatus()) || "Annulée".equals(selectedReservation.getStatus())) {
            ControllerUtils.showErrorAlert("Modification impossible", "Cette réservation ne peut pas être modifiée.");
            return;
        }
        try {
            WindowManager.closeWindowsForView("EditReservationWindow");
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/views/EditReservationWindow.fxml"));
            Scene scene = new Scene(loader.load());
            Stage stage = new Stage();
            EditReservationController controller = loader.getController();
            controller.loadReservationData(selectedReservation);
            stage.setScene(scene);
            stage.initModality(Modality.APPLICATION_MODAL);
            WindowManager.register("EditReservationWindow", stage);
            stage.sizeToScene();
            stage.setResizable(false);
            stage.centerOnScreen();
            stage.showAndWait();
            loadReservations();
            loadActiveSessions();
        } catch (IOException e) {
            ControllerUtils.showErrorAlert("Erreur", "Erreur lors de l'ouverture de la fenêtre de modification.");
        }
    }

    @FXML
    private void deleteReservation() {
        Reservation selectedReservation = reservationTable.getSelectionModel().getSelectedItem();
        if (selectedReservation == null) {
            ControllerUtils.showErrorAlert("Aucune réservation sélectionnée", "Veuillez sélectionner une réservation.");
            return;
        }
        if ("Active".equals(selectedReservation.getStatus())) {
            ControllerUtils.showErrorAlert("Suppression impossible", "Cette réservation est active.");
            return;
        }
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION, "Supprimer cette réservation ?", ButtonType.YES, ButtonType.NO);
        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                try {
                    boolean success = Fabrique.getService().deleteReservation(selectedReservation.getId());
                    if (success) {
                        loadReservations();
                        loadActiveSessions();
                        ControllerUtils.showInfoAlert("Réservation supprimée", "Réservation supprimée avec succès.");
                    }
                } catch (Exception e) {
                    ControllerUtils.showErrorAlert("Erreur", "Erreur lors de la suppression: " + e.getMessage());
                }
            }
        });
    }
}
