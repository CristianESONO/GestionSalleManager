package com.utils;

import org.mindrot.jbcrypt.BCrypt;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Réinitialise le mot de passe du SuperAdmin (SuperAdmin@kayplay.com).
 * Utilise uniquement JDBC (pas Hibernate) pour éviter verrouillage et erreurs de schéma.
 * Fermez l'application avant d'exécuter si la base est utilisée ailleurs.
 *
 * Usage : args[0] = nouveau mot de passe, args[1] = chemin vers gestionsalles.sqlite
 * Ex. : mvn exec:java -Dexec.mainClass="com.utils.ResetSuperAdminPassword" "-Dexec.args=Kayplay2024! C:\Users\HP\Desktop\GestionSalle\demo\gestionsalles.sqlite"
 */
public class ResetSuperAdminPassword {

    private static final String SUPERADMIN_EMAIL = "SuperAdmin@kayplay.com";

    public static void main(String[] args) {
        String newPassword = args != null && args.length > 0 ? args[0].trim() : "Kayplay2024!";
        String dbPath = args != null && args.length > 1 ? args[1].trim() : null;
        if (newPassword.length() < 8) {
            System.err.println("Le mot de passe doit contenir au moins 8 caractères.");
            return;
        }
        if (dbPath == null || dbPath.isEmpty()) {
            System.err.println("Indiquez le chemin de la base en 2e argument.");
            System.err.println("Ex. : \"Kayplay2024!\" \"C:\\Users\\HP\\Desktop\\GestionSalle\\demo\\gestionsalles.sqlite\"");
            return;
        }

        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            System.err.println("Driver SQLite introuvable : " + e.getMessage());
            return;
        }

        String url = "jdbc:sqlite:" + dbPath;
        try (Connection conn = DriverManager.getConnection(url)) {
            conn.setAutoCommit(false);
            // Ajouter les colonnes de sécurité si absentes (pour que l'app ne plante pas au prochain lancement)
            try (PreparedStatement ps = conn.prepareStatement("ALTER TABLE users ADD COLUMN failed_login_attempts integer")) {
                ps.executeUpdate();
            } catch (SQLException e) {
                if (!e.getMessage().contains("duplicate column")) throw e;
            }
            try (PreparedStatement ps = conn.prepareStatement("ALTER TABLE users ADD COLUMN locked_until datetime")) {
                ps.executeUpdate();
            } catch (SQLException e) {
                if (!e.getMessage().contains("duplicate column")) throw e;
            }
            conn.commit();
            conn.setAutoCommit(true);
        } catch (SQLException e) {
            System.err.println("Impossible d'ajuster le schéma (non bloquant) : " + e.getMessage());
        }

        try (Connection conn = DriverManager.getConnection(url)) {
            String hash = BCrypt.hashpw(newPassword, BCrypt.gensalt());
            String sql = "UPDATE users SET password = ? WHERE LOWER(TRIM(email)) = LOWER(TRIM(?))";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, hash);
                ps.setString(2, SUPERADMIN_EMAIL);
                int updated = ps.executeUpdate();
                if (updated > 0) {
                    System.out.println("Mot de passe mis à jour pour le compte avec email '" + SUPERADMIN_EMAIL + "'.");
                    System.out.println("Nouveau mot de passe : " + newPassword);
                } else {
                    System.err.println("Aucun utilisateur avec l'email '" + SUPERADMIN_EMAIL + "'.");
                    listUsersJdbc(conn);
                }
            }
        } catch (SQLException e) {
            System.err.println("Erreur : " + e.getMessage());
            if (e.getMessage() != null && e.getMessage().contains("locked")) {
                System.err.println("Fermez l'application GestionSalles puis réessayez.");
            }
        }
    }

    private static void listUsersJdbc(Connection conn) {
        try (PreparedStatement ps = conn.prepareStatement("SELECT id, email, name, role, user_type FROM users");
             ResultSet rs = ps.executeQuery()) {
            boolean hasRow = false;
            System.err.println("Utilisateurs dans la base :");
            while (rs.next()) {
                hasRow = true;
                System.err.println("  id=" + rs.getObject(1) + " | email=" + rs.getString(2)
                    + " | name=" + rs.getString(3) + " | role=" + rs.getString(4)
                    + " | user_type=" + rs.getString(5));
            }
            if (!hasRow) System.err.println("  (aucun)");
        } catch (SQLException e) {
            System.err.println("Impossible de lister les utilisateurs : " + e.getMessage());
        }
    }
}
