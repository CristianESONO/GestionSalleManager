package com.core;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

// L'importation de 'com.App' n'est plus nécessaire ici
// car cette classe ne dépend plus directement de l'application principale
// pour son fonctionnement.

public class MysqlDb { // Ne pas implémenter IDatabase ici

    // La connexion unique et partagée pour toute l'application
    private static Connection connection = null;
    private static String DB_FILE_PATH = null;
    // Un objet de verrouillage pour s'assurer que les opérations sur la connexion sont synchronisées
    private static final Object CONNECTION_LOCK = new Object(); 

    // Bloc statique exécuté une seule fois au chargement de la classe
    static {
        String fileName = "gestionsalles.sqlite";
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
            foundPath = fileName; // Créera le fichier dans le répertoire d'exécution si non trouvé ailleurs
        }

        DB_FILE_PATH = foundPath.replace("%20", " "); // Nettoie les %20
        
        // Charger le driver JDBC une seule fois au démarrage de l'application
        try {
            Class.forName("org.sqlite.JDBC");
            System.out.println("Driver SQLite JDBC chargé avec succès.");
        } catch (ClassNotFoundException e) {
            System.err.println("Erreur: Driver SQLite JDBC introuvable. Assurez-vous que la dépendance SQLite est correctement configurée dans votre projet (ex: pom.xml ou librairies).");
            e.printStackTrace();
            // Gérer l'erreur de manière appropriée, par exemple en arrêtant l'application si la DB est cruciale.
            // System.exit(1); pourrait être appelé ici.
        }
    }

    /**
     * Fournit une connexion unique et partagée à la base de données.
     * Si la connexion n'existe pas ou est fermée, elle est réétablie.
     * Cette méthode est synchronisée pour éviter les problèmes d'accès concurrents.
     *
     * @return L'objet Connection unique.
     * @throws SQLException Si une erreur survient lors de l'établissement de la connexion.
     */
    public static Connection getConnection() throws SQLException {
        synchronized (CONNECTION_LOCK) { // Synchronisation pour la sécurité des threads
            if (connection == null || connection.isClosed()) {
                System.out.println("Ouverture/réinitialisation de la connexion à la base de données : " + DB_FILE_PATH);
                connection = DriverManager.getConnection("jdbc:sqlite:" + DB_FILE_PATH);
                // Par défaut, l'auto-commit est activé pour SQLite.
                // Si vous avez besoin de transactions multi-requêtes, désactivez-le temporairement puis commitez/rollback.
                connection.setAutoCommit(true); 
            }
            return connection;
        }
    }

    /**
     * Ferme la connexion unique à la base de données.
     * Cette méthode doit être appelée une seule fois, idéalement lors de l'arrêt de l'application,
     * pour libérer les ressources de la base de données.
     */
    public static void closeConnection() {
        synchronized (CONNECTION_LOCK) { // Synchronisation pour la sécurité des threads
            if (connection != null) {
                try {
                    if (!connection.isClosed()) {
                        System.out.println("Fermeture de la connexion à la base de données.");
                        connection.close();
                    }
                } catch (SQLException e) {
                    System.err.println("Erreur lors de la fermeture de la connexion à la base de données : " + e.getMessage());
                    e.printStackTrace();
                } finally {
                    connection = null; // Assure que la référence à la connexion est nulle après fermeture
                }
            }
        }
    }
}