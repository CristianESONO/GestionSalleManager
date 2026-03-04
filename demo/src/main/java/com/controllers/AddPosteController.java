package com.controllers;

import com.core.Fabrique;
import com.entities.Poste;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.event.ActionEvent;
import javafx.stage.Stage;

import java.sql.SQLException; // Import pour gérer SQLException

public class AddPosteController {

    @FXML
    private TextField posteNameField;

    @FXML
    private CheckBox chkHorsService;

    @FXML
    private Button btnAjouterPoste;

    @FXML
    private Button btnAnnuler;

    // Déclaration du contrôleur parent pour rafraîchir la liste des postes après ajout
    private PosteController parentController; 

    @FXML
    public void initialize() {
        // Le champ de texte est désactivé car le nom est généré automatiquement
        posteNameField.setEditable(false);
        posteNameField.setPromptText("Nom généré automatiquement...");
    }

    // Setter pour le contrôleur parent (à appeler lors de l'ouverture de cette fenêtre)
    public void setParentController(PosteController parentController) {
        this.parentController = parentController;
    }

    @FXML
    private void addPoste(ActionEvent event) { // Supprimez 'throws Exception' ici pour gérer les exceptions localement
        Poste newPoste = new Poste();
        newPoste.setHorsService(chkHorsService.isSelected());

        // Générer le nom du poste avant l'insertion.
        // NOTE: Fabrique.getService().getPostes().size() peut ne pas être fiable pour générer un ID unique
        // si des postes sont supprimés ou si plusieurs instances de l'app sont lancées.
        // L'ID généré par la base de données (GenerationType.IDENTITY) est le plus fiable.
        // Le nom pourrait être "Poste nouvellement créé" et mis à jour avec l'ID réel après persistance.
        String generatedName = "Poste " + (Fabrique.getService().getPostes().size() + 1); // Simple, mais peut être amélioré
        newPoste.setName(generatedName); // Définir le nom généré

        try {
            // Insérer le poste avec le nom généré
            newPoste = Fabrique.getService().addPoste(newPoste);
            
            // Mettre à jour le champ de texte avec le nom et l'ID du poste nouvellement créé
            posteNameField.setText(newPoste.getName() + " (ID: " + newPoste.getId() + ")");
            
            ControllerUtils.showInfoAlert("Poste ajouté", "Le poste '" + newPoste.getName() + "' a été ajouté avec succès !");

            // Rafraîchir la liste dans le contrôleur parent si défini
            if (parentController != null) {
                parentController.loadPostes(); 
            }

            closeWindow(); // Fermer la fenêtre après succès
            
        } catch (SQLException e) {
            // Gérer spécifiquement l'erreur SQLite BUSY ou d'autres erreurs SQL
            if (e.getMessage() != null && e.getMessage().contains("database is locked")) {
                ControllerUtils.showErrorAlert("Erreur de base de données",
                    "La base de données est actuellement verrouillée. Veuillez réessayer. Si le problème persiste, redémarrez l'application.");
            } else {
                ControllerUtils.showErrorAlert("Erreur lors de l'ajout",
                    "Une erreur SQL s'est produite lors de l'ajout du poste : " + e.getMessage());
            }
            e.printStackTrace();
        } catch (Exception e) {
            // Capturer d'autres exceptions inattendues
            ControllerUtils.showErrorAlert("Erreur inattendue", "Une erreur inattendue s'est produite : " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void cancel(ActionEvent event) {
        closeWindow();
    }

    private void closeWindow() {
        Stage stage = (Stage) btnAjouterPoste.getScene().getWindow();
        stage.close();
    }
}
