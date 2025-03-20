package com.controllers;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import com.App;
import com.core.Fabrique;
import com.entities.User;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class ConnexionController implements Initializable {

    @FXML
    private TextField txtLogin;

    @FXML
    private PasswordField txtPassword;

    @FXML
    private Label lblError;

    //@FXML
    //private Button btnInscription;

    public static User user;

    @Override
    public void initialize(URL arg0, ResourceBundle arg1) {
        lblError.setVisible(false); // Masquer le message d'erreur au démarrage
    }

    
    @FXML
    public void handleConnexion() {
        String login = txtLogin.getText().trim();
        String password = txtPassword.getText().trim();

        if (login.isEmpty() || password.isEmpty()) {
            showError("Veuillez entrer un login et un mot de passe.");
            return;
        }

        user = Fabrique.getService().seConnecter(login, password);
        if (user == null) {
            showError("Identifiants incorrects.");
        } else {
            lblError.setVisible(false);
            try {
                // Récupérer la scène actuelle
                Scene currentScene = txtLogin.getScene(); // ou btnInscription.getScene()
                // Rediriger vers la page d'accueil
                App.setRoot("accueil", 1024, 768, currentScene); // Passer la scène actuelle
            } catch (IOException e) {
                e.printStackTrace();
                showError("Erreur lors du chargement de la page d'accueil.");
            }
        }
    }

   /* @FXML
    private void handleInscription() {
        try {
            // Charger la scène de l'inscription
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/views/register.fxml"));
            Scene registerScene = new Scene(loader.load());

            // Créer une nouvelle fenêtre (stage) pour l'inscription
            Stage registerStage = new Stage();
            registerStage.setScene(registerScene);
            registerStage.setTitle("Inscription");

            // Afficher la fenêtre
            registerStage.show();

            // Fermer la fenêtre actuelle (connexion)
            Stage currentStage = (Stage) btnInscription.getScene().getWindow();
            currentStage.close();
        } catch (IOException e) {
            e.printStackTrace();
            showError("Erreur lors du chargement de la page d'inscription.");
        }
    }*/ 

    private void showError(String message) {
        lblError.setText(message);
        lblError.setVisible(true);
    }
}