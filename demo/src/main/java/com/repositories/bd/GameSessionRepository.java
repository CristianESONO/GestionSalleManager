package com.repositories.bd;

import com.core.MysqlDb;
import com.entities.Client;
import com.entities.Game;
import com.entities.GameSession;
import com.repositories.IGameSessionRepository;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class GameSessionRepository extends MysqlDb implements IGameSessionRepository {

    // Déclaration des requêtes SQL
    private final String SQL_SELECT_ALL = "SELECT * FROM game_sessions";
    private final String SQL_INSERT = "INSERT INTO game_sessions (client_id, game_id, paidDuration, remainingTime, status) VALUES (?, ?, ?, ?, ?)";
    private final String SQL_SELECT_BY_ID = "SELECT * FROM game_sessions WHERE id = ?";
    private final String SQL_UPDATE = "UPDATE game_sessions SET client_id = ?, game_id = ?, paidDuration = ?, remainingTime = ?, status = ? WHERE id = ?";
    private final String SQL_DELETE = "DELETE FROM game_sessions WHERE id = ?";
    private final String SQL_REDUCE_TIME = "UPDATE game_sessions SET remainingTime = ? WHERE id = ?";
    private final String SQL_EXISTS_BY_ID = "SELECT COUNT(*) FROM game_sessions WHERE id = ?";

    // Repositories pour Client et Game
    private final ClientRepository clientRepository;
    private final GameRepository gameRepository;

    // Verrou pour la synchronisation
    private final Object lock = new Object();

    public GameSessionRepository() {
        this.clientRepository = new ClientRepository();
        this.gameRepository = new GameRepository();
    }

    @Override
    public List<GameSession> getAllGameSessions() {
        synchronized (lock) {
            List<GameSession> gameSessions = new ArrayList<>();
            this.openConnexionBD();
            try {
                PreparedStatement ps = conn.prepareStatement(SQL_SELECT_ALL);
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    GameSession gameSession = mapResultSetToGameSession(rs);
                    gameSessions.add(gameSession);
                }
                rs.close();
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                this.closeConnexionBD();
            }
            return gameSessions;
        }
    }

    @Override
    public GameSession addGameSession(GameSession gameSession) throws Exception {
        synchronized (lock) {
            this.openConnexionBD();
            try {
                PreparedStatement ps = conn.prepareStatement(SQL_INSERT, PreparedStatement.RETURN_GENERATED_KEYS);
                ps.setInt(1, gameSession.getClient().getId());
                ps.setInt(2, gameSession.getGame().getId());
                ps.setLong(3, gameSession.getPaidDuration().toMinutes());
                ps.setLong(4, gameSession.getRemainingTime().toMinutes());
                ps.setString(5, gameSession.getStatus());
                ps.executeUpdate();

                // Récupérer l'ID généré
                ResultSet rs = ps.getGeneratedKeys();
                if (rs.next()) {
                    gameSession.setId(rs.getInt(1));
                }
                rs.close();
                return gameSession;
            } catch (SQLException e) {
                e.printStackTrace();
                throw new Exception("Erreur lors de l'ajout de la session de jeu", e);
            } finally {
                this.closeConnexionBD();
            }
        }
    }

    @Override
    public GameSession getGameSessionById(int id) {
        synchronized (lock) {
            GameSession gameSession = null;
            this.openConnexionBD();
            try {
                PreparedStatement ps = conn.prepareStatement(SQL_SELECT_BY_ID);
                ps.setInt(1, id);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    gameSession = mapResultSetToGameSession(rs);
                }
                rs.close();
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                this.closeConnexionBD();
            }
            return gameSession;
        }
    }

    @Override
    public boolean updateGameSession(GameSession gameSession) {
        synchronized (lock) {
            this.openConnexionBD();
            try {
                PreparedStatement ps = conn.prepareStatement(SQL_UPDATE);
                ps.setInt(1, gameSession.getClient().getId());
                ps.setInt(2, gameSession.getGame().getId());
                ps.setLong(3, gameSession.getPaidDuration().toMinutes());
                ps.setLong(4, gameSession.getRemainingTime().toMinutes());
                ps.setString(5, gameSession.getStatus());
                ps.setInt(6, gameSession.getId());
                int rowsUpdated = ps.executeUpdate();
                return rowsUpdated > 0;
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                this.closeConnexionBD();
            }
            return false;
        }
    }

    @Override
    public boolean deleteGameSession(int id) {
        synchronized (lock) {
            this.openConnexionBD();
            try {
                PreparedStatement ps = conn.prepareStatement(SQL_DELETE);
                ps.setInt(1, id);
                int rowsDeleted = ps.executeUpdate();
                return rowsDeleted > 0;
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                this.closeConnexionBD();
            }
            return false;
        }
    }

    @Override
    public boolean reduceRemainingTime(int gameSessionId, Duration timeElapsed) throws Exception {
        synchronized (lock) {
            this.openConnexionBD();
            try {
                GameSession gameSession = getGameSessionById(gameSessionId);
                if (gameSession != null) {
                    Duration newRemainingTime = gameSession.getRemainingTime().minus(timeElapsed);
                    if (newRemainingTime.isNegative()) {
                        throw new IllegalArgumentException("Le temps restant ne peut pas être négatif.");
                    }

                    PreparedStatement ps = conn.prepareStatement(SQL_REDUCE_TIME);
                    ps.setLong(1, newRemainingTime.toMinutes());
                    ps.setInt(2, gameSessionId);
                    int rowsUpdated = ps.executeUpdate();
                    return rowsUpdated > 0;
                }
            } catch (SQLException e) {
                e.printStackTrace();
                throw new Exception("Erreur lors de la réduction du temps restant", e);
            } finally {
                this.closeConnexionBD();
            }
            return false;
        }
    }

    @Override
    public boolean existsById(int id) {
        synchronized (lock) {
            this.openConnexionBD();
            try {
                PreparedStatement ps = conn.prepareStatement(SQL_EXISTS_BY_ID);
                ps.setInt(1, id);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
                rs.close();
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                this.closeConnexionBD();
            }
            return false;
        }
    }

    // Méthode utilitaire pour mapper un ResultSet à un objet GameSession
    private GameSession mapResultSetToGameSession(ResultSet rs) throws SQLException {
        GameSession gameSession = new GameSession();
        gameSession.setId(rs.getInt("id"));

        // Charger les objets Client et Game
        int clientId = rs.getInt("client_id");
        int gameId = rs.getInt("game_id");
        Client client = clientRepository.findById(clientId);
        Game game = gameRepository.findById(gameId);

        gameSession.setClient(client);
        gameSession.setGame(game);
        gameSession.setPaidDuration(Duration.ofMinutes(rs.getLong("paidDuration")));
        gameSession.setRemainingTime(Duration.ofMinutes(rs.getLong("remainingTime")));
        gameSession.setStatus(rs.getString("status"));
        return gameSession;
    }
}