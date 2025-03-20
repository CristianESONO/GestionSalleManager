package com.controllers;

import com.core.Fabrique;
import com.entities.Poste;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class EditPosteController {

    private Poste poste;

    @FXML
    private TextField txtNom, txtStatus;
    @FXML
    private CheckBox posteAvailableCheckBox;

    // Initialisation des champs avec les informations actuelles du poste
    @FXML
    public void initialize() {
        if (poste != null) {
            txtNom.setText(poste.getName());  // Nom du poste généré automatiquement
            txtStatus.setText(poste.getStatus());  // Statut actuel
            posteAvailableCheckBox.setSelected(poste.isAvailable());  // Disponibilité actuelle
        }
    }

    // Définir le poste à modifier
    public void setPoste(Poste poste) {
        if (poste != null) {
            this.poste = poste;
        } else {
            System.out.println("Erreur : Poste est nul");
        }
    }
    
    // Sauvegarder les modifications du poste
    @FXML
    private void savePoste() {
        try {
            
            // Mettre à jour le statut du poste
            poste.setStatus(txtStatus.getText());
            poste.setAvailable(posteAvailableCheckBox.isSelected());

            // Sauvegarder les modifications via le service
            Fabrique.getService().updatePoste(poste);

            // Fermer la fenêtre
            Stage stage = (Stage) txtNom.getScene().getWindow();
            stage.close();
        } catch (Exception e) {
            // Gérer les erreurs si quelque chose se passe mal
            e.printStackTrace();
        }
    }

    // Annuler la modification et fermer la fenêtre
    @FXML
    private void cancelEdit() {
        // Fermer la fenêtre sans enregistrer les modifications
        Stage stage = (Stage) txtNom.getScene().getWindow();
        stage.close();
    }
}
