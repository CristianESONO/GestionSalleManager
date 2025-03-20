package com.repositories.bd;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.core.MysqlDb;
import com.entities.Client;
import com.entities.Game;
import com.entities.Poste;
import com.entities.Reservation;
import com.repositories.IReservationRepository;

public class ReservationRepository extends MysqlDb implements IReservationRepository {

    private static final String SQL_INSERT = "INSERT INTO reservations (client_id, reservationDate, duration, codeParrainage, numeroTicket, poste_id, game_id) VALUES (?, ?, ?, ?, ?, ?, ?)";
    private static final String SQL_SELECT = "SELECT * FROM reservations";
    private static final String SQL_SELECT_BY_ID = "SELECT * FROM reservations WHERE id = ?";
    private static final String SQL_UPDATE = "UPDATE reservations SET client_id = ?, reservationDate = ?, duration = ?, codeParrainage = ?, numeroTicket = ?, poste_id = ?, game_id = ? WHERE id = ?";
    private static final String SQL_DELETE = "DELETE FROM reservations WHERE id = ?";
    private static final String SQL_SELECT_BY_CLIENT_ID = "SELECT * FROM reservations WHERE client_id = ?";
    private static final String SQL_CHECK_EXISTS_BY_TICKET = "SELECT COUNT(*) FROM reservations WHERE numeroTicket = ?";
    private static final String SQL_CHECK_EXISTS_BY_ID = "SELECT COUNT(*) FROM reservations WHERE id = ?";

    private final Object lock = new Object();

    @Override
    public Reservation insert(Reservation reservation) {
        synchronized (lock) {
            this.openConnexionBD();
            try {
                PreparedStatement ps = conn.prepareStatement(SQL_INSERT, PreparedStatement.RETURN_GENERATED_KEYS);
                ps.setInt(1, reservation.getClient().getId());
                ps.setString(2, reservation.getReservationDate().toString());
                ps.setLong(3, reservation.getDuration().toMinutes()); // Convertir Duration en minutes
                ps.setString(4, reservation.getCodeParrainage());
                ps.setString(5, reservation.getNumeroTicket());
                ps.setInt(6, reservation.getPoste().getId());
                ps.setInt(7, reservation.getGame().getId());
                ps.executeUpdate();

                ResultSet rs = ps.getGeneratedKeys();
                if (rs.next()) {
                    reservation.setId(rs.getInt(1));
                }
                rs.close();
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                this.closeConnexionBD();
            }
            return reservation;
        }
    }

    @Override
    public List<Reservation> findAll() {
        synchronized (lock) {
            List<Reservation> reservations = new ArrayList<>();
            this.openConnexionBD();
            try {
                PreparedStatement ps = conn.prepareStatement(SQL_SELECT);
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    Reservation reservation = new Reservation();
                    reservation.setId(rs.getInt("id"));
                    reservation.setReservationDate(LocalDateTime.parse(rs.getString("reservationDate")));
                    reservation.setDuration(Duration.ofMinutes(rs.getLong("duration")));
                    reservation.setCodeParrainage(rs.getString("codeParrainage"));
                    reservation.setNumeroTicket(rs.getString("numeroTicket"));

                    // Charger le client en fonction de la clé étrangère
                    int clientId = rs.getInt("client_id");
                    ClientRepository clientRepo = new ClientRepository();
                    Client client = clientRepo.findById(clientId);
                    reservation.setClient(client);

                    // Charger le poste en fonction de la clé étrangère
                    int posteId = rs.getInt("poste_id");
                    PosteRepository posteRepo = new PosteRepository();
                    Poste poste = posteRepo.findById(posteId);
                    reservation.setPoste(poste);

                    // Charger le jeu en fonction de la clé étrangère
                    int gameId = rs.getInt("game_id");
                    GameRepository gameRepo = new GameRepository();
                    Game game = gameRepo.findById(gameId);
                    reservation.setGame(game);
   

                    reservations.add(reservation);
                }
                rs.close();
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                this.closeConnexionBD();
            }
            return reservations;
        }
    }

    @Override
    public Reservation findById(int id) {
        synchronized (lock) {
            Reservation reservation = null;
            this.openConnexionBD();
            try {
                PreparedStatement ps = conn.prepareStatement(SQL_SELECT_BY_ID);
                ps.setInt(1, id);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    reservation = new Reservation();
                    reservation.setId(rs.getInt("id"));
                    reservation.setReservationDate(LocalDateTime.parse(rs.getString("reservationDate")));
                    reservation.setDuration(Duration.ofMinutes(rs.getLong("duration")));
                    reservation.setCodeParrainage(rs.getString("codeParrainage"));
                    reservation.setNumeroTicket(rs.getString("numeroTicket"));

                    // Charger le client en fonction de la clé étrangère
                    int clientId = rs.getInt("client_id");
                    ClientRepository clientRepo = new ClientRepository();
                    Client client = clientRepo.findById(clientId);
                    reservation.setClient(client);

                    // Charger le poste en fonction de la clé étrangère
                    int posteId = rs.getInt("poste_id");
                    PosteRepository posteRepo = new PosteRepository();
                    Poste poste = posteRepo.findById(posteId);
                    reservation.setPoste(poste);

                    // Charger le jeu en fonction de la clé étrangère
                    int gameId = rs.getInt("game_id");
                    GameRepository gameRepo = new GameRepository();
                    Game game = gameRepo.findById(gameId);
                    reservation.setGame(game);
   
                }
                rs.close();
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                this.closeConnexionBD();
            }
            return reservation;
        }
    }

    @Override
    public boolean update(Reservation reservation) {
        synchronized (lock) {
            this.openConnexionBD();
            try {
                PreparedStatement ps = conn.prepareStatement(SQL_UPDATE);
                ps.setInt(1, reservation.getClient().getId());
                ps.setString(2, reservation.getReservationDate().toString());
                ps.setLong(3, reservation.getDuration().toMinutes());
                ps.setString(4, reservation.getCodeParrainage());
                ps.setString(5, reservation.getNumeroTicket());
                ps.setInt(6, reservation.getPoste().getId());
                ps.setInt(7, reservation.getGame().getId());
                ps.setInt(8, reservation.getId());
                ps.executeUpdate();
                ps.close();
                return true;
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                this.closeConnexionBD();
            }
            return false;
        }
    }

    @Override
    public boolean delete(int id) {
        synchronized (lock) {
            this.openConnexionBD();
            try {
                PreparedStatement ps = conn.prepareStatement(SQL_DELETE);
                ps.setInt(1, id);
                ps.executeUpdate();
                return true;
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                this.closeConnexionBD();
            }
            return false;
        }
    }

    @Override
    public List<Reservation> findByClientId(int clientId) {
        synchronized (lock) {
            List<Reservation> reservations = new ArrayList<>();
            this.openConnexionBD();
            try {
                PreparedStatement ps = conn.prepareStatement(SQL_SELECT_BY_CLIENT_ID);
                ps.setInt(1, clientId);
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    Reservation reservation = new Reservation();
                    reservation.setId(rs.getInt("id"));
                    reservation.setReservationDate(LocalDateTime.parse(rs.getString("reservationDate")));
                    reservation.setDuration(Duration.ofMinutes(rs.getLong("duration")));
                    reservation.setCodeParrainage(rs.getString("codeParrainage"));
                    reservation.setNumeroTicket(rs.getString("numeroTicket"));

                    // Charger le client en fonction de la clé étrangère
                    ClientRepository clientRepo = new ClientRepository();
                    Client client = clientRepo.findById(clientId);
                    reservation.setClient(client);

                    // Charger le poste en fonction de la clé étrangère
                    int posteId = rs.getInt("poste_id");
                    PosteRepository posteRepo = new PosteRepository();
                    Poste poste = posteRepo.findById(posteId);
                    reservation.setPoste(poste);

                    // Charger le jeu en fonction de la clé étrangère
                    int gameId = rs.getInt("game_id");
                    GameRepository gameRepo = new GameRepository();
                    Game game = gameRepo.findById(gameId);
                    reservation.setGame(game);

                    reservations.add(reservation);
                }
                rs.close();
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                this.closeConnexionBD();
            }
            return reservations;
        }
    }

    @Override
    public boolean existsByTicketNumber(String numeroTicket) {
        synchronized (lock) {
            this.openConnexionBD();
            try {
                PreparedStatement ps = conn.prepareStatement(SQL_CHECK_EXISTS_BY_TICKET);
                ps.setString(1, numeroTicket);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            } catch (SQLException e) {
                e.printStackTrace();
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
                PreparedStatement ps = conn.prepareStatement(SQL_CHECK_EXISTS_BY_ID);
                ps.setInt(1, id);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                this.closeConnexionBD();
            }
            return false;
        }
    }
}