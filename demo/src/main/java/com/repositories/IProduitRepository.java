package com.repositories;

import com.entities.Produit;
import java.util.List;

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
}
