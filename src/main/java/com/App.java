package com;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import com.controllers.MainSceneController;
import com.core.JpaUtil;
import com.update.UpdateDownloader;
import com.update.VersionChecker;

public class App extends Application {
    private static Scene scene;
    private static Stage mainStage;
    private static MainSceneController mainSceneController;

   @Override
    public void start(Stage stage) throws IOException {
        // 1. Vérifier si un fichier de verrouillage existe (indiquant qu'une mise à jour a été installée)
        
        String lockFilePath = System.getProperty("user.home") + "/Desktop/update_lock.txt";
        java.nio.file.Path lockFile = java.nio.file.Paths.get(lockFilePath);
        

        // 2. Initialiser la fenêtre principale
        JpaUtil.initEntityManagerFactory();
        mainStage = stage;
        mainStage.initStyle(StageStyle.UNDECORATED);
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/views/MainSceneBase.fxml"));
        Parent root = loader.load();
        mainSceneController = loader.getController();
        scene = new Scene(root);
        mainStage.setTitle("GESTION KAYPLAY");
        Image logo = new Image(getClass().getResourceAsStream("/com/img/71xzcr0FFvL.jpg"));
        mainStage.getIcons().add(logo);
        mainStage.setWidth(900);
        mainStage.setHeight(600);
        scene.getStylesheets().add(getClass().getResource("/com/css/style.css").toExternalForm());
        mainStage.setScene(scene);
        mainStage.show();

        // 3. Si un fichier de verrouillage existe, le supprimer et charger directement l'écran de connexion
        
        if (java.nio.file.Files.exists(lockFile)) {
            java.nio.file.Files.delete(lockFile); // Supprimer le fichier de verrouillage
            mainSceneController.loadView("connexion");
            mainStage.centerOnScreen();
        }
        // 4. Sinon, afficher l'écran de chargement et vérifier les mises à jour
        else {
            mainSceneController.loadView("LoadingScreen");
            new Thread(() -> {
                try {
                    Thread.sleep(3000);
                    checkForUpdates();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();
        }
        
        
        // Chargement direct de l'écran de connexion (sans vérification de mise à jour)
        mainSceneController.loadView("connexion");
        mainStage.centerOnScreen();
    }

    @Override
    public void stop() throws Exception {
        JpaUtil.closeEntityManagerFactory();
        super.stop();
    }

    public static void setRoot(String fxmlName) {
        if (mainSceneController != null) {
            mainSceneController.loadView(fxmlName);
        } else {
            System.err.println("Erreur: MainSceneController n'est pas initialisé.");
        }
    }


    private static String getCurrentVersion() {
        try (InputStream input = App.class.getResourceAsStream("/com/config.properties")) {
            Properties prop = new Properties();
            prop.load(input);
            return prop.getProperty("current_version", "1.0.2"); // "1.0.2" est la valeur par défaut
        } catch (Exception e) {
            System.err.println("Erreur lors de la lecture de la version actuelle : " + e.getMessage());
            return "1.0.2"; // Valeur par défaut en cas d'erreur
        }
    }

    
    private static void checkForUpdates() {
    String currentVersion = getCurrentVersion(); // Lire la version actuelle depuis config.properties
    String lastVersion = VersionChecker.getLastVersionFromServer();
    System.out.println("Dernière version récupérée : " + lastVersion);
    if (lastVersion != null && !lastVersion.equals(currentVersion)) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Mise à jour disponible");
            alert.setHeaderText("Une nouvelle version est disponible.");
            alert.setContentText("Version actuelle : " + currentVersion + "\nDernière version : " + lastVersion);
            alert.showAndWait();
            // Télécharger et installer la mise à jour
            UpdateDownloader.downloadAndInstallUpdate(lastVersion);
        });
    } else {
        // Si aucune mise à jour n'est nécessaire, charger l'écran de connexion
        Platform.runLater(() -> {
            mainSceneController.loadView("connexion");
            mainStage.centerOnScreen();
        });
    }
}

    

    public static void main(String[] args) {
        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
            System.err.println("Exception non attrapée dans " + thread.getName());
            throwable.printStackTrace();
        });
        
        launch();
    }
}