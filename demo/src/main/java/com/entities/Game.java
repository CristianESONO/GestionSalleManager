package com.entities;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects; // Ajout de l'import pour Objects.hash

@Entity
@Table(name = "games")
public class Game {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    
    private String name;
    
    private String type; // Renommé 'genre' en 'type' si c'est le cas, sinon gardez 'genre'
    
    private String imagePath; // Chemin de l'image du jeu

    @OneToMany(mappedBy = "game", fetch = FetchType.LAZY)
    private List<GameSession> gameSessions;

    // Relation ManyToMany avec Poste
    @ManyToMany(mappedBy = "games", fetch = FetchType.LAZY) // Ajout de FetchType.LAZY pour être explicite
    private List<Poste> postes = new ArrayList<>(); // Initialisation de la collection

    // Constructeur par défaut (OBLIGATOIRE pour JPA)
    public Game() { }

    // Constructeur avec paramètres (sans ID, pour les nouvelles entités)
    public Game(String name, String type, String imagePath) {
        this.name = name;
        this.type = type;
        this.imagePath = imagePath;
    }

    // Constructeur complet avec ID
    public Game(int id, String name, String type, String imagePath) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.imagePath = imagePath;
    }

    // Getters et setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() { // Si vous avez renommé 'genre' en 'type'
        return type;
    }

    public void setType(String type) { // Si vous avez renommé 'genre' en 'type'
        this.type = type;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public List<GameSession> getGameSessions() {
        return gameSessions;
    }

    public void setGameSessions(List<GameSession> gameSessions) {
        this.gameSessions = gameSessions;
    }

    public List<Poste> getPostes() {
        return postes;
    }

    public void setPostes(List<Poste> postes) {
        this.postes = postes;
    }

    // Méthode pour ajouter un poste (utilitaire)
    public void addPoste(Poste poste) {
        if (this.postes == null) {
            this.postes = new ArrayList<>();
        }
        this.postes.add(poste);
        // Assurez-vous aussi que le poste a ce jeu si la relation est bidirectionnelle
        // if (!poste.getGames().contains(this)) {
        //     poste.getGames().add(this);
        // }
    }

    // Méthode pour supprimer un poste (utilitaire)
    public void removePoste(Poste poste) {
        if (this.postes != null) {
            this.postes.remove(poste);
            // if (poste.getGames().contains(this)) {
            //     poste.getGames().remove(this);
            // }
        }
    }

    @Override
    public String toString() {
        return name;
    }

    // --- Ajout de equals() et hashCode() ---
    // Essentiel pour la comparaison correcte des entités par leur ID unique
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Game game = (Game) o;
        return id == game.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
