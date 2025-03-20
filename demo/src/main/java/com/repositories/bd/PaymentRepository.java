package com.repositories.bd;

import com.core.MysqlDb;
import com.entities.Client;
import com.entities.Payment;
import com.repositories.IPaymentRepository;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PaymentRepository extends MysqlDb implements IPaymentRepository {

    ClientRepository clientRepository = new ClientRepository();

    // Déclaration des requêtes SQL
    private final String SQL_SELECT_ALL = "SELECT * FROM payments";
    private final String SQL_INSERT = "INSERT INTO payments (numeroTicket, dateHeure, montantTotal, modePaiement, client_id, detailsProduits) VALUES (?, ?, ?, ?, ?, ?)";
    private final String SQL_SELECT_BY_ID = "SELECT * FROM payments WHERE id = ?";
    private final String SQL_UPDATE = "UPDATE payments SET numeroTicket = ?, dateHeure = ?, montantTotal = ?, modePaiement = ?, client_id = ?, detailsProduits = ? WHERE id = ?";
    private final String SQL_DELETE_BY_ID = "DELETE FROM payments WHERE id = ?";
    private final String SQL_EXISTS_BY_ID = "SELECT COUNT(*) FROM payments WHERE id = ?";

    // Verrou pour la synchronisation
    private final Object lock = new Object();

    @Override
    public List<Payment> getAllPayments() {
        synchronized (lock) {
            List<Payment> payments = new ArrayList<>();
            this.openConnexionBD();
            try {
                PreparedStatement ps = conn.prepareStatement(SQL_SELECT_ALL);
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    Payment payment = mapResultSetToPayment(rs);
                    payments.add(payment);
                }
                rs.close();
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                this.closeConnexionBD();
            }
            return payments;
        }
    }

    @Override
    public void addPayment(Payment payment) {
        synchronized (lock) {
            this.openConnexionBD();
            try {
                PreparedStatement ps = conn.prepareStatement(SQL_INSERT, PreparedStatement.RETURN_GENERATED_KEYS);
                ps.setString(1, payment.getNumeroTicket());
                ps.setTimestamp(2, new java.sql.Timestamp(payment.getDateHeure().getTime()));
                ps.setDouble(3, payment.getMontantTotal());
                ps.setString(4, payment.getModePaiement());
                ps.setInt(5, payment.getClient().getId());
                ps.setString(6, payment.getDetailsProduits());
                ps.executeUpdate();

                // Récupérer l'ID généré
                ResultSet rs = ps.getGeneratedKeys();
                if (rs.next()) {
                    payment.setId(rs.getInt(1));
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
    public Optional<Payment> getPaymentById(int id) {
        synchronized (lock) {
            Payment payment = null;
            this.openConnexionBD();
            try {
                PreparedStatement ps = conn.prepareStatement(SQL_SELECT_BY_ID);
                ps.setInt(1, id);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    payment = mapResultSetToPayment(rs);
                }
                rs.close();
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                this.closeConnexionBD();
            }
            return Optional.ofNullable(payment);
        }
    }

    @Override
    public void updatePayment(Payment payment) {
        synchronized (lock) {
            this.openConnexionBD();
            try {
                PreparedStatement ps = conn.prepareStatement(SQL_UPDATE);
                ps.setString(1, payment.getNumeroTicket());
                ps.setTimestamp(2, new java.sql.Timestamp(payment.getDateHeure().getTime()));
                ps.setDouble(3, payment.getMontantTotal());
                ps.setString(4, payment.getModePaiement());
                ps.setInt(5, payment.getClient().getId());
                ps.setString(6, payment.getDetailsProduits());
                ps.setInt(7, payment.getId());
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
    public void deletePayment(int id) {
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

    // Méthode utilitaire pour mapper un ResultSet à un objet Payment
    private Payment mapResultSetToPayment(ResultSet rs) throws SQLException {
        Payment payment = new Payment();
        payment.setId(rs.getInt("id"));
        payment.setNumeroTicket(rs.getString("numeroTicket"));
        payment.setDateHeure(new java.util.Date(rs.getTimestamp("dateHeure").getTime()));
        payment.setMontantTotal(rs.getDouble("montantTotal"));
        payment.setModePaiement(rs.getString("modePaiement"));
          // Charger le client associé
        int clientId = rs.getInt("client_id");
        if (!rs.wasNull()) { // Vérifie si client_id n'est pas null
            Client client = clientRepository.findById(clientId); // Méthode pour récupérer le client par son ID
            payment.setClient(client);
        } else {
            payment.setClient(null); // Si client_id est null, définir le client à null
        }

        payment.setDetailsProduits(rs.getString("detailsProduits"));
        return payment;
    }
}