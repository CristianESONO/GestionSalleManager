package com.repositories.bd;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.core.MysqlDb;
import com.entities.Client;
import com.entities.Role;
import com.repositories.IClientRepository;

public class ClientRepository extends MysqlDb implements IClientRepository {

    private static final String SQL_INSERT = "INSERT INTO clients (name, email, password, role, registrationDate, phone, birthDate, address, loyaltyPoints) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
    private static final String SQL_SELECT = "SELECT * FROM clients";
    private static final String SQL_SELECT_BY_ID = "SELECT * FROM clients WHERE id = ?";
    private static final String SQL_UPDATE = "UPDATE clients SET name = ?, email = ?, password = ?, role = ?, registrationDate = ?, phone = ?, birthDate = ?, address = ?, loyaltyPoints = ? WHERE id = ?";
    private static final String SQL_DELETE = "DELETE FROM clients WHERE id = ?";
    private static final String SQL_SELECT_BY_EMAIL = "SELECT * FROM clients WHERE email = ?";
    private static final String SQL_SELECT_BY_loyaltyPoints = "SELECT * FROM clients WHERE loyaltyPoints > ?";
    private static final String SQL_SELECT_BY_PHONE = "SELECT * FROM clients WHERE phone LIKE ?";
    private final String SQL_EXISTS_BY_NAME = "SELECT COUNT(*) FROM clients WHERE name = ?";

    private final Object lock = new Object();

    @Override
    public Client insert(Client client) {
        synchronized (lock) {
            this.openConnexionBD();
            try {
                PreparedStatement ps = conn.prepareStatement(SQL_INSERT, PreparedStatement.RETURN_GENERATED_KEYS);
                ps.setString(1, client.getName());
                ps.setString(2, client.getEmail());
                ps.setString(3, client.getPassword());
                ps.setString(4, client.getRole().toString());
                ps.setDate(5, new java.sql.Date(client.getRegistrationDate().getTime()));
                ps.setString(6, client.getPhone());
                ps.setDate(7, new java.sql.Date(client.getBirthDate().getTime()));
                ps.setString(8, client.getAddress());
                ps.setInt(9, client.getLoyaltyPoints());
                ps.executeUpdate();

                ResultSet rs = ps.getGeneratedKeys();
                if (rs.next()) {
                    client.setId(rs.getInt(1));
                }
                rs.close();
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                this.closeConnexionBD();
            }
            return client;
        }
    }

    @Override
    public List<Client> findAll() {
        synchronized (lock) {
            List<Client> clients = new ArrayList<>();
            this.openConnexionBD();
            try {
                PreparedStatement ps = conn.prepareStatement(SQL_SELECT);
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    Client client = new Client();
                    client.setId(rs.getInt("id"));
                    client.setName(rs.getString("name"));
                    client.setEmail(rs.getString("email"));
                    client.setPassword(rs.getString("password"));
                    client.setRole(Role.valueOf(rs.getString("role")));
                    client.setRegistrationDate(rs.getDate("registrationDate"));
                    client.setPhone(rs.getString("phone"));
                    client.setBirthDate(rs.getDate("birthDate"));
                    client.setAddress(rs.getString("address"));
                    client.setLoyaltyPoints(rs.getInt("loyaltyPoints"));
                    clients.add(client);
                }
                rs.close();
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                this.closeConnexionBD();
            }
            return clients;
        }
    }

    @Override
    public Client findById(int id) {
        synchronized (lock) {
            Client client = null;
            this.openConnexionBD();
            try {
                PreparedStatement ps = conn.prepareStatement(SQL_SELECT_BY_ID);
                ps.setInt(1, id);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    client = new Client();
                    client.setId(rs.getInt("id"));
                    client.setName(rs.getString("name"));
                    client.setEmail(rs.getString("email"));
                    client.setPassword(rs.getString("password"));
                    client.setRole(Role.valueOf(rs.getString("role")));
                    client.setRegistrationDate(rs.getDate("registrationDate"));
                    client.setPhone(rs.getString("phone"));
                    client.setBirthDate(rs.getDate("birthDate"));
                    client.setAddress(rs.getString("address"));
                    client.setLoyaltyPoints(rs.getInt("loyaltyPoints"));
                }
                rs.close();
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                this.closeConnexionBD();
            }
            return client;
        }
    }

    @Override
    public void update(Client client) {
        synchronized (lock) {
            this.openConnexionBD();
            try {
                PreparedStatement ps = conn.prepareStatement(SQL_UPDATE);
                ps.setString(1, client.getName());
                ps.setString(2, client.getEmail());
                ps.setString(3, client.getPassword());
                ps.setString(4, client.getRole().toString());
                ps.setDate(5, new java.sql.Date(client.getRegistrationDate().getTime()));
                ps.setString(6, client.getPhone());
                ps.setDate(7, new java.sql.Date(client.getBirthDate().getTime()));
                ps.setString(8, client.getAddress());
                ps.setInt(9, client.getLoyaltyPoints());
                ps.setInt(10, client.getId());
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
    public void delete(Client client) {
        synchronized (lock) {
            this.openConnexionBD();
            try {
                PreparedStatement ps = conn.prepareStatement(SQL_DELETE);
                ps.setInt(1, client.getId());
                ps.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                    this.closeConnexionBD();
                }
            }
        }

    @Override
    public Client findByEmail(String email) {
        synchronized (lock) {
            Client client = null;
            this.openConnexionBD();
            try {
                PreparedStatement ps = conn.prepareStatement(SQL_SELECT_BY_EMAIL);
                ps.setString(1, email);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    client = new Client();
                    client.setId(rs.getInt("id"));
                    client.setName(rs.getString("name"));
                    client.setEmail(rs.getString("email"));
                    client.setPassword(rs.getString("password"));
                    client.setRole(Role.valueOf(rs.getString("role")));
                    client.setRegistrationDate(rs.getDate("registrationDate"));
                    client.setPhone(rs.getString("phone"));
                    client.setBirthDate(rs.getDate("birthDate"));
                    client.setAddress(rs.getString("address"));
                    client.setLoyaltyPoints(rs.getInt("loyaltyPoints"));
                }
                rs.close();
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                this.closeConnexionBD();
            }
            return client;
        }
    }

    @Override
    public List<Client> findByLoyaltyPointsGreaterThan(int points) {
        synchronized (lock) {
            List<Client> clients = new ArrayList<>();
            this.openConnexionBD();
            try {
                PreparedStatement ps = conn.prepareStatement(SQL_SELECT_BY_loyaltyPoints);
                ps.setInt(1, points);
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    Client client = new Client();
                    client.setId(rs.getInt("id"));
                    client.setName(rs.getString("name"));
                    client.setEmail(rs.getString("email"));
                    client.setPassword(rs.getString("password"));
                    client.setRole(Role.valueOf(rs.getString("role")));
                    client.setRegistrationDate(rs.getDate("registrationDate"));
                    client.setPhone(rs.getString("phone"));
                    client.setBirthDate(rs.getDate("birthDate"));
                    client.setAddress(rs.getString("address"));
                    client.setLoyaltyPoints(rs.getInt("loyaltyPoints"));
                    clients.add(client);
                }
                rs.close();
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                this.closeConnexionBD();
            }
            return clients;
        }
    }

    @Override
    public Client findByTel(String tel) {
        synchronized (lock) {
            Client client = null;
            this.openConnexionBD();
            try {
                PreparedStatement ps = conn.prepareStatement(SQL_SELECT_BY_PHONE);
                ps.setString(1, "%" + tel + "%");
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    client = new Client();
                    client.setId(rs.getInt("id"));
                    client.setName(rs.getString("name"));
                    client.setEmail(rs.getString("email"));
                    client.setPassword(rs.getString("password"));
                    client.setRole(Role.valueOf(rs.getString("role")));
                    client.setRegistrationDate(rs.getDate("registrationDate"));
                    client.setPhone(rs.getString("phone"));
                    client.setBirthDate(rs.getDate("birthDate"));
                    client.setAddress(rs.getString("address"));
                    client.setLoyaltyPoints(rs.getInt("loyaltyPoints"));
                }
                rs.close();
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                this.closeConnexionBD();
            }
            return client;
        }
    }

    @Override
    public boolean existsByName(String name) {
        synchronized (lock) {
            boolean exists = false;
            this.openConnexionBD();
            try {
                PreparedStatement ps = conn.prepareStatement(SQL_EXISTS_BY_NAME);
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