package com.entities;

import jakarta.persistence.*;
import java.util.Date;

@Entity
@Table(name = "parrains")  // Table spécifique pour les parrains
public class Parrain extends User {

    private String phone;
    private String address;
    private String codeParrainage;  // Attribut spécifique à Parrain

    // Constructeur par défaut
    public Parrain() {
        super();
        this.setRole(Role.Parrain);  // Par défaut, le rôle d'un Parrain est Role.Parrain
    }

     // Constructeur avec paramètres (avec ID)
     public Parrain(int id, String name, String email, String password, Date registrationDate, String phone, String address, String codeParrainage) {
        super(id, name, email, password, Role.Parrain, registrationDate);  // Le rôle est défini ici
        this.phone = phone;
        this.address = address;
        this.codeParrainage = codeParrainage;
    }

    // Constructeur sans ID
     // Constructeur sans ID
    public Parrain(String name, String email, String password, Date registrationDate, String phone, String address, String codeParrainage) {
        super(name, email, password, registrationDate);  // Le rôle est défini par défaut dans la classe User
        this.phone = phone;
        this.address = address;
        this.codeParrainage = codeParrainage;
        this.setRole(Role.Parrain);  // Le rôle est défini explicitement ici
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


}