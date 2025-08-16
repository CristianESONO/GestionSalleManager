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

    private static EntityManagerFactory entityManagerFactory;
    private static String DB_FILE_PATH = null;

    static {
        String fileName = "gestionsalles.sqlite";

        
        // --- MODE PRODUCTION --- (désactivé)
         String appDataPath = System.getenv("APPDATA"); // Chemin vers AppData\Roaming
        if (appDataPath != null) {
            Path dbDir = Paths.get(appDataPath, "GestionSalles");
            try {
                // Crée le dossier s'il n'existe pas
                if (!Files.exists(dbDir)) {
                    Files.createDirectories(dbDir);
                }
                DB_FILE_PATH = Paths.get(dbDir.toString(), fileName).toString();
            } catch (Exception e) {
                System.err.println("Erreur lors de la création du dossier AppData : " + e.getMessage());
                DB_FILE_PATH = fileName; // Re repli sur le répertoire courant
            }
        } else {
            DB_FILE_PATH = fileName; // Repli sur le répertoire courant si APPDATA n'est pas défini
        }
      

        // --- MODE DEVELOPPEMENT ---
       /* DB_FILE_PATH = "C:\\Users\\HP\\Desktop\\GestionSalle\\demo\\gestionsalles.sqlite";
        System.out.println("Mode DEV : base de données dans " + DB_FILE_PATH);  */

        try {
            Class.forName("org.sqlite.JDBC");
            System.out.println("Driver SQLite JDBC chargé avec succès.");
        } catch (ClassNotFoundException e) {
            System.err.println("Erreur: Driver SQLite JDBC introuvable.");
            e.printStackTrace();
            System.exit(1);
        }
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
            System.out.println("Fermeture de l'EntityManagerFactory.");
            entityManagerFactory.close();
            entityManagerFactory = null;
        }
    }
}
