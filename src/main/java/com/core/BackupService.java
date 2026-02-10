package com.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Sauvegardes automatiques et manuelles de la base SQLite, avec rétention limitée.
 */
public final class BackupService {

    private static final Logger log = LoggerFactory.getLogger(BackupService.class);
    private static final String BACKUP_PREFIX = "gestionsalles_";
    private static final String BACKUP_SUFFIX = ".sqlite";
    private static final DateTimeFormatter FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd_HHmm");

    private static volatile ScheduledExecutorService scheduler;
    private static volatile boolean autoBackupStarted;

    private BackupService() {}

    /** Répertoire des sauvegardes : config ou APPDATA/GestionSalles/backups. */
    public static Path getBackupDir() throws IOException {
        String dir = AppConfig.getBackupDir();
        if (dir != null && !dir.isEmpty()) {
            Path p = Paths.get(dir);
            Files.createDirectories(p);
            return p;
        }
        Path appDir = AppConfig.getExternalConfigPath().getParent();
        if (appDir == null) appDir = Paths.get(System.getProperty("user.home", ""), "GestionSalles");
        Path backupDir = appDir.resolve("backups");
        Files.createDirectories(backupDir);
        return backupDir;
    }

    /** Effectue une sauvegarde manuelle. Retourne le chemin du fichier créé ou null en cas d'erreur. */
    public static Path backupNow() {
        String sourcePath = JpaUtil.getDbFilePath();
        if (sourcePath == null || sourcePath.isEmpty()) {
            log.warn("Chemin de la base inconnu, sauvegarde ignorée");
            return null;
        }
        Path source = Paths.get(sourcePath);
        if (!Files.isRegularFile(source)) {
            log.warn("Fichier base introuvable: {}", sourcePath);
            return null;
        }
        try {
            Path backupDir = getBackupDir();
            String name = BACKUP_PREFIX + LocalDateTime.now().format(FORMAT) + BACKUP_SUFFIX;
            Path target = backupDir.resolve(name);
            Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
            log.info("Sauvegarde créée: {}", target);
            purgeOldBackups(backupDir);
            return target;
        } catch (IOException e) {
            log.error("Erreur lors de la sauvegarde", e);
            return null;
        }
    }

    /** Supprime les sauvegardes les plus anciennes au-delà du nombre max autorisé. */
    public static void purgeOldBackups(Path backupDir) throws IOException {
        int maxCount = AppConfig.getBackupMaxCount();
        List<Path> list = new ArrayList<>();
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(backupDir, BACKUP_PREFIX + "*" + BACKUP_SUFFIX)) {
            for (Path p : stream) list.add(p);
        }
        list.sort(Comparator.comparing(p -> {
            try { return Files.getLastModifiedTime(p); }
            catch (IOException e) { return java.nio.file.attribute.FileTime.fromMillis(0); }
        }, Comparator.reverseOrder()));
        if (list.size() <= maxCount) return;
        for (int i = maxCount; i < list.size(); i++) {
            Files.deleteIfExists(list.get(i));
            log.debug("Sauvegarde ancienne supprimée: {}", list.get(i));
        }
    }

    /** Démarre les sauvegardes automatiques (intervalle en minutes depuis la config). */
    public static synchronized void startAutoBackup() {
        if (autoBackupStarted) return;
        int intervalMin = AppConfig.getBackupIntervalMinutes();
        if (intervalMin <= 0) return;
        scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "BackupService");
            t.setDaemon(true);
            return t;
        });
        scheduler.scheduleAtFixedRate(() -> {
            try {
                backupNow();
            } catch (Exception e) {
                log.error("Sauvegarde automatique échouée", e);
            }
        }, intervalMin, intervalMin, TimeUnit.MINUTES);
        autoBackupStarted = true;
        log.info("Sauvegardes automatiques activées (toutes les {} min)", intervalMin);
    }

    /** Arrête les sauvegardes automatiques (à appeler en arrêt propre). */
    public static synchronized void stopAutoBackup() {
        if (scheduler != null) {
            scheduler.shutdown();
            try {
                if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) scheduler.shutdownNow();
            } catch (InterruptedException e) {
                scheduler.shutdownNow();
                Thread.currentThread().interrupt();
            }
            scheduler = null;
        }
        autoBackupStarted = false;
    }
}
