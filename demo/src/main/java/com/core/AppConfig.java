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

    /** Clés copiées du JAR vers la config externe (même poste, même déploiement). */
    private static final String[] PAYMENT_QR_SYNC_KEYS = {
            "payment.qr.api.baseUrl",
            "payment.qr.api.path",
            "payment.qr.api.key",
            "payment.qr.wave.staticPayload",
            "payment.qr.orange.staticPayload"
    };

    private AppConfig() {}

    private static synchronized void load() {
        if (loaded) return;
        props = new Properties();
        // Valeurs par défaut (dev)
        props.setProperty("db.path", "");
        props.setProperty("current_version", "1.3.8");
        props.setProperty("backup.dir", "");
        props.setProperty("backup.maxCount", "30");
        props.setProperty("backup.intervalMinutes", "60");
        props.setProperty("login.maxAttempts", "5");
        props.setProperty("login.lockMinutes", "15");
        props.setProperty("password.minLength", "8");
        props.setProperty("tv.enabled", "false");
        props.setProperty("tv.api.baseUrl", "");
        props.setProperty("tv.api.key", "");

        // Paiement mobile (JavaFX uniquement) : QR affiché au caissier ; clés Wave/OM restent sur votre PHP
        props.setProperty("payment.qr.api.baseUrl", "");
        props.setProperty("payment.qr.api.path", "/api/desktop-payment-qr.php");
        props.setProperty("payment.qr.api.key", "");
        props.setProperty("payment.qr.wave.staticPayload", "");
        props.setProperty("payment.qr.orange.staticPayload", "");

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

    /**
     * Recopie les paramètres paiement QR du fichier embarqué ({@value #CONFIG_RESOURCE}) vers
     * {@code %APPDATA%/GestionSalles/config.properties} lorsqu'ils sont non vides dans le JAR.
     * Ainsi une nouvelle version (ou une mise à jour) applique URL / clé sans intervention sur chaque PC.
     * Les autres clés du fichier externe (ex. {@code db.path}) sont conservées.
     */
    public static synchronized void applyBundledPaymentQrDefaultsToExternalFile() {
        Properties bundled = new Properties();
        try (InputStream in = AppConfig.class.getResourceAsStream(CONFIG_RESOURCE)) {
            if (in != null) {
                bundled.load(in);
            }
        } catch (IOException ignored) {
            return;
        }

        Path extPath = getExternalConfigPath();
        if (extPath == null) {
            return;
        }
        try {
            Files.createDirectories(extPath.getParent());
        } catch (IOException ignored) {
            return;
        }

        Properties ext = new Properties();
        if (Files.isRegularFile(extPath)) {
            try (InputStream in = Files.newInputStream(extPath)) {
                ext.load(in);
            } catch (IOException ignored) {
                // repartir d'un fichier vide si illisible
            }
        }

        boolean changed = false;
        for (String key : PAYMENT_QR_SYNC_KEYS) {
            String v = bundled.getProperty(key);
            if (v == null) {
                continue;
            }
            v = v.trim();
            if (v.isEmpty()) {
                continue;
            }
            String prev = ext.getProperty(key, "");
            if (!v.equals(prev.trim())) {
                ext.setProperty(key, v);
                changed = true;
            }
        }

        if (!changed) {
            return;
        }

        try (OutputStream out = Files.newOutputStream(extPath)) {
            ext.store(out, "GestionSalles — paramètres paiement synchronisés depuis l'application");
        } catch (IOException ignored) {
            return;
        }

        if (loaded) {
            reload();
        }
    }

    private static synchronized void reload() {
        loaded = false;
        props = null;
        load();
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
        return get("current_version", "1.3.8");
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

    // --- Contrôle TV (style Switch SAB) ---
    public static boolean isTvControlEnabled() {
        return "true".equalsIgnoreCase(get("tv.enabled"));
    }

    public static String getTvApiBaseUrl() {
        return get("tv.api.baseUrl");
    }

    public static String getTvApiKey() {
        return get("tv.api.key");
    }

    /** Ex. https://www.kayplaygamingroom.com — sans slash final. Vide = pas d'appel API (QR statique ou message seul). */
    public static String getPaymentQrApiBaseUrl() {
        return get("payment.qr.api.baseUrl");
    }

    /** Chemin relatif, ex. /api/desktop-payment-qr.php */
    public static String getPaymentQrApiPath() {
        String p = get("payment.qr.api.path");
        return p.isEmpty() ? "/api/desktop-payment-qr.php" : p;
    }

    /** Clé partagée optionnelle (en-tête X-Desktop-Key) pour sécuriser votre endpoint PHP. */
    public static String getPaymentQrApiKey() {
        return get("payment.qr.api.key");
    }

    /** Texte ou URL encodé en QR si l'API n'est pas utilisée ou échoue (Wave). */
    public static String getPaymentQrWaveStaticPayload() {
        return get("payment.qr.wave.staticPayload");
    }

    /** Idem Orange Money. */
    public static String getPaymentQrOrangeStaticPayload() {
        return get("payment.qr.orange.staticPayload");
    }
}
