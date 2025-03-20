package com.repositories.bd;

import com.core.MysqlDb;
import com.entities.Produit;
import com.repositories.IProduitRepository;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class ProduitRepository extends MysqlDb implements IProduitRepository {

    private final String SQL_SELECT = "SELECT * FROM `produits`";
    private final String SQL_INSERT = "INSERT INTO `produits` (`nom`, `prix`, `stock`, `dateAjout`, `image`) VALUES (?, ?, ?, ?, ?)";
    private final String SQL_SELECT_BY_ID = "SELECT * FROM `produits` WHERE `id` = ?";
    private final String SQL_UPDATE = "UPDATE `produits` SET `nom` = ?, `prix` = ?, `stock` = ?, `dateAjout` = ?, `image` = ? WHERE `id` = ?";
    private final String SQL_DELETE = "DELETE FROM `produits` WHERE `id` = ?";
    private final String SQL_SELECT_EN_STOCK = "SELECT * FROM `produits` WHERE `stock` > 0";
    private final String SQL_CHECK_EXISTS = "SELECT COUNT(*) FROM `produits` WHERE `nom` = ?";

    private final Object lock = new Object();

    @Override
    public Produit insert(Produit produit) {
        synchronized (lock) {
            this.openConnexionBD();
            try {
                PreparedStatement ps = conn.prepareStatement(SQL_INSERT, PreparedStatement.RETURN_GENERATED_KEYS);
                ps.setString(1, produit.getNom());
                ps.setBigDecimal(2, produit.getPrix());
                ps.setInt(3, produit.getStock());
                ps.setObject(4, produit.getDateAjout());
                ps.setString(5, produit.getImage());
                ps.executeUpdate();

                ResultSet rs = ps.getGeneratedKeys();
                if (rs.next()) {
                    produit.setId(rs.getInt(1));
                }
                rs.close();
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                this.closeConnexionBD();
            }
            return produit;
        }
    }

    @Override
    public List<Produit> findAll() {
        synchronized (lock) {
            List<Produit> produits = new ArrayList<>();
            this.openConnexionBD();
            try {
                PreparedStatement ps = conn.prepareStatement(SQL_SELECT);
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    Produit produit = new Produit();
                    produit.setId(rs.getInt("id"));
                    produit.setNom(rs.getString("nom"));
                    produit.setPrix(rs.getBigDecimal("prix"));
                    produit.setStock(rs.getInt("stock"));

                    // Récupérer la date sous forme de chaîne et la convertir en LocalDateTime
                    String dateAjoutStr = rs.getString("dateAjout");
                    if (dateAjoutStr != null) {
                        // Si la date est dans un format ISO ou similaire
                        DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME;
                        LocalDateTime dateAjout = LocalDateTime.parse(dateAjoutStr, formatter);
                        produit.setDateAjout(dateAjout);
                    }

                    produit.setImage(rs.getString("image"));
                    produits.add(produit);
                }
                rs.close();
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                this.closeConnexionBD();
            }
            return produits;
        }
    }


    @Override
    public Produit findById(int id) {
        synchronized (lock) {
            Produit produit = null;
            this.openConnexionBD();
            try {
                PreparedStatement ps = conn.prepareStatement(SQL_SELECT_BY_ID);
                ps.setInt(1, id);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    produit = new Produit();
                    produit.setId(rs.getInt("id"));
                    produit.setNom(rs.getString("nom"));
                    produit.setPrix(rs.getBigDecimal("prix"));
                    produit.setStock(rs.getInt("stock"));
                    produit.setDateAjout(rs.getObject("dateAjout", LocalDateTime.class));
                    produit.setImage(rs.getString("image"));
                }
                rs.close();
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                this.closeConnexionBD();
            }
            return produit;
        }
    }

    @Override
    public void update(Produit produit) {
        synchronized (lock) {
            this.openConnexionBD();
            try {
                PreparedStatement ps = conn.prepareStatement(SQL_UPDATE);
                ps.setString(1, produit.getNom());
                ps.setBigDecimal(2, produit.getPrix());
                ps.setInt(3, produit.getStock());
                ps.setObject(4, produit.getDateAjout());
                ps.setString(5, produit.getImage());
                ps.setInt(6, produit.getId());
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
    public void delete(Produit produit) {
        synchronized (lock) {
            this.openConnexionBD();
            try {
                PreparedStatement ps = conn.prepareStatement(SQL_DELETE);
                ps.setInt(1, produit.getId());
                ps.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                this.closeConnexionBD();
            }
        }
    }

    @Override
    public List<Produit> findProduitsEnStock() {
        synchronized (lock) {
            List<Produit> produitsEnStock = new ArrayList<>();
            this.openConnexionBD();
            try {
                PreparedStatement ps = conn.prepareStatement(SQL_SELECT_EN_STOCK);
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    Produit produit = new Produit();
                    produit.setId(rs.getInt("id"));
                    produit.setNom(rs.getString("nom"));
                    produit.setPrix(rs.getBigDecimal("prix"));
                    produit.setStock(rs.getInt("stock"));
                    produit.setDateAjout(rs.getObject("dateAjout", LocalDateTime.class));
                    produit.setImage(rs.getString("image"));
                    produitsEnStock.add(produit);
                }
                rs.close();
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                this.closeConnexionBD();
            }
            return produitsEnStock;
        }
    }

    @Override
    public boolean checkIfProduitExists(String nom) {
        synchronized (lock) {
            boolean exists = false;
            this.openConnexionBD();
            try {
                PreparedStatement ps = conn.prepareStatement(SQL_CHECK_EXISTS);
                ps.setString(1, nom);
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