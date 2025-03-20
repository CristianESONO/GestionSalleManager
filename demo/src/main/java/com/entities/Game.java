package com.entities;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "games")
public class Game {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)  // Génération automatique de l'ID
    private int id;
    
    private String name;
    
    private String description;
    
    private String type;
    
    private String status;
    
    private String imagePath; // Chemin de l'image du jeu

    // Collections pour les relations
    @OneToMany(mappedBy = "game", fetch = FetchType.LAZY)
    private List<GameSession> gameSessions;

    // Relation ManyToMany avec Poste
    @ManyToMany(mappedBy = "games")
    private List<Poste> postes;
  

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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<Poste> getPostes() {
        return postes;
    }

    public void setPostes(List<Poste> postes) {
        this.postes = postes;
    }

      // Méthode pour ajouter un poste
    public void addPoste(Poste poste) {
        if (postes == null) {
            postes = new ArrayList<>();
        }
        postes.add(poste);
    }

    // Méthode pour supprimer un poste
    public void removePoste(Poste poste) {
        if (postes != null) {
            postes.remove(poste);
        }
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

    // Constructeur par défaut
    public Game() { }

    // Constructeur avec paramètres
    public Game(int id, String name, String description, String type, String status, String imagePath) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.type = type;
        this.status = status;
        this.imagePath = imagePath;
    }

    // Constructeur avec gameSessions
    public Game(int id, String name, String description, String type, String status, String imagePath, List<GameSession> gameSessions) {
        this(id, name, description, type, status, imagePath); // Appel du constructeur de base
        this.gameSessions = gameSessions;
    }

    // Constructeur avec gameSessions sans spécifier l'ID
    public Game(String name, String description, String type, String status, String imagePath, List<GameSession> gameSessions) {
        this(name, description, type, status, imagePath); // Appel du constructeur de base
        this.gameSessions = gameSessions;
    }

    // Constructeur sans gameSessions
    public Game(String name, String description, String type, String status, String imagePath) {
        this.name = name;
        this.description = description;
        this.type = type;
        this.status = status;
        this.imagePath = imagePath;
    }

    @Override
    public String toString() {
        return name;
    }
}
