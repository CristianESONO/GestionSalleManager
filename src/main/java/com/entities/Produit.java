package com.entities;

import javax.persistence.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Objects; // Ajout de l'import pour Objects.hash
import java.util.Set;

@Entity
@Table(name = "produits")
public class Produit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private String nom;

    private LocalDate dateLimiteConsommation;

    private BigDecimal prix;

    private int stock;

    private LocalDateTime dateAjout;

    private String image;

    @ManyToMany(fetch = FetchType.EAGER) // Chargement immédiat
    @JoinTable(
        name = "promotion_produit",
        joinColumns = @JoinColumn(name = "produit_id"),
        inverseJoinColumns = @JoinColumn(name = "promotion_id")
    )
    private Set<Promotion> promotions = new HashSet<>();


    private BigDecimal ancienPrix;

    public Produit() {}

    public Produit(String nom, BigDecimal prix, int stock, String image, LocalDate dateLimiteConsommation) {
        this.nom = nom;
        this.prix = prix;
        this.stock = stock;
        this.image = image;
        this.dateAjout = LocalDateTime.now();
        this.dateLimiteConsommation = dateLimiteConsommation;
    }

    // === Méthodes métier ===

    public boolean reducerStock(int quantite) {
        if (quantite <= stock) {
            stock -= quantite;
            return true;
        }
        return false;
    }

    public void augmenterStock(int quantite) {
        stock += quantite;
    }

    public void appliquerPromotion() {
        for (Promotion promo : promotions) {
            if (promo.isValid()) {
                this.ancienPrix = this.prix;
                BigDecimal taux = BigDecimal.valueOf(promo.getTauxReduction());
                BigDecimal remise = prix.multiply(taux).setScale(2, RoundingMode.HALF_UP);
                this.prix = prix.subtract(remise);
                break;
            }
        }
    }





    @Override
    public String toString() {
        return String.format("%s - %.2f FCFA (Stock : %d)", nom, prix, stock);
    }

    // === Getters & Setters ===

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

    public LocalDate getDateLimiteConsommation() {
        return dateLimiteConsommation;
    }

    public void setDateLimiteConsommation(LocalDate dateLimiteConsommation) {
        this.dateLimiteConsommation = dateLimiteConsommation;
    }

    public BigDecimal getPrix() {
        return prix;
    }

    public void setPrix(BigDecimal prix) {
        this.prix = prix;
    }

    public BigDecimal getAncienPrix() {
        return ancienPrix;
    }

    public void setAncienPrix(BigDecimal ancienPrix) {
        this.ancienPrix = ancienPrix;
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

    public Set<Promotion> getPromotions() {
        return promotions;
    }

    public void setPromotions(Set<Promotion> promotions) {
        this.promotions = promotions;
    }

    public void addPromotion(Promotion promotion) {
        this.promotions.add(promotion);
    }

    public void removePromotion(Promotion promotion) {
        this.promotions.remove(promotion);
    }

    // --- Ajout de equals() et hashCode() ---
    // Essentiel pour la comparaison correcte des entités par leur ID unique
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Produit produit = (Produit) o;
        return id == produit.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
