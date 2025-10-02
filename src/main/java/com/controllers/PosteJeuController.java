package com.controllers;

import com.core.Fabrique;
import com.core.WindowManager;
import com.entities.Game;
import com.entities.GameSession;
import com.entities.Poste;
import com.entities.User;
import com.entities.Role;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.Text; // Although not strictly used for Text in current logic, keep if you plan to use it
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class PosteJeuController {

    @FXML
    private GridPane gridPostes;

    @FXML
    private Button btnAjouterPoste;

    @FXML
    private Button btnAjouterJeu;

    private List<Poste> postes;
    private List<Game> jeux;
    private int currentPage = 0;
    private final int BASE_COLUMNS = 4;
    private final int BASE_ROWS = 2;

    private boolean isSuperAdmin;

    private final Set<Integer> notifiedSessionsEnded = new HashSet<>();
    private final Set<Integer> notifiedSessionsTwoMinutes = new HashSet<>();

    @FXML
    public void initialize() throws Exception {
        User currentUser = Fabrique.getService().getCurrentUser();
        this.isSuperAdmin = currentUser.getRole() == Role.SuperAdmin;

        if (btnAjouterPoste != null) btnAjouterPoste.setVisible(this.isSuperAdmin);
        if (btnAjouterJeu != null) btnAjouterJeu.setVisible(this.isSuperAdmin);

        jeux = Fabrique.getService().getAllGames();
        if (jeux == null) {
            jeux = new ArrayList<>();
        }

        postes = Fabrique.getService().getPostes();
        displayPage(currentPage);

        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(5), ev -> {
            Platform.runLater(() -> {
                try {
                    displayPage(currentPage);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();

        gridPostes.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                newScene.windowProperty().addListener((obs2, oldWindow, newWindow) -> {
                    if (newWindow instanceof Stage) {
                        Stage stage = (Stage) newWindow;
                        stage.widthProperty().addListener((obs3, oldWidth, newWidth) -> {
                            try {
                                adjustGridColumns((Double) newWidth);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        });
                    }
                });
            }
        });
    }

    private void adjustGridColumns(double newWidth) throws Exception {
        int newColumns;
        if (newWidth > 1200) {
            newColumns = 5;
        } else if (newWidth > 900) {
            newColumns = 4;
        } else if (newWidth > 600) {
            newColumns = 3;
        } else {
            newColumns = 2;
        }

        gridPostes.getColumnConstraints().clear();
        for (int i = 0; i < newColumns; i++) {
            ColumnConstraints cc = new ColumnConstraints();
            cc.setHgrow(Priority.ALWAYS);
            cc.setPercentWidth(100.0 / newColumns);
            gridPostes.getColumnConstraints().add(cc);
        }

        displayPage(currentPage);
    }

    private void displayPage(int page) throws Exception {
        gridPostes.getChildren().clear();
        gridPostes.setHgap(20);
        gridPostes.setVgap(20);

        int currentColumns = gridPostes.getColumnConstraints().isEmpty() ? BASE_COLUMNS : gridPostes.getColumnConstraints().size();
        int itemsPerPage = currentColumns * BASE_ROWS;

        int startIndex = page * itemsPerPage;
        int endIndex = Math.min(startIndex + itemsPerPage, postes.size());

        int row = 0, col = 0;
        for (int i = startIndex; i < endIndex; i++) {
            Poste poste = postes.get(i);
            VBox card = createPosteCard(poste);
            gridPostes.add(card, col, row);

            col++;
            if (col >= currentColumns) {
                col = 0;
                row++;
            }
        }
    }

   /**
 * Creates a modern, styled VBox representing a single Poste card.
 * Includes status logic and admin buttons.
 *
 * @param poste The Poste data to display.
 * @return A VBox styled as a poste card.
 * @throws Exception If there's an error retrieving game sessions.
 */
private VBox createPosteCard(Poste poste) throws Exception {
    VBox cardContent = new VBox(10);
    cardContent.setPadding(new Insets(15));
    cardContent.setAlignment(Pos.CENTER_LEFT);
    cardContent.setStyle("-fx-background-color: #ffffff; -fx-background-radius: 10px; -fx-border-radius: 10px; " +
                         "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 10, 0, 0, 4);");

    cardContent.setMaxWidth(Double.MAX_VALUE);
    cardContent.setPrefHeight(Region.USE_COMPUTED_SIZE);

    String finalStatus;
    String statusColorHex;
    java.time.Duration shortestRemaining = null;

    if (poste.isHorsService()) {
        finalStatus = "Hors service";
        statusColorHex = "#D50000";
    } else {
        List<GameSession> allSessions = Fabrique.getService().getAllGameSessions();
        List<GameSession> sessionsForThisPoste = allSessions.stream()
            .filter(session -> session.getPoste() != null && 
                            session.getPoste().getId() == poste.getId() && 
                            ("Active".equals(session.getStatus()) || "En pause".equals(session.getStatus())))
            .collect(Collectors.toList());

        boolean hasActiveSession = false;
        boolean hasPausedSession = false;
        
        for (GameSession session : sessionsForThisPoste) {
            java.time.Duration remaining = session.getRemainingTime();
            
            // Vérifier d'abord si c'est une session en pause
            if ("En pause".equals(session.getStatus())) {
                hasPausedSession = true;
                if (remaining.isPositive()) {
                    if (shortestRemaining == null || remaining.compareTo(shortestRemaining) < 0) {
                        shortestRemaining = remaining;
                    }
                }
            } 
            // Sinon, c'est une session active
            else if ("Active".equals(session.getStatus())) {
                hasActiveSession = true;
                if (remaining.isPositive()) {
                    if (shortestRemaining == null || remaining.compareTo(shortestRemaining) < 0) {
                        shortestRemaining = remaining;
                    }

                    // Notification pour les sessions actives seulement
                    if (remaining.toMinutes() <= 2 && !notifiedSessionsTwoMinutes.contains(session.getId())) {
                        notifiedSessionsTwoMinutes.add(session.getId());
                        showTwoMinutesLeftNotification(poste.getName());
                    }
                } else {
                    // Session active expirée
                    if (!notifiedSessionsEnded.contains(session.getId())) {
                        notifiedSessionsEnded.add(session.getId());
                        session.setStatus("Terminée");
                        Fabrique.getService().updateGameSession(session);
                        openSessionEndDialog(session);
                    }
                }
            }
        }

        // Déterminer le statut d'affichage (priorité à "En pause")
        if (hasPausedSession) {
            finalStatus = "En pause";
            statusColorHex = "#FF9800"; // Orange plus foncé pour pause
        } else if (hasActiveSession) {
            finalStatus = "Occupé";
            statusColorHex = "#FFA500"; // Orange standard
        } else {
            finalStatus = "Disponible";
            statusColorHex = "#00C853"; // Vert
        }
    }

    Circle statusCircle = new Circle(10);
    statusCircle.setFill(Color.web(statusColorHex));
    statusCircle.setStroke(Color.web("#bdc3c7"));
    statusCircle.setStrokeWidth(1);

    Label posteName = new Label(poste.getName());
    posteName.setFont(Font.font("Arial", 18));
    posteName.setStyle("-fx-font-weight: bold; -fx-text-fill: #2c3e50;");

    Label statusLabel = new Label("Statut: " + finalStatus);
    
    // Afficher le temps restant seulement si disponible et pertinent
    if (shortestRemaining != null && !poste.isHorsService() && 
        ("Occupé".equals(finalStatus) || "En pause".equals(finalStatus))) {
        
        long minutes = shortestRemaining.toMinutes();
        long seconds = shortestRemaining.toSeconds() % 60;
        
        if ("En pause".equals(finalStatus)) {
            statusLabel.setText(String.format("Statut: %s (Temps gelé: %02d:%02d)", finalStatus, minutes, seconds));
            statusLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #FF9800; -fx-font-weight: bold;");
        } else {
            statusLabel.setText(String.format("Statut: %s (Reste: %02d:%02d)", finalStatus, minutes, seconds));
            statusLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #e67e22; -fx-font-weight: bold;");
        }
    } else {
        // Style différent selon le statut
        if ("En pause".equals(finalStatus)) {
            statusLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #FF9800; -fx-font-weight: bold;");
        } else if ("Occupé".equals(finalStatus)) {
            statusLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #e67e22; -fx-font-weight: bold;");
        } else if ("Hors service".equals(finalStatus)) {
            statusLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #D50000; -fx-font-weight: bold;");
        } else {
            statusLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #00C853; -fx-font-weight: bold;");
        }
    }

    HBox statusBox = new HBox(10, statusCircle, posteName);
    statusBox.setAlignment(Pos.CENTER_LEFT);
    VBox.setMargin(statusBox, new Insets(0, 0, 5, 0));

    Label gamesHeader = new Label("Jeux installés:");
    gamesHeader.setFont(Font.font("Arial", 14));
    gamesHeader.setStyle("-fx-font-weight: bold; -fx-text-fill: #34495e;");

    VBox gamesList = new VBox(5);
    if (poste.getGames() != null && !poste.getGames().isEmpty()) {
        for (Game jeu : poste.getGames()) {
            Label gameLabel = new Label("• " + jeu.getName());
            gameLabel.setFont(Font.font("Arial", 12));
            gameLabel.setStyle("-fx-text-fill: #34495e;");
            gamesList.getChildren().add(gameLabel);
        }
    } else {
        Label noGamesLabel = new Label("Aucun jeu associé.");
        noGamesLabel.setFont(Font.font("Arial", 12));
        noGamesLabel.setStyle("-fx-text-fill: #95a5a6; -fx-font-style: italic;");
        gamesList.getChildren().add(noGamesLabel);
    }

    ScrollPane scrollJeux = new ScrollPane(gamesList);
    scrollJeux.setFitToWidth(true);
    scrollJeux.setPrefHeight(Region.USE_COMPUTED_SIZE);
    scrollJeux.setMaxHeight(80);
    scrollJeux.setStyle("-fx-background-color: transparent; -fx-border-color: #ecf0f1; -fx-border-radius: 5px;");

    cardContent.getChildren().addAll(statusBox, statusLabel, gamesHeader, scrollJeux);

    if (this.isSuperAdmin) {
        HBox adminButtons = new HBox(10);
        adminButtons.setAlignment(Pos.CENTER_RIGHT);
        VBox.setMargin(adminButtons, new Insets(10, 0, 0, 0));

        Button btnModifier = new Button("Modifier");
        btnModifier.getStyleClass().add("card-button-modify");
        btnModifier.setOnAction(e -> modifierPoste(poste));
        btnModifier.setStyle("-fx-background-color: #95a5a6; -fx-text-fill: white; -fx-font-size: 12px; " +
                             "-fx-padding: 8px 15px; -fx-background-radius: 5px; -fx-cursor: hand;");

        Button btnSupprimer = new Button("Supprimer");
        btnSupprimer.getStyleClass().add("card-button-delete");
        btnSupprimer.setOnAction(e -> supprimerPoste(poste));
        btnSupprimer.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-size: 12px; " +
                              "-fx-padding: 8px 15px; -fx-background-radius: 5px; -fx-cursor: hand;");
        
        adminButtons.getChildren().addAll(btnModifier, btnSupprimer);
        cardContent.getChildren().add(adminButtons);
    }

    return cardContent;
}

    private void showTwoMinutesLeftNotification(String posteName) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Attention");
            alert.setHeaderText("Temps restant faible");
            alert.setContentText("Il reste 2 minutes pour la session du poste " + posteName + ".");
            alert.show();

            com.utils.NotificationUtil.playAlertSound();
        });
    }

    private void modifierPoste(Poste poste) {
        try {
            WindowManager.closeWindowsForView("EditPosteWindow");
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/views/EditPosteWindow.fxml"));
            Scene scene = new Scene(loader.load());
            Stage stage = new Stage();
            stage.setScene(scene);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Modifier le Poste");
            WindowManager.register("EditPosteWindow", stage);
            stage.sizeToScene();
            stage.setResizable(false);
            stage.centerOnScreen();

            stage.show();

            com.controllers.EditPosteController controller = loader.getController();
            controller.setPoste(poste);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void refreshPostes() throws Exception {
        postes = Fabrique.getService().getPostes();
        displayPage(currentPage);
    }

    private void supprimerPoste(Poste poste) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation de suppression");
        alert.setHeaderText("Voulez-vous vraiment supprimer ce poste ?");
        alert.setContentText("Poste : " + poste.getName());

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                Fabrique.getService().deletePoste(poste);
                postes.remove(poste);
                displayPage(currentPage);

                Alert info = new Alert(Alert.AlertType.INFORMATION);
                info.setTitle("Suppression réussie");
                info.setHeaderText(null);
                info.setContentText("Le poste a été supprimé avec succès !");
                info.showAndWait();
            } catch (Exception e) {
                e.printStackTrace();
                Alert error = new Alert(Alert.AlertType.ERROR);
                error.setTitle("Erreur");
                error.setHeaderText("Une erreur est survenue");
                error.setContentText(e.getMessage());
                error.showAndWait();
            }
        }
    }

    @FXML
    private void ajouterPoste(ActionEvent event) {
        try {
            WindowManager.closeWindowsForView("AddPosteWindow");
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/views/AddPosteWindow.fxml"));
            Scene scene = new Scene(loader.load());
            Stage stage = new Stage();
            stage.setScene(scene);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Ajouter un Poste");
            WindowManager.register("AddPosteWindow", stage);
            stage.sizeToScene();
            stage.setResizable(false);
            stage.centerOnScreen();

            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void ajouterJeu(ActionEvent event) {
        try {
            WindowManager.closeWindowsForView("AddGameWindow");
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/views/AddGameWindow.fxml"));
            Scene scene = new Scene(loader.load());
            Stage stage = new Stage();
            stage.setScene(scene);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Ajouter un Jeu");
            WindowManager.register("AddGameWindow", stage);
            stage.sizeToScene();
            stage.setResizable(false);
            stage.centerOnScreen();

            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void previousPage(ActionEvent event) throws Exception {
        if (currentPage > 0) {
            currentPage--;
            displayPage(currentPage);
        }
    }

    @FXML
    private void nextPage(ActionEvent event) throws Exception {
        int currentColumns = gridPostes.getColumnConstraints().isEmpty() ? BASE_COLUMNS : gridPostes.getColumnConstraints().size();
        int itemsPerPage = currentColumns * BASE_ROWS;
        int maxPages = (int) Math.ceil((double) postes.size() / itemsPerPage);
        if (currentPage < maxPages - 1) {
            currentPage++;
            displayPage(currentPage);
        }
    }

    private void openSessionEndDialog(GameSession session) {
        Platform.runLater(() -> {
            try {
                WindowManager.closeWindowsForView("SessionEndDialog");
                com.utils.NotificationUtil.playAlertSound();
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/views/SessionEndDialog.fxml"));
                Scene scene = new Scene(loader.load());
                Stage dialogStage = new Stage();
                dialogStage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
                dialogStage.setScene(scene);
                dialogStage.setTitle("Session terminée");
                WindowManager.register("SessionEndDialog", dialogStage);
                dialogStage.show();

                SessionEndDialogController controller = loader.getController();
                controller.setSession(session);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }
}