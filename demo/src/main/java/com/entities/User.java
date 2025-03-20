package com.entities;


import java.util.Date;

import jakarta.persistence.*;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private String name;

    @Column(unique = true) // Assurer l'unicité de l'email
    private String email;

    private String password;

    @Enumerated(EnumType.STRING) // Spécifier le type d'énumération (pour gérer les rôles)
    private Role role;

    private Date registrationDate;

    // Constructeur par défaut
    public User() {
        this.role = Role.Admin;  // Par défaut, rôle Admin
    }

    // Constructeur complet
    public User(int id, String name, String email, String password, Date registrationDate) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.password = password;
        this.registrationDate = registrationDate;
        this.role = Role.Admin;  // Par défaut, rôle Admin
    }

    // Constructeur sans ID
    public User(String name, String email, String password, Date registrationDate) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.registrationDate = registrationDate;
        this.role = Role.Admin;  // Par défaut, rôle Admin
    }

    // Constructeur avec rôle
    public User(String name, String email, Role role) {
        this.name = name;
        this.email = email;
        this.role = role;
    }

    // Constructeur complet avec tous les attributs
    public User(int id, String name, String email, String password, Role role, Date registrationDate) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.password = password;
        this.role = role;
        this.registrationDate = registrationDate;
    }

    // Getters et Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public Date getRegistrationDate() {
        return registrationDate;
    }

    public void setRegistrationDate(Date registrationDate) {
        this.registrationDate = registrationDate;
    }

    @Override
public String toString() {
    return name;
}

}
