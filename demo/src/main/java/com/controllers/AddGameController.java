package com.controllers;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import com.core.Fabrique;
import com.entities.Game;
import com.entities.Poste;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class AddGameController {

    @FXML
    private TextField txtNom, txtDescription, txtType, txtStatus, txtImagePath;

    @FXML
    private ListView<Poste> listPostes; // ListView pour sélectionner les postes

    @FXML
    private Button btnAddImage; // Bouton pour choisir une image

    private ObservableList<Poste> postesList; // Liste observable des postes

    @FXML
    public void initialize() {
        // Charger la liste des postes disponibles
        loadPostes();
          // Configurer le ListView pour permettre la sélection multiple
        listPostes.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
    }

    private void loadPostes() {
        // Récupérer la liste des postes disponibles depuis le service
        List<Poste> postes = Fabrique.getService().getPostes();
        postesList = FXCollections.observableArrayList(postes);
        listPostes.setItems(postesList);
    }

    @FXML
    private void addGame() {
        try {
            // Récupérer les valeurs du formulaire
            String name = txtNom.getText();
            String description = txtDescription.getText();
            String type = txtType.getText();
            String status = txtStatus.getText();
            String imagePath = txtImagePath.getText(); // Récupérer le chemin de l'image

            // Vérifier que tous les champs sont remplis
            if (name.isEmpty() || description.isEmpty() || type.isEmpty() || status.isEmpty() || imagePath.isEmpty()) {
                showAlert(AlertType.WARNING, "Champs manquants", "Veuillez remplir tous les champs.");
                return;
            }

            // Vérifier que l'image existe réellement
            Path path = Paths.get(imagePath);
            if (!Files.exists(path) || !Files.isRegularFile(path)) {
                showAlert(AlertType.ERROR, "Image invalide", "Le fichier d'image sélectionné est invalide ou n'existe pas.");
                return;
            }

            // Créer un objet Game
            Game game = new Game(name, description, type, status, imagePath);
            // Ajouter le jeu via le service
            Fabrique.getService().addGame(game);
             

              // Ajouter les postes sélectionnés au jeu
            ObservableList<Poste> selectedPostes = listPostes.getSelectionModel().getSelectedItems();
            for (Poste poste : selectedPostes) {
                game.addPoste(poste);

                Fabrique.getService().addPosteToGame(poste, game);
            }
    

        

            // Fermer la fenêtre d'ajout (si nécessaire)
            closeWindow();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(AlertType.ERROR, "Erreur", "Une erreur s'est produite lors de l'ajout du jeu.");
        }
    }

    @FXML
    private void handleAddImage() {
        // Ouvrir une boîte de dialogue pour sélectionner un fichier image
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif"));

        // Récupérer le fichier sélectionné
        File file = fileChooser.showOpenDialog(new Stage());

        if (file != null) {
            // Mettre à jour le chemin de l'image dans le champ de texte
            txtImagePath.setText(file.getAbsolutePath());
        }
    }

    @FXML
    private void cancel() {
        // Fermer la fenêtre sans rien faire
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
        Stage stage = (Stage) btnAddImage.getScene().getWindow();
        stage.close();
    }
}