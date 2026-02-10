package com.core;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

/**
 * Configuration centralisée : base de données, version, chemins.
 * Charge d'abord un fichier externe (à côté du JAR ou dans APPDATA/GestionSalles),
 * puis complète avec les ressources du classpath (/com/config.properties).
 */
public final class AppConfig {

    private static final String CONFIG_RESOURCE = "/com/config.properties";
    private static final String APP_NAME = "GestionSalles";
    private static final String CONFIG_FILENAME = "config.properties";

    private static Properties props;
    private static volatile boolean loaded;

    private AppConfig() {}

    private static synchronized void load() {
        if (loaded) return;
        props = new Properties();
        // Valeurs par défaut (dev)
        props.setProperty("db.path", "");
        props.setProperty("current_version", "1.3.3");
        props.setProperty("backup.dir", "");
        props.setProperty("backup.maxCount", "30");
        props.setProperty("backup.intervalMinutes", "60");
        props.setProperty("login.maxAttempts", "5");
        props.setProperty("login.lockMinutes", "15");
        props.setProperty("password.minLength", "8");

        // 1. Charger depuis le classpath (ressource dans le JAR)
        try (InputStream in = AppConfig.class.getResourceAsStream(CONFIG_RESOURCE)) {
            if (in != null) {
                props.load(in);
            }
        } catch (IOException e) {
            // Ignore, garde les défauts
        }

        // 2. Écraser avec un fichier externe s'il existe
        Path externalPath = getExternalConfigPath();
        if (externalPath != null && Files.isRegularFile(externalPath)) {
            try (InputStream in = Files.newInputStream(externalPath)) {
                props.load(in);
            } catch (IOException e) {
                // Ignore
            }
        }
        loaded = true;
    }

    /** Chemin du fichier config externe : répertoire du JAR ou APPDATA/GestionSalles. */
    public static Path getExternalConfigPath() {
        String appData = System.getenv("APPDATA");
        if (appData == null) appData = System.getenv("HOME");
        if (appData == null) appData = System.getProperty("user.home", "");
        Path appDir = Paths.get(appData, APP_NAME);
        try {
            if (!Files.exists(appDir)) Files.createDirectories(appDir);
        } catch (IOException ignored) {}
        return appDir.resolve(CONFIG_FILENAME);
    }

    public static String get(String key) {
        if (!loaded) load();
        return props.getProperty(key, "").trim();
    }

    public static String get(String key, String defaultValue) {
        String v = get(key);
        return v.isEmpty() ? defaultValue : v;
    }

    public static int getInt(String key, int defaultValue) {
        String v = get(key);
        if (v.isEmpty()) return defaultValue;
        try {
            return Integer.parseInt(v);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public static String getDbPath() {
        return get("db.path");
    }

    public static String getCurrentVersion() {
        return get("current_version", "1.3.3");
    }

    /** Répertoire des sauvegardes (vide = déduire depuis APPDATA/GestionSalles/backups). */
    public static String getBackupDir() {
        return get("backup.dir");
    }

    public static int getBackupMaxCount() {
        return getInt("backup.maxCount", 30);
    }

    public static int getBackupIntervalMinutes() {
        return getInt("backup.intervalMinutes", 60);
    }

    public static int getLoginMaxAttempts() {
        return getInt("login.maxAttempts", 5);
    }

    public static int getLoginLockMinutes() {
        return getInt("login.lockMinutes", 15);
    }

    public static int getPasswordMinLength() {
        return getInt("password.minLength", 8);
    }

    /**
     * Met à jour la version courante dans la config externe (ex. après une mise à jour).
     * Met aussi à jour le cache en mémoire.
     */
    public static void setCurrentVersion(String version) {
        if (version == null || version.trim().isEmpty()) return;
        if (!loaded) load();
        props.setProperty("current_version", version.trim());
        Path externalPath = getExternalConfigPath();
        if (externalPath == null) return;
        try {
            if (Files.exists(externalPath)) {
                try (InputStream in = Files.newInputStream(externalPath)) {
                    Properties ext = new Properties();
                    ext.load(in);
                    ext.setProperty("current_version", version.trim());
                    try (OutputStream out = Files.newOutputStream(externalPath)) {
                        ext.store(out, "GestionSalles - version mise à jour");
                    }
                }
            } else {
                try (OutputStream out = Files.newOutputStream(externalPath)) {
                    props.store(out, "GestionSalles");
                }
            }
        } catch (IOException ignored) {}
    }

    /** Chemin du fichier de verrouillage utilisé après une mise à jour (même répertoire que la config). */
    public static Path getUpdateLockPath() {
        Path configPath = getExternalConfigPath();
        return configPath != null ? configPath.getParent().resolve("update_lock.txt") : null;
    }
}
