package com.repositories.bd;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.core.MysqlDb;
import com.repositories.IPosteGameRepository;

public class PosteGameRepository extends MysqlDb implements IPosteGameRepository {
    private static final String SQL_INSERT = "INSERT INTO poste_game (poste_id, game_id) VALUES (?, ?)";
    private static final String SQL_DELETE = "DELETE FROM poste_game WHERE poste_id = ? AND game_id = ?";
    private static final String SQL_EXISTS = "SELECT COUNT(*) FROM poste_game WHERE poste_id = ? AND game_id = ?";

    private final Object lock = new Object();

    @Override
    public void addPosteToGame(int gameId, int posteId) {
        synchronized (lock) {
            this.openConnexionBD();
            try (PreparedStatement ps = conn.prepareStatement(SQL_INSERT)) {
                ps.setInt(1, posteId);
                ps.setInt(2, gameId);

                ps.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                this.closeConnexionBD();
            }
        }
    }

    @Override
    public void removePosteFromGame(int gameId, int posteId) {
        synchronized (lock) {
            this.openConnexionBD();
            try (PreparedStatement ps = conn.prepareStatement(SQL_DELETE)) {
                ps.setInt(1, posteId);
                ps.setInt(2, gameId);

                ps.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                this.closeConnexionBD();
            }
        }
    }

    @Override
    public boolean exists(int gameId, int posteId) {
        boolean exists = false;
        synchronized (lock) {
            this.openConnexionBD();
            try (PreparedStatement ps = conn.prepareStatement(SQL_EXISTS)) {
                ps.setInt(1, posteId);
                ps.setInt(2, gameId);

                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        exists = rs.getInt(1) > 0;
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                this.closeConnexionBD();
            }
        }
        return exists;
    }
}
