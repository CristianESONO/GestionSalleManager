package com.repositories.bd;

import com.core.MysqlDb;
import com.entities.Game;
import com.entities.Poste;
import com.repositories.IPosteRepository;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class PosteRepository extends MysqlDb implements IPosteRepository {

    private final String SQL_SELECT = "SELECT * FROM `postes`";
    private final String SQL_INSERT = "INSERT INTO `postes` (`status`, `isAvailable`) VALUES (?, ?)";
    private final String SQL_SELECT_BY_ID = "SELECT * FROM `postes` WHERE `id` = ?";
    private final String SQL_UPDATE = "UPDATE `postes` SET `status` = ?, `isAvailable` = ? WHERE `id` = ?";
    private final String SQL_DELETE = "DELETE FROM `postes` WHERE `id` = ?";
    private final String SQL_CHECK_AVAILABILITY = "SELECT COUNT(*) FROM `postes` WHERE `id` = ? AND `status` = 'En service' AND `isAvailable` = 1";
    private final String SQL_SELECT_BY_GAME = "SELECT p.* FROM postes p " +
    "JOIN poste_game gp ON p.id = gp.poste_id " +
    "WHERE gp.game_id = ?";


    private final Object lock = new Object();

    @Override
    public Poste insert(Poste poste) {
        synchronized (lock) {
            this.openConnexionBD();
            try {
                PreparedStatement ps = conn.prepareStatement(SQL_INSERT, PreparedStatement.RETURN_GENERATED_KEYS);
                ps.setString(1, poste.getStatus());
                ps.setBoolean(2, poste.isAvailable());
                ps.executeUpdate();

                ResultSet rs = ps.getGeneratedKeys();
                if (rs.next()) {
                    poste.setId(rs.getInt(1));
                }
                rs.close();
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                this.closeConnexionBD();
            }
            return poste;
        }
    }

    @Override
     public List<Poste> findByGame(Game game) {
        List<Poste> postes = new ArrayList<>();
        this.openConnexionBD();
        try {
            PreparedStatement ps = conn.prepareStatement(SQL_SELECT_BY_GAME);
            ps.setInt(1, game.getId());
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Poste poste = new Poste();
                poste.setId(rs.getInt("id"));
                poste.setStatus(rs.getString("status"));
                poste.setAvailable(rs.getBoolean("isAvailable"));
                postes.add(poste);
            }
            rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            this.closeConnexionBD();
        }
        return postes;
    }

    @Override
    public List<Poste> findAll() {
        synchronized (lock) {
            List<Poste> postes = new ArrayList<>();
            this.openConnexionBD();
            try {
                PreparedStatement ps = conn.prepareStatement(SQL_SELECT);
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    Poste poste = new Poste();
                    poste.setId(rs.getInt("id"));
                    poste.setStatus(rs.getString("status"));
                    poste.setAvailable(rs.getBoolean("isAvailable"));
                    postes.add(poste);
                }
                rs.close();
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                this.closeConnexionBD();
            }
            return postes;
        }
    }

    @Override
    public Poste findById(int id) {
        synchronized (lock) {
            Poste poste = null;
            this.openConnexionBD();
            try {
                PreparedStatement ps = conn.prepareStatement(SQL_SELECT_BY_ID);
                ps.setInt(1, id);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    poste = new Poste();
                    poste.setId(rs.getInt("id"));
                    poste.setStatus(rs.getString("status"));
                    poste.setAvailable(rs.getBoolean("isAvailable"));
                }
                rs.close();
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                this.closeConnexionBD();
            }
            return poste;
        }
    }

    @Override
    public void update(Poste poste) {
        synchronized (lock) {
            this.openConnexionBD();
            try {
                PreparedStatement ps = conn.prepareStatement(SQL_UPDATE);
                ps.setString(1, poste.getStatus());
                ps.setBoolean(2, poste.isAvailable());
                ps.setInt(3, poste.getId());
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
    public void delete(Poste poste) {
        synchronized (lock) {
            this.openConnexionBD();
            try {
                PreparedStatement ps = conn.prepareStatement(SQL_DELETE);
                ps.setInt(1, poste.getId());
                ps.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                this.closeConnexionBD();
            }
        }
    }

    @Override
    public boolean checkAvailability(int id) {
        synchronized (lock) {
            boolean isAvailable = false;
            this.openConnexionBD();
            try {
                PreparedStatement ps = conn.prepareStatement(SQL_CHECK_AVAILABILITY);
                ps.setInt(1, id);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    isAvailable = rs.getInt(1) > 0;
                }
                rs.close();
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                this.closeConnexionBD();
            }
            return isAvailable;
        }
    }
}