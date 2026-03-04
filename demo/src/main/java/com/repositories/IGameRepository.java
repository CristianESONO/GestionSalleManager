// In IGameRepository.java
package com.repositories;

import com.entities.Game;
import com.entities.Poste; // Import Poste

import java.util.List;

public interface IGameRepository {
    void insert(Game game);
    List<Game> findAll();
    Game findById(int id);
    void update(Game game);
    void delete(int id);
    // Remove List<Game> findByStatus(String status);
    boolean existsByName(String name);
    boolean existsById(int naidme);
    void addPosteToGame(int gameId, int posteId);
    void removePosteFromGame(int gameId, int posteId);
    List<Poste> getPostesByGameId(int gameId); // New method to get postes associated with a game
}