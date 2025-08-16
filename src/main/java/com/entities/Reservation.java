package com.entities;

import javax.persistence.*;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.Random;

import com.converters.DurationConverter;

@Entity
@Table(name = "reservations")
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id")
    private Client client;

    private LocalDateTime reservationDate;

    @Convert(converter = DurationConverter.class)
    private Duration duration;

    private String codeParrainage;
    private String numeroTicket;
    private String status;

    @Column(name = "total_price")
    private double totalPrice;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "poste_id")
    private Poste poste;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "game_id")
    private Game game;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User createdBy;

    // Relation ManyToOne avec Promotion. La réservation "appartient" à une promotion (ou aucune).
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "promotion_id")
    private Promotion appliedPromotion;

    // --- CONSTRUCTEURS ---
    public Reservation() {
        this.numeroTicket = generateRandomTicketNumber();
        this.status = "PENDING";
    }

    public Reservation(int id, Client client, LocalDateTime reservationDate, Duration duration,
                       String codeParrainage, String numeroTicket, Poste poste, Game game,
                       double totalPrice, Promotion appliedPromotion, String status, User createdBy) {
        this.id = id;
        this.client = client;
        this.reservationDate = reservationDate;
        this.duration = duration;
        this.codeParrainage = codeParrainage;
        this.numeroTicket = numeroTicket;
        this.poste = poste;
        this.game = game;
        this.totalPrice = totalPrice;
        this.appliedPromotion = appliedPromotion;
        this.status = status;
        this.createdBy = createdBy;
        validateDuration();
    }

    // Constructeur principal pour une nouvelle réservation
    public Reservation(Client client, LocalDateTime reservationDate, Duration duration,
                       String codeParrainage, Poste poste, Game game, Promotion appliedPromotion, String status, User createdBy) {
        this();
        this.client = client;
        this.reservationDate = reservationDate;
        this.duration = duration;
        this.codeParrainage = codeParrainage;
        this.poste = poste;
        this.game = game;
        this.appliedPromotion = appliedPromotion;
        this.status = status;
        this.createdBy = createdBy;
        validateDuration();
        
        // C'est ici que le prix est calculé pour la première fois
        this.totalPrice = calculatePriceBasedOnDuration();
    }


    // --- Getters et Setters ---
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public Client getClient() { return client; }
    public void setClient(Client client) { this.client = client; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public User getCreatedBy() { return createdBy; }
    public void setCreatedBy(User createdBy) { this.createdBy = createdBy; }
    public LocalDateTime getReservationDate() { return reservationDate; }
    public void setReservationDate(LocalDateTime reservationDate) { this.reservationDate = reservationDate; }
    public Duration getDuration() { return duration; }
    public void setDuration(Duration duration) { this.duration = duration; }
    public String getCodeParrainage() { return codeParrainage; }
    public void setCodeParrainage(String codeParrainage) { this.codeParrainage = codeParrainage; }
    public String getNumeroTicket() { return numeroTicket; }
    public void setNumeroTicket(String numeroTicket) { this.numeroTicket = numeroTicket; }
    public double getTotalPrice() { return totalPrice; }
    public void setTotalPrice(double totalPrice) { this.totalPrice = totalPrice; }
    public Poste getPoste() { return poste; }
    public void setPoste(Poste poste) { this.poste = poste; }
    public Game getGame() { return game; }
    public void setGame(Game game) { this.game = game; }
    public Promotion getAppliedPromotion() { return appliedPromotion; }
    public void setAppliedPromotion(Promotion appliedPromotion) { this.appliedPromotion = appliedPromotion; }


    /**
     * Génère un numéro de ticket aléatoire unique.
     */
    public static String generateRandomTicketNumber() {
        Random random = new Random();
        int randomNumber = random.nextInt(1_000_000);
        return String.format("TICKRES-%06d", randomNumber);
    }

    /**
     * Calcule le prix total de la réservation en fonction de la durée et de la promotion appliquée.
     * Cette méthode est le cœur de la logique de calcul.
     */
    public double calculatePriceBasedOnDuration() {
        if (duration == null || duration.toMinutes() <= 0) {
            return 0.0;
        }

        long totalMinutes = duration.toMinutes();
        double basePrice;

        if (totalMinutes <= 15) {
            basePrice = 300.0;
        } else if (totalMinutes <= 30) {
            basePrice = 500.0;
        } else {
            basePrice = 500.0;
            long minutesBeyond30 = totalMinutes - 30;
            long numberOf15MinBlocks = (long) Math.ceil((double) minutesBeyond30 / 15);
            basePrice += numberOf15MinBlocks * 250.0;
        }

        // **LOGIQUE CLÉ** :
        // 1. On vérifie d'abord si une promotion a été appliquée à cette réservation.
        // 2. Si c'est le cas, on utilise la méthode 'isValid()' de l'objet Promotion
        //    en lui passant la date de la réservation.
        //    Ceci garantit que le prix est calculé en fonction des conditions
        //    de la promotion au moment de la réservation.
        if (this.appliedPromotion != null && this.appliedPromotion.isValid(this.reservationDate.toLocalDate())) {
            return basePrice * (1 - this.appliedPromotion.getTauxReduction());
        }

        // Si aucune promotion n'est appliquée ou si elle n'est pas valide, on retourne le prix de base.
        return basePrice;
    }

    public void validateDuration() {
        if (duration == null || duration.toMinutes() < 15) {
            throw new IllegalArgumentException("La durée minimale de réservation est de 15 minutes.");
        }
    }

    @Transient
    public Duration getRemainingTime() {
        if (reservationDate == null || duration == null) {
            return Duration.ZERO;
        }
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime endTime = reservationDate.plus(duration);
        Duration remaining = Duration.between(now, endTime);
        return remaining.isNegative() ? Duration.ZERO : remaining;
    }

    @Transient
    public boolean isReservationOver() {
        return getRemainingTime().isNegative() || getRemainingTime().isZero();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Reservation that = (Reservation) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    public void addExtraTime(Duration extraDuration) {
        if (extraDuration == null || extraDuration.isNegative()) {
            throw new IllegalArgumentException("La durée supplémentaire doit être positive");
        }
        this.duration = this.duration.plus(extraDuration);
        this.totalPrice = calculatePriceBasedOnDuration();
    }

    @Override
    public String toString() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        return "Reservation{" +
               "id=" + id +
               ", client=" + (client != null ? client.getId() : "null") +
               ", reservationDate=" + (reservationDate != null ? reservationDate.format(formatter) : "N/A") +
               ", duration=" + (duration != null ? duration.toMinutes() + " min" : "N/A") +
               ", numeroTicket='" + numeroTicket + '\'' +
               ", status='" + status + '\'' +
               ", totalPrice=" + String.format("%.2f", totalPrice) +
               ", poste=" + (poste != null ? poste.getId() : "null") +
               ", game=" + (game != null ? game.getId() : "null") +
               ", appliedPromotion=" + (appliedPromotion != null ? appliedPromotion.getId() : "null") +
               '}';
    }
}