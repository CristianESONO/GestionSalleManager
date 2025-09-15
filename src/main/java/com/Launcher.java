package com;

import java.io.IOException;
import java.net.ServerSocket;

public class Launcher {
    private static ServerSocket lockSocket;

    public static void main(String[] args) {
        try {
            lockSocket = new ServerSocket(45678);
            // Lance l'application principale
            App.main(args);
        } catch (IOException e) {
            System.out.println("L'application est déjà en cours d'exécution.");
        }
    }
}
