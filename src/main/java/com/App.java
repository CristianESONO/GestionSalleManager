package com;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;

import com.controllers.MainSceneController; // Importez le nouveau contrôleur
import com.core.JpaUtil; // Importez la classe JpaUtil

public class App extends Application {

    private static Scene scene;
    private static Stage mainStage;
    private static MainSceneController mainSceneController; // Pour accéder au contrôleur de la scène principale

    @Override
    public void start(Stage stage) throws IOException {
        // --- ÉTAPE 1 : Initialiser l'EntityManagerFactory de JPA au démarrage de l'application ---
        // C'est crucial pour que JPA soit prêt à l'emploi avant toute opération de base de données.
        JpaUtil.initEntityManagerFactory();

        mainStage = stage;
        mainStage.initStyle(StageStyle.UNDECORATED); // Toujours sans décorations natives

        // 1. Charger le FXML de base qui contient la barre de titre et le contentPane
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/views/MainSceneBase.fxml"));
        Parent root = loader.load();
        mainSceneController = loader.getController(); // Obtenir le contrôleur de la scène principale

        scene = new Scene(root);
        mainStage.setTitle("GESTION KAYPLAY"); // Titre de la fenêtre du système

        Image logo = new Image(getClass().getResourceAsStream("/com/img/71xzcr0FFvL.jpg"));
        mainStage.getIcons().add(logo);

        mainStage.setWidth(900); // Définissez une taille initiale pour la fenêtre principale
        mainStage.setHeight(600);

        scene.getStylesheets().add(getClass().getResource("/com/css/style.css").toExternalForm());

        mainStage.setScene(scene);
        mainStage.show();

        // 2. Charger IMMÉDIATEMENT le LoadingScreen dans le contentPane
        // La logique dans MainSceneController.loadView() masquera le menu
        mainSceneController.loadView("LoadingScreen"); // <-- NOUVEAU : Charge l'écran de chargement

        // 3. Lancer un nouveau thread pour le délai et le passage à la connexion
        new Thread(() -> {
            try {
                Thread.sleep(3000); // Délai de 3 secondes pour le LoadingScreen
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            Platform.runLater(() -> {
                // Changer la vue interne vers la connexion après le délai
                mainSceneController.loadView("connexion");
                mainStage.centerOnScreen();
            });
        }).start();
    }

    @Override
    public void stop() throws Exception {
        // --- ÉTAPE 2 : Fermer l'EntityManagerFactory de JPA à l'arrêt de l'application ---
        // C'est essentiel pour libérer toutes les ressources de la base de données,
        // y compris le pool de connexions d'Hibernate.
        JpaUtil.closeEntityManagerFactory();

        super.stop(); // Appeler la méthode stop de la superclasse
    }

    // Cette méthode est maintenant appelée par les contrôleurs pour changer de vue
    public static void setRoot(String fxmlName) {
        if (mainSceneController != null) {
            mainSceneController.loadView(fxmlName);
        } else {
            System.err.println("Erreur: MainSceneController n'est pas initialisé.");
        }
    }

    public static void main(String[] args) {
        launch();
    }
}
