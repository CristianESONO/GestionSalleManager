package com.repositories;

import com.entities.Client;
import java.util.List;

public interface IClientRepository {

    // Ajouter un client
    Client insert(Client client);

    // Récupérer tous les clients
    List<Client> findAll();

    // Récupérer un client par son ID
    Client findById(int id);

    public Client findByTel(String tel);

    // Mettre à jour un client
    void update(Client client);

    // Supprimer un client
    void delete(Client client);

    // Récupérer un client par son email (si nécessaire)
    Client findByEmail(String email);

    public boolean existsByName(String name);

    /** Recherche des clients dont le nom contient la chaîne donnée (insensible à la casse). */
    List<Client> findByNameContaining(String name);

    // Récupérer les clients ayant un certain nombre de points de fidélité
    List<Client> findByLoyaltyPointsGreaterThan(int points);
}
