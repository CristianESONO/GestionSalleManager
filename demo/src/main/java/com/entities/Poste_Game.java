package com.entities;

public class Poste_Game {
    private int posteId;
    private int gameId;

    // Constructeur
    public Poste_Game(int posteId, int gameId) {
        this.posteId = posteId;
        this.gameId = gameId;
    }

    // Getters et Setters
    public int getPosteId() {
        return posteId;
    }

    public void setPosteId(int posteId) {
        this.posteId = posteId;
    }

    public int getGameId() {
        return gameId;
    }

    public void setGameId(int gameId) {
        this.gameId = gameId;
    }

    // Méthode toString pour affichage
    @Override
    public String toString() {
        return "Poste_Game{" +
                "posteId=" + posteId +
                ", gameId=" + gameId +
                '}';
    }
}
