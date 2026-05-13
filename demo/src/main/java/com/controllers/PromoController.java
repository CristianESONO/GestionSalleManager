package com.controllers;

import com.core.Fabrique;
import com.core.WindowManager;
import com.entities.Produit;
import com.entities.Promotion;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter; // Keep this import
import java.util.List;
import java.util.Optional;

public class PromoController {

    @FXML private TableView<Promotion> promoTable;
    @FXML private TableColumn<Promotion, String> nomColumn;
    @FXML private TableColumn<Promotion, Float> tauxColumn;
    @FXML private TableColumn<Promotion, LocalDate> dateDebutColumn;
    @FXML private TableColumn<Promotion, LocalDate> dateFinColumn;
    @FXML private TableColumn<Promotion, Boolean> activeColumn;
    @FXML private TableColumn<Promotion, Void> actionsColumn;

    private ObservableList<Promotion> promotions;

    // Define a formatter for display purposes, consistent across cells
    private static final DateTimeFormatter DISPLAY_DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @FXML
    public void initialize() {
        // Initialiser les colonnes
        nomColumn.setCellValueFactory(new PropertyValueFactory<>("nom"));
        tauxColumn.setCellValueFactory(new PropertyValueFactory<>("tauxReduction"));

        // Configuration de la colonne Date Début
        dateDebutColumn.setCellValueFactory(new PropertyValueFactory<>("dateDebut"));
        dateDebutColumn.setCellFactory(column -> new TableCell<Promotion, LocalDate>() {
            // Use the shared formatter
            @Override
            protected void updateItem(LocalDate item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(DISPLAY_DATE_FORMATTER.format(item)); // Use the shared formatter
                }
            }
        });

        // Configuration de la colonne Date Fin
        dateFinColumn.setCellValueFactory(new PropertyValueFactory<>("dateFin"));
        dateFinColumn.setCellFactory(column -> new TableCell<Promotion, LocalDate>() {
            // Use the shared formatter
            @Override
            protected void updateItem(LocalDate item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(DISPLAY_DATE_FORMATTER.format(item)); // Use the shared formatter
                }
            }
        });

        activeColumn.setCellValueFactory(new PropertyValueFactory<>("actif"));
        activeColumn.setCellFactory(col -> new TableCell<>() {
            private final CheckBox checkBox = new CheckBox();
            {
                checkBox.setDisable(true);
            }
            @Override
            protected void updateItem(Boolean active, boolean empty) {
                super.updateItem(active, empty);
                if (empty || active == null) {
                    setGraphic(null);
                } else {
                    checkBox.setSelected(active);
                    setGraphic(checkBox);
                }
            }
        });

        // Charger les données
        loadPromotions();

        // Ajouter la colonne d'actions
        addActionsToTable();
    }

    private void loadPromotions() {
        try {
            List<Promotion> promoList = Fabrique.getService().getAllPromotions();
            promotions = FXCollections.observableArrayList(promoList);
            promoTable.setItems(promotions);
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible de charger les promotions : " + e.getMessage());
            promotions = FXCollections.observableArrayList();
            promoTable.setItems(promotions);
        }
    }


    private void addActionsToTable() {
        actionsColumn.setCellFactory(col -> new TableCell<>() {
            private final Button btnToggle = new Button();
            private final Button btnEdit = new Button("Modifier");
            private final Button btnDelete = new Button("Supprimer");
            private final HBox container = new HBox(5, btnToggle, btnEdit, btnDelete);

            {
                btnToggle.setOnAction(event -> {
                    Promotion promo = getTableView().getItems().get(getIndex());
                    if (promo.isActif()) {
                        disablePromotion(promo);
                    } else {
                        enablePromotion(promo);
                    }
                });

                btnEdit.setOnAction(event -> {
                    Promotion promo = getTableView().getItems().get(getIndex());
                    openEditPromoWindow(promo);
                });

                btnDelete.setOnAction(event -> {
                    Promotion promo = getTableView().getItems().get(getIndex());
                    deletePromotion(promo);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Promotion promo = getTableView().getItems().get(getIndex());
                    btnToggle.setText(promo.isActif() ? "Désactiver" : "Activer");
                    setGraphic(container);
                }
            }
        });
    }

    private void enablePromotion(Promotion promo) {
        LocalDate today = LocalDate.now();

        if (promo.getDateDebut() == null || promo.getDateFin() == null) {
            showAlert("Données manquantes", "Les dates de début et/ou de fin de la promotion sont manquantes.");
            return;
        }

        if (today.isBefore(promo.getDateDebut())) {
            showAlert("Impossible d'activer", "La promotion '" + promo.getNom() + "' n'a pas encore commencé.");
            return;
        }

        if (today.isAfter(promo.getDateFin())) {
            showAlert("Impossible d'activer", "La promotion '" + promo.getNom() + "' est déjà terminée.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText("Activer la promotion");
        confirm.setContentText("Voulez-vous activer la promotion '" + promo.getNom() + "' ?");
        Optional<ButtonType> result = confirm.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                Fabrique.getService().appliquerPromotion(promo);
                showAlert("Succès", "La promotion '" + promo.getNom() + "' a été activée avec succès.");
                loadPromotions();
            } catch (Exception e) {
                e.printStackTrace();
                showAlert("Erreur", "Impossible d'activer la promotion : " + e.getMessage());
            }
        }
    }

    private void disablePromotion(Promotion promo) {
        if (!promo.isActif()) {
            showAlert("Info", "La promotion est déjà désactivée.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText("Désactiver la promotion");
        confirm.setContentText("Voulez-vous vraiment désactiver la promotion '" + promo.getNom() + "' ?");
        Optional<ButtonType> result = confirm.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                // Utilisez le service pour gérer la désactivation
                Fabrique.getService().retirerPromotion(promo);
                
                Platform.runLater(() -> {
                    showAlert("Succès", "La promotion '" + promo.getNom() + "' a été désactivée avec succès.");
                    loadPromotions();
                });
            } catch (Exception e) {
                e.printStackTrace();
                showAlert("Erreur", "Impossible de désactiver la promotion : " + e.getMessage());
            }
        }
    }

    @FXML
    private void openAddPromoWindow(ActionEvent event) {
        try {
            WindowManager.closeWindowsForView("AddPromoWindow");
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/views/AddPromoWindow.fxml"));
            Stage stage = new Stage();
            stage.setTitle("Ajouter Promotion");
            stage.initModality(Modality.APPLICATION_MODAL);
            Scene scene = new Scene(loader.load());
            scene.getStylesheets().add(getClass().getResource("/com/css/style.css").toExternalForm());
            stage.setScene(scene);
            WindowManager.register("AddPromoWindow", stage);
            stage.sizeToScene();
            stage.setResizable(false);
            stage.centerOnScreen();

            stage.showAndWait();

            loadPromotions();

        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible d'ouvrir la fenêtre d'ajout.");
        }
    }

    private void openEditPromoWindow(Promotion promo) {
        try {
            WindowManager.closeWindowsForView("EditPromoWindow");
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/views/EditPromoWindow.fxml"));
            Stage stage = new Stage();
            stage.setTitle("Modifier Promotion");
            stage.initModality(Modality.APPLICATION_MODAL);
            Scene scene = new Scene(loader.load());
            scene.getStylesheets().add(getClass().getResource("/com/css/style.css").toExternalForm());
            stage.setScene(scene);
            WindowManager.register("EditPromoWindow", stage);
            stage.sizeToScene();
            stage.setResizable(false);
            stage.centerOnScreen();

            EditPromoController controller = loader.getController();
             controller.setPromotion(Fabrique.getService().getPromotionByIdWithProduits(promo.getId()));
            stage.showAndWait();

            loadPromotions();

        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible d'ouvrir la fenêtre de modification.");
        }
    }

    private void deletePromotion(Promotion promo) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText("Suppression de la promotion");
        confirm.setContentText("Voulez-vous vraiment supprimer la promotion '" + promo.getNom() + "' ? Cela retirera également la promotion de tous les produits associés.");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                Fabrique.getService().deletePromotion(promo);
                showAlert("Succès", "La promotion '" + promo.getNom() + "' a été supprimée avec succès.");
                loadPromotions();
            } catch (Exception e) {
                e.printStackTrace();
                showAlert("Erreur", "Impossible de supprimer la promotion : " + e.getMessage());
            }
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.show();
    }
}