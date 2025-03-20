package com.repositories;

public interface IPosteGameRepository {
    void addPosteToGame(int gameId, int posteId);
    void removePosteFromGame(int gameId, int posteId);
    boolean exists(int gameId, int posteId);
}
