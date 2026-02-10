package com.repositories.bd; // Ou com.repositories.bd si vous préférez

import com.core.JpaUtil; // Importez votre classe utilitaire JPA
import com.entities.Client; // Importez Client car findById retourne un Client
import com.entities.Role;
import com.entities.User;
import com.repositories.IUserRepository;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;

import java.util.List;

public class UserRepositoryJpa implements IUserRepository {

    @Override
    public User findUserByLoginAndPassword(String email, String password) {
        EntityManager em = JpaUtil.getEntityManager();
        User user = null;
        try {
            // JPQL pour rechercher un utilisateur par email et mot de passe
            TypedQuery<User> query = em.createQuery(
                "SELECT u FROM User u WHERE u.email = :email AND u.password = :password", User.class);
            query.setParameter("email", email);
            query.setParameter("password", password); // Assurez-vous que le mot de passe n'est pas stocké en clair en production!
            user = query.getSingleResult(); // Tente de récupérer un résultat unique
        } catch (NoResultException e) {
            // Aucun utilisateur trouvé avec ces identifiants
            System.out.println("Aucun utilisateur trouvé pour l'email: " + email);
            user = null;
        } catch (Exception e) {
            System.err.println("Erreur lors de la recherche de l'utilisateur par login et mot de passe : " + e.getMessage());
            e.printStackTrace();
        } finally {
            em.close(); // Ferme l'EntityManager
        }
        return user;
    }

    @Override
    public boolean existsByRole(Role role) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            // Requête JPQL pour vérifier l'existence d'un utilisateur avec le rôle spécifié
            TypedQuery<Long> query = em.createQuery(
                "SELECT COUNT(u) FROM User u WHERE u.role = :role", Long.class);
            query.setParameter("role", role);
            Long count = query.getSingleResult();
            return count > 0;
        } catch (Exception e) {
            System.err.println("Erreur lors de la vérification de l'existence d'un utilisateur par rôle : " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            em.close();
        }
    }

    // Dans com.repositories.jpa.UserRepositoryJpa.java
    @Override
    public User findByEmail(String email) {
        EntityManager em = JpaUtil.getEntityManager();
        User user = null;
        try {
            TypedQuery<User> query = em.createQuery(
                "SELECT u FROM User u WHERE u.email = :email", User.class);
            query.setParameter("email", email);
            user = query.getSingleResult();
        } catch (NoResultException e) {
            // Aucun utilisateur trouvé avec cet email
            user = null;
        } catch (Exception e) {
            System.err.println("Erreur lors de la recherche de l'utilisateur par email : " + e.getMessage());
            e.printStackTrace();
        } finally {
            em.close();
        }
        return user;
    }

    

    @Override
    public List<User> findAll() {
        EntityManager em = JpaUtil.getEntityManager();
        List<User> users = null;
        try {
            // JPQL pour récupérer tous les utilisateurs.
            // Note: Avec la stratégie SINGLE_TABLE, cela récupérera tous les Users, Clients, Parrains.
            users = em.createQuery("SELECT u FROM User u", User.class).getResultList();
        } catch (Exception e) {
            System.err.println("Erreur lors de la récupération de tous les utilisateurs : " + e.getMessage());
            e.printStackTrace();
        } finally {
            em.close();
        }
        return users;
    }

    @Override
    public void addUser(User newUser) {
        EntityManager em = JpaUtil.getEntityManager();
        EntityTransaction transaction = null;
        try {
            transaction = em.getTransaction();
            transaction.begin(); // Démarre une transaction
            em.persist(newUser); // Persiste la nouvelle entité
            transaction.commit(); // Valide la transaction
            System.out.println("Utilisateur ajouté avec succès. ID: " + newUser.getId() + ", Nom: " + newUser.getName());
        } catch (Exception e) {
            if (transaction != null && transaction.isActive()) {
                transaction.rollback(); // Annule la transaction en cas d'erreur
            }
            System.err.println("Erreur lors de l'ajout de l'utilisateur : " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Erreur lors de l'ajout de l'utilisateur", e); // Relance l'exception
        } finally {
            em.close();
        }
    }

    @Override
    public void updateUser(User user) {
        EntityManager em = JpaUtil.getEntityManager();
        EntityTransaction transaction = null;
        try {
            transaction = em.getTransaction();
            transaction.begin();
            em.merge(user); // Met à jour l'entité (ou l'attache si elle est détachée)
            transaction.commit();
            System.out.println("Utilisateur mis à jour avec succès. ID: " + user.getId());
        } catch (Exception e) {
            if (transaction != null && transaction.isActive()) {
                transaction.rollback();
            }
            System.err.println("Erreur lors de la mise à jour de l'utilisateur : " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Erreur lors de la mise à jour de l'utilisateur", e);
        } finally {
            em.close();
        }
    }

    @Override
    public void delete(int id) {
        EntityManager em = JpaUtil.getEntityManager();
        EntityTransaction transaction = null;
        try {
            transaction = em.getTransaction();
            transaction.begin();
            User user = em.find(User.class, id); // Trouve l'entité par ID
            if (user != null) {
                em.remove(user); // Supprime l'entité
            }
            transaction.commit();
            System.out.println("Utilisateur supprimé avec succès. ID: " + id);
        } catch (Exception e) {
            if (transaction != null && transaction.isActive()) {
                transaction.rollback();
            }
            System.err.println("Erreur lors de la suppression de l'utilisateur : " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Erreur lors de la suppression de l'utilisateur", e);
        } finally {
            em.close();
        }
    }

    @Override
    public Client findById(int id) {
        EntityManager em = JpaUtil.getEntityManager();
        Client client = null;
        try {
            // Utilise find() pour récupérer par ID.
            // Comme Client est une sous-classe de User, find() avec Client.class fonctionnera.
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
    public boolean existsByName(String name) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            // JPQL pour vérifier l'existence par nom
            Long count = em.createQuery("SELECT COUNT(u) FROM User u WHERE u.name = :name", Long.class)
                           .setParameter("name", name)
                           .getSingleResult();
            return count > 0;
        } catch (Exception e) {
            System.err.println("Erreur lors de la vérification de l'existence de l'utilisateur par nom : " + e.getMessage());
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
            // Utilise find() qui est efficace pour vérifier l'existence par ID
            User user = em.find(User.class, id);
            return user != null;
        } catch (Exception e) {
            System.err.println("Erreur lors de la vérification de l'existence de l'utilisateur par ID : " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            em.close();
        }
    }
}
