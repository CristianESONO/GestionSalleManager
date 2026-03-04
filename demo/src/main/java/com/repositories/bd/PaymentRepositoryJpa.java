package com.repositories.bd; // Vous pouvez le placer dans com.repositories.bd si vous préférez

import com.core.JpaUtil;
import com.entities.Payment;
import com.entities.User;
import com.repositories.IPaymentRepository;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class PaymentRepositoryJpa implements IPaymentRepository {

     @Override
    public List<Payment> getAllPayments() {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            return em.createQuery(
                "SELECT p FROM Payment p LEFT JOIN FETCH p.client LEFT JOIN FETCH p.createdBy", 
                Payment.class)
                .getResultList();
        } catch (Exception e) {
            System.err.println("Erreur lors de la récupération des paiements: " + e.getMessage());
            return Collections.emptyList(); // Ne jamais retourner null
        } finally {
            em.close();
        }
    }


    public List<Payment> getPaymentsByUser(User user) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            return em.createQuery(
                "SELECT p FROM Payment p WHERE p.createdBy = :user", 
                Payment.class)
                .setParameter("user", user)
                .getResultList();
        } catch (Exception e) {
            System.err.println("Erreur lors de la récupération des paiements par user: " + e.getMessage());
            return Collections.emptyList();
        } finally {
            em.close();
        }
    }

    @Override
    public void addPayment(Payment payment) {
        EntityManager em = JpaUtil.getEntityManager();
        EntityTransaction transaction = null;
        try {
            transaction = em.getTransaction();
            transaction.begin();
            em.persist(payment);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null && transaction.isActive()) {
                transaction.rollback();
            }
            throw new RuntimeException("Erreur lors de l'ajout du paiement", e);
        } finally {
            em.close();
        }
    }


    @Override
    public Optional<Payment> getPaymentById(int id) {
        EntityManager em = JpaUtil.getEntityManager();
        Payment payment = null;
        try {
            // Utilise find() pour récupérer un paiement par son ID.
            // Si vous avez besoin d'accéder au Client lié (LAZY par défaut) après la fermeture de l'EM,
            // vous devrez le charger explicitement ici (par exemple, payment.getClient().getId()).
            payment = em.find(Payment.class, id);
            
            // Force le chargement du client si nécessaire pour éviter LazyInitializationException
            if (payment != null && payment.getClient() != null) {
                payment.getClient().getId(); // Accède à une propriété pour déclencher le chargement
            }
            return Optional.ofNullable(payment); // Retourne Optional.of(payment) si trouvé, Optional.empty() sinon
        } catch (Exception e) {
            System.err.println("Erreur lors de la recherche du paiement par ID : " + e.getMessage());
            e.printStackTrace();
            return Optional.empty(); // Retourne un Optional vide en cas d'erreur
        } finally {
            em.close();
        }
    }

    @Override
    public void updatePayment(Payment payment) {
        EntityManager em = JpaUtil.getEntityManager();
        EntityTransaction transaction = null;
        try {
            transaction = em.getTransaction();
            transaction.begin();
            em.merge(payment); // Met à jour l'entité (ou l'attache si elle est détachée)
            transaction.commit();
            System.out.println("Paiement mis à jour avec succès. ID: " + payment.getId());
        } catch (Exception e) {
            if (transaction != null && transaction.isActive()) {
                transaction.rollback();
            }
            System.err.println("Erreur lors de la mise à jour du paiement : " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Erreur lors de la mise à jour du paiement", e);
        } finally {
            em.close();
        }
    }

    @Override
    public void deletePayment(int id) {
        EntityManager em = JpaUtil.getEntityManager();
        EntityTransaction transaction = null;
        try {
            transaction = em.getTransaction();
            transaction.begin();
            // Il faut d'abord attacher l'entité si elle n'est pas déjà gérée par cet EntityManager
            Payment payment = em.find(Payment.class, id);
            if (payment != null) {
                em.remove(payment); // Supprime l'entité
            }
            transaction.commit();
            System.out.println("Paiement supprimé avec succès. ID: " + id);
        } catch (Exception e) {
            if (transaction != null && transaction.isActive()) {
                transaction.rollback();
            }
            System.err.println("Erreur lors de la suppression du paiement : " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Erreur lors de la suppression du paiement", e);
        } finally {
            em.close();
        }
    }

    @Override
    public boolean existsById(int id) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            // Utilise find() pour vérifier l'existence par ID
            Payment payment = em.find(Payment.class, id);
            return payment != null;
        } catch (Exception e) {
            System.err.println("Erreur lors de la vérification de l'existence du paiement par ID : " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            em.close();
        }
    }
}
