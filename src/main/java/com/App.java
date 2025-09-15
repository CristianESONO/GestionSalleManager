package com;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import java.io.IOException;
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

        mainSceneController.loadView("LoadingScreen");

        new Thread(() -> {
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            checkForUpdates(); // Appelle checkForUpdates() sans Platform.runLater()
        }).start();
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

    private static void checkForUpdates() {
        String currentVersion = "1.0.0";
        String lastVersion = VersionChecker.getLastVersionFromServer();
        System.out.println("Dernière version récupérée : " + lastVersion); // Log pour débogage

        if (lastVersion != null && !lastVersion.equals(currentVersion)) {
            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Mise à jour disponible");
                alert.setHeaderText("Une nouvelle version est disponible !");
                alert.setContentText("Version actuelle : " + currentVersion + "\nDernière version : " + lastVersion);
                ButtonType buttonOui = new ButtonType("Oui");
                ButtonType buttonNon = new ButtonType("Non");
                alert.getButtonTypes().setAll(buttonOui, buttonNon);
                alert.showAndWait().ifPresent(response -> {
                    if (response == buttonOui) {
                        UpdateDownloader.downloadAndInstallUpdate(lastVersion);
                    } else {
                        mainSceneController.loadView("connexion");
                        mainStage.centerOnScreen();
                    }
                });
            });
        } else {
            mainSceneController.loadView("connexion");
            mainStage.centerOnScreen();
        }
    }

    public static void main(String[] args) {
        launch();
    }
}
