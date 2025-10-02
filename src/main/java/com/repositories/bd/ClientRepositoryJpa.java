package com.repositories.bd; // Vous pouvez le placer dans com.repositories.bd si vous préférez

import com.core.JpaUtil; // Importez votre classe utilitaire JPA
import com.entities.Client;
import com.repositories.IClientRepository;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;

import java.util.List;

public class ClientRepositoryJpa implements IClientRepository {

    @Override
    public Client insert(Client client) {
        EntityManager em = JpaUtil.getEntityManager();
        EntityTransaction transaction = null;
        try {
            transaction = em.getTransaction();
            transaction.begin(); // Démarre une transaction
            em.persist(client); // Persiste la nouvelle entité Client
            transaction.commit(); // Valide la transaction
            System.out.println("Client inséré avec succès. ID: " + client.getId() + ", Nom: " + client.getName());
        } catch (Exception e) {
            if (transaction != null && transaction.isActive()) {
                transaction.rollback(); // Annule la transaction en cas d'erreur
            }
            System.err.println("Erreur lors de l'insertion du client : " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Erreur lors de l'insertion du client", e); // Relance l'exception
        } finally {
            em.close(); // Ferme l'EntityManager
        }
        return client;
    }

    @Override
    public List<Client> findAll() {
        EntityManager em = JpaUtil.getEntityManager();
        List<Client> clients = null;
        try {
            // Charge les clients ET leurs réservations en une seule requête
            clients = em.createQuery(
                "SELECT DISTINCT c FROM Client c LEFT JOIN FETCH c.reservations", Client.class)
                .getResultList();
        } catch (Exception e) {
            System.err.println("Erreur lors de la récupération de tous les clients : " + e.getMessage());
            e.printStackTrace();
        } finally {
            em.close();
        }
        return clients;
    }


    @Override
    public Client findById(int id) {
        EntityManager em = JpaUtil.getEntityManager();
        Client client = null;
        try {
            // Utilise find() pour récupérer un client par son ID
            client = em.find(Client.class, id);
        } catch (Exception e) {
            System.err.println("Erreur lors de la recherche du client par ID : " + e.getMessage());
            e.printStackTrace();
        } finally {
            em.close();
        }
        return client;
    }

    @Override
    public Client findByTel(String phone) {
        EntityManager em = JpaUtil.getEntityManager();
        Client client = null;
        try {
            // JPQL pour rechercher un client par numéro de téléphone
            TypedQuery<Client> query = em.createQuery(
                "SELECT c FROM Client c WHERE c.phone = :phone", Client.class);
            query.setParameter("phone", phone);
            client = query.getSingleResult(); // Tente de récupérer un résultat unique
        } catch (NoResultException e) {
            // Aucun client trouvé avec ce numéro de téléphone
            System.out.println("Aucun client trouvé pour le téléphone: " + phone);
            client = null;
        } catch (Exception e) {
            System.err.println("Erreur lors de la recherche du client par téléphone : " + e.getMessage());
            e.printStackTrace();
        } finally {
            em.close();
        }
        return client;
    }

    @Override
    public void update(Client client) {
        EntityManager em = JpaUtil.getEntityManager();
        EntityTransaction transaction = null;
        try {
            transaction = em.getTransaction();
            transaction.begin();
            em.merge(client); // Met à jour l'entité (ou l'attache si elle est détachée)
            transaction.commit();
            System.out.println("Client mis à jour avec succès. ID: " + client.getId());
        } catch (Exception e) {
            if (transaction != null && transaction.isActive()) {
                transaction.rollback();
            }
            System.err.println("Erreur lors de la mise à jour du client : " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Erreur lors de la mise à jour du client", e);
        } finally {
            em.close();
        }
    }

    @Override
    public void delete(Client client) {
        EntityManager em = JpaUtil.getEntityManager();
        EntityTransaction transaction = null;
        try {
            transaction = em.getTransaction();
            transaction.begin();
            // Il faut d'abord attacher l'entité si elle n'est pas déjà gérée par cet EntityManager
            Client managedClient = em.find(Client.class, client.getId());
            if (managedClient != null) {
                em.remove(managedClient); // Supprime l'entité
            }
            transaction.commit();
            System.out.println("Client supprimé avec succès. ID: " + client.getId());
        } catch (Exception e) {
            if (transaction != null && transaction.isActive()) {
                transaction.rollback();
            }
            System.err.println("Erreur lors de la suppression du client : " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Erreur lors de la suppression du client", e);
        } finally {
            em.close();
        }
    }

    @Override
    public Client findByEmail(String email) {
        EntityManager em = JpaUtil.getEntityManager();
        Client client = null;
        try {
            // JPQL pour rechercher un client par email.
            // Puisque Client hérite de User, et User a un champ email, cette requête est valide.
            TypedQuery<Client> query = em.createQuery(
                "SELECT c FROM Client c WHERE c.email = :email", Client.class);
            query.setParameter("email", email);
            client = query.getSingleResult();
        } catch (NoResultException e) {
            System.out.println("Aucun client trouvé pour l'email: " + email);
            client = null;
        } catch (Exception e) {
            System.err.println("Erreur lors de la recherche du client par email : " + e.getMessage());
            e.printStackTrace();
        } finally {
            em.close();
        }
        return client;
    }

    @Override
    public boolean existsByName(String name) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            // JPQL pour vérifier l'existence d'un client par son nom
            Long count = em.createQuery("SELECT COUNT(c) FROM Client c WHERE c.name = :name", Long.class)
                           .setParameter("name", name)
                           .getSingleResult();
            return count > 0;
        } catch (Exception e) {
            System.err.println("Erreur lors de la vérification de l'existence du client par nom : " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            em.close();
        }
    }

    @Override
    public List<Client> findByLoyaltyPointsGreaterThan(int points) {
        EntityManager em = JpaUtil.getEntityManager();
        List<Client> clients = null;
        try {
            // JPQL pour récupérer les clients avec plus de points de fidélité que la valeur donnée
            TypedQuery<Client> query = em.createQuery(
                "SELECT c FROM Client c WHERE c.loyaltyPoints > :points", Client.class);
            query.setParameter("points", points);
            clients = query.getResultList();
        } catch (Exception e) {
            System.err.println("Erreur lors de la recherche des clients par points de fidélité : " + e.getMessage());
            e.printStackTrace();
        } finally {
            em.close();
        }
        return clients;
    }
}
