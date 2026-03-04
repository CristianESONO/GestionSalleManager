package com.repositories;

import java.util.List;

import com.entities.Parrain;

public interface IParrainRepository {
    // Ajouter un parrain
    Parrain insert(Parrain parrain);

    // Récupérer tous les parrains
    List<Parrain> findAll();

    // Récupérer un parrain par son ID
    Parrain findById(int id);

    public Parrain findByTel(String tel);

    // Mettre à jour un parrain
    void update(Parrain parrain);

    // Supprimer un parrain
    void delete(Parrain parrain);

    // Récupérer un parrain par son email (si nécessaire)
    Parrain findByEmail(String email);

    // Récupérer un parrain par son code de parrainage
    Parrain findByCodeParrainage(String codeParrainage);
    

    public boolean existsByName(String name);
    
}
