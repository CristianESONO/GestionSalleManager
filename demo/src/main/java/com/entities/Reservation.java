package com.entities;

import jakarta.persistence.*;
import java.time.Duration;
import java.time.LocalDateTime;

@Entity
@Table(name = "reservations")
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)  // Génération automatique de l'ID
    private int id;

    @ManyToOne(fetch = FetchType.LAZY)  // Relation avec Client (clé étrangère)
    @JoinColumn(name = "client_id", referencedColumnName = "id")
    private Client client;

    private LocalDateTime reservationDate;  // Date de la réservation

    private Duration duration;  // Durée de la réservation

    private String codeParrainage;  // Nullable

    private String numeroTicket;  // Numéro du ticket généré



    @ManyToOne(fetch = FetchType.LAZY)  // Relation avec Poste (clé étrangère)
    @JoinColumn(name = "poste_id", referencedColumnName = "id")
    private Poste poste;

    @ManyToOne(fetch = FetchType.LAZY)  // Relation avec Game (clé étrangère)
    @JoinColumn(name = "game_id", referencedColumnName = "id")
    private Game game;  // Nouveau champ pour le jeu

    // Getters et setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Client getClient() {
        return client;
    }

    public void setClient(Client client) {
        this.client = client;
    }

    public LocalDateTime getReservationDate() {
        return reservationDate;
    }

    public void setReservationDate(LocalDateTime reservationDate) {
        this.reservationDate = reservationDate;
    }

    public Duration getDuration() {
        return duration;
    }

    public void setDuration(Duration duration) {
        this.duration = duration;
    }

    public String getCodeParrainage() {
        return codeParrainage;
    }

    public void setCodeParrainage(String codeParrainage) {
        this.codeParrainage = codeParrainage;
    }

    public String getNumeroTicket() {
        return numeroTicket;
    }

    public void setNumeroTicket(String numeroTicket) {
        this.numeroTicket = numeroTicket;
    }

    public Poste getPoste() {
        return poste;
    }

    public void setPoste(Poste poste) {
        this.poste = poste;
    }

    public Game getGame() {
        return game;
    }

    public void setGame(Game game) {
        this.game = game;
    }

    // Génération du numéro de ticket automatique
    public String generateTicketNumber() {
        return String.format("TKT-%s-%d", LocalDateTime.now().toString().replaceAll("[-:.]", ""), client.getId());
    }

    // Méthode pour calculer le prix total basé sur la durée
    public double calculateTotalPrice() {
        // Durée minimale de réservation : 15 minutes
        if (duration.toMinutes() < 15) {
            throw new IllegalArgumentException("La durée minimale de réservation est de 15 minutes.");
        }
    
        // Tarif pour les 15 premières minutes
        final double initialPrice = 300.0; // 300 FCFA pour les 15 premières minutes
        final double additionalPricePer15Minutes = 250.0; // 250 FCFA pour chaque tranche de 15 minutes supplémentaires
    
        // Calcul du nombre de tranches de 15 minutes
        long totalMinutes = duration.toMinutes();
        long additional15MinutesBlocks = (totalMinutes - 15) / 15; // Nombre de tranches supplémentaires
    
        // Calcul du prix total
        double totalPrice = initialPrice + (additional15MinutesBlocks * additionalPricePer15Minutes);
    
        return totalPrice;
    }

    public void validateDuration() {
        if (duration.toMinutes() < 15) {
            throw new IllegalArgumentException("La durée minimale de réservation est de 15 minutes.");
        }
    }

    public Duration getRemainingTime() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime endTime = reservationDate.plus(duration);
        return Duration.between(now, endTime);
    }

    public boolean isReservationOver() {
        return getRemainingTime().isNegative() || getRemainingTime().isZero();
    }

    // Constructeurs
    public Reservation() { }

    public Reservation(int id, Client client, LocalDateTime reservationDate, Duration duration, String codeParrainage, String numeroTicket, Poste poste, Game game) {
        this.id = id;
        this.client = client;
        this.reservationDate = reservationDate;
        this.duration = duration;
        this.codeParrainage = codeParrainage;
        this.numeroTicket = numeroTicket;
        this.poste = poste;
        this.game = game;  // Initialisation du jeu
        validateDuration(); // Valider la durée lors de la création
    }

    public Reservation(Client client, LocalDateTime reservationDate, Duration duration, String codeParrainage, Poste poste, Game game) {
        this.client = client;
        this.reservationDate = reservationDate;
        this.duration = duration;
        this.codeParrainage = codeParrainage;
        this.poste = poste;
        this.game = game;  // Initialisation du jeu
        validateDuration(); // Valider la durée lors de la création
    }
}
