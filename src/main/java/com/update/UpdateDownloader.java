package com.update;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;

public class UpdateDownloader {
    public static void downloadAndInstallUpdate(String version) {
        String downloadUrl = "https://github.com/CristianESONO/GestionSalleManager/releases/download/v" + version + "/GestionSalle_Setup_" + version + ".exe";
        String destinationPath = "C:\\Users\\HP\\Desktop\\GestionSalle_Setup_latest.exe";

        try {
            downloadUpdate(downloadUrl, destinationPath);
            installUpdate(destinationPath);
        } catch (Exception e) {
            System.err.println("Erreur : " + e.getMessage());
        }
    }

    private static void downloadUpdate(String downloadUrl, String destinationPath) throws Exception {
        try (InputStream in = new URL(downloadUrl).openStream();
             ReadableByteChannel rbc = Channels.newChannel(in);
             FileOutputStream fos = new FileOutputStream(destinationPath)) {
            fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
        }
    }

    private static void installUpdate(String setupPath) throws Exception {
        ProcessBuilder pb = new ProcessBuilder(setupPath, "/SILENT", "/NORESTART");
        pb.start();
        System.exit(0);
    }
}
