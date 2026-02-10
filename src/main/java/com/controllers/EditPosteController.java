package com.controllers;

import com.core.Fabrique;
import com.entities.Poste;
import javafx.fxml.FXML;
import javafx.scene.control.Button; // Assurez-vous d'avoir les imports pour Button si vous les utilisez dans le FXML
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.event.ActionEvent; // Import pour ActionEvent
import javafx.stage.Stage;

import java.sql.SQLException; // Import pour gérer SQLException

public class EditPosteController {

    private Poste poste;

    @FXML
    private TextField txtNom;

    @FXML
    private CheckBox chkHorsService;

    // Référence au contrôleur parent pour rafraîchir l'affichage
    private PosteController parentController;

    @FXML
    public void initialize() {
        // Rien à faire ici, les données sont chargées via setPoste()
    }

    /**
     * Définit le poste à éditer et initialise les champs de l'interface utilisateur.
     * @param poste Le poste à modifier.
     */
    public void setPoste(Poste poste) {
        if (poste != null) {
            this.poste = poste;
            txtNom.setText(poste.getName());
            txtNom.setEditable(false); // Le nom n'est pas modifiable
            chkHorsService.setSelected(poste.isHorsService()); // coche si poste est hors service
        } else {
            System.err.println("Erreur : Poste transmis est nul.");
            ControllerUtils.showErrorAlert("Erreur", "Impossible de charger les données du poste. Le poste est nul.");
        }
    }

    /**
     * Setter pour le contrôleur parent.
     * @param parentController L'instance du PosteController parent.
     */
    public void setParentController(PosteController parentController) {
        this.parentController = parentController;
    }

    @FXML
    private void savePoste(ActionEvent event) { // Ajout de ActionEvent pour la cohérence
        try {
            if (poste == null) {
                ControllerUtils.showErrorAlert("Erreur de sauvegarde", "Aucune donnée de poste à sauvegarder.");
                return;
            }

            // Met à jour l'état horsService selon la case cochée
            poste.setHorsService(chkHorsService.isSelected());

            // Mise à jour en base
            Fabrique.getService().updatePoste(poste);

            ControllerUtils.showInfoAlert("Poste mis à jour", "Le poste '" + poste.getName() + "' a été mis à jour avec succès !");

            // Rafraîchir la liste dans le contrôleur parent si défini
            if (parentController != null) {
                parentController.loadPostes(); 
            }

            closeWindow(); // Fermer la fenêtre
            
        } catch (SQLException e) {
            // Gérer spécifiquement l'erreur SQLite BUSY ou d'autres erreurs SQL
            if (e.getMessage() != null && e.getMessage().contains("database is locked")) {
                ControllerUtils.showErrorAlert("Erreur de base de données",
                    "La base de données est actuellement verrouillée. Veuillez réessayer. Si le problème persiste, redémarrez l'application.");
            } else {
                ControllerUtils.showErrorAlert("Erreur lors de la mise à jour",
                    "Une erreur SQL s'est produite lors de la mise à jour du poste : " + e.getMessage());
            }
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace(); // Affichage de l'erreur en console
            ControllerUtils.showErrorAlert("Erreur inattendue", "Une erreur inattendue est survenue lors de la sauvegarde : " + e.getMessage());
        }
    }

    @FXML
    private void cancelEdit(ActionEvent event) { // Ajout de ActionEvent pour la cohérence
        closeWindow();
    }

    private void closeWindow() {
        Stage stage = (Stage) txtNom.getScene().getWindow();
        stage.close();
    }
}
