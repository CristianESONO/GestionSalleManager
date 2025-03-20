package com.repositories.bd;

import com.core.MysqlDb;
import com.entities.Role;
import com.entities.User;
import com.repositories.IUserRepository;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class UserRepository extends MysqlDb implements IUserRepository {

    // Déclaration des requêtes SQL
    private final String SQL_SELECT_BY_LOGIN_AND_PASSWORD = "SELECT * FROM users WHERE email = ? AND password = ?";
    private final String SQL_SELECT_ALL = "SELECT * FROM users";
    private final String SQL_INSERT = "INSERT INTO users (name, email, password, role, registrationDate) VALUES (?, ?, ?, ?, ?)";
    private final String SQL_EXISTS_BY_NAME = "SELECT COUNT(*) FROM users WHERE name = ?";
    private final String SQL_DELETE_BY_ID = "DELETE FROM users WHERE id = ?";
    private final String SQL_UPDATE = "UPDATE users SET name = ?, email = ?, password = ?, role = ?, registrationDate = ? WHERE id = ?";

    // Verrou pour la synchronisation
    private final Object lock = new Object();

    @Override
    public User findUserByLoginAndPassword(String login, String password) {
        synchronized (lock) {
            User user = null;
            this.openConnexionBD();
            try {
                PreparedStatement ps = conn.prepareStatement(SQL_SELECT_BY_LOGIN_AND_PASSWORD);
                ps.setString(1, login);
                ps.setString(2, password);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    user = mapResultSetToUser(rs);
                }
                rs.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }  finally{
                try{
                    ps.close();
                }catch (Exception e){}
            }
            this.closeConnexionBD();
            return user;
        }
    }

    @Override
    public List<User> findAll() {
        synchronized (lock) {
            List<User> users = new ArrayList<>();
            this.openConnexionBD();
            try {
                PreparedStatement ps = conn.prepareStatement(SQL_SELECT_ALL);
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    User user = mapResultSetToUser(rs);
                    users.add(user);
                }
                rs.close();
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                this.closeConnexionBD();
            }
            return users;
        }
    }

    @Override
    public void addUser(User newUser) {
        synchronized (lock) {
            this.openConnexionBD();
            try {
                PreparedStatement ps = conn.prepareStatement(SQL_INSERT, PreparedStatement.RETURN_GENERATED_KEYS);
                ps.setString(1, newUser.getName());
                ps.setString(2, newUser.getEmail());
                ps.setString(3, newUser.getPassword());
                ps.setString(4, newUser.getRole().toString());
                ps.setDate(5, new java.sql.Date(newUser.getRegistrationDate().getTime()));
                ps.executeUpdate();

                // Récupérer l'ID généré
                ResultSet rs = ps.getGeneratedKeys();
                if (rs.next()) {
                    newUser.setId(rs.getInt(1));
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

    @Override
    public void delete(int id) {
        synchronized (lock) {
            this.openConnexionBD();
            try {
                PreparedStatement ps = conn.prepareStatement(SQL_DELETE_BY_ID);
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
    public void updateUser(User user) {
        synchronized (lock) {
            this.openConnexionBD();
            try {
                PreparedStatement ps = conn.prepareStatement(SQL_UPDATE);
                ps.setString(1, user.getName());
                ps.setString(2, user.getEmail());
                ps.setString(3, user.getPassword());
                ps.setString(4, user.getRole().toString());
                ps.setDate(5, new java.sql.Date(user.getRegistrationDate().getTime()));
                ps.setInt(6, user.getId());
                ps.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                this.closeConnexionBD();
            }
        }
    }

    // Méthode utilitaire pour mapper un ResultSet à un objet User
    private User mapResultSetToUser(ResultSet rs) throws SQLException {
        User user = new User();
        user.setId(rs.getInt("id"));
        user.setName(rs.getString("name"));
        user.setEmail(rs.getString("email"));
        user.setPassword(rs.getString("password"));
        user.setRole(Role.valueOf(rs.getString("role")));
        user.setRegistrationDate(new Date(rs.getDate("registrationDate").getTime()));
        return user;
    }
}