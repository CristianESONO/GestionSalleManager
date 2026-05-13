package com.controllers;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.core.Fabrique;
import com.entities.Game;
import com.entities.Poste;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ComboBox; // Ajouté pour le ComboBox
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TextField;
import javafx.scene.image.Image; // Ajouté pour l'aperçu
import javafx.scene.image.ImageView; // Ajouté pour l'aperçu
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class AddGameController {

    @FXML
    private TextField txtName; // Renommé de txtNom
    @FXML
    private ComboBox<String> cbType; // Changé de TextField à ComboBox
    @FXML
    private TextField txtImagePath;
    @FXML
    private ImageView imageViewPreview; // Pour afficher l'aperçu de l'image
    @FXML
    private ListView<Poste> lvPostes; // Renommé de listPostes pour consistance

    private ObservableList<Poste> allPostes; // Liste observable de tous les postes
    private GameController gameController; // Référence au GameController pour rafraîchir la vue principale

    // Setter pour le GameController parent
    public void setGameController(GameController gameController) {
        this.gameController = gameController;
    }

    @FXML
    public void initialize() {
        // Initialise les types de jeux si ce n'est pas déjà fait dans le FXML
        if (cbType.getItems().isEmpty()) {
            cbType.getItems().addAll("Action", "Aventure", "Stratégie", "Sport", "Course", "Simulation", "RPG", "Casual", "Autre");
        }

        // Configure le ListView pour permettre la sélection multiple
        lvPostes.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        // Charger la liste des postes disponibles
        loadAllPostes();
    }

    private void loadAllPostes() {
        try {
            List<Poste> postes = Fabrique.getService().getPostes(); // Assurez-vous que Fabrique.getService().getAllPostes() existe
            allPostes = FXCollections.observableArrayList(postes);
            lvPostes.setItems(allPostes);
        } catch (Exception e) {
            showAlert(AlertType.ERROR, "Erreur de chargement", "Impossible de charger les postes disponibles.");
            e.printStackTrace();
        }
    }

    @FXML
    private void handleSave() { // Renommé de addGame pour être plus générique
        try {
            // Récupérer les valeurs du formulaire
            String name = txtName.getText().trim();
            String type = cbType.getValue();
            String imagePath = txtImagePath.getText().trim();

            // Vérifier que tous les champs obligatoires sont remplis
            if (name.isEmpty() || type == null || imagePath.isEmpty()) {
                showAlert(AlertType.WARNING, "Champs manquants", "Veuillez remplir tous les champs obligatoires (Nom, Type, Chemin de l'image).");
                return;
            }

            // Vérifier que le fichier image existe réellement
            File imageFile = new File(imagePath);
            if (!imageFile.exists() || !imageFile.isFile()) {
                showAlert(AlertType.ERROR, "Image invalide", "Le fichier d'image sélectionné est invalide ou n'existe pas.");
                return;
            }

            // Créer un objet Game (sans description ni status)
            Game game = new Game(name, type, imagePath); // Assurez-vous d'avoir un constructeur Game(String name, String type, String imagePath)

            // Ajouter le jeu via le service
            Fabrique.getService().addGame(game); // Cette méthode doit retourner le jeu nouvellement créé avec son ID (ou rafraîchir game.id)

            // Récupérer les postes sélectionnés
            ObservableList<Poste> selectedPostes = lvPostes.getSelectionModel().getSelectedItems();

            // Associer les postes sélectionnés au jeu
            List<Poste> gamePostes = new ArrayList<>(selectedPostes);
            game.setPostes(gamePostes); // Met à jour l'objet Game en mémoire

            // Persister les associations dans la base de données
            for (Poste poste : selectedPostes) {
                Fabrique.getService().addPosteToGame(poste, game); // Cette méthode doit exister dans votre service
            }

            showAlert(AlertType.INFORMATION, "Succès", "Jeu ajouté avec succès !");
            closeWindow();
            if (gameController != null) {
                gameController.refreshGames(); // Rafraîchit la vue principale après l'ajout
            }

        } catch (Exception e) {
            showAlert(AlertType.ERROR, "Erreur", "Une erreur s'est produite lors de l'ajout du jeu : " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleBrowseImage() { // Renommé de handleAddImage
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif"));

        File file = fileChooser.showOpenDialog(new Stage());

        if (file != null) {
            txtImagePath.setText(file.getAbsolutePath());
            loadImagePreview(file.getAbsolutePath()); // Affiche l'aperçu
        }
    }

    // Charge et affiche l'aperçu de l'image
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

    @FXML
    private void handleCancel() { // Renommé de cancel
        closeWindow();
    }

    // Méthode pour afficher des alertes
    private void showAlert(AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Méthode pour fermer la fenêtre
    private void closeWindow() {
        Stage stage = (Stage) txtName.getScene().getWindow(); // Utilisez un champ FXML pour obtenir la scène
        stage.close();
    }
}