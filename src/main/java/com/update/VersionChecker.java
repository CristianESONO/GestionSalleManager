package com.update;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * Vérifie la dernière version disponible sur le serveur.
 * Timeout court pour ne pas bloquer le démarrage.
 */
public final class VersionChecker {

    private static final String VERSION_URL = "https://raw.githubusercontent.com/CristianESONO/GestionSalleManager/main/last_version.txt";
    private static final int CONNECT_TIMEOUT_MS = 4_000;
    private static final int READ_TIMEOUT_MS = 4_000;
    private static final java.util.regex.Pattern VERSION_PATTERN = java.util.regex.Pattern.compile("\\d+\\.\\d+\\.\\d+");

    private VersionChecker() {}

    /**
     * Récupère la dernière version depuis le serveur.
     * @return la version (ex. "1.3.4") ou null si indisponible / erreur / timeout
     */
    public static String getLastVersionFromServer() {
        HttpURLConnection connection = null;
        try {
            URL url = new URL(VERSION_URL);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("User-Agent", "GestionSalles-Updater/1.0");
            connection.setConnectTimeout(CONNECT_TIMEOUT_MS);
            connection.setReadTimeout(READ_TIMEOUT_MS);
            connection.setInstanceFollowRedirects(true);

            int code = connection.getResponseCode();
            if (code != HttpURLConnection.HTTP_OK) {
                return null;
            }

            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
                String line = reader.readLine();
                if (line == null) return null;
                String trimmed = line.trim();
                return VERSION_PATTERN.matcher(trimmed).matches() ? trimmed : null;
            }
        } catch (Exception e) {
            return null;
        } finally {
            if (connection != null) {
                try {
                    connection.disconnect();
                } catch (Exception ignored) {}
            }
        }
    }
}
