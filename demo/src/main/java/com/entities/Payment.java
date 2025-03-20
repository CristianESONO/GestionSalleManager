package com.entities;

import jakarta.persistence.*;
import java.util.Date;

@Entity
@Table(name = "payments")
public class Payment {

    // Identifiant unique du paiement
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)  // Génération automatique de l'ID
    private int id; 

    // Numéro du ticket généré
    private String numeroTicket; 
    
    // Date et heure du paiement
    private Date dateHeure; 
    
    // Montant total payé
    private double montantTotal; 
    
    // Mode de paiement (Cash, Carte, Mobile Money, etc.)
    private String modePaiement; 
    
    // Client associé au paiement
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id")  // La colonne client_id sera utilisée pour la relation
    private Client client; 
    
    // Liste des produits achetés sous forme de texte
    private String detailsProduits; 
    
    // Constructeur par défaut
    public Payment() { }

    // Constructeur avec paramètres
    public Payment(int id, String numeroTicket, Date dateHeure, double montantTotal, String modePaiement, Client client, String detailsProduits) {
        this.id = id;
        this.numeroTicket = numeroTicket;
        this.dateHeure = dateHeure;
        this.montantTotal = montantTotal;
        this.modePaiement = modePaiement;
        this.client = client;
        this.detailsProduits = detailsProduits;
    }

    // Constructeur sans Client pour les cas où le client est encore inconnu
    public Payment(int id, String numeroTicket, Date dateHeure, double montantTotal, String modePaiement, String detailsProduits) {
        this.id = id;
        this.numeroTicket = numeroTicket;
        this.dateHeure = dateHeure;
        this.montantTotal = montantTotal;
        this.modePaiement = modePaiement;
        this.detailsProduits = detailsProduits;
    }

    // Getters et setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNumeroTicket() {
        return numeroTicket;
    }

    public void setNumeroTicket(String numeroTicket) {
        this.numeroTicket = numeroTicket;
    }

    public Date getDateHeure() {
        return dateHeure;
    }

    public void setDateHeure(Date dateHeure) {
        this.dateHeure = dateHeure;
    }

    public double getMontantTotal() {
        return montantTotal;
    }

    public void setMontantTotal(double montantTotal) {
        this.montantTotal = montantTotal;
    }

    public String getModePaiement() {
        return modePaiement;
    }

    public void setModePaiement(String modePaiement) {
        this.modePaiement = modePaiement;
    }

    public Client getClient() {
        return client;
    }

    public void setClient(Client client) {
        this.client = client;
    }


    public String getDetailsProduits() {
        return detailsProduits;
    }

    public void setDetailsProduits(String detailsProduits) {
        this.detailsProduits = detailsProduits;
    }

    // Méthode pour afficher un résumé du paiement
    public String getPaymentSummary() {
        return String.format("Paiement %s de %.2f effectué le %s avec %s. Produits: %s.", 
                             numeroTicket, montantTotal, dateHeure.toString(), modePaiement, detailsProduits);
    }
}
