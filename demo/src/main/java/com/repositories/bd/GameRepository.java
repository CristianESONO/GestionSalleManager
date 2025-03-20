package com.repositories.bd;

import com.core.MysqlDb;
import com.entities.Game;
import com.repositories.IGameRepository;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class GameRepository extends MysqlDb implements IGameRepository {

    private final String SQL_SELECT = "SELECT * FROM `games`";
    private final String SQL_INSERT = "INSERT INTO `games` (`name`, `description`, `type`, `status`, `imagePath`) VALUES (?, ?, ?, ?, ?)";
    private final String SQL_SELECT_BY_ID = "SELECT * FROM `games` WHERE `id` = ?";
    private final String SQL_UPDATE = "UPDATE `games` SET `name` = ?, `description` = ?, `type` = ?, `status` = ?, `imagePath` = ? WHERE `id` = ?";
    private final String SQL_DELETE = "DELETE FROM `games` WHERE `id` = ?";
    private final String SQL_SELECT_BY_STATUS = "SELECT * FROM `games` WHERE `status` = ?";
    private final String SQL_CHECK_EXISTS = "SELECT COUNT(*) FROM `games` WHERE `name` = ?";

    private final String SQL_INSERT_GAME_POSTE = "INSERT INTO `game_poste` (`game_id`, `poste_id`) VALUES (?, ?)";
    private final String SQL_DELETE_GAME_POSTE = "DELETE FROM `game_poste` WHERE `game_id` = ? AND `poste_id` = ?";

    private final Object lock = new Object();

      // Méthode pour associer un poste à un jeu
      @Override
      public void addPosteToGame(int gameId, int posteId) {
        synchronized (lock) {
            this.openConnexionBD();
            try {
                PreparedStatement ps = conn.prepareStatement(SQL_INSERT_GAME_POSTE);
                ps.setInt(1, gameId);
                ps.setInt(2, posteId);
                ps.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                this.closeConnexionBD();
            }
        }
    }

    // Méthode pour dissocier un poste d'un jeu
    @Override
    public void removePosteFromGame(int gameId, int posteId) {
        synchronized (lock) {
            this.openConnexionBD();
            try {
                PreparedStatement ps = conn.prepareStatement(SQL_DELETE_GAME_POSTE);
                ps.setInt(1, gameId);
                ps.setInt(2, posteId);
                ps.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                this.closeConnexionBD();
            }
        }
    }

    @Override
    public void insert(Game game) {
        synchronized (lock) {
            this.openConnexionBD();
            try {
                PreparedStatement ps = conn.prepareStatement(SQL_INSERT, PreparedStatement.RETURN_GENERATED_KEYS);
                ps.setString(1, game.getName());
                ps.setString(2, game.getDescription());
                ps.setString(3, game.getType());
                ps.setString(4, game.getStatus());
                ps.setString(5, game.getImagePath());
                ps.executeUpdate();

                ResultSet rs = ps.getGeneratedKeys();
                if (rs.next()) {
                    game.setId(rs.getInt(1));
                }
                rs.close();
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                this.closeConnexionBD();
            }
        }
    }

    @Override
    public List<Game> findAll() {
        synchronized (lock) {
            List<Game> games = new ArrayList<>();
            this.openConnexionBD();
            try {
                PreparedStatement ps = conn.prepareStatement(SQL_SELECT);
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    Game game = new Game();
                    game.setId(rs.getInt("id"));
                    game.setName(rs.getString("name"));
                    game.setDescription(rs.getString("description"));
                    game.setType(rs.getString("type"));
                    game.setStatus(rs.getString("status"));
                    game.setImagePath(rs.getString("imagePath"));
                    games.add(game);
                }
                rs.close();
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                this.closeConnexionBD();
            }
            return games;
        }
    }

    @Override
    public Game findById(int id) {
        synchronized (lock) {
            Game game = null;
            this.openConnexionBD();
            try {
                PreparedStatement ps = conn.prepareStatement(SQL_SELECT_BY_ID);
                ps.setInt(1, id);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    game = new Game();
                    game.setId(rs.getInt("id"));
                    game.setName(rs.getString("name"));
                    game.setDescription(rs.getString("description"));
                    game.setType(rs.getString("type"));
                    game.setStatus(rs.getString("status"));
                    game.setImagePath(rs.getString("imagePath"));
                }
                rs.close();
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                this.closeConnexionBD();
            }
            return game;
        }
    }

    @Override
    public void update(Game game) {
        synchronized (lock) {
            this.openConnexionBD();
            try {
                PreparedStatement ps = conn.prepareStatement(SQL_UPDATE);
                ps.setString(1, game.getName());
                ps.setString(2, game.getDescription());
                ps.setString(3, game.getType());
                ps.setString(4, game.getStatus());
                ps.setString(5, game.getImagePath());
                ps.setInt(6, game.getId());
                ps.executeUpdate();
                ps.close();
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                this.closeConnexionBD();
            }
        }
    }

    @Override
    public void delete(int id) {
        synchronized (lock) {
            this.openConnexionBD();
            try {
                PreparedStatement ps = conn.prepareStatement(SQL_DELETE);
                ps.setInt(1, id);
                ps.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                this.closeConnexionBD();
            }
        }
    }

    @Override
    public List<Game> findByStatus(String status) {
        synchronized (lock) {
            List<Game> games = new ArrayList<>();
            this.openConnexionBD();
            try {
                PreparedStatement ps = conn.prepareStatement(SQL_SELECT_BY_STATUS);
                ps.setString(1, status);
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    Game game = new Game();
                    game.setId(rs.getInt("id"));
                    game.setName(rs.getString("name"));
                    game.setDescription(rs.getString("description"));
                    game.setType(rs.getString("type"));
                    game.setStatus(rs.getString("status"));
                    game.setImagePath(rs.getString("imagePath"));
                    games.add(game);
                }
                rs.close();
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                this.closeConnexionBD();
            }
            return games;
        }
    }

    @Override
    public boolean existsByName(String name) {
        synchronized (lock) {
            boolean exists = false;
            this.openConnexionBD();
            try {
                PreparedStatement ps = conn.prepareStatement(SQL_CHECK_EXISTS);
                ps.setString(1, name);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    exists = rs.getInt(1) > 0;
                }
                rs.close();
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                this.closeConnexionBD();
            }
            return exists;
        }
    }
}