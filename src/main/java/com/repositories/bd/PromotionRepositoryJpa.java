package com.repositories.bd;

import com.core.JpaUtil;
import com.entities.Promotion;
import com.entities.Produit;
import com.repositories.IPromoRepository;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.hibernate.Hibernate;

import java.util.List;
import java.util.Optional;

public class PromotionRepositoryJpa implements IPromoRepository {

    @Override
    public Promotion insert(Promotion promo) {
        EntityManager em = JpaUtil.getEntityManager();
        EntityTransaction transaction = null;
        try {
            transaction = em.getTransaction();
            transaction.begin();
            em.persist(promo);
            transaction.commit();
            return promo;
        } catch (Exception e) {
            if (transaction != null && transaction.isActive()) {
                transaction.rollback();
            }
            throw new RuntimeException("Erreur lors de l'insertion de la promotion", e);
        } finally {
            em.close();
        }
    }

    @Override
    public void update(Promotion promo) {
        EntityManager em = JpaUtil.getEntityManager();
        EntityTransaction transaction = null;
        try {
            transaction = em.getTransaction();
            transaction.begin();
            em.merge(promo);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null && transaction.isActive()) {
                transaction.rollback();
            }
            throw new RuntimeException("Erreur lors de la mise à jour de la promotion", e);
        } finally {
            em.close();
        }
    }

    @Override
    public void delete(Promotion promo) {
        EntityManager em = JpaUtil.getEntityManager();
        EntityTransaction transaction = null;
        try {
            transaction = em.getTransaction();
            transaction.begin();
            
            // Charge la promotion avec ses produits
            Promotion managedPromo = em.find(Promotion.class, promo.getId());
            if (managedPromo != null) {
                // Détache les produits de la promotion avant suppression
                managedPromo.getProduits().clear();
                em.remove(managedPromo);
            }
            
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null && transaction.isActive()) {
                transaction.rollback();
            }
            throw new RuntimeException("Erreur lors de la suppression de la promotion", e);
        } finally {
            em.close();
        }
    }

    @Override
    public Promotion findById(int id) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            return em.find(Promotion.class, id);
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de la recherche de la promotion par ID", e);
        } finally {
            em.close();
        }
    }

    @Override
    public Promotion findByIdWithProduits(int id) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            TypedQuery<Promotion> query = em.createQuery(
                "SELECT DISTINCT p FROM Promotion p LEFT JOIN FETCH p.produits WHERE p.id = :id", 
                Promotion.class);
            query.setParameter("id", id);
            return query.getSingleResult();
        } catch (NoResultException e) {
            return null;
        } finally {
            em.close();
        }
    }

    @Override
    public List<Promotion> findAll() {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            return em.createQuery("SELECT p FROM Promotion p", Promotion.class).getResultList();
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de la récupération de toutes les promotions", e);
        } finally {
            em.close();
        }
    }

    @Override
    public Optional<Promotion> findByNom(String nom) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            TypedQuery<Promotion> query = em.createQuery(
                "SELECT p FROM Promotion p WHERE p.nom = :nom", Promotion.class);
            query.setParameter("nom", nom);
            return Optional.ofNullable(query.getSingleResult());
        } catch (NoResultException e) {
            return Optional.empty();
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de la recherche de la promotion par nom", e);
        } finally {
            em.close();
        }
    }

    @Override
    public List<Promotion> findAllWithProduits() {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            // Utilisation de JOIN FETCH pour charger toutes les promotions avec leurs produits
            TypedQuery<Promotion> query = em.createQuery(
                "SELECT DISTINCT p FROM Promotion p LEFT JOIN FETCH p.produits", 
                Promotion.class);
            
            List<Promotion> result = query.getResultList();
            
            // Initialisation supplémentaire pour chaque collection
            for (Promotion p : result) {
                if (p.getProduits() != null) {
                    p.getProduits().size(); // Force l'initialisation
                }
            }
            
            return result;
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de la récupération des promotions avec produits", e);
        } finally {
            em.close();
        }
    }

    @Override
    public boolean existsById(int id) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            CriteriaBuilder cb = em.getCriteriaBuilder();
            CriteriaQuery<Long> cq = cb.createQuery(Long.class);
            Root<Promotion> root = cq.from(Promotion.class);
            
            cq.select(cb.count(root));
            cq.where(cb.equal(root.get("id"), id));
            
            return em.createQuery(cq).getSingleResult() > 0;
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de la vérification de l'existence de la promotion", e);
        } finally {
            em.close();
        }
    }
}