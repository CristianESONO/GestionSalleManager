package com.entities;

import java.util.List;

import jakarta.persistence.*;

@Entity
@Table(name = "postes")
public class Poste {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)  // Génération automatique de l'ID
    private int id;

    private String status;  // État du poste (exemple : "En service", "Hors service")
    
    private boolean isAvailable;  // Spécifie si le poste est disponible

    // Relation ManyToMany avec Game
    @ManyToMany
    @JoinTable(
        name = "poste_game",  // Nom de la table de jointure
        joinColumns = @JoinColumn(name = "poste_id"),  // Colonne pour l'ID du poste
        inverseJoinColumns = @JoinColumn(name = "game_id")  // Colonne pour l'ID du jeu
    )
    private List<Game> games;

    // Getters et setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public List<Game> getGames() {
        return games;
    }

    public void setGames(List<Game> games) {
        this.games = games;
    }


    public String getName() {
        return "Poste N°" + id;  // Génère automatiquement le nom du poste
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public boolean isAvailable() {
        return isAvailable;
    }

    public void setAvailable(boolean available) {
        isAvailable = available;
    }

    // Constructeurs
    public Poste() { }

    public Poste(int id, String status, boolean isAvailable) {
        this.id = id;
        this.status = status;
        this.isAvailable = isAvailable;
    }

    @Override
    public String toString() {
        return getName();
    }

}
