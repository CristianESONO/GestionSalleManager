package com.entities;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects; // Ajout de l'import pour Objects.hash

import javax.persistence.*;

@Entity
@Table(name = "postes")
public class Poste {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    // Ajout du champ 'name' pour stocker le nom du poste
    // Si ce champ est NOT NULL dans votre base de données, assurez-vous de le définir avant l'insertion.
    private String name; 

    private boolean horsService; 

    @ManyToMany(fetch = FetchType.LAZY) // FetchType.LAZY est la valeur par défaut, mais explicite c'est bien.
    @JoinTable(
        name = "poste_game",
        joinColumns = @JoinColumn(name = "poste_id"),
        inverseJoinColumns = @JoinColumn(name = "game_id")
    )
    private List<Game> games = new ArrayList<>();

    // Constructeur par défaut (OBLIGATOIRE pour JPA)
    public Poste() {
        // Initialisation par défaut si nécessaire
    }

    // Constructeur avec ID et horsService
    public Poste(int id, boolean horsService) {
        this.id = id;
        this.horsService = horsService;
        // Le nom peut être défini après l'insertion si basé sur l'ID
        // ou passé comme paramètre si la DB le permet (ex: "Poste N°" + id)
    }

    // Constructeur avec tous les champs (y compris le nom)
    public Poste(int id, String name, boolean horsService) {
        this.id = id;
        this.name = name;
        this.horsService = horsService;
    }
    
    // Constructeur sans ID (pour les nouvelles entités)
    public Poste(String name, boolean horsService) {
        this.name = name;
        this.horsService = horsService;
    }


    // === GETTERS & SETTERS ===
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        // Si le nom est généré comme "Poste N°X" et stocké, retournez-le.
        // Sinon, si le nom n'est PAS une colonne en DB et est TOUJOURS dérivé de l'ID,
        // vous pouvez laisser cette méthode comme elle était.
        // Mais si vous avez ajouté une colonne 'name' dans la DB (ce qui est recommandé),
        // alors cette méthode doit retourner la valeur du champ 'name'.
        return name; // Retourne le champ 'name'
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isHorsService() {
        return horsService;
    }

    public void setHorsService(boolean horsService) {
        this.horsService = horsService;
    }

    public List<Game> getGames() {
        return games;
    }

    public void setGames(List<Game> games) {
        this.games = games;
    }

    @Override
    public String toString() {
        // Utilise le champ 'name' pour l'affichage
        return name != null ? name : "Poste N°" + id; // Fallback si le nom est null
    }

    // --- Ajout de equals() et hashCode() ---
    // Essentiel pour la comparaison correcte des entités par leur ID unique
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Poste poste = (Poste) o;
        return id == poste.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
