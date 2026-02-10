package com.update;

import com.core.AppConfig;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.StageStyle;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Télécharge et installe les mises à jour avec une barre de progression et possibilité d'annuler.
 */
public final class UpdateDownloader {

    private static final String DOWNLOAD_URL_TEMPLATE = "https://github.com/CristianESONO/GestionSalleManager/releases/download/v%s/GestionSalle_Setup_%s.exe";
    private static final int BUFFER_SIZE = 32_768;

    private UpdateDownloader() {}

    /**
     * Lance le téléchargement puis l'installation de la version donnée.
     * Affiche une fenêtre avec barre de progression et bouton Annuler.
     */
    public static void downloadAndInstallUpdate(String version) {
        String downloadUrl = String.format(DOWNLOAD_URL_TEMPLATE, version, version);
        Path appDir = AppConfig.getExternalConfigPath() != null ? AppConfig.getExternalConfigPath().getParent() : null;
        String destDir = appDir != null ? appDir.toString() : System.getProperty("user.home");
        String destinationPath = destDir + File.separator + "GestionSalle_Setup_" + version + ".exe";

        AtomicBoolean cancelled = new AtomicBoolean(false);
        Dialog<Void> progressDialog = new Dialog<>();
        progressDialog.initStyle(StageStyle.UTILITY);
        progressDialog.setTitle("Mise à jour");
        progressDialog.setHeaderText("Téléchargement de la version " + version + "...");
        progressDialog.getDialogPane().getButtonTypes().add(ButtonType.CANCEL);
        progressDialog.setResizable(false);

        ProgressBar progressBar = new ProgressBar(0);
        progressBar.setPrefWidth(360);
        Label statusLabel = new Label("Connexion...");
        VBox box = new VBox(12, progressBar, statusLabel);
        box.setStyle("-fx-padding: 16;");
        progressDialog.getDialogPane().setContent(box);

        progressDialog.setOnCloseRequest(e -> cancelled.set(true));

        Button cancelBtn = (Button) progressDialog.getDialogPane().lookupButton(ButtonType.CANCEL);
        if (cancelBtn != null) {
            cancelBtn.setOnAction(e -> {
                cancelled.set(true);
                progressDialog.close();
            });
        }

        progressDialog.show();

        new Thread(() -> {
            try {
                downloadUpdateWithProgress(downloadUrl, destinationPath, cancelled, (progress, message) -> {
                    Platform.runLater(() -> {
                        progressBar.setProgress(progress < 0 ? ProgressBar.INDETERMINATE_PROGRESS : progress);
                        statusLabel.setText(message);
                    });
                });
                if (cancelled.get()) {
                    Platform.runLater(() -> {
                        progressDialog.close();
                        new File(destinationPath).delete();
                    });
                    return;
                }
                Platform.runLater(() -> {
                    progressDialog.close();
                    try {
                        installUpdate(destinationPath, version);
                    } catch (Exception ex) {
                        new Alert(Alert.AlertType.ERROR, "Échec de l'installation : " + ex.getMessage()).showAndWait();
                    }
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    progressDialog.close();
                    new Alert(Alert.AlertType.ERROR, "Échec du téléchargement : " + e.getMessage()).showAndWait();
                });
            }
        }).start();
    }

    @FunctionalInterface
    public interface ProgressCallback {
        void onProgress(double progress, String message);
    }

    private static void downloadUpdateWithProgress(String downloadUrl, String destinationPath,
                                                   AtomicBoolean cancelled, ProgressCallback callback) throws Exception {
        URL url = new URL(downloadUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestProperty("User-Agent", "GestionSalles-Updater/1.0");
        connection.setConnectTimeout(8000);
        connection.setReadTimeout(10000);

        int responseCode = connection.getResponseCode();
        if (responseCode != HttpURLConnection.HTTP_OK) {
            throw new Exception("Serveur indisponible (HTTP " + responseCode + ")");
        }

        int totalSize = connection.getContentLength();
        boolean lengthKnown = totalSize > 0;

        try (InputStream in = connection.getInputStream();
             FileOutputStream fos = new FileOutputStream(destinationPath)) {
            byte[] buffer = new byte[BUFFER_SIZE];
            int bytesRead;
            long totalRead = 0;

            while (!cancelled.get() && (bytesRead = in.read(buffer)) != -1) {
                fos.write(buffer, 0, bytesRead);
                totalRead += bytesRead;
                if (lengthKnown && totalSize > 0) {
                    double p = (double) totalRead / totalSize;
                    String msg = String.format("%.0f %% — %s / %s",
                            p * 100,
                            formatSize(totalRead),
                            formatSize(totalSize));
                    callback.onProgress(p, msg);
                } else {
                    callback.onProgress(-1, formatSize(totalRead) + " téléchargés...");
                }
            }
            if (cancelled.get()) throw new InterruptedException("Annulé");
            if (totalRead == 0) throw new Exception("Aucune donnée reçue");
        } finally {
            connection.disconnect();
        }
    }

    private static String formatSize(long bytes) {
        if (bytes < 1024) return bytes + " o";
        if (bytes < 1024 * 1024) return String.format("%.1f Ko", bytes / 1024.0);
        return String.format("%.1f Mo", bytes / (1024.0 * 1024.0));
    }

    private static void installUpdate(String setupPath, String newVersion) throws Exception {
        Path lockPath = AppConfig.getUpdateLockPath();
        if (lockPath != null) {
            Files.createDirectories(lockPath.getParent());
            Files.write(lockPath, "update_installed".getBytes());
        }

        AppConfig.setCurrentVersion(newVersion);

        ProcessBuilder pb = new ProcessBuilder(setupPath, "/SILENT", "/NORESTART");
        pb.inheritIO();
        pb.start();

        System.exit(0);
    }
}
