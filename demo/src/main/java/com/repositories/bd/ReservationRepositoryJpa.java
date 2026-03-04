package com.repositories.bd;

import com.core.JpaUtil;
import com.entities.Reservation;
import com.entities.User;
import com.repositories.IReservationRepository;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;
import java.time.LocalDateTime;
import java.util.List;

public class ReservationRepositoryJpa implements IReservationRepository {

    @Override
    public Reservation insert(Reservation reservation) {
        EntityManager em = JpaUtil.getEntityManager();
        EntityTransaction transaction = null;
        try {
            transaction = em.getTransaction();
            transaction.begin();
            em.persist(reservation);
            transaction.commit();
            System.out.println("Réservation insérée avec succès. ID: " + reservation.getId() + 
                             ", Ticket: " + reservation.getNumeroTicket() + 
                             ", Créée par: " + (reservation.getCreatedBy() != null ? reservation.getCreatedBy().getId() : "null"));
        } catch (Exception e) {
            if (transaction != null && transaction.isActive()) {
                transaction.rollback();
            }
            System.err.println("Erreur lors de l'insertion de la réservation : " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Erreur lors de l'insertion de la réservation", e);
        } finally {
            em.close();
        }
        return reservation;
    }

    @Override
    public List<Reservation> findAll() {
        EntityManager em = JpaUtil.getEntityManager();
        List<Reservation> reservations = null;
        try {
            reservations = em.createQuery(
                "SELECT r FROM Reservation r " +
                "LEFT JOIN FETCH r.client " +
                "LEFT JOIN FETCH r.poste " +
                "LEFT JOIN FETCH r.game " +
                "LEFT JOIN FETCH r.appliedPromotion " +
                "LEFT JOIN FETCH r.createdBy", Reservation.class) // Ajout du fetch pour createdBy
                .getResultList();
        } catch (Exception e) {
            System.err.println("Erreur lors de la récupération de toutes les réservations : " + e.getMessage());
            e.printStackTrace();
        } finally {
            em.close();
        }
        return reservations;
    }

    @Override
    public Reservation findById(int id) {
        EntityManager em = JpaUtil.getEntityManager();
        Reservation reservation = null;
        try {
            reservation = em.find(Reservation.class, id);
            
            if (reservation != null) {
                // Force le chargement des relations LAZY
                if (reservation.getClient() != null) reservation.getClient().getId();
                if (reservation.getPoste() != null) reservation.getPoste().getId();
                if (reservation.getGame() != null) reservation.getGame().getId();
                if (reservation.getAppliedPromotion() != null) reservation.getAppliedPromotion().getId();
                if (reservation.getCreatedBy() != null) reservation.getCreatedBy().getId(); // Nouveau
            }
        } catch (Exception e) {
            System.err.println("Erreur lors de la recherche de la réservation par ID : " + e.getMessage());
            e.printStackTrace();
        } finally {
            em.close();
        }
        return reservation;
    }

    @Override
    public List<Reservation> findByClientIdAndDateRange(int clientId, LocalDateTime startDate, LocalDateTime endDate) {
        EntityManager em = JpaUtil.getEntityManager();
        List<Reservation> reservations = null;
        try {
            TypedQuery<Reservation> query = em.createQuery(
                "SELECT r FROM Reservation r WHERE r.client.id = :clientId " +
                "AND r.reservationDate >= :startDate AND r.reservationDate <= :endDate", Reservation.class);
            query.setParameter("clientId", clientId);
            query.setParameter("startDate", startDate);
            query.setParameter("endDate", endDate);
            reservations = query.getResultList();
        } catch (Exception e) {
            System.err.println("Erreur lors de la recherche des réservations par client et plage de dates : " + e.getMessage());
            e.printStackTrace();
        } finally {
            em.close();
        }
        return reservations;
    }

    @Override
    public boolean update(Reservation reservation) {
        EntityManager em = JpaUtil.getEntityManager();
        EntityTransaction transaction = null;
        try {
            transaction = em.getTransaction();
            transaction.begin();
            em.merge(reservation);
            transaction.commit();
            System.out.println("Réservation mise à jour avec succès. ID: " + reservation.getId() + 
                             ", Modifiée par: " + (reservation.getCreatedBy() != null ? reservation.getCreatedBy().getId() : "null"));
            return true;
        } catch (Exception e) {
            if (transaction != null && transaction.isActive()) {
                transaction.rollback();
            }
            System.err.println("Erreur lors de la mise à jour de la réservation : " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Erreur lors de la mise à jour de la réservation", e);
        } finally {
            em.close();
        }
    }

    @Override
    public boolean delete(int id) {
        EntityManager em = JpaUtil.getEntityManager();
        EntityTransaction transaction = null;
        try {
            transaction = em.getTransaction();
            transaction.begin();
            Reservation reservation = em.find(Reservation.class, id);
            if (reservation != null) {
                System.out.println("Suppression de la réservation créée par: " + 
                                 (reservation.getCreatedBy() != null ? reservation.getCreatedBy().getId() : "null"));
                em.remove(reservation);
            }
            transaction.commit();
            System.out.println("Réservation supprimée avec succès. ID: " + id);
            return true;
        } catch (Exception e) {
            if (transaction != null && transaction.isActive()) {
                transaction.rollback();
            }
            System.err.println("Erreur lors de la suppression de la réservation : " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Erreur lors de la suppression de la réservation", e);
        } finally {
            em.close();
        }
    }

    @Override
    public List<Reservation> findByClientId(int clientId) {
        EntityManager em = JpaUtil.getEntityManager();
        List<Reservation> reservations = null;
        try {
            TypedQuery<Reservation> query = em.createQuery(
                "SELECT r FROM Reservation r WHERE r.client.id = :clientId", Reservation.class);
            query.setParameter("clientId", clientId);
            reservations = query.getResultList();
        } catch (Exception e) {
            System.err.println("Erreur lors de la recherche des réservations par ID client : " + e.getMessage());
            e.printStackTrace();
        } finally {
            em.close();
        }
        return reservations;
    }

    @Override
    public boolean existsByTicketNumber(String numeroTicket) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            Long count = em.createQuery("SELECT COUNT(r) FROM Reservation r WHERE r.numeroTicket = :numeroTicket", Long.class)
                           .setParameter("numeroTicket", numeroTicket)
                           .getSingleResult();
            return count > 0;
        } catch (NoResultException e) {
            return false;
        } catch (Exception e) {
            System.err.println("Erreur lors de la vérification de l'existence de la réservation par numéro de ticket : " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            em.close();
        }
    }

    @Override
    public boolean existsById(int id) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            Reservation reservation = em.find(Reservation.class, id);
            return reservation != null;
        } catch (Exception e) {
            System.err.println("Erreur lors de la vérification de l'existence de la réservation par ID : " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            em.close();
        }
    }

    @Override
    public List<Reservation> findReservationsByUser(User user) {
        EntityManager em = JpaUtil.getEntityManager();
        List<Reservation> reservations = null;
        try {
            TypedQuery<Reservation> query = em.createQuery(
                "SELECT r FROM Reservation r " +
                "LEFT JOIN FETCH r.client " +
                "LEFT JOIN FETCH r.poste " +
                "LEFT JOIN FETCH r.game " +
                "WHERE r.createdBy = :user", Reservation.class);
            query.setParameter("user", user);
            reservations = query.getResultList();
        } catch (Exception e) {
            System.err.println("Erreur lors de la recherche des réservations par utilisateur : " + e.getMessage());
            e.printStackTrace();
        } finally {
            em.close();
        }
        return reservations;
    }

    // Nouvelle méthode pour trouver les réservations par ID utilisateur
    public List<Reservation> findReservationsByUserId(int userId) {
        EntityManager em = JpaUtil.getEntityManager();
        List<Reservation> reservations = null;
        try {
            TypedQuery<Reservation> query = em.createQuery(
                "SELECT r FROM Reservation r " +
                "LEFT JOIN FETCH r.client " +
                "LEFT JOIN FETCH r.poste " +
                "LEFT JOIN FETCH r.game " +
                "WHERE r.createdBy.id = :userId", Reservation.class);
            query.setParameter("userId", userId);
            reservations = query.getResultList();
        } catch (Exception e) {
            System.err.println("Erreur lors de la recherche des réservations par ID utilisateur : " + e.getMessage());
            e.printStackTrace();
        } finally {
            em.close();
        }
        return reservations;
    }
}