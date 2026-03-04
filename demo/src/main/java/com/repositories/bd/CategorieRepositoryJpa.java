package com.repositories.bd;

import com.core.JpaUtil;
import com.entities.Categorie;
import com.repositories.ICategorieRepository;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;
import java.util.List;

public class CategorieRepositoryJpa implements ICategorieRepository {

    @Override
    public Categorie insert(Categorie categorie) {
        EntityManager em = JpaUtil.getEntityManager();
        EntityTransaction tx = null;
        try {
            tx = em.getTransaction();
            tx.begin();
            em.persist(categorie);
            tx.commit();
            return categorie;
        } catch (Exception e) {
            if (tx != null && tx.isActive()) tx.rollback();
            throw new RuntimeException(e);
        } finally {
            em.close();
        }
    }

    @Override
    public List<Categorie> findAll() {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            return em.createQuery("SELECT c FROM Categorie c ORDER BY c.nom", Categorie.class).getResultList();
        } finally {
            em.close();
        }
    }

    @Override
    public Categorie findById(int id) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            return em.find(Categorie.class, id);
        } finally {
            em.close();
        }
    }

    @Override
    public void update(Categorie categorie) {
        EntityManager em = JpaUtil.getEntityManager();
        EntityTransaction tx = null;
        try {
            tx = em.getTransaction();
            tx.begin();
            em.merge(categorie);
            tx.commit();
        } catch (Exception e) {
            if (tx != null && tx.isActive()) tx.rollback();
            throw new RuntimeException(e);
        } finally {
            em.close();
        }
    }

    @Override
    public void delete(Categorie categorie) {
        EntityManager em = JpaUtil.getEntityManager();
        EntityTransaction tx = null;
        try {
            tx = em.getTransaction();
            tx.begin();
            Categorie managed = em.find(Categorie.class, categorie.getId());
            if (managed != null) em.remove(managed);
            tx.commit();
        } catch (Exception e) {
            if (tx != null && tx.isActive()) tx.rollback();
            throw new RuntimeException(e);
        } finally {
            em.close();
        }
    }

    @Override
    public boolean existsByNom(String nom) {
        if (nom == null || nom.trim().isEmpty()) return false;
        EntityManager em = JpaUtil.getEntityManager();
        try {
            Long count = em.createQuery("SELECT COUNT(c) FROM Categorie c WHERE LOWER(TRIM(c.nom)) = LOWER(TRIM(:nom))", Long.class)
                .setParameter("nom", nom)
                .getSingleResult();
            return count != null && count > 0;
        } finally {
            em.close();
        }
    }

    @Override
    public Categorie findByNom(String nom) {
        if (nom == null || nom.trim().isEmpty()) return null;
        EntityManager em = JpaUtil.getEntityManager();
        try {
            TypedQuery<Categorie> q = em.createQuery("SELECT c FROM Categorie c WHERE LOWER(TRIM(c.nom)) = LOWER(TRIM(:nom))", Categorie.class);
            q.setParameter("nom", nom);
            return q.getSingleResult();
        } catch (NoResultException e) {
            return null;
        } finally {
            em.close();
        }
    }
}
