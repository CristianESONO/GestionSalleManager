package com.update;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Properties;

public class UpdateManager {

    private static final String CONFIG_FILE = "/config.properties";

    public static void checkForUpdates() {
        try {
            // Charger les propriétés locales
            Properties localProps = new Properties();
            localProps.load(UpdateManager.class.getResourceAsStream(CONFIG_FILE));

            String currentVersion = localProps.getProperty("current_version");
            String updateUrl = localProps.getProperty("update_url");
            String jarUrl = localProps.getProperty("jar_url");

            // Télécharger la version distante
            Properties remoteProps = new Properties();
            remoteProps.load(new URL(updateUrl).openStream());

            String remoteVersion = remoteProps.getProperty("version");

            System.out.println("Version locale : " + currentVersion);
            System.out.println("Version distante : " + remoteVersion);

            // Comparaison des versions
            if (!currentVersion.equals(remoteVersion)) {
                System.out.println("Mise à jour disponible ! Téléchargement...");

                // Télécharger le nouveau .jar
                File newJar = new File("update_temp.jar");
                try (InputStream in = new URL(jarUrl).openStream()) {
                    Files.copy(in, newJar.toPath(), StandardCopyOption.REPLACE_EXISTING);
                }

                System.out.println("Mise à jour téléchargée. Lancement...");

                // Lancer le nouveau .jar et fermer l'ancien
                new ProcessBuilder("java", "-jar", newJar.getName()).start();
                System.exit(0); // Arrêter l’ancienne version
            } else {
                System.out.println("Aucune mise à jour disponible.");
            }

        } catch (Exception e) {
            System.out.println("Erreur lors de la vérification de mise à jour : " + e.getMessage());
        }
    }
}
