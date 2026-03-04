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
import java.util.Optional;

import com.controllers.MainSceneController;
import com.core.AppConfig;
import com.core.BackupService;
import com.core.JpaUtil;
import com.update.UpdateDownloader;
import com.update.VersionChecker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class App extends Application {
    private static final Logger log = LoggerFactory.getLogger(App.class);
    private static Scene scene;
    private static Stage mainStage;
    private static MainSceneController mainSceneController;

    /** Délai minimum d'affichage de l'écran de chargement (ms) pour un rendu fluide */
    private static final long LOADING_MIN_DISPLAY_MS = 1_200;

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

        BackupService.startAutoBackup();

        java.nio.file.Path lockPath = AppConfig.getUpdateLockPath();
        if (lockPath != null && java.nio.file.Files.exists(lockPath)) {
            try {
                java.nio.file.Files.deleteIfExists(lockPath);
            } catch (IOException ignored) {}
            mainSceneController.loadView("connexion");
            mainStage.centerOnScreen();
            return;
        }

        mainSceneController.loadView("LoadingScreen");
        long startTime = System.currentTimeMillis();

        new Thread(() -> {
            try {
                String lastVersion = VersionChecker.getLastVersionFromServer();
                long elapsed = System.currentTimeMillis() - startTime;
                long remaining = Math.max(0, LOADING_MIN_DISPLAY_MS - elapsed);
                if (remaining > 0) {
                    Thread.sleep(remaining);
                }
                final String latest = lastVersion;
                Platform.runLater(() -> onVersionCheckDone(latest, false));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.warn("Vérification MAJ interrompue");
                Platform.runLater(() -> {
                    mainSceneController.loadView("connexion");
                    mainStage.centerOnScreen();
                });
            }
        }).start();
    }

    /**
     * Appelé après la vérification de version (démarrage ou manuel).
     * @param lastVersion dernière version serveur, ou null si indisponible
     * @param fromMenu true si l'utilisateur a cliqué sur "Vérifier les mises à jour"
     */
    private static void onVersionCheckDone(String lastVersion, boolean fromMenu) {
        String currentVersion = AppConfig.getCurrentVersion();
        if (lastVersion != null && !lastVersion.equals(currentVersion)) {
            showUpdateDialog(currentVersion, lastVersion, fromMenu);
        } else {
            if (fromMenu) {
                Alert upToDate = new Alert(Alert.AlertType.INFORMATION);
                upToDate.setTitle("Mise à jour");
                upToDate.setHeaderText("Vous êtes à jour");
                upToDate.setContentText("Version actuelle : " + currentVersion);
                upToDate.showAndWait();
            } else {
                mainSceneController.loadView("connexion");
                mainStage.centerOnScreen();
            }
        }
    }

    private static void showUpdateDialog(String currentVersion, String lastVersion, boolean fromMenu) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Mise à jour disponible");
        alert.setHeaderText("Une nouvelle version est disponible.");
        alert.setContentText("Version actuelle : " + currentVersion + "\nNouvelle version : " + lastVersion + "\n\nSouhaitez-vous mettre à jour maintenant ?");
        ButtonType updateNow = new ButtonType("Mettre à jour maintenant");
        ButtonType later = new ButtonType("Plus tard");
        alert.getButtonTypes().setAll(updateNow, later);
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == updateNow) {
            UpdateDownloader.downloadAndInstallUpdate(lastVersion);
        } else {
            if (!fromMenu) {
                mainSceneController.loadView("connexion");
                mainStage.centerOnScreen();
            }
        }
    }

    /**
     * Vérification manuelle des mises à jour (depuis le menu).
     * Affiche "Vous êtes à jour" ou la boîte de mise à jour.
     */
    public static void checkForUpdatesManually() {
        new Thread(() -> {
            String lastVersion = VersionChecker.getLastVersionFromServer();
            Platform.runLater(() -> onVersionCheckDone(lastVersion, true));
        }).start();
    }

    @Override
    public void stop() throws Exception {
        BackupService.stopAutoBackup();
        JpaUtil.closeEntityManagerFactory();
        super.stop();
    }

    public static void setRoot(String fxmlName) {
        if (mainSceneController != null) {
            mainSceneController.loadView(fxmlName);
        } else {
            log.error("MainSceneController non initialisé");
        }
    }

    public static void main(String[] args) {
        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
            log.error("Exception non gérée dans {}", thread.getName(), throwable);
            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Erreur");
                alert.setHeaderText("Une erreur inattendue s'est produite.");
                String msg = throwable.getMessage();
                alert.setContentText(msg != null && !msg.isEmpty() ? msg : throwable.getClass().getSimpleName());
                alert.showAndWait();
            });
        });
        launch(args);
    }
}
