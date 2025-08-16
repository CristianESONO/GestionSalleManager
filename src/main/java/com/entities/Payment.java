package com.entities;

import javax.persistence.*;
import java.util.Date;
import java.util.Objects; // Ajout de l'import pour Objects.hash

@Entity
@Table(name = "payments")
public class Payment {

    // Identifiant unique du paiement
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Génération automatique de l'ID
    private int id; 

    // Numéro du ticket généré
    private String numeroTicket; 
    
    // Date et heure du paiement
    @Temporal(TemporalType.TIMESTAMP) // Spécifie que la date/heure doit être stockée avec l'heure
    private Date dateHeure; 
    
    // Montant total payé
    private double montantTotal; 
    
    // Mode de paiement (Cash, Carte, Mobile Money, etc.)
    private String modePaiement; 
    
    // Client associé au paiement
    @ManyToOne(fetch = FetchType.LAZY) // FetchType.LAZY est la valeur par défaut pour ManyToOne
    @JoinColumn(name = "client_id") // La colonne client_id sera utilisée pour la relation
    private Client client; 

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;
    
  

    // Liste des produits achetés sous forme de texte
    // Si ce champ peut être très long, vous pourriez envisager @Column(columnDefinition = "TEXT")
    // ou @Lob si la base de données le supporte pour des CLOBs.
    private String detailsProduits; 
    
    // Constructeur par défaut (OBLIGATOIRE pour JPA)
    public Payment() { }

    // Constructeur avec paramètres (avec ID)
    public Payment(int id, String numeroTicket, Date dateHeure, double montantTotal, String modePaiement, Client client, String detailsProduits, User createdBy) {
        this.id = id;
        this.numeroTicket = numeroTicket;
        this.dateHeure = dateHeure;
        this.montantTotal = montantTotal;
        this.modePaiement = modePaiement;
        this.client = client;
        this.detailsProduits = detailsProduits;
        this.createdBy = createdBy;
    }

    // Constructeur sans Client pour les cas où le client est encore inconnu ou optionnel
    public Payment(int id, String numeroTicket, Date dateHeure, double montantTotal, String modePaiement, String detailsProduits, User createdBy) {
        this.id = id;
        this.numeroTicket = numeroTicket;
        this.dateHeure = dateHeure;
        this.montantTotal = montantTotal;
        this.modePaiement = modePaiement;
        this.detailsProduits = detailsProduits;
        this.createdBy = createdBy;
    }
    
    // Constructeur pour les nouvelles entités sans ID (l'ID sera généré par la DB)
    public Payment(String numeroTicket, Date dateHeure, double montantTotal, String modePaiement, Client client, String detailsProduits, User createdBy) {
        this.numeroTicket = numeroTicket;
        this.dateHeure = dateHeure;
        this.montantTotal = montantTotal;
        this.modePaiement = modePaiement;
        this.client = client;
        this.detailsProduits = detailsProduits;
        this.createdBy = createdBy;
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

      public User getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(User createdBy) {
        this.createdBy = createdBy;
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
    @Override
    public String toString() { // Renommé en toString pour une utilisation plus standard
        return String.format("Paiement %s de %.2f effectué le %s avec %s. Produits: %s.", 
                             numeroTicket, montantTotal, dateHeure != null ? dateHeure.toString() : "N/A", modePaiement, detailsProduits);
    }

    // --- AJOUT IMPORTANT : equals() et hashCode() ---
    // Ces méthodes sont cruciales pour les entités JPA, surtout lorsqu'elles sont
    // utilisées dans des collections (Set, List) ou pour des comparaisons.
    // Elles doivent être basées sur l'ID de l'entité.
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Payment payment = (Payment) o;
        return id == payment.id; // L'égalité est basée sur l'ID unique
    }

    @Override
    public int hashCode() {
        return Objects.hash(id); // Le hashCode doit être cohérent avec equals
    }
}
