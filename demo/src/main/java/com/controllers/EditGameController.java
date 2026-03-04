package com.controllers;

import com.core.Fabrique;
import com.entities.Game;
import com.entities.Poste;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set; // Important: Ensure Set is used

public class EditGameController {

    private Game game;
    private GameController gameController;

    @FXML
    private TextField txtName;
    @FXML
    private ComboBox<String> cbType;
    @FXML
    private TextField txtImagePath;
    @FXML
    private ImageView imageViewPreview;
    @FXML
    private ListView<Poste> lvPostes;

    private ObservableList<Poste> allPostes;

    @FXML
    public void initialize() {
        lvPostes.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        if (cbType.getItems().isEmpty()) {
            cbType.getItems().addAll("Action", "Aventure", "Stratégie", "Sport", "Course", "Simulation", "RPG", "Casual", "Autre");
        }

        loadAllPostes();
    }

    public void setGame(Game game) {
        this.game = game;
        if (game != null) {
            txtName.setText(game.getName());
            cbType.setValue(game.getType());
            txtImagePath.setText(game.getImagePath());
            loadImagePreview(game.getImagePath());

            // Load postes again if allPostes might be null or outdated when setGame is called
            // (This handles cases where setGame might be called before or after initialize fully loads postes)
            if (allPostes == null || allPostes.isEmpty()) {
                loadAllPostes();
            }
            preselectAssociatedPostes();
        }
    }

    public void setGameController(GameController gameController) {
        this.gameController = gameController;
    }

    private void loadAllPostes() {
        try {
            // Assuming Fabrique.getService().getAllPostes() or .getPostes() returns List<Poste>
            List<Poste> postes = Fabrique.getService().getPostes(); // Using getAllPostes for clarity, ensure your service method name matches
            allPostes = FXCollections.observableArrayList(postes);
            lvPostes.setItems(allPostes);
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur de chargement", "Impossible de charger les postes disponibles.");
            e.printStackTrace();
        }
    }

    private void preselectAssociatedPostes() {
        if (game != null && game.getPostes() != null && allPostes != null) {
            // Clear any previous selections before applying new ones
            lvPostes.getSelectionModel().clearSelection();

            for (Poste associatedPoste : game.getPostes()) {
                for (int i = 0; i < allPostes.size(); i++) {
                    // Critical: Poste.equals() and .hashCode() must be correctly implemented
                    if (allPostes.get(i).equals(associatedPoste)) {
                        lvPostes.getSelectionModel().select(i);
                        break;
                    }
                }
            }
        }
    }

    @FXML
    private void handleSave() {
        if (game == null) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Aucun jeu sélectionné pour la modification.");
            return;
        }

        try {
            String newName = txtName.getText().trim();
            String newType = cbType.getValue();
            String newImagePath = txtImagePath.getText().trim();

            if (newName.isEmpty() || newType == null || newImagePath.isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Champs manquants", "Veuillez remplir tous les champs obligatoires (Nom, Type, Chemin de l'image).");
                return;
            }

            File imageFile = new File(newImagePath);
            if (!imageFile.exists() || !imageFile.isFile()) {
                showAlert(Alert.AlertType.ERROR, "Chemin d'image invalide", "Le fichier image spécifié n'existe pas ou n'est pas un fichier valide.");
                return;
            }

            game.setName(newName);
            game.setType(newType);
            game.setImagePath(newImagePath);

            Fabrique.getService().updateGame(game);

            // --- Gérer les associations de postes ---
            ObservableList<Poste> selectedPostesObservable = lvPostes.getSelectionModel().getSelectedItems();
            List<Poste> selectedPostes = new ArrayList<>(selectedPostesObservable); // Convert ObservableList to Set

            // Ensure game.getPostes() returns a Set<Poste>
            List<Poste> currentAssociatedPostes = game.getPostes();

            // Postes to add: in selectedPostes but not in currentAssociatedPostes
            List<Poste> postesToAdd = new ArrayList<>(selectedPostes);
            postesToAdd.removeAll(currentAssociatedPostes);

            // Postes to remove: in currentAssociatedPostes but not in selectedPostes
            List<Poste> postesToRemove = new ArrayList<>(currentAssociatedPostes);
            postesToRemove.removeAll(selectedPostes);

            for (Poste poste : postesToAdd) {
                Fabrique.getService().addPosteToGame(poste, game);
            }
            for (Poste poste : postesToRemove) {
                Fabrique.getService().removePosteFromGame(poste, game);
            }
            // Update the in-memory Game object's postes set
            game.setPostes(selectedPostes); // Set now directly holds the selected unique postes

            showAlert(Alert.AlertType.INFORMATION, "Succès", "Jeu modifié avec succès !");
            closeWindow();
            if (gameController != null) {
                gameController.refreshGames();
            }

        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur de modification", "Une erreur est survenue lors de la modification du jeu : " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleCancel() {
        closeWindow();
    }

    @FXML
    private void handleBrowseImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif"));

        File file = fileChooser.showOpenDialog(new Stage());

        if (file != null) {
            txtImagePath.setText(file.getAbsolutePath());
            loadImagePreview(file.getAbsolutePath());
        }
    }

    private void loadImagePreview(String imagePath) {
        if (imagePath == null || imagePath.isEmpty()) {
            imageViewPreview.setImage(null);
            return;
        }
        try {
            File file = new File(imagePath);
            if (file.exists() && !file.isDirectory()) {
                Image image = new Image(file.toURI().toString());
                imageViewPreview.setImage(image);
            } else {
                imageViewPreview.setImage(null);
            }
        } catch (Exception e) {
            System.err.println("Erreur lors du chargement de l'aperçu de l'image: " + e.getMessage());
            imageViewPreview.setImage(null);
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void closeWindow() {
        Stage stage = (Stage) txtName.getScene().getWindow();
        stage.close();
    }
}