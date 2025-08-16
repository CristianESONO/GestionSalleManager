package com.entities;

import javax.persistence.*;
import java.time.LocalDate; // Importation pour LocalDate
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "promotions")
public class Promotion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private String nom;

    private boolean actif;

    private float tauxReduction;

    @Column(name = "date_debut") // Nouvelle colonne pour la date de début
    private LocalDate dateDebut;

    @Column(name = "date_fin") // Nouvelle colonne pour la date de fin
    private LocalDate dateFin;

    @ManyToMany(mappedBy = "promotions", fetch = FetchType.LAZY)
    private Set<Produit> produits = new HashSet<>();

    // Relation ManyToMany avec Reservation (pour appliquer la promotion aux réservations)
    // Pas besoin de mappedBy ici si Promotion est la "owning side" pour les réservations,
    // mais dans votre cas, c'est la Réservation qui aura une référence à la Promotion appliquée.
    // Pour l'instant, nous n'ajouterons pas directement Set<Reservation> ici car la relation est unidirectionnelle depuis Reservation.

    public Promotion() {}

    public Promotion(String nom, boolean actif, float tauxReduction, LocalDate dateDebut, LocalDate dateFin) {
        this.nom = nom;
        this.actif = actif;
        this.tauxReduction = tauxReduction;
        this.dateDebut = dateDebut;
        this.dateFin = dateFin;
    }

    // === GETTERS & SETTERS ===

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

    public boolean isActif() {
        return actif;
    }

    public void setActif(boolean actif) {
        this.actif = actif;
    }

    public float getTauxReduction() {
        return tauxReduction;
    }

    public void setTauxReduction(float tauxReduction) {
        if (tauxReduction < 0 || tauxReduction > 1) {
            throw new IllegalArgumentException("Le taux de réduction doit être compris entre 0 et 1.");
        }
        this.tauxReduction = tauxReduction;
    }

    public LocalDate getDateDebut() {
        return dateDebut;
    }

    public void setDateDebut(LocalDate dateDebut) {
        this.dateDebut = dateDebut;
    }

    public LocalDate getDateFin() {
        return dateFin;
    }

   public void setDateFin(LocalDate dateFin) {
        if (dateDebut != null && dateFin != null && dateFin.isBefore(dateDebut)) {
            throw new IllegalArgumentException("La date de fin doit être postérieure à la date de début.");
        }
        this.dateFin = dateFin;
    }

    public Set<Produit> getProduits() {
        return produits;
    }

    public void setProduits(Set<Produit> produits) {
        this.produits = produits;
    }

    public void addProduit(Produit produit) {
        this.produits.add(produit);
        // Important: assurez-vous que Produit a aussi un Set<Promotion> et une méthode addPromotion
        if (!produit.getPromotions().contains(this)) {
            produit.getPromotions().add(this);
        }
    }

    public void removeProduit(Produit produit) {
        this.produits.remove(produit);
        // Important: assurez-vous que Produit a aussi un Set<Promotion> et une méthode removePromotion
        if (produit.getPromotions().contains(this)) {
            produit.getPromotions().remove(this);
        }
    }

    /**
     * Restaure les prix originaux de tous les produits associés à cette promotion.
     * Cette méthode doit être appelée quand la promotion est désactivée.
     */
   // Remove this method from Promotion.java
    public void restoreOriginalPrices() {
        for (Produit p : produits) {
            if (p.getAncienPrix() != null) {
                p.setPrix(p.getAncienPrix());
                p.setAncienPrix(null);
            }
        }
    }
    /**
     * Vérifie si la promotion est actuellement valide.
     * Une promotion est valide si elle est active et que la date actuelle est entre sa date de début et sa date de fin (incluses).
     * @return true si la promotion est valide, false sinon.
     */
    public boolean isValid() {
        LocalDate today = LocalDate.now();
        return actif &&
            dateDebut != null && !today.isBefore(dateDebut) &&
            dateFin != null && !today.isAfter(dateFin);
    }

    // --- NOUVELLE MÉTHODE AJOUTÉE ICI ---
    /**
     * Vérifie si la promotion est valide pour une date spécifiée.
     * Une promotion est valide si elle est active et que la 'checkDate' est entre sa date de début et sa date de fin (incluses).
     * @param checkDate La date à vérifier.
     * @return true si la promotion est valide pour la date spécifiée, false sinon.
     */
    public boolean isValid(LocalDate checkDate) {
        return actif &&
            dateDebut != null && !checkDate.isBefore(dateDebut) &&
            dateFin != null && !checkDate.isAfter(dateFin);
    }

    @Override
    public String toString() {
        return "Promotion{" +
            "id=" + id +
            ", nom='" + nom + '\'' +
            ", actif=" + actif +
            ", tauxReduction=" + tauxReduction +
            ", dateDebut=" + dateDebut +
            ", dateFin=" + dateFin +
            '}';
    }

}