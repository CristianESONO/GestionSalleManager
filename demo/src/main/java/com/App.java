package com;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;

public class App extends Application {

    private static Scene scene;

    @Override
    public void start(Stage stage) throws IOException {
        // Charger l'écran de chargement
        scene = new Scene(loadFXML("LoadingScreen")); // Charger LoadingScreen.fxml
        stage.setTitle("Gestion | Salle de Jeux");

        // Charger l'icône de la fenêtre
        Image logo = new Image(getClass().getResourceAsStream("/com/img/71xzcr0FFvL.jpg"));
        stage.getIcons().add(logo);

        // Définir la taille de la fenêtre pour qu'elle soit comme un téléphone
        stage.setWidth(450);  // largeur pour la page connexion
        stage.setHeight(640); // hauteur pour la page connexion

        // Configurer la scène et afficher la fenêtre
        stage.setScene(scene);
        stage.show();

        // Simuler un temps de chargement (3 secondes)
        new Thread(() -> {
            try {
                Thread.sleep(3000); // Simuler un chargement de 3 secondes
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            // Passer à la vue de connexion après le chargement
            Platform.runLater(() -> {
                try {
                    setRoot("connexion", 450, 640, scene); // Passer la scène actuelle
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }).start();
    }

    public static void setRoot(String fxml, double width, double height, Scene currentScene) throws IOException {
        if (currentScene == null) {
            throw new IllegalStateException("La scène n'a pas été initialisée.");
        }
        Parent root = loadFXML(fxml);
        currentScene.setRoot(root);
    
        Stage stage = (Stage) currentScene.getWindow();
        if (stage == null) {
            throw new IllegalStateException("La scène n'est pas associée à une fenêtre.");
        }
    
        stage.setWidth(width);
        stage.setHeight(height);
    }

    public static Parent loadFXML(String fxml) throws IOException {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("/com/views/" + fxml + ".fxml"));
            return fxmlLoader.load();
        } catch (IOException e) {
            System.err.println("Erreur : Le fichier FXML '" + fxml + "' est introuvable.");
            throw e;
        }
    }

    public static void main(String[] args) {
        launch();
    }
}