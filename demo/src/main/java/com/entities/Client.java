package com.entities;

import jakarta.persistence.*;

import java.time.Duration;
import java.util.Date;
import java.util.List;

import com.core.Fabrique;

@Entity
@Table(name = "clients")
public class Client extends User {

    private String phone;
    private Date birthDate;
    private String address;
    private int loyaltyPoints;

    // Relations avec d'autres entités
    @OneToMany(mappedBy = "client", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<GameSession> gameSessions;

    @OneToMany(mappedBy = "client", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Payment> payments;

    @OneToMany(mappedBy = "client", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Reservation> reservations;

    // Constructeur par défaut
    public Client() {
        super();
        this.setRole(Role.Client);  // Par défaut, le rôle d'un Client est Role.Client
    }

    // Constructeur avec paramètres
    public Client(int id, String name, String email, String password, Date registrationDate,
                  String phone, Date birthDate, String address, int loyaltyPoints) {
        super(id, name, email, password, Role.Client, registrationDate);  // Le rôle est défini ici
        this.phone = phone;
        this.birthDate = birthDate;
        this.address = address;
        this.loyaltyPoints = loyaltyPoints;
    }

    // Constructeur avec des paramètres sans spécifier le rôle, car il est déjà défini par défaut
    public Client(String name, String email, String password, Date registrationDate,
                  String phone, Date birthDate, String address, int loyaltyPoints) {
        super(name, email, password, registrationDate);  // Le rôle est défini par défaut dans la classe User
        this.phone = phone;
        this.birthDate = birthDate;
        this.address = address;
        this.loyaltyPoints = loyaltyPoints;
        this.setRole(Role.Client);  // Le rôle est défini explicitement ici
    }

    // Constructeur avec collections
    public Client(String phone, Date birthDate, String address, int loyaltyPoints,
                  List<GameSession> gameSessions, List<Payment> payments, List<Reservation> reservations) {
        this.phone = phone;
        this.birthDate = birthDate;
        this.address = address;
        this.loyaltyPoints = loyaltyPoints;
        this.gameSessions = gameSessions;
        this.payments = payments;
        this.reservations = reservations;
    }

    // Getters et Setters
    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public Date getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(Date birthDate) {
        this.birthDate = birthDate;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public int getLoyaltyPoints() {
        return loyaltyPoints;
    }

    public void setLoyaltyPoints(int loyaltyPoints) {
        this.loyaltyPoints = loyaltyPoints;
    }

    public List<GameSession> getGameSessions() {
        return gameSessions;
    }

    public void setGameSessions(List<GameSession> gameSessions) {
        this.gameSessions = gameSessions;
    }

    public List<Payment> getPayments() {
        return payments;
    }

    public void setPayments(List<Payment> payments) {
        this.payments = payments;
    }

    public List<Reservation> getReservations() {
        return reservations;
    }

    public void setReservations(List<Reservation> reservations) {
        this.reservations = reservations;
    }

      // Méthode pour calculer le temps de jeu cumulé
    public Duration getTotalPlayTime() {
        List<Reservation> reservations = Fabrique.getService().findReservationsByClientId(this.getId());
        return reservations.stream()
                .map(Reservation::getDuration)
                .reduce(Duration.ZERO, Duration::plus);
    }
}
