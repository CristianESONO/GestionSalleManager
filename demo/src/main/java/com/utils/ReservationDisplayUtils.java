package com.utils;

import com.entities.Game;
import com.entities.GameSession;
import com.entities.Poste;
import com.entities.Reservation;

/**
 * Utilitaires pour afficher les réservations sans erreur lorsque le jeu ou le poste a été supprimé
 * (évite "could not initialize proxy - no session").
 */
public final class ReservationDisplayUtils {

    private ReservationDisplayUtils() {}

    /** Retourne le nom du poste ou "Poste supprimé" si l'entité est supprimée / proxy non initialisable. */
    public static String safeGetPosteName(Reservation r) {
        if (r == null) return "N/A";
        try {
            Poste p = r.getPoste();
            if (p == null) return "N/A";
            String name = p.getName();
            return name != null ? name : ("Poste " + p.getId());
        } catch (Exception e) {
            return "Poste supprimé";
        }
    }

    /** Retourne le nom du jeu ou "Jeu supprimé" si l'entité est supprimée / proxy non initialisable. */
    public static String safeGetGameName(Reservation r) {
        if (r == null) return "N/A";
        try {
            Game g = r.getGame();
            if (g == null) return "N/A";
            String name = g.getName();
            return name != null ? name : "N/A";
        } catch (Exception e) {
            return "Jeu supprimé";
        }
    }

    /** Retourne le poste ou null si proxy non initialisable. */
    public static Poste safeGetPoste(Reservation r) {
        if (r == null) return null;
        try {
            return r.getPoste();
        } catch (Exception e) {
            return null;
        }
    }

    /** Retourne le game ou null si proxy non initialisable. */
    public static Game safeGetGame(Reservation r) {
        if (r == null) return null;
        try {
            return r.getGame();
        } catch (Exception e) {
            return null;
        }
    }

    /** Pour GameSession : nom du poste ou "Poste supprimé". */
    public static String safeGetPosteName(GameSession s) {
        if (s == null) return "N/A";
        try {
            Poste p = s.getPoste();
            if (p == null) return "N/A";
            String name = p.getName();
            return name != null ? name : ("Poste " + p.getId());
        } catch (Exception e) {
            return "Poste supprimé";
        }
    }

    /** Pour GameSession : nom du jeu ou "Jeu supprimé". */
    public static String safeGetGameName(GameSession s) {
        if (s == null) return "N/A";
        try {
            Game g = s.getGame();
            if (g == null) return "N/A";
            String name = g.getName();
            return name != null ? name : "N/A";
        } catch (Exception e) {
            return "Jeu supprimé";
        }
    }
}
