package com.repositories;

import com.entities.GameSession;
import com.entities.Poste;

import java.time.Duration;
import java.util.List;

public interface IGameSessionRepository {
    List<GameSession> getAllGameSessions();
    GameSession addGameSession(GameSession gameSession) throws Exception;
    GameSession getGameSessionById(int id);
    boolean updateGameSession(GameSession gameSession);
    boolean deleteGameSession(int id);
    boolean existsById(int id);
    boolean reduceRemainingTime(int gameSessionId, Duration timeElapsed) throws Exception;
    List<GameSession> findGameSessionsByClientId(int clientId);
    List<GameSession> findPausedSessionsByClientIdWithRelations(int clientId);
    List<GameSession> findPausedSessionsByPoste(Poste poste);
    // Dans com.repositories.IGameSessionRepository.java
    GameSession findActiveSessionForPoste(int posteId);
    GameSession findGameSessionByIdWithRelations(int id);
}
