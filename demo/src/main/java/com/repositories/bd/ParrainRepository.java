package com.repositories.bd;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.core.MysqlDb;
import com.entities.Client;
import com.entities.Parrain;
import com.entities.Role;
import com.repositories.IParrainRepository;

public class ParrainRepository extends MysqlDb implements IParrainRepository {

    private static final String SQL_INSERT = "INSERT INTO parrains (name, email, password, role, registrationDate, phone, address, codeParrainage) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
    private static final String SQL_SELECT = "SELECT * FROM parrains";
    private static final String SQL_SELECT_BY_ID = "SELECT * FROM parrains WHERE id = ?";
    private static final String SQL_UPDATE = "UPDATE parrains SET name = ?, email = ?, password = ?, role = ?, registrationDate = ?, phone = ?, address = ?, codeParrainage = ? WHERE id = ?";
    private static final String SQL_DELETE = "DELETE FROM parrains WHERE id = ?";
    private static final String SQL_SELECT_BY_EMAIL = "SELECT * FROM parrains WHERE email = ?";
    private static final String SQL_SELECT_BY_PHONE = "SELECT * FROM parrains WHERE phone = ?";
    private static final String SQL_CHECK_EXISTS_BY_NAME = "SELECT COUNT(*) FROM parrains WHERE name = ?";

    private final Object lock = new Object();

    @Override
    public Parrain insert(Parrain parrain) {
        synchronized (lock) {
            this.openConnexionBD();
            try {
                PreparedStatement ps = conn.prepareStatement(SQL_INSERT, PreparedStatement.RETURN_GENERATED_KEYS);
                ps.setString(1, parrain.getName());
                ps.setString(2, parrain.getEmail());
                ps.setString(3, parrain.getPassword());
                ps.setString(4, parrain.getRole().toString());
                ps.setDate(5, new java.sql.Date(parrain.getRegistrationDate().getTime()));
                ps.setString(6, parrain.getPhone());
                ps.setString(7, parrain.getAddress());
                ps.setString(8, parrain.getCodeParrainage());
                ps.executeUpdate();

                ResultSet rs = ps.getGeneratedKeys();
                if (rs.next()) {
                    parrain.setId(rs.getInt(1));
                }
                rs.close();
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                this.closeConnexionBD();
            }
            return parrain;
        }
    }

    @Override
    public List<Parrain> findAll() {
         synchronized (lock) {
            List<Parrain> parrains = new ArrayList<>();
            this.openConnexionBD();
            try {
                PreparedStatement ps = conn.prepareStatement(SQL_SELECT);
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    Parrain parrain = new Parrain();
                    parrain.setId(rs.getInt("id"));
                    parrain.setName(rs.getString("name"));
                    parrain.setEmail(rs.getString("email"));
                    parrain.setPassword(rs.getString("password"));
                    parrain.setRole(Role.valueOf(rs.getString("role")));
                    parrain.setRegistrationDate(rs.getDate("registrationDate"));
                    parrain.setPhone(rs.getString("phone"));
                    parrain.setAddress(rs.getString("address"));
                    parrain.setCodeParrainage(rs.getString("codeParrainage"));
                    parrains.add(parrain);
                }
                rs.close();
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                this.closeConnexionBD();
            }
            return parrains;
        }
    }

    @Override
    public Parrain findById(int id) {
        synchronized (lock) {
            Parrain parrain = null;
            this.openConnexionBD();
            try {
                PreparedStatement ps = conn.prepareStatement(SQL_SELECT_BY_ID);
                ps.setInt(1, id);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    parrain = new Parrain();
                    parrain.setId(rs.getInt("id"));
                    parrain.setName(rs.getString("name"));
                    parrain.setEmail(rs.getString("email"));
                    parrain.setPassword(rs.getString("password"));
                    parrain.setRole(Role.valueOf(rs.getString("role")));
                    parrain.setRegistrationDate(rs.getDate("registrationDate"));
                    parrain.setPhone(rs.getString("phone"));
                    parrain.setAddress(rs.getString("address"));
                    parrain.setCodeParrainage(rs.getString("codeParrainage"));
                }
                rs.close();
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                this.closeConnexionBD();
            }
            return parrain;
        }
    }

    @Override
    public Parrain findByTel(String tel) {
        synchronized (lock) {
            Parrain parrain = null;
            this.openConnexionBD();
            try {
                PreparedStatement ps = conn.prepareStatement(SQL_SELECT_BY_PHONE);
                ps.setString(1, "%" + tel + "%");
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    parrain = new Parrain();
                    parrain.setId(rs.getInt("id"));
                    parrain.setName(rs.getString("name"));
                    parrain.setEmail(rs.getString("email"));
                    parrain.setPassword(rs.getString("password"));
                    parrain.setRole(Role.valueOf(rs.getString("role")));
                    parrain.setRegistrationDate(rs.getDate("registrationDate"));
                    parrain.setPhone(rs.getString("phone"));
                    parrain.setAddress(rs.getString("address"));
                    parrain.setCodeParrainage(rs.getString("codeParrainage"));
                }
                rs.close();
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                this.closeConnexionBD();
            }
            return parrain;
        }
    }

    @Override
    public void update(Parrain parrain) {
        synchronized (lock) {
            this.openConnexionBD();
            try {
                PreparedStatement ps = conn.prepareStatement(SQL_UPDATE);
                ps.setString(1, parrain.getName());
                ps.setString(2, parrain.getEmail());
                ps.setString(3, parrain.getPassword());
                ps.setString(4, parrain.getRole().toString());
                ps.setDate(5, new java.sql.Date(parrain.getRegistrationDate().getTime()));
                ps.setString(6, parrain.getPhone());
                ps.setString(7, parrain.getAddress());
                ps.setString(8, parrain.getCodeParrainage());
                ps.setInt(9, parrain.getId());
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
    public void delete(Parrain parrain) {
        synchronized (lock) {
            this.openConnexionBD();
            try {
                PreparedStatement ps = conn.prepareStatement(SQL_DELETE);
                ps.setInt(1, parrain.getId());
                ps.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                    this.closeConnexionBD();
                }
            }
    }

    @Override
    public Parrain findByEmail(String email) {
        synchronized (lock) {
            Parrain parrain = null;
            this.openConnexionBD();
            try {
                PreparedStatement ps = conn.prepareStatement(SQL_SELECT_BY_EMAIL);
                ps.setString(1, email);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    parrain = new Parrain();
                    parrain.setId(rs.getInt("id"));
                    parrain.setName(rs.getString("name"));
                    parrain.setEmail(rs.getString("email"));
                    parrain.setPassword(rs.getString("password"));
                    parrain.setRole(Role.valueOf(rs.getString("role")));
                    parrain.setRegistrationDate(rs.getDate("registrationDate"));
                    parrain.setPhone(rs.getString("phone"));
                    parrain.setAddress(rs.getString("address"));
                    parrain.setCodeParrainage(rs.getString("codeParrainage"));
                }
                rs.close();
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                this.closeConnexionBD();
            }
            return parrain;
        }
    }

    @Override
    public boolean existsByName(String name) {
        synchronized (lock) {
            boolean exists = false;
            this.openConnexionBD();
            try {
                PreparedStatement ps = conn.prepareStatement(SQL_CHECK_EXISTS_BY_NAME);
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
