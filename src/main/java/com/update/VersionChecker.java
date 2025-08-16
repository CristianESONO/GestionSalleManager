package com.update;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;

public class VersionChecker {
    public static String getLastVersionFromServer() {
        try {
            URL url = new URL("https://raw.githubusercontent.com/CristianESONO/GestionSalleManager/main/last_version.txt");
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()))) {
                return reader.readLine();
            }
        } catch (Exception e) {
            System.err.println("Erreur : " + e.getMessage());
            return null;
        }
    }
}
