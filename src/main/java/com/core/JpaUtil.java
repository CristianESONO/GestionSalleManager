package com.core;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class JpaUtil {

    private static final String DB_FILENAME = "gestionsalles.sqlite";

    private static EntityManagerFactory entityManagerFactory;
    private static String DB_FILE_PATH = null;

    static {
        // En production : APPDATA/GestionSalles/gestionsalles.sqlite (ou équivalent selon l'OS)
        Path appDir = AppConfig.getExternalConfigPath() != null ? AppConfig.getExternalConfigPath().getParent() : null;
        if (appDir == null) {
            String home = System.getProperty("user.home", "");
            appDir = Paths.get(System.getenv("APPDATA") != null ? System.getenv("APPDATA") : home, "GestionSalles");
        }
        try {
            if (!Files.exists(appDir)) Files.createDirectories(appDir);
        } catch (Exception ignored) {}
        String productionPath = appDir.resolve(DB_FILENAME).toAbsolutePath().toString();

        String configuredPath = AppConfig.getDbPath();
        if (configuredPath != null && !configuredPath.trim().isEmpty()) {
            DB_FILE_PATH = configuredPath.trim();
        } else {
            DB_FILE_PATH = productionPath;
        }

        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Driver SQLite JDBC introuvable.", e);
        }
    }

    public static String getDbFilePath() {
        return DB_FILE_PATH;
    }

    public static void initEntityManagerFactory() {
        if (entityManagerFactory == null) {
            try {
                Map<String, String> properties = new HashMap<>();
                properties.put("javax.persistence.jdbc.url", "jdbc:sqlite:" + DB_FILE_PATH);
                entityManagerFactory = Persistence.createEntityManagerFactory("gestionSallesPU", properties);
            } catch (Exception e) {
                System.err.println("Erreur d'initialisation: " + e.getMessage());
                e.printStackTrace();
                System.exit(1);
            }
        }
    }

    public static EntityManager getEntityManager() {
        if (entityManagerFactory == null) {
            throw new IllegalStateException("EntityManagerFactory non initialisée.");
        }
        return entityManagerFactory.createEntityManager();
    }

    public static void closeEntityManagerFactory() {
        if (entityManagerFactory != null && entityManagerFactory.isOpen()) {
            entityManagerFactory.close();
            entityManagerFactory = null;
        }
    }
}