package com.repositories.bd; // Vous pouvez le placer dans com.repositories.bd si vous préférez

import com.core.JpaUtil;
import com.entities.Parrain;
import com.repositories.IParrainRepository;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;

import java.util.List;

public class ParrainRepositoryJpa implements IParrainRepository {

    @Override
    public Parrain insert(Parrain parrain) {
        EntityManager em = JpaUtil.getEntityManager();
        EntityTransaction transaction = null;
        try {
            transaction = em.getTransaction();
            transaction.begin();
            em.persist(parrain); // Persiste la nouvelle entité Parrain
            transaction.commit();
            System.out.println("Parrain inséré avec succès. ID: " + parrain.getId() + ", Nom: " + parrain.getName());
        } catch (Exception e) {
            if (transaction != null && transaction.isActive()) {
                transaction.rollback();
            }
            System.err.println("Erreur lors de l'insertion du parrain : " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Erreur lors de l'insertion du parrain", e);
        } finally {
            em.close();
        }
        return parrain;
    }

    @Override
    public List<Parrain> findAll() {
        EntityManager em = JpaUtil.getEntityManager();
        List<Parrain> parrains = null;
        try {
            // JPQL pour récupérer tous les parrains (qui sont des sous-types de User avec DiscriminatorValue "PARRAIN")
            parrains = em.createQuery("SELECT p FROM Parrain p", Parrain.class).getResultList();
        } catch (Exception e) {
            System.err.println("Erreur lors de la récupération de tous les parrains : " + e.getMessage());
            e.printStackTrace();
        } finally {
            em.close();
        }
        return parrains;
    }

    @Override
    public Parrain findById(int id) {
        EntityManager em = JpaUtil.getEntityManager();
        Parrain parrain = null;
        try {
            // Utilise find() pour récupérer un parrain par son ID
            parrain = em.find(Parrain.class, id);
        } catch (Exception e) {
            System.err.println("Erreur lors de la recherche du parrain par ID : " + e.getMessage());
            e.printStackTrace();
        } finally {
            em.close();
        }
        return parrain;
    }

    @Override
    public Parrain findByTel(String phone) {
        EntityManager em = JpaUtil.getEntityManager();
        Parrain parrain = null;
        try {
            // JPQL pour rechercher un parrain par numéro de téléphone
            TypedQuery<Parrain> query = em.createQuery(
                "SELECT p FROM Parrain p WHERE p.phone = :phone", Parrain.class);
            query.setParameter("phone", phone);
            parrain = query.getSingleResult();
        } catch (NoResultException e) {
            System.out.println("Aucun parrain trouvé pour le téléphone: " + phone);
            parrain = null;
        } catch (Exception e) {
            System.err.println("Erreur lors de la recherche du parrain par téléphone : " + e.getMessage());
            e.printStackTrace();
        } finally {
            em.close();
        }
        return parrain;
    }

    @Override
    public void update(Parrain parrain) {
        EntityManager em = JpaUtil.getEntityManager();
        EntityTransaction transaction = null;
        try {
            transaction = em.getTransaction();
            transaction.begin();
            em.merge(parrain); // Met à jour l'entité (ou l'attache si elle est détachée)
            transaction.commit();
            System.out.println("Parrain mis à jour avec succès. ID: " + parrain.getId());
        } catch (Exception e) {
            if (transaction != null && transaction.isActive()) {
                transaction.rollback();
            }
            System.err.println("Erreur lors de la mise à jour du parrain : " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Erreur lors de la mise à jour du parrain", e);
        } finally {
            em.close();
        }
    }

    @Override
    public void delete(Parrain parrain) {
        EntityManager em = JpaUtil.getEntityManager();
        EntityTransaction transaction = null;
        try {
            transaction = em.getTransaction();
            transaction.begin();
            // Il faut d'abord attacher l'entité si elle n'est pas déjà gérée par cet EntityManager
            Parrain managedParrain = em.find(Parrain.class, parrain.getId());
            if (managedParrain != null) {
                em.remove(managedParrain); // Supprime l'entité
            }
            transaction.commit();
            System.out.println("Parrain supprimé avec succès. ID: " + parrain.getId());
        } catch (Exception e) {
            if (transaction != null && transaction.isActive()) {
                transaction.rollback();
            }
            System.err.println("Erreur lors de la suppression du parrain : " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Erreur lors de la suppression du parrain", e);
        } finally {
            em.close();
        }
    }

    @Override
    public Parrain findByEmail(String email) {
        EntityManager em = JpaUtil.getEntityManager();
        Parrain parrain = null;
        try {
            // JPQL pour rechercher un parrain par email.
            // Puisque Parrain hérite de User, et User a un champ email, cette requête est valide.
            TypedQuery<Parrain> query = em.createQuery(
                "SELECT p FROM Parrain p WHERE p.email = :email", Parrain.class);
            query.setParameter("email", email);
            parrain = query.getSingleResult();
        } catch (NoResultException e) {
            System.out.println("Aucun parrain trouvé pour l'email: " + email);
            parrain = null;
        } catch (Exception e) {
            System.err.println("Erreur lors de la recherche du parrain par email : " + e.getMessage());
            e.printStackTrace();
        } finally {
            em.close();
        }
        return parrain;
    }

    @Override
    public Parrain findByCodeParrainage(String codeParrainage) {
        EntityManager em = JpaUtil.getEntityManager();
        Parrain parrain = null;
        try {
            // JPQL pour rechercher un parrain par son code de parrainage
            TypedQuery<Parrain> query = em.createQuery(
                "SELECT p FROM Parrain p WHERE p.codeParrainage = :codeParrainage", Parrain.class);
            query.setParameter("codeParrainage", codeParrainage);
            parrain = query.getSingleResult();
        } catch (NoResultException e) {
            System.out.println("Aucun parrain trouvé pour le code de parrainage: " + codeParrainage);
            parrain = null;
        } catch (Exception e) {
            System.err.println("Erreur lors de la recherche du parrain par code de parrainage : " + e.getMessage());
            e.printStackTrace();
        } finally {
            em.close();
        }
        return parrain;
    }

    @Override
    public boolean existsByName(String name) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            // JPQL pour vérifier l'existence d'un parrain par son nom
            Long count = em.createQuery("SELECT COUNT(p) FROM Parrain p WHERE p.name = :name", Long.class)
                           .setParameter("name", name)
                           .getSingleResult();
            return count > 0;
        } catch (Exception e) {
            System.err.println("Erreur lors de la vérification de l'existence du parrain par nom : " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            em.close();
        }
    }
}
