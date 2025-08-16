package com.repositories.bd;

import com.core.JpaUtil;
import com.entities.Produit;
import com.entities.Promotion;
import com.repositories.IProduitRepository;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.hibernate.Hibernate;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

public class ProduitRepositoryJpa implements IProduitRepository {

    @Override
    public Produit insert(Produit produit) {
        EntityManager em = JpaUtil.getEntityManager();
        EntityTransaction transaction = null;
        try {
            transaction = em.getTransaction();
            transaction.begin();
            em.persist(produit);
            transaction.commit();
            return produit;
        } catch (Exception e) {
            if (transaction != null && transaction.isActive()) {
                transaction.rollback();
            }
            throw new RuntimeException("Erreur lors de l'insertion du produit", e);
        } finally {
            em.close();
        }
    }

    @Override
    public List<Produit> findAll() {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            return em.createQuery("SELECT p FROM Produit p", Produit.class)
                     .getResultList();
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de la récupération des produits", e);
        } finally {
            em.close();
        }
    }

    @Override
    public Produit findById(int id) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            return em.find(Produit.class, id);
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de la recherche du produit par ID", e);
        } finally {
            em.close();
        }
    }

    @Override
    public Produit findByIdWithPromotions(int produitId) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            // Utilisation de JOIN FETCH pour charger les promotions en une seule requête
            TypedQuery<Produit> query = em.createQuery(
                "SELECT DISTINCT p FROM Produit p LEFT JOIN FETCH p.promotions WHERE p.id = :id", 
                Produit.class);
            query.setParameter("id", produitId);
            
            Produit produit = query.getSingleResult();
            
            // Initialisation supplémentaire pour s'assurer que tout est chargé
            if (produit != null && produit.getPromotions() != null) {
                produit.getPromotions().size(); // Force l'initialisation
            }
            
            return produit;
        } catch (NoResultException e) {
            return null;
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de la recherche du produit avec promotions", e);
        } finally {
            em.close();
        }
    }

    @Override
    public void update(Produit produit) {
        EntityManager em = JpaUtil.getEntityManager();
        EntityTransaction transaction = null;
        try {
            transaction = em.getTransaction();
            transaction.begin();
            em.merge(produit);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null && transaction.isActive()) {
                transaction.rollback();
            }
            throw new RuntimeException("Erreur lors de la mise à jour du produit", e);
        } finally {
            em.close();
        }
    }

    @Override
    public void delete(Produit produit) {
        EntityManager em = JpaUtil.getEntityManager();
        EntityTransaction transaction = null;
        try {
            transaction = em.getTransaction();
            transaction.begin();
            
            // Charge le produit avec ses promotions
            Produit managedProduit = em.find(Produit.class, produit.getId());
            if (managedProduit != null) {
                // Détache les promotions du produit avant suppression
                managedProduit.getPromotions().clear();
                em.remove(managedProduit);
            }
            
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null && transaction.isActive()) {
                transaction.rollback();
            }
            throw new RuntimeException("Erreur lors de la suppression du produit", e);
        } finally {
            em.close();
        }
    }

    @Override
    public List<Produit> findProduitsEnStock() {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            return em.createQuery("SELECT p FROM Produit p WHERE p.stock > 0", Produit.class)
                     .getResultList();
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de la recherche des produits en stock", e);
        } finally {
            em.close();
        }
    }

    @Override
    public boolean checkIfProduitExists(String nom) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            Long count = em.createQuery("SELECT COUNT(p) FROM Produit p WHERE p.nom = :nom", Long.class)
                           .setParameter("nom", nom)
                           .getSingleResult();
            return count > 0;
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de la vérification de l'existence du produit", e);
        } finally {
            em.close();
        }
    }

    @Override
    public void updatePrixEtAncienPrix(int produitId, BigDecimal ancienPrix, BigDecimal nouveauPrix) {
        EntityManager em = JpaUtil.getEntityManager();
        EntityTransaction transaction = null;
        try {
            transaction = em.getTransaction();
            transaction.begin();
            
            Produit produit = em.find(Produit.class, produitId);
            if (produit != null) {
                produit.setAncienPrix(ancienPrix);
                produit.setPrix(nouveauPrix);
            }
            
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null && transaction.isActive()) {
                transaction.rollback();
            }
            throw new RuntimeException("Erreur lors de la mise à jour du prix et de l'ancien prix", e);
        } finally {
            em.close();
        }
    }

    @Override
    public void restaurerPrixInitial(int produitId, BigDecimal ancienPrix) {
        EntityManager em = JpaUtil.getEntityManager();
        EntityTransaction transaction = null;
        try {
            transaction = em.getTransaction();
            transaction.begin();
            
            Produit produit = em.find(Produit.class, produitId);
            if (produit != null) {
                produit.setPrix(ancienPrix);
                produit.setAncienPrix(null);
            }
            
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null && transaction.isActive()) {
                transaction.rollback();
            }
            throw new RuntimeException("Erreur lors de la restauration du prix initial", e);
        } finally {
            em.close();
        }
    }

    @Override
    public Set<Produit> findByPromotionId(int promotionId) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            TypedQuery<Produit> query = em.createQuery(
                "SELECT p FROM Produit p JOIN p.promotions promo WHERE promo.id = :promoId", 
                Produit.class);
            query.setParameter("promoId", promotionId);
            return new java.util.HashSet<>(query.getResultList());
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de la recherche des produits par promotion", e);
        } finally {
            em.close();
        }
    }
}