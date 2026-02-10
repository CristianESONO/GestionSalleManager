package com.repositories.bd;

import com.core.JpaUtil;
import com.entities.GameSession;
import com.entities.Poste;
import com.repositories.IGameSessionRepository;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;
import java.time.Duration;
import java.util.List;

public class GameSessionRepositoryJpa implements IGameSessionRepository {

    @Override
    public List<GameSession> getAllGameSessions() {
        EntityManager em = JpaUtil.getEntityManager();
        List<GameSession> gameSessions = null;
        try {
            gameSessions = em.createQuery(
                "SELECT gs FROM GameSession gs " +
                "LEFT JOIN FETCH gs.client " +
                "LEFT JOIN FETCH gs.game " +
                "LEFT JOIN FETCH gs.poste " +
                "LEFT JOIN FETCH gs.reservation", GameSession.class)
                .getResultList();
        } catch (Exception e) {
            System.err.println("Erreur lors de la récupération de toutes les sessions de jeu : " + e.getMessage());
            e.printStackTrace();
        } finally {
            em.close();
        }
        return gameSessions;
    }

    @Override
    public GameSession findGameSessionByIdWithRelations(int id) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            return em.createQuery(
                "SELECT gs FROM GameSession gs " +
                "LEFT JOIN FETCH gs.reservation r " +
                "LEFT JOIN FETCH r.client c " +
                "LEFT JOIN FETCH r.appliedPromotion ap " +
                "LEFT JOIN FETCH r.game g " +
                "LEFT JOIN FETCH r.poste p " +
                "LEFT JOIN FETCH p.games pg " +
                "WHERE gs.id = :id", GameSession.class)
                .setParameter("id", id)
                .getSingleResult();
        } catch (NoResultException e) {
            return null;
        } finally {
            em.close();
        }
    }

    @Override
    public GameSession addGameSession(GameSession gameSession) throws Exception {
        EntityManager em = JpaUtil.getEntityManager();
        EntityTransaction transaction = null;
        int retryCount = 0;
        final int maxRetries = 3;
        
        while (retryCount < maxRetries) {
            try {
                transaction = em.getTransaction();
                transaction.begin();
                
                if (gameSession.getPoste() != null && !em.contains(gameSession.getPoste())) {
                    gameSession.setPoste(em.merge(gameSession.getPoste()));
                }
                if (gameSession.getClient() != null && !em.contains(gameSession.getClient())) {
                    gameSession.setClient(em.merge(gameSession.getClient()));
                }
                
                em.persist(gameSession);
                em.flush();
                transaction.commit();
                
                System.out.println("Session ajoutée. ID: " + gameSession.getId());
                return gameSession;
                
            } catch (Exception e) {
                if (transaction != null && transaction.isActive()) {
                    try {
                        transaction.rollback();
                    } catch (Exception rollbackEx) {
                        System.err.println("Erreur lors du rollback: " + rollbackEx.getMessage());
                    }
                }
                
                if (e.getCause() instanceof org.sqlite.SQLiteException 
                    && e.getCause().getMessage().contains("locked")) {
                    
                    retryCount++;
                    System.out.println("Tentative " + retryCount + "/" + maxRetries + " - Base verrouillée, nouvelle tentative...");
                    
                    if (retryCount < maxRetries) {
                        try {
                            Thread.sleep(100 * retryCount);
                        } catch (InterruptedException ie) {
                            Thread.currentThread().interrupt();
                        }
                        continue;
                    }
                }
                
                System.err.println("Échec après " + retryCount + " tentatives: " + e.getMessage());
                throw new RuntimeException("Échec de l'ajout de la session", e);
            } finally {
                if (em != null && em.isOpen() && (transaction == null || !transaction.isActive())) {
                    em.close();
                }
            }
        }
        return null;
    }

    @Override
    public GameSession findActiveSessionForPoste(int posteId) {
        EntityManager em = JpaUtil.getEntityManager();
        GameSession session = null;
        try {
            TypedQuery<GameSession> query = em.createQuery(
                "SELECT gs FROM GameSession gs WHERE gs.poste.id = :posteId AND gs.status = 'Active'", GameSession.class);
            query.setParameter("posteId", posteId);
            session = query.getSingleResult();
        } catch (NoResultException e) {
            session = null;
        } catch (Exception e) {
            System.err.println("Erreur lors de la recherche de la session active pour le poste : " + e.getMessage());
            e.printStackTrace();
        } finally {
            em.close();
        }
        return session;
    }

    @Override
    public List<GameSession> findPausedSessionsByPoste(Poste poste) {
        EntityManager em = JpaUtil.getEntityManager();
        List<GameSession> sessions = null;
        try {
            TypedQuery<GameSession> query = em.createQuery(
                "SELECT gs FROM GameSession gs WHERE gs.poste = :poste AND gs.status = 'En pause'", GameSession.class);
            query.setParameter("poste", poste);
            sessions = query.getResultList();
        } catch (Exception e) {
            System.err.println("Erreur lors de la recherche des sessions en pause par poste : " + e.getMessage());
            e.printStackTrace();
            sessions = List.of(); // Retourne une liste vide en cas d'erreur
        } finally {
            em.close();
        }
        return sessions;
    }

    @Override
    public GameSession getGameSessionById(int id) {
        EntityManager em = JpaUtil.getEntityManager();
        GameSession gameSession = null;
        try {
            gameSession = em.find(GameSession.class, id);
            if (gameSession != null) {
                if (gameSession.getClient() != null) gameSession.getClient().getId();
                if (gameSession.getGame() != null) gameSession.getGame().getId();
                if (gameSession.getPoste() != null) gameSession.getPoste().getId();
                if (gameSession.getReservation() != null) gameSession.getReservation().getId();
            }
        } catch (Exception e) {
            System.err.println("Erreur lors de la recherche de la session de jeu par ID : " + e.getMessage());
            e.printStackTrace();
        } finally {
            em.close();
        }
        return gameSession;
    }

    @Override
    public boolean updateGameSession(GameSession gameSession) {
        EntityManager em = JpaUtil.getEntityManager();
        EntityTransaction transaction = null;
        try {
            transaction = em.getTransaction();
            transaction.begin();
            em.merge(gameSession);
            transaction.commit();
            System.out.println("Session de jeu mise à jour avec succès. ID: " + gameSession.getId());
            return true;
        } catch (Exception e) {
            if (transaction != null && transaction.isActive()) {
                transaction.rollback();
            }
            System.err.println("Erreur lors de la mise à jour de la session de jeu : " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Erreur lors de la mise à jour de la session de jeu", e);
        } finally {
            em.close();
        }
    }

    @Override
    public boolean deleteGameSession(int id) {
        EntityManager em = JpaUtil.getEntityManager();
        EntityTransaction transaction = null;
        try {
            transaction = em.getTransaction();
            transaction.begin();
            GameSession gameSession = em.find(GameSession.class, id);
            if (gameSession != null) {
                em.remove(gameSession);
            }
            transaction.commit();
            System.out.println("Session de jeu supprimée avec succès. ID: " + id);
            return true;
        } catch (Exception e) {
            if (transaction != null && transaction.isActive()) {
                transaction.rollback();
            }
            System.err.println("Erreur lors de la suppression de la session de jeu : " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Erreur lors de la suppression de la session de jeu", e);
        } finally {
            em.close();
        }
    }

    @Override
    public boolean existsById(int id) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            GameSession gameSession = em.find(GameSession.class, id);
            return gameSession != null;
        } catch (Exception e) {
            System.err.println("Erreur lors de la vérification de l'existence de la session de jeu par ID : " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            em.close();
        }
    }

    @Override
    public boolean reduceRemainingTime(int gameSessionId, Duration timeElapsed) throws Exception {
        EntityManager em = JpaUtil.getEntityManager();
        EntityTransaction transaction = null;
        try {
            transaction = em.getTransaction();
            transaction.begin();
            GameSession gameSession = em.find(GameSession.class, gameSessionId);
            if (gameSession != null) {
                Duration currentPaidDuration = gameSession.getPaidDuration();
                if (currentPaidDuration != null && currentPaidDuration.compareTo(timeElapsed) >= 0) {
                    gameSession.setPaidDuration(currentPaidDuration.minus(timeElapsed));
                    if (gameSession.getPaidDuration().isZero() || gameSession.getPaidDuration().isNegative()) {
                        gameSession.setStatus("Completed");
                        gameSession.setEndTime(gameSession.getStartTime().plus(currentPaidDuration));
                    }
                    em.merge(gameSession);
                    transaction.commit();
                    System.out.println("Temps restant de la session " + gameSessionId + " réduit avec succès.");
                    return true;
                } else if (currentPaidDuration != null && currentPaidDuration.compareTo(Duration.ZERO) > 0) {
                    gameSession.setPaidDuration(Duration.ZERO);
                    gameSession.setStatus("Completed");
                    gameSession.setEndTime(gameSession.getStartTime().plus(currentPaidDuration));
                    em.merge(gameSession);
                    transaction.commit();
                    System.out.println("Session " + gameSessionId + " terminée car temps écoulé supérieur à la durée payée.");
                    return true;
                } else {
                    System.out.println("Session " + gameSessionId + " déjà terminée ou durée payée nulle.");
                    transaction.rollback();
                    return false;
                }
            } else {
                System.err.println("Session de jeu non trouvée pour la réduction du temps avec ID: " + gameSessionId);
                transaction.rollback();
                return false;
            }
        } catch (Exception e) {
            if (transaction != null && transaction.isActive()) {
                transaction.rollback();
            }
            System.err.println("Erreur lors de la réduction du temps restant de la session de jeu : " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Erreur lors de la réduction du temps restant de la session de jeu", e);
        } finally {
            em.close();
        }
    }

    @Override
    public List<GameSession> findGameSessionsByClientId(int clientId) {
        EntityManager em = JpaUtil.getEntityManager();
        List<GameSession> gameSessions = null;
        try {
            TypedQuery<GameSession> query = em.createQuery(
                "SELECT gs FROM GameSession gs LEFT JOIN FETCH gs.client WHERE gs.client.id = :clientId", GameSession.class);
            query.setParameter("clientId", clientId);
            gameSessions = query.getResultList();
        } catch (Exception e) {
            System.err.println("Erreur lors de la recherche des sessions de jeu par ID client : " + e.getMessage());
            e.printStackTrace();
        } finally {
            em.close();
        }
        return gameSessions;
    }

   @Override
public List<GameSession> findPausedSessionsByClientIdWithRelations(int clientId) {
    EntityManager em = JpaUtil.getEntityManager();
    List<GameSession> gameSessions = null;
    try {
        TypedQuery<GameSession> query = em.createQuery(
            "SELECT gs FROM GameSession gs " +
            "LEFT JOIN FETCH gs.client " +
            "LEFT JOIN FETCH gs.game " +
            "LEFT JOIN FETCH gs.poste " +
            "LEFT JOIN FETCH gs.reservation r " +
            "LEFT JOIN FETCH r.game " +
            "LEFT JOIN FETCH r.poste " +
            "WHERE gs.client.id = :clientId AND gs.status = 'En pause'", GameSession.class);
        query.setParameter("clientId", clientId);
        gameSessions = query.getResultList();

        // Ajouter des logs pour vérifier les résultats
        System.out.println("Nombre de sessions en pause trouvées pour le client " + clientId + ": " + gameSessions.size());
        for (GameSession session : gameSessions) {
            System.out.println("Session ID: " + session.getId());
            System.out.println("Statut: " + session.getStatus());
            System.out.println("Temps restant: " + (session.getPausedRemainingTime() != null ? session.getPausedRemainingTime().toMinutes() : "null") + " minutes");
            System.out.println("Client: " + (session.getClient() != null ? session.getClient().getName() : "null"));
            System.out.println("Jeu: " + (session.getGame() != null ? session.getGame().getName() : "null"));
            System.out.println("Poste: " + (session.getPoste() != null ? session.getPoste().getName() : "null"));
            System.out.println("Réservation: " + (session.getReservation() != null ? session.getReservation().getNumeroTicket() : "null"));
        }
    } catch (Exception e) {
        System.err.println("Erreur lors de la recherche des sessions en pause par ID client : " + e.getMessage());
        e.printStackTrace();
    } finally {
        em.close();
    }
    return gameSessions;
}


}