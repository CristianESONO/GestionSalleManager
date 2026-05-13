package com.core;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MysqlDb implements IDatabase {
    private static final Logger LOGGER = Logger.getLogger(MysqlDb.class.getName());
    private static final String DB_NAME = "gestionsalles.sqlite";
    protected Connection conn;
    protected PreparedStatement ps;

    @Override
    public void openConnexionBD() {
        try {
            // Chargement du driver SQLite
            Class.forName("org.sqlite.JDBC");
            
            // Chemin de la base de données
            Path dbPath = getDatabasePath();
            
            // Connexion à la base (la crée si elle n'existe pas)
            conn = DriverManager.getConnection("jdbc:sqlite:" + dbPath);
            
            // Initialisation de la structure
            initializeDatabase();
            
            // Vérification de l'utilisateur admin
            verifyAdminExists();
            
            LOGGER.info("Connexion à la base de données établie avec succès");
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Échec de la connexion à la base de données", e);
            throw new RuntimeException("Échec de la connexion à la base de données", e);
        }
    }

    private Path getDatabasePath() throws IOException {
        String userHome = System.getProperty("user.home");
        Path appDir = Paths.get(userHome, ".GestionSalles");
        
        if (!Files.exists(appDir)) {
            Files.createDirectories(appDir);
            LOGGER.info("Répertoire de la base de données créé: " + appDir);
        }
        
        return appDir.resolve(DB_NAME);
    }

    private void initializeDatabase() throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            // Désactiver temporairement les contraintes
            stmt.execute("PRAGMA foreign_keys=OFF");
            
            // Table users
            stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS users (" +
                "    id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "    name TEXT NOT NULL," +
                "    email TEXT UNIQUE NOT NULL," +
                "    password TEXT NOT NULL," +
                "    role TEXT NOT NULL," +
                "    registrationDate DATE" +
                ");"
            );

            // Table clients
            stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS clients (" +
                "    id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "    name TEXT NOT NULL," +
                "    email TEXT UNIQUE NOT NULL," +
                "    password TEXT NOT NULL," +
                "    role TEXT NOT NULL," +
                "    registrationDate DATE," +
                "    phone TEXT," +
                "    birthDate DATE," +
                "    address TEXT," +
                "    loyaltyPoints INTEGER" +
                ");"
            );

            // Table games
            stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS games (" +
                "    id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "    name TEXT NOT NULL," +
                "    type TEXT," +
                "    status TEXT," +
                "    imagePath TEXT" +
                ");"
            );

            // Table postes
            stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS postes (" +
                "    id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "    status TEXT," +
                "    isAvailable BOOLEAN" +
                ");"
            );

            // Table poste_game
            stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS poste_game (" +
                "    poste_id INTEGER," +
                "    game_id INTEGER," +
                "    PRIMARY KEY (poste_id, game_id)," +
                "    FOREIGN KEY (poste_id) REFERENCES postes(id) ON DELETE CASCADE," +
                "    FOREIGN KEY (game_id) REFERENCES games(id) ON DELETE CASCADE" +
                ");"
            );

            // Table reservations
            stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS reservations (" +
                "    id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "    client_id INTEGER," +
                "    reservationDate DATETIME NOT NULL," +
                "    duration INTEGER," +
                "    codeParrainage TEXT," +
                "    numeroTicket TEXT," +
                "    poste_id INTEGER," +
                "    game_id INTEGER," +
                "    FOREIGN KEY(client_id) REFERENCES clients(id)," +
                "    FOREIGN KEY(game_id) REFERENCES games(id)," +
                "    FOREIGN KEY(poste_id) REFERENCES postes(id)" +
                ");"
            );

            // Table game_sessions
            stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS game_sessions (" +
                "    id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "    client_id INTEGER," +
                "    game_id INTEGER," +
                "    paidDuration INTEGER," +
                "    remainingTime INTEGER," +
                "    score INTEGER," +
                "    status TEXT," +
                "    FOREIGN KEY (client_id) REFERENCES clients(id)," +
                "    FOREIGN KEY (game_id) REFERENCES games(id)" +
                ");"
            );

            // Table payments
            stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS payments (" +
                "    id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "    numeroTicket TEXT NOT NULL," +
                "    dateHeure DATETIME NOT NULL," +
                "    montantTotal DECIMAL(10, 2) NOT NULL," +
                "    modePaiement TEXT NOT NULL," +
                "    client_id INTEGER," +
                "    detailsProduits TEXT," +
                "    FOREIGN KEY (client_id) REFERENCES clients(id)" +
                ");"
            );

            // Table parrains
            stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS parrains (" +
                "    id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "    name TEXT NOT NULL," +
                "    email TEXT UNIQUE NOT NULL," +
                "    password TEXT NOT NULL," +
                "    role TEXT NOT NULL," +
                "    registrationDate DATETIME NOT NULL," +
                "    phone TEXT," +
                "    address TEXT," +
                "    codeParrainage TEXT" +
                ");"
            );

            // Table produits
            stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS produits (" +
                "    id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "    nom TEXT NOT NULL," +
                "    prix DECIMAL(10, 2) NOT NULL," +
                "    stock INTEGER NOT NULL," +
                "    dateAjout DATETIME," +
                "    image TEXT" +
                ");"
            );

            // Réactiver les contraintes
            stmt.execute("PRAGMA foreign_keys=ON");
            
            LOGGER.info("Structure de la base de données initialisée avec succès");
        }
    }

    private void createDefaultAdmin() throws SQLException {
        try (Statement checkTableStmt = conn.createStatement();
             var rs = checkTableStmt.executeQuery(
                 "SELECT count(*) FROM sqlite_master WHERE type='table' AND name='users'")) {
            
            if (rs.next() && rs.getInt(1) > 0) {
                try (Statement checkAdminStmt = conn.createStatement();
                     var adminRs = checkAdminStmt.executeQuery(
                         "SELECT COUNT(*) FROM users WHERE email = 'admin@admin.com'")) {
                    
                    if (adminRs.next() && adminRs.getInt(1) == 0) {
                        String insertSql = "INSERT INTO users (name, email, password, role, registrationDate) " +
                                         "VALUES (?, ?, ?, ?, ?)";
                        
                        try (PreparedStatement pstmt = conn.prepareStatement(insertSql)) {
                            pstmt.setString(1, "Administrateur");
                            pstmt.setString(2, "admin@admin.com");
                            pstmt.setString(3, "admin");
                            pstmt.setString(4, "SuperAdmin");
                            
                            // Modification ici pour utiliser java.sql.Date
                            pstmt.setDate(5, new java.sql.Date(System.currentTimeMillis()));
                            
                            pstmt.executeUpdate();
                            LOGGER.info("Utilisateur SuperAdmin créé avec succès");
                        }
                    }
                }
            }
        }
    }

    private void verifyAdminExists() throws SQLException {
        createDefaultAdmin();
    }

    @Override
    public void closeConnexionBD() {
        try {
            if (ps != null) {
                ps.close();
            }
            if (conn != null) {
                conn.close();
                LOGGER.info("Connexion à la base de données fermée");
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de la fermeture de la connexion", e);
        }
    }
}