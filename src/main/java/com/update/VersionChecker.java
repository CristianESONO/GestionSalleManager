package com.update;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class VersionChecker {
    public static String getLastVersionFromServer() {
    try {
        URL url = new URL("https://raw.githubusercontent.com/CristianESONO/GestionSalleManager/main/last_version.txt");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestProperty("User-Agent", "Mozilla/5.0");
        
        if (connection.getResponseCode() != 200) {
            System.err.println("Erreur: Impossible d'accéder au fichier de version");
            return null;
        }
        
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
            String version = reader.readLine();
            if (version != null && version.matches("\\d+\\.\\d+\\.\\d+")) {
                return version.trim();
            }
            return null;
        }
    } catch (Exception e) {
        System.err.println("Erreur de connexion: " + e.getMessage());
        return null;
    }
}
}
