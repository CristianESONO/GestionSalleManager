package com.repositories;

import com.entities.Categorie;
import java.util.List;

public interface ICategorieRepository {
    Categorie insert(Categorie categorie);
    List<Categorie> findAll();
    Categorie findById(int id);
    void update(Categorie categorie);
    void delete(Categorie categorie);
    boolean existsByNom(String nom);
    Categorie findByNom(String nom);
}
