package com.core;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class JpaUtil {

    private static EntityManagerFactory entityManagerFactory;
    private static String DB_FILE_PATH = null;

    static {
        String fileName = "gestionsalles.sqlite";

        
        // --- MODE PRODUCTION --- (désactivé)
        String programFilesX86 = System.getenv("ProgramFiles(x86)");
        String programFiles64 = System.getenv("ProgramW6432");

        String foundPath = null;

        if (programFilesX86 != null) {
            String path1 = Paths.get(programFilesX86, "GestionSalles", fileName).toString();
            if (Files.exists(Paths.get(path1))) {
                foundPath = path1;
            }
        }

        if (foundPath == null && programFiles64 != null) {
            String path2 = Paths.get(programFiles64, "GestionSalles", fileName).toString();
            if (Files.exists(Paths.get(path2))) {
                foundPath = path2;
            }
        }

        if (foundPath == null) {
            System.err.println("Base de données introuvable dans Program Files. Tentative de créer dans le répertoire courant.");
            foundPath = fileName;
        }

        DB_FILE_PATH = foundPath.replace("%20", " ");
      

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
