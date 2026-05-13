package com.repositories;

import com.entities.Produit;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

public interface IProduitRepository {
    // Ajouter un produit
    Produit insert(Produit produit);

    // Récupérer tous les produits
    List<Produit> findAll();

    // Récupérer un produit par son ID
    Produit findById(int id);

    // Mettre à jour un produit
    void update(Produit produit);

    // Supprimer un produit
    void delete(Produit produit);

    // Récupérer les produits en stock
    List<Produit> findProduitsEnStock();

    // Vérifier si un produit existe déjà par son nom
    boolean checkIfProduitExists(String nom);
    

    // ✅ Mettre à jour le prix et l'ancien prix d'un produit
    void updatePrixEtAncienPrix(int produitId, BigDecimal ancienPrix, BigDecimal nouveauPrix);
    void restaurerPrixInitial(int produitId, BigDecimal ancienPrix) throws Exception;

    Produit findByIdWithPromotions(int produitId);

    Set<Produit> findByPromotionId(int promotionId);


}
