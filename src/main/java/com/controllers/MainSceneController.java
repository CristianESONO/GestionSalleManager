package com.controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.stage.Stage;
import javafx.scene.control.Alert.AlertType;

import java.io.IOException;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

import com.App;
import com.core.Fabrique; // Pour obtenir l'utilisateur courant
import com.entities.Role; // Pour vérifier le rôle de l'utilisateur
import com.entities.User;

public class MainSceneController implements Initializable {

    // FXML IDs pour la barre de titre
    @FXML private Button btnMinimize;
    @FXML private Button btnMaximize;
    @FXML private Button btnClose;
    @FXML private HBox titleBar;
    @FXML private Region titleBarDragRegion;
    @FXML private Label windowTitleLabel;

    // FXML IDs pour les éléments du menu
    @FXML private Label lblUserRole;
    @FXML private Button btnUtilisateurs;
    @FXML private Button btnPosteJeu;
    @FXML private Button btnPostes;
    @FXML private Button btnJeux;
    @FXML private Button btnReservations;
    @FXML private Button btnProduits;
    @FXML private Button btnFinances;
    @FXML private Button btnPromotions;
    @FXML private Button btnDeconnexion;

    @FXML private AnchorPane contentPane; // Le panneau où les vues seront chargées

    private double xOffset = 0;
    private double yOffset = 0;
    private boolean isMaximized = false;

    private static MainSceneController instance;

    public MainSceneController() {
        instance = this;
    }

    public static MainSceneController getInstance() {
        return instance;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Initialisation de la barre de titre
        if (titleBarDragRegion != null) {
            titleBarDragRegion.setOnMousePressed(event -> {
                xOffset = event.getSceneX();
                yOffset = event.getSceneY();
            });

            titleBarDragRegion.setOnMouseDragged(event -> {
                if (!isMaximized) {
                    Stage stage = (Stage) ((Region)event.getSource()).getScene().getWindow();
                    stage.setX(event.getScreenX() - xOffset);
                    stage.setY(event.getScreenY() - yOffset);
                }
            });

            titleBarDragRegion.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2) {
                    handleMaximizeRestore();
                }
            });
        }
        if (windowTitleLabel != null) {
            windowTitleLabel.setText("GESTION KAYPLAY");
        }
        
        // AU DÉMARRAGE DE L'APP, LE MENU EST MASQUÉ PAR DÉFAUT
        // La visibilité sera gérée par loadView() après le chargement de la première vue.
        setMenuVisibility(false); 
    }

    /**
     * Met à jour la visibilité des éléments du menu en fonction de l'utilisateur connecté.
     * @param user L'utilisateur actuellement connecté.
     */
    public void updateMenuForUser(User user) {
        boolean isLoggedIn = (user != null);
        boolean isSuperAdmin = isLoggedIn && user.getRole() == Role.SuperAdmin;
        
        // Rend les éléments du menu visibles si l'utilisateur est connecté et gère la logique de rôle.
        setMenuVisibility(isLoggedIn); 

        if (isLoggedIn) {
            if (lblUserRole != null) {
                lblUserRole.setText("(" + user.getRole().name() + ")");
            }

            // Gérer la visibilité des boutons en fonction du rôle
            if (btnUtilisateurs != null) {
                btnUtilisateurs.setVisible(true); // Ou selon la logique de rôle spécifique
                btnUtilisateurs.setManaged(true); 
            }
            
            if (btnPosteJeu != null) {
                btnPosteJeu.setVisible(!isSuperAdmin);
                btnPosteJeu.setManaged(!isSuperAdmin);
            }

            if (btnPostes != null) {
                btnPostes.setVisible(isSuperAdmin);
                btnPostes.setManaged(isSuperAdmin);
            }

            if (btnJeux != null) {
                btnJeux.setVisible(isSuperAdmin);
                btnJeux.setManaged(isSuperAdmin);
            }

            if (btnReservations != null) {
                btnReservations.setVisible(!isSuperAdmin);
                btnReservations.setManaged(!isSuperAdmin);
            }

            if (btnProduits != null) {
                btnProduits.setVisible(true); 
                btnProduits.setManaged(true);
            }
            if (btnFinances != null) {
                btnFinances.setVisible(true); 
                btnFinances.setManaged(true);
            }

            if (btnPromotions != null) {
                btnPromotions.setVisible(isSuperAdmin);
                btnPromotions.setManaged(isSuperAdmin);
            }
            
        } else {
            // Si l'utilisateur n'est pas loggé (user est null), on s'assure que tout le menu est caché
            if (lblUserRole != null) {
                lblUserRole.setText(""); 
            }
            setMenuVisibility(false); // Cache tous les boutons du menu
        }
    }
    
    /**
     * Contrôle la visibilité et la gestion de tous les boutons et éléments du menu latéral.
     * Inclut des vérifications null pour éviter les erreurs si un fx:id est manquant dans le FXML.
     * @param visible true pour rendre les éléments visibles et gérés, false sinon.
     */
    public void setMenuVisibility(boolean visible) {
        if (lblUserRole != null) lblUserRole.setVisible(visible);
        if (btnUtilisateurs != null) { btnUtilisateurs.setManaged(visible); btnUtilisateurs.setVisible(visible); }
        if (btnPosteJeu != null) { btnPosteJeu.setManaged(visible); btnPosteJeu.setVisible(visible); }
        if (btnPostes != null) { btnPostes.setManaged(visible); btnPostes.setVisible(visible); }
        if (btnJeux != null) { btnJeux.setManaged(visible); btnJeux.setVisible(visible); }
        if (btnReservations != null) { btnReservations.setManaged(visible); btnReservations.setVisible(visible); }
        if (btnProduits != null) { btnProduits.setManaged(visible); btnProduits.setVisible(visible); }
        if (btnFinances != null) { btnFinances.setManaged(visible); btnFinances.setVisible(visible); }
        if (btnPromotions != null) { btnPromotions.setManaged(visible); btnPromotions.setVisible(visible); }
        if (btnDeconnexion != null) { btnDeconnexion.setManaged(visible); btnDeconnexion.setVisible(visible); }
    }


    // Méthodes de contrôle de la fenêtre (inchangées)
    @FXML private void handleMinimize() {
        Stage stage = (Stage) btnMinimize.getScene().getWindow();
        stage.setIconified(true);
    }

    @FXML private void handleMaximizeRestore() {
        Stage stage = (Stage) btnMaximize.getScene().getWindow();
        if (stage.isFullScreen()) {
            stage.setFullScreen(false);
            isMaximized = false;
            btnMaximize.setText("☐");
        } else if (stage.isMaximized()) {
            stage.setMaximized(false);
            isMaximized = false;
            btnMaximize.setText("☐");
        } else {
            stage.setMaximized(true);
            isMaximized = true;
            btnMaximize.setText("❐");
        }
    }

    @FXML private void handleClose() {
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle("Confirmation de fermeture");
        alert.setHeaderText(null);
        alert.setContentText("Êtes-vous sûr de vouloir quitter l'application ?");
        Optional<ButtonType> option = alert.showAndWait();

        if (option.isPresent() && option.get().equals(ButtonType.OK)) {
            Stage stage = (Stage) btnClose.getScene().getWindow();
            stage.close();
            Platform.exit();
            System.exit(0);
        }
    }

    /**
     * Charge une vue FXML donnée dans le contentPane.
     * @param fxmlName Le nom du fichier FXML (sans l'extension .fxml)
     */
    public void loadView(String fxmlName) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/views/" + fxmlName + ".fxml"));
            Parent view = loader.load();
            
            contentPane.getChildren().clear();
            contentPane.getChildren().add(view);
            AnchorPane.setTopAnchor(view, 0.0);
            AnchorPane.setBottomAnchor(view, 0.0);
            AnchorPane.setLeftAnchor(view, 0.0);
            AnchorPane.setRightAnchor(view, 0.0);

            // LOGIQUE CLÉ : Masquer le menu si c'est la page de connexion, d'inscription ou de chargement
            if (fxmlName.equals("connexion") || fxmlName.equals("register") || fxmlName.equals("LoadingScreen")) { // <-- MODIFICATION ICI
                setMenuVisibility(false); // Cache le menu pour ces pages
                if (lblUserRole != null) lblUserRole.setText(""); // Efface le rôle affiché
                AnchorPane.setLeftAnchor(contentPane, 0.0); // La vue prend toute la largeur
            } else {
                // Pour les autres vues (qui nécessitent une connexion), on affiche et met à jour le menu
                AnchorPane.setLeftAnchor(contentPane, 220.0); // La vue prend la place à droite du menu
                updateMenuForUser(Fabrique.getService().getCurrentUser());
            }

        } catch (IOException e) {
            e.printStackTrace();
            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle("Erreur de chargement de vue");
            alert.setHeaderText("Impossible de charger la vue");
            alert.setContentText("Une erreur est survenue lors du chargement de " + fxmlName + ".fxml.\nVérifiez le nom du fichier et son chemin.");
            alert.showAndWait();
        }
    }

    /**
     * Méthode pour changer le titre affiché dans la barre de titre personnalisée.
     * Peut être appelée depuis d'autres contrôleurs.
     * @param title Le nouveau titre.
     */
    public void setWindowTitle(String title) {
        if (windowTitleLabel != null) {
            windowTitleLabel.setText(title);
        }
    }
    
    // --- Actions des boutons du menu (inchangées) ---
    @FXML public void handleLoadViewUtilisateurs() { loadView("listuser"); setWindowTitle("GESTION KAYPLAY - Utilisateurs"); }
    @FXML public void handleLoadViewPostesJeu() { loadView("PosteJeuView"); setWindowTitle("GESTION KAYPLAY - Postes & Jeux"); }
    @FXML public void handleLoadViewPostes() { loadView("postes"); setWindowTitle("GESTION KAYPLAY - Postes"); }
    @FXML public void handleLoadViewJeux() { loadView("jeux"); setWindowTitle("GESTION KAYPLAY - Jeux"); }
    @FXML public void handleLoadViewReservations() { loadView("reservations"); setWindowTitle("GESTION KAYPLAY - Réservations"); }
    @FXML public void handleLoadViewProduits() { loadView("produits"); setWindowTitle("GESTION KAYPLAY - Produits"); }
    @FXML public void handleLoadViewFinances() { loadView("finances"); setWindowTitle("GESTION KAYPLAY - Finances"); }
    @FXML public void handleLoadViewSession() { loadView("GameSession"); setWindowTitle("GESTION KAYPLAY - Sessions de Jeu"); }
    @FXML public void handleLoadViewPromotions() { loadView("promo"); setWindowTitle("GESTION KAYPLAY - Promotions"); }

    @FXML
    public void handleDeconnexion() {
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText(null);
        alert.setContentText("Êtes-vous sûr de vouloir vous déconnecter ?");
        Optional<ButtonType> option = alert.showAndWait();

        if (option.isPresent() && option.get().equals(ButtonType.OK)) {
            Fabrique.getService().setCurrentUser(null); // Déconnexion de l'utilisateur
            loadView("connexion"); // Revenir à la page de connexion
            setWindowTitle("GESTION KAYPLAY - Connexion");
        }
    }
}