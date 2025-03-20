package com.repositories;

import com.entities.Game;
import java.util.List;

public interface IGameRepository {
    List<Game> findAll();
    void insert(Game game);
    Game findById(int id);
    void update(Game game);
    void delete(int id);
    List<Game> findByStatus(String status);
    void addPosteToGame(int gameId, int posteId);
    void removePosteFromGame(int gameId, int posteId);
    boolean existsByName(String name);
}
