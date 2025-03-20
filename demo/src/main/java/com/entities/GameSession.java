package com.entities;

import jakarta.persistence.*;
import java.time.Duration;

@Entity
@Table(name = "game_sessions")
public class GameSession {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    
    @Column(name = "paidDuration")
    private Duration paidDuration; // Temps total payé par le client
    
    @Column(name = "remainingTime")
    private Duration remainingTime; // Temps restant dans la session
    
    private String status; // Pending, Active, Completed, Stopped
    
    private static final double pricePerMinute = 50.0; // Prix par minute (en FCFA)

    // Relations
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "game_id", nullable = false)
    private Game game;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reservation_id")
    private Reservation reservation;

    // Getters et setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Duration getRemainingTime() {
        return remainingTime;
    }

    public void setRemainingTime(Duration remainingTime) {
        if (remainingTime.isNegative()) {
            throw new IllegalArgumentException("Le temps restant ne peut pas être négatif.");
        }
        this.remainingTime = remainingTime;
    }

    public Duration getPaidDuration() {
        return paidDuration;
    }

    public void setPaidDuration(Duration paidDuration) {
        if (paidDuration.isNegative()) {
            throw new IllegalArgumentException("La durée payée ne peut pas être négative.");
        }
        this.paidDuration = paidDuration;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = (status == null || status.isEmpty()) ? "Pending" : status;
    }

    public Client getClient() {
        return client;
    }

    public void setClient(Client client) {
        this.client = client;
    }

    public Game getGame() {
        return game;
    }

    public void setGame(Game game) {
        this.game = game;
    }

    public Reservation getReservation() {
        return reservation;
    }

    public void setReservation(Reservation reservation) {
        this.reservation = reservation;
    }

    // Propriété calculée pour obtenir le prix total
    public double getTotalPrice() {
        return paidDuration.toMinutes() * pricePerMinute;
    }

    // Constructeurs
    public GameSession() {
        this.status = "Pending";
        this.remainingTime = Duration.ZERO;
        this.paidDuration = Duration.ZERO;
    }

    public GameSession(int id, Duration paidDuration, Duration remainingTime, String status, Client client, Game game, Reservation reservation) {
        this.id = id;
        this.paidDuration = paidDuration;
        this.remainingTime = remainingTime;
        this.status = (status == null || status.isEmpty()) ? "Pending" : status;
        this.client = client;
        this.game = game;
        this.reservation = reservation;
    }
}