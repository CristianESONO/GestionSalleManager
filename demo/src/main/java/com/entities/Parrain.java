package com.entities;

import javax.persistence.*;
import java.util.Date;
import java.util.Objects; // Pour equals/hashCode

@Entity                  // Si vous voulez une table séparée, il faut changer la stratégie d'héritage.
@DiscriminatorValue("PARRAIN") // Indique la valeur de 'user_type' pour les entités Parrain
public class Parrain extends User {

    private String phone;
    private String address;
    private int parrainagePoints = 0;

    @Column(unique = true) // Assurer l'unicité du code de parrainage
    private String codeParrainage; // Attribut spécifique à Parrain

    // Constructeur par défaut (OBLIGATOIRE pour JPA)
    public Parrain() {
        super(); // Appelle le constructeur par défaut de User
        this.setRole(Role.Parrain); // Définit le rôle spécifique à Parrain
    }

    // Constructeur avec paramètres (avec ID)
    public Parrain(int id, String name, String email, String password, Date registrationDate, String phone, String address, String codeParrainage, int parrainagePoints) {
        super(id, name, email, password, Role.Parrain, registrationDate); // Le rôle est défini ici
        this.phone = phone;
        this.address = address;
        this.codeParrainage = codeParrainage;
        this.parrainagePoints = parrainagePoints;
    }

    // Constructeur sans ID
    public Parrain(String name, String email, String password, Date registrationDate, String phone, String address, String codeParrainage, int parrainagePoints) {
        super(name, email, password, registrationDate); // Appelle le constructeur de User
        this.phone = phone;
        this.address = address;
        this.codeParrainage = codeParrainage;
        this.parrainagePoints = parrainagePoints;
        this.setRole(Role.Parrain); // Le rôle est défini explicitement ici
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

    public String getCodeParrainage() {
        return codeParrainage;
    }

    public void setCodeParrainage(String codeParrainage) {
        this.codeParrainage = codeParrainage;
    }

    public int getParrainagePoints() {
        return parrainagePoints;
    }

    public void setParrainagePoints(int parrainagePoints) {
        this.parrainagePoints = parrainagePoints;
    }

    public void addParrainagePoints(int points) {
        this.parrainagePoints += points;
    }

    // Les méthodes equals() et hashCode() de User sont suffisantes car l'ID est géré par la super-classe
    // et toutes les entités de cette hiérarchie sont dans la même table.
    // Il n'est pas nécessaire de les redéfinir ici, sauf si vous avez une logique de comparaison différente.
}
