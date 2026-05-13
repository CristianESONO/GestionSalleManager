package com.repositories.bd; // Vous pouvez le placer dans com.repositories.bd si vous préférez

import com.core.JpaUtil;
import com.entities.Game;
import com.entities.Poste;
import com.repositories.IPosteRepository;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;

import java.util.List;

public class PosteRepositoryJpa implements IPosteRepository {

    @Override
    public Poste insert(Poste poste) {
        EntityManager em = JpaUtil.getEntityManager();
        EntityTransaction transaction = null;
        try {
            transaction = em.getTransaction();
            transaction.begin();
            em.persist(poste); // Persiste la nouvelle entité Poste
            transaction.commit();
            System.out.println("Poste inséré avec succès. ID: " + poste.getId() + ", Nom: " + poste.getName());
        } catch (Exception e) {
            if (transaction != null && transaction.isActive()) {
                transaction.rollback();
            }
            System.err.println("Erreur lors de l'insertion du poste : " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Erreur lors de l'insertion du poste", e);
        } finally {
            em.close();
        }
        return poste;
    }

    @Override
    public List<Poste> findAll() {
        EntityManager em = JpaUtil.getEntityManager();
        List<Poste> postes = null;
        try {
            // LEFT JOIN FETCH pour récupérer tous les postes, avec ou sans jeux associés
            postes = em.createQuery("SELECT DISTINCT p FROM Poste p LEFT JOIN FETCH p.games", Poste.class).getResultList();
        } catch (Exception e) {
            System.err.println("Erreur lors de la récupération de tous les postes : " + e.getMessage());
            e.printStackTrace();
        } finally {
            em.close();
        }
        return postes;
    }


    @Override
    public Poste findById(int id) {
        EntityManager em = JpaUtil.getEntityManager();
        Poste poste = null;
        try {
            // Utilise find() pour récupérer un poste par son ID.
            // Note: find() ne charge pas les collections LAZY par défaut.
            // Si vous avez besoin des jeux immédiatement, vous devrez y accéder dans la même transaction,
            // ou utiliser un JOIN FETCH si vous récupérez via une requête.
            poste = em.find(Poste.class, id);
            
            // Si le poste est trouvé et que vous avez besoin de charger ses jeux immédiatement
            // (même si la relation est LAZY), vous pouvez y accéder ici.
            // Cela déclenchera le chargement des jeux tant que l'EntityManager est ouvert.
            if (poste != null && poste.getGames() != null) {
                poste.getGames().size(); // Accéder à la collection pour la forcer à se charger (si LAZY)
            }
        } catch (Exception e) {
            System.err.println("Erreur lors de la recherche du poste par ID : " + e.getMessage());
            e.printStackTrace();
        } finally {
            em.close();
        }
        return poste;
    }

    @Override
    public void update(Poste poste) {
        EntityManager em = JpaUtil.getEntityManager();
        EntityTransaction transaction = null;
        try {
            transaction = em.getTransaction();
            transaction.begin();
            // Pour mettre à jour les relations ManyToMany, il est souvent nécessaire de récupérer
            // l'entité gérée, de modifier sa collection, puis de fusionner.
            // Cela dépend de la stratégie de cascade et de l'état de l'entité.
            // Pour des attributs simples, em.merge(poste) suffit si l'objet est détaché.
            em.merge(poste); 
            transaction.commit();
            System.out.println("Poste mis à jour avec succès. ID: " + poste.getId());
        } catch (Exception e) {
            if (transaction != null && transaction.isActive()) {
                transaction.rollback();
            }
            System.err.println("Erreur lors de la mise à jour du poste : " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Erreur lors de la mise à jour du poste", e);
        } finally {
            em.close();
        }
    }

    @Override
    public void delete(Poste poste) {
        EntityManager em = JpaUtil.getEntityManager();
        EntityTransaction transaction = null;
        try {
            transaction = em.getTransaction();
            transaction.begin();
            // Il faut d'abord attacher l'entité si elle n'est pas déjà gérée par cet EntityManager
            Poste managedPoste = em.find(Poste.class, poste.getId());
            if (managedPoste != null) {
                em.remove(managedPoste); // Supprime l'entité
            }
            transaction.commit();
            System.out.println("Poste supprimé avec succès. ID: " + poste.getId());
        } catch (Exception e) {
            if (transaction != null && transaction.isActive()) {
                transaction.rollback();
            }
            System.err.println("Erreur lors de la suppression du poste : " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Erreur lors de la suppression du poste", e);
        } finally {
            em.close();
        }
    }

    @Override
    public boolean checkAvailability(int id) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            // JPQL pour compter les postes disponibles (horsService = false)
            Long count = em.createQuery("SELECT COUNT(p) FROM Poste p WHERE p.id = :id AND p.horsService = false", Long.class)
                           .setParameter("id", id)
                           .getSingleResult();
            return count > 0;
        } catch (NoResultException e) {
            return false; // Aucun résultat trouvé, donc non disponible
        } catch (Exception e) {
            System.err.println("Erreur lors de la vérification de la disponibilité du poste : " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            em.close();
        }
    }

    @Override
    public List<Poste> findByGame(Game game) {
        EntityManager em = JpaUtil.getEntityManager();
        List<Poste> postes = null;
        try {
            // JPQL pour trouver les postes qui ont un jeu spécifique.
            // JOIN FETCH est utilisé pour charger la liste 'games' de chaque poste immédiatement (EAGERLY)
            // afin d'éviter le problème N+1 et de s'assurer que la liste est disponible en dehors de la session EM.
            TypedQuery<Poste> query = em.createQuery(
                "SELECT p FROM Poste p JOIN FETCH p.games g WHERE g.id = :gameId", Poste.class);
            query.setParameter("gameId", game.getId());
            postes = query.getResultList();
        } catch (Exception e) {
            System.err.println("Erreur lors de la recherche des postes par jeu : " + e.getMessage());
            e.printStackTrace();
        } finally {
            em.close();
        }
        return postes;
    }

    @Override
    public List<Poste> findPostesSansJeux() {
        EntityManager em = JpaUtil.getEntityManager();
        List<Poste> postes = null;
        try {
            postes = em.createQuery(
                "SELECT p FROM Poste p LEFT JOIN p.games g WHERE g IS NULL", Poste.class
            ).getResultList();
        } catch (Exception e) {
            System.err.println("Erreur lors de la récupération des postes sans jeux : " + e.getMessage());
            e.printStackTrace();
        } finally {
            em.close();
        }
        return postes;
    }

}
