package com.entities;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Entity
@Table(name = "produits")
public class Produit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)  // Génération automatique de l'ID
    private int id;  // Clé primaire
    
    private String nom;  // Nom du produit
    
    private BigDecimal prix;  // Prix du produit
    
    private int stock;  // Stock disponible
    
    private LocalDateTime dateAjout;  // Date d'ajout du produit
    
    private String image;  // Image du produit (chemin ou URL)

    // Constructeur par défaut
    public Produit() {}

    // Constructeur avec paramètres
    public Produit(String nom, BigDecimal prix, int stock, String image) {
        this.nom = nom;
        this.prix = prix;
        this.stock = stock;
        this.image = image;  // Ajouter l'image
        this.dateAjout = LocalDateTime.now();  // Initialiser avec la date et l'heure actuelles
    }

    // Méthode pour réduire le stock lors de l'ajout au panier
    public boolean reducerStock(int quantite) {
        if (quantite <= stock) {
            stock -= quantite;  // Réduit le stock si la quantité est disponible
            return true;
        }
        return false;  // Si la quantité demandée est plus grande que le stock
    }

    // Méthode pour augmenter le stock
    public void augmenterStock(int quantite) {
        stock += quantite;
    }

    // Méthode pour afficher les informations du produit
    @Override
    public String toString() {
        return String.format("%s - %.2f FCFA (Stock : %d)", nom, prix, stock);
    }

    // Getters et setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public BigDecimal getPrix() {
        return prix;
    }

    public void setPrix(BigDecimal prix) {
        this.prix = prix;
    }

    public int getStock() {
        return stock;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }

    public LocalDateTime getDateAjout() {
        return dateAjout;
    }

    public void setDateAjout(LocalDateTime dateAjout) {
        this.dateAjout = dateAjout;
    }

    public String getImage() {
        return image;
    }

     public void setImage(String image) {
        this.image = image;
    }

    
}
