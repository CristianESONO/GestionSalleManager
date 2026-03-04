package com.entities;

import javax.persistence.*;
import java.util.Date;
import java.util.List;
import java.util.Objects; // Import pour Objects.hash() si equals/hashCode étaient redéfinis

// L'annotation @Entity est déjà présente.
// Nous ajoutons @DiscriminatorValue pour la stratégie d'héritage SINGLE_TABLE définie dans User.
@Entity
@DiscriminatorValue("CLIENT") // Indique la valeur de 'user_type' pour les entités Client
                         // Toutes les données de Client iront dans la table 'users'.
public class Client extends User {

    private String phone;
    private String address;
    private int loyaltyPoints;

    // Relations avec d'autres entités
    // CascadeType.ALL est puissant et peut être dangereux si mal utilisé.
    // Il signifie que les opérations (persist, merge, remove) sur Client se propageront aux GameSession, Payment, Reservation.
    // Assurez-vous que c'est le comportement désiré.
    @OneToMany(mappedBy = "client", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<GameSession> gameSessions;

    @OneToMany(mappedBy = "client", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Payment> payments;

    @OneToMany(mappedBy = "client", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Reservation> reservations;

    // Constructeur par défaut (OBLIGATOIRE pour JPA)
    public Client() {
        super(); // Appelle le constructeur par défaut de User
        this.setRole(Role.Client); // Par défaut, le rôle d'un Client est Role.Client
    }

    // Constructeur avec paramètres (ID inclus)
    public Client(int id, String name, String email, String password, Date registrationDate,
                  String phone, String address, int loyaltyPoints) {
        super(id, name, email, password, Role.Client, registrationDate); // Le rôle est défini ici
        this.phone = phone;
        this.address = address;
        this.loyaltyPoints = loyaltyPoints;
    }

    // Constructeur avec des paramètres sans spécifier le rôle, car il est déjà défini par défaut
    public Client(String name, String email, String password, Date registrationDate,
                  String phone, String address, int loyaltyPoints) {
        super(name, email, password, registrationDate); // Appelle le constructeur de User
        this.phone = phone;
        this.address = address;
        this.loyaltyPoints = loyaltyPoints;
        this.setRole(Role.Client); // Le rôle est défini explicitement ici
    }

    // Constructeur avec collections (utile pour la construction du graphe d'objets, mais JPA le gère aussi)
    // Ce constructeur n'est généralement pas utilisé directement pour la persistance avec JPA.
    public Client(String name, String email, String password, Date registrationDate, String phone, String address, int loyaltyPoints,
                  List<GameSession> gameSessions, List<Payment> payments, List<Reservation> reservations) {
        super(name, email, password, registrationDate);
        this.phone = phone;
        this.address = address;
        this.loyaltyPoints = loyaltyPoints;
        this.gameSessions = gameSessions;
        this.payments = payments;
        this.reservations = reservations;
        this.setRole(Role.Client);
    }


    // Getters et Setters
    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
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
        // Il est préférable de ne pas initialiser la liste ici si elle est LAZY,
        // car cela pourrait masquer une LazyInitializationException si vous y accédez
        // en dehors d'une session EntityManager. JPA l'initialise si elle est chargée.
        return reservations;
    }

    public void setReservations(List<Reservation> reservations) {
        this.reservations = reservations;
    }

    /**
     * Ajoute des points de fidélité au client.
     * @param duration La durée de jeu en minutes.
     */
    public void addLoyaltyPointsFromDuration(int duration) {
        int points = duration / 15; // 1 point pour chaque tranche de 15 minutes
        this.loyaltyPoints += points;
    }

    // La méthode getTotalPlayTime() a été déplacée vers la couche de service.
    // Elle n'est plus dans l'entité Client.

    @Override
    public String toString() {
        return "Client{" +
               "id=" + getId() +
               ", name='" + getName() + '\'' +
               ", email='" + getEmail() + '\'' +
               ", phone='" + phone + '\'' +
               ", address='" + address + '\'' +
               ", loyaltyPoints=" + loyaltyPoints +
               '}';
    }

    // Les méthodes equals() et hashCode() sont héritées de la classe User
    // et sont suffisantes car l'ID est géré par la super-classe et toutes
    // les entités de cette hiérarchie sont dans la même table.
    // Il n'est pas nécessaire de les redéfinir ici, sauf si vous avez une logique
    // de comparaison différente pour Client qui ne se base pas uniquement sur l'ID.
}
