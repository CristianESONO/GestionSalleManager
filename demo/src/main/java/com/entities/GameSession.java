package com.entities;

import javax.persistence.*;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects; // Used for equals/hashCode

import com.converters.DurationConverter; // Importez votre nouveau convertisseur

@Entity
@Table(name = "game_sessions")
public class GameSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    // Appliquer le convertisseur pour persister Duration en Long (minutes)
    @Convert(converter = DurationConverter.class)
    @Column(name = "paidDuration")
    private Duration paidDuration;

    // Si 'remainingTime' est toujours calculé dynamiquement et NE DOIT PAS être stocké en DB,
    // utilisez @Transient. Sinon, appliquez aussi le convertisseur.
    // Étant donné votre getter, il semble que ce soit une valeur calculée.
    @Transient // Indique que ce champ n'est pas persistant en base de données
    private Duration remainingTime; // Ce champ est calculé, pas directement persisté

    // LocalDateTime est généralement bien géré par Hibernate pour les colonnes de type TIMESTAMP/DATETIME.
    // Pour SQLite, il sera souvent stocké comme TEXT (ISO 8601) ou INTEGER (epoch seconds/millis).
    @Column(name = "startTime")
    private LocalDateTime startTime;

    @Column(name = "endTime")
    private LocalDateTime endTime;

    private String status; // Pending, Active, Completed, Stopped

    // Cette constante n'est pas une colonne de DB, donc pas besoin d'annotation JPA
    private static final double pricePerMinute = 50.0;

    // Relations (JPA annotations sont déjà là, mais vérifions les FetchType)
    // FetchType.LAZY est la valeur par défaut pour ManyToOne, mais explicite c'est bien.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id") // Assurez-vous que le nom de la colonne est correct dans votre DB
    private Client client;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "game_id", nullable = false) // Assurez-vous que le nom de la colonne est correct dans votre DB
    private Game game;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "poste_id", nullable = false) // Assurez-vous que le nom de la colonne est correct dans votre DB
    private Poste poste;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reservation_id") // Assurez-vous que le nom de la colonne est correct dans votre DB
    private Reservation reservation;

    @Column(name = "paused_remaining_time")
    private Duration pausedRemainingTime;

    @Column(name = "is_paused")
    private boolean isPaused;

    // --- Getters / Setters ---

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Duration getPaidDuration() {
        return paidDuration;
    }

    public void setPaidDuration(Duration paidDuration) {
        if (paidDuration != null && paidDuration.isNegative()) {
            throw new IllegalArgumentException("La durée payée ne peut pas être négative.");
        }
        this.paidDuration = paidDuration;
    }

    public Duration getPausedRemainingTime() {
        return pausedRemainingTime;
    }

    public void setPausedRemainingTime(Duration pausedRemainingTime) {
        this.pausedRemainingTime = pausedRemainingTime;
    }

    public boolean isPaused() {
        return isPaused;
    }

    public void setPaused(boolean paused) {
        isPaused = paused;
    }

   @Transient
    public Duration getRemainingTime() {
        // Si la session est en pause, retourner le temps restant sauvegardé
        if ("En pause".equalsIgnoreCase(this.status)) {
            return pausedRemainingTime != null ? pausedRemainingTime : Duration.ZERO;
        }
        
        // Si la session est terminée, retourner zéro
        if ("Terminée".equalsIgnoreCase(this.status) && this.endTime != null) {
            return Duration.ZERO;
        }
        
        // Pour les sessions actives, calculer normalement
        if (startTime == null || paidDuration == null) {
            return Duration.ZERO;
        }
        
        Duration elapsed = Duration.between(startTime, LocalDateTime.now());
        Duration remaining = paidDuration.minus(elapsed);
        return remaining.isNegative() ? Duration.ZERO : remaining;
    }


    // Le setter pour remainingTime est conservé si vous avez une logique qui le définit,
    // mais il n'aura pas d'impact sur la persistance en DB grâce à @Transient.
    public void setRemainingTime(Duration remainingTime) {
        if (remainingTime != null && remainingTime.isNegative()) {
            throw new IllegalArgumentException("Le temps restant ne peut pas être négatif.");
        }
        this.remainingTime = remainingTime;
    }

    public void addExtraTime(Duration extra) 
    {
        if (extra == null || extra.isNegative()) {
            throw new IllegalArgumentException("Le temps supplémentaire doit être positif");
        }
        this.paidDuration = this.paidDuration.plus(extra);
        // Pas besoin de recalculer remainingTime car il est calculé dynamiquement
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
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

    public Poste getPoste() {
        return poste;
    }

    public void setPoste(Poste poste) {
        this.poste = poste;
    }

    public Reservation getReservation() {
        return reservation;
    }

    public void setReservation(Reservation reservation) {
        this.reservation = reservation;
    }

    public double calculateSessionPrice() {
        if (paidDuration == null) {
            return 0.0;
        }
        return paidDuration.toMinutes() * pricePerMinute;
    }

    // --- Constructors ---
    public GameSession() {
        this.status = "Pending";
        this.paidDuration = Duration.ZERO;
    }

    public GameSession(Duration paidDuration, LocalDateTime startTime, LocalDateTime endTime, String status, Client client, Game game, Poste poste, Reservation reservation) {
        this.paidDuration = paidDuration;
        this.startTime = startTime;
        this.endTime = endTime;
        this.status = (status == null || status.isEmpty()) ? "Pending" : status;
        this.client = client;
        this.game = game;
        this.poste = poste;
        this.reservation = reservation;
    }

    // It's good practice to override equals and hashCode for entities
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GameSession that = (GameSession) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "GameSession{" +
               "id=" + id +
               ", paidDuration=" + paidDuration +
               ", remainingTime=" + getRemainingTime() + // Use the getter for dynamic calculation
               ", startTime=" + startTime +
               ", endTime=" + endTime +
               ", status='" + status + '\'' +
               ", client=" + (client != null ? client.getId() : "null") + // Avoid loading full object in toString
               ", game=" + (game != null ? game.getId() : "null") +
               ", poste=" + (poste != null ? poste.getId() : "null") +
               ", reservation=" + (reservation != null ? reservation.getId() : "null") +
               '}';
    }

    /**
     * Calculates the actual time played for the session.
     * This method is crucial for getTotalPlayTime.
     * @return The actual play time as a Duration.
     */
    public Duration getActualPlayTime() {
        if (startTime == null) {
            return Duration.ZERO;
        }

        if ("Completed".equalsIgnoreCase(status) && endTime != null) {
            return Duration.between(startTime, endTime);
        } else if ("Active".equalsIgnoreCase(status)) {
            // For active sessions, calculate time elapsed so far
            return Duration.between(startTime, LocalDateTime.now());
        }
        // For "Pending", "Stopped", "Cancelled" or other states where actual play time might not be relevant
        return Duration.ZERO;
    }
}
