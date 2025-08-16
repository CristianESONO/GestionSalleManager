package com.entities;

import java.util.Date;
import java.util.Objects;

import javax.persistence.*;

// Définir la stratégie d'héritage. SINGLE_TABLE est souvent la plus simple pour les hiérarchies.
// Toutes les sous-classes seront stockées dans la même table 'users'.
@Entity
@Table(name = "users")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
// Colonne pour distinguer les types d'entités (User, Client, Parrain, etc.)
@DiscriminatorColumn(name = "user_type", discriminatorType = DiscriminatorType.STRING)
// Valeur par défaut pour la classe User elle-même si elle est instanciée directement
@DiscriminatorValue("USER") 
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
        this.role = Role.Admin; // Par défaut, rôle Admin pour un User générique
    }

    // Constructeur complet
    public User(int id, String name, String email, String password, Date registrationDate) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.password = password;
        this.registrationDate = registrationDate;
        this.role = Role.Admin;
    }

    // Constructeur sans ID
    public User(String name, String email, String password, Date registrationDate) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.registrationDate = registrationDate;
        this.role = Role.Admin;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return id == user.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
