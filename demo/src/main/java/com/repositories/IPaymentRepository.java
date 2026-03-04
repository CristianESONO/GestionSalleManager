package com.repositories;

import com.entities.Payment;
import java.util.List;
import java.util.Optional;

public interface IPaymentRepository {
    
    // Récupérer tous les paiements
    List<Payment> getAllPayments();
    
    // Ajouter un nouveau paiement
    void addPayment(Payment payment);
    
    // Récupérer un paiement par son ID
    Optional<Payment> getPaymentById(int id);
    
    // Mettre à jour un paiement
    void updatePayment(Payment payment);
    
    // Supprimer un paiement par ID
    void deletePayment(int id);

    boolean existsById(int id);
}
