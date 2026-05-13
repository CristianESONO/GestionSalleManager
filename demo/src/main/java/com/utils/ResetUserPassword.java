package com.utils;

import org.mindrot.jbcrypt.BCrypt;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Réinitialise le mot de passe d'un utilisateur par son email.
 * Fermez l'application avant d'exécuter si la base est utilisée.
 *
 * Usage : args[0] = email, args[1] = nouveau mot de passe, args[2] = chemin DB (optionnel)
 * Ex. : mvn exec:java -Dexec.mainClass="com.utils.ResetUserPassword" "-Dexec.args=martin@kayplay.sn Kayplay2024!"
 */
public class ResetUserPassword {

    public static void main(String[] args) {
        String email = args != null && args.length > 0 ? args[0].trim() : null;
        String newPassword = args != null && args.length > 1 ? args[1].trim() : "Kayplay2024!";
        String dbPath = args != null && args.length > 2 ? args[2].trim() : null;

        if (email == null || email.isEmpty()) {
            System.err.println("Indiquez l'email de l'utilisateur en 1er argument.");
            System.err.println("Ex. : martin@kayplay.sn Kayplay2024!");
            return;
        }
        if (newPassword.length() < 8) {
            System.err.println("Le mot de passe doit contenir au moins 8 caractères.");
            return;
        }

        if (dbPath == null || dbPath.isEmpty()) {
            Path appDir = Paths.get(
                System.getenv("APPDATA") != null ? System.getenv("APPDATA") : System.getProperty("user.home", ""),
                "GestionSalles"
            );
            Path defaultDb = appDir.resolve("gestionsalles.sqlite");
            if (Files.exists(defaultDb)) {
                dbPath = defaultDb.toAbsolutePath().toString();
            } else {
                Path projectDb = Paths.get("gestionsalles.sqlite");
                if (Files.exists(projectDb)) {
                    dbPath = projectDb.toAbsolutePath().toString();
                } else {
                    System.err.println("Indiquez le chemin de la base en 3e argument.");
                    System.err.println("Ex. : martin@kayplay.sn Kayplay2024! \"C:\\chemin\\vers\\gestionsalles.sqlite\"");
                    return;
                }
            }
        }

        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            System.err.println("Driver SQLite introuvable : " + e.getMessage());
            return;
        }

        String url = "jdbc:sqlite:" + dbPath;
        try (Connection conn = DriverManager.getConnection(url)) {
            String hash = BCrypt.hashpw(newPassword, BCrypt.gensalt());
            String sql = "UPDATE users SET password = ? WHERE LOWER(TRIM(email)) = LOWER(TRIM(?))";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, hash);
                ps.setString(2, email);
                int updated = ps.executeUpdate();
                if (updated > 0) {
                    System.out.println("Mot de passe mis à jour pour : " + email);
                    System.out.println("Nouveau mot de passe : " + newPassword);
                } else {
                    System.err.println("Aucun utilisateur avec l'email : " + email);
                }
            }
        } catch (SQLException e) {
            System.err.println("Erreur : " + e.getMessage());
        }
    }
}
