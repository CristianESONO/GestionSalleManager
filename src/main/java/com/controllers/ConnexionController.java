package com.controllers;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import com.App;
import com.core.Fabrique;
import com.entities.User;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.event.EventHandler;

public class ConnexionController implements Initializable {

    @FXML private TextField txtLogin;
    @FXML private PasswordField txtPassword;
    @FXML private Label lblError;
    @FXML private Button btnInscription;

    public static User user; // Conserve cette variable si elle est utilisée ailleurs

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        lblError.setVisible(false);
        
        // Gestionnaire pour la touche Entrer
        EventHandler<KeyEvent> enterKeyHandler = event -> {
            if (event.getCode() == KeyCode.ENTER) {
                handleConnexion();
            }
        };
        
        // Appliquer le gestionnaire aux deux champs de texte
        txtLogin.setOnKeyPressed(enterKeyHandler);
        txtPassword.setOnKeyPressed(enterKeyHandler);
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
            Fabrique.getService().setCurrentUser(user);
            lblError.setVisible(false);

            try {
                // Charger la vue "accueil"
                MainSceneController.getInstance().loadView("accueil");
                // Mettre à jour la barre de titre
                MainSceneController.getInstance().setWindowTitle("GESTION KAYPLAY - Accueil");
                // Mettre à jour la visibilité du menu en fonction du rôle de l'utilisateur connecté
                MainSceneController.getInstance().updateMenuForUser(user);
            } catch (Exception e) {
                e.printStackTrace();
                showError("Erreur lors du chargement de la page d'accueil.");
            }
        }
    }

    @FXML
    private void handleInscription() {
        try {
            MainSceneController.getInstance().loadView("register");
            MainSceneController.getInstance().setWindowTitle("GESTION KAYPLAY - Inscription");
        } catch (Exception e) {
            e.printStackTrace();
            showError("Erreur lors du chargement de la page d'inscription.");
        }
    }

    private void showError(String message) {
        lblError.setText(message);
        lblError.setVisible(true);
    }
}