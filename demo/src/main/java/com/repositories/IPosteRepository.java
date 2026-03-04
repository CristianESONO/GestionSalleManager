package com.repositories;

import com.entities.Game;
import com.entities.Poste;
import java.util.List;

public interface IPosteRepository {
    Poste insert(Poste poste);
    List<Poste> findAll();
    Poste findById(int id);
    void update(Poste poste);
    void delete(Poste poste);
    boolean checkAvailability(int id);
    List<Poste> findByGame(Game game);
    List<Poste> findPostesSansJeux();

}
