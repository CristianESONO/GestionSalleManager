package com.repositories.bd; // Vous pouvez le placer dans com.repositories.bd si vous préférez

import com.core.JpaUtil;
import com.entities.Game;
import com.entities.Poste;
import com.repositories.IGameRepository;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;

import java.util.List;

public class GameRepositoryJpa implements IGameRepository {

    @Override
    public void insert(Game game) {
        EntityManager em = JpaUtil.getEntityManager();
        EntityTransaction transaction = null;
        try {
            transaction = em.getTransaction();
            transaction.begin();
            em.persist(game); // Persiste la nouvelle entité Game
            transaction.commit();
            System.out.println("Jeu inséré avec succès. ID: " + game.getId() + ", Nom: " + game.getName());
        } catch (Exception e) {
            if (transaction != null && transaction.isActive()) {
                transaction.rollback();
            }
            System.err.println("Erreur lors de l'insertion du jeu : " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Erreur lors de l'insertion du jeu", e);
        } finally {
            em.close();
        }
    }

    @Override
    public List<Game> findAll() {
        EntityManager em = JpaUtil.getEntityManager();
        List<Game> games = null;
        try {
            // JPQL pour récupérer tous les jeux.
            // LEFT JOIN FETCH p.postes est utilisé pour charger les postes associés
            // en une seule requête, évitant ainsi les problèmes de LazyInitializationException.
            games = em.createQuery("SELECT DISTINCT g FROM Game g LEFT JOIN FETCH g.postes", Game.class)
          .getResultList();

        } catch (Exception e) {
            System.err.println("Erreur lors de la récupération de tous les jeux : " + e.getMessage());
            e.printStackTrace();
        } finally {
            em.close();
        }
        return games;
    }

    @Override
    public Game findById(int id) {
        EntityManager em = JpaUtil.getEntityManager();
        Game game = null;
        try {
            // Utilise find() pour récupérer un jeu par son ID.
            // Si vous avez besoin des postes associés (LAZY par défaut) après la fermeture de l'EM,
            // vous devrez les charger explicitement ici.
            game = em.find(Game.class, id);
            if (game != null && game.getPostes() != null) {
                game.getPostes().size(); // Accède à la collection pour la forcer à se charger
            }
        } catch (Exception e) {
            System.err.println("Erreur lors de la recherche du jeu par ID : " + e.getMessage());
            e.printStackTrace();
        } finally {
            em.close();
        }
        return game;
    }

    @Override
    public void update(Game game) {
        EntityManager em = JpaUtil.getEntityManager();
        EntityTransaction transaction = null;
        try {
            transaction = em.getTransaction();
            transaction.begin();
            em.merge(game); // Met à jour l'entité (ou l'attache si elle est détachée)
            transaction.commit();
            System.out.println("Jeu mis à jour avec succès. ID: " + game.getId());
        } catch (Exception e) {
            if (transaction != null && transaction.isActive()) {
                transaction.rollback();
            }
            System.err.println("Erreur lors de la mise à jour du jeu : " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Erreur lors de la mise à jour du jeu", e);
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
            Game game = em.find(Game.class, id); // Trouve l'entité par ID
            if (game != null) {
                em.remove(game); // Supprime l'entité
            }
            transaction.commit();
            System.out.println("Jeu supprimé avec succès. ID: " + id);
        } catch (Exception e) {
            if (transaction != null && transaction.isActive()) {
                transaction.rollback();
            }
            System.err.println("Erreur lors de la suppression du jeu : " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Erreur lors de la suppression du jeu", e);
        } finally {
            em.close();
        }
    }

    @Override
    public boolean existsByName(String name) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            // JPQL pour vérifier l'existence par nom
            Long count = em.createQuery("SELECT COUNT(g) FROM Game g WHERE g.name = :name", Long.class)
                           .setParameter("name", name)
                           .getSingleResult();
            return count > 0;
        } catch (NoResultException e) {
            return false; // Aucun résultat trouvé, donc n'existe pas
        } catch (Exception e) {
            System.err.println("Erreur lors de la vérification de l'existence du jeu par nom : " + e.getMessage());
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
            // Utilise find() pour vérifier l'existence par ID
            Game game = em.find(Game.class, id);
            return game != null;
        } catch (Exception e) {
            System.err.println("Erreur lors de la vérification de l'existence du jeu par ID : " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            em.close();
        }
    }

    @Override
    public void addPosteToGame(int gameId, int posteId) {
        EntityManager em = JpaUtil.getEntityManager();
        EntityTransaction transaction = null;
        try {
            transaction = em.getTransaction();
            transaction.begin();

            Game game = em.find(Game.class, gameId);
            Poste poste = em.find(Poste.class, posteId);

            if (game != null && poste != null) {
                // Ajouter le poste à la collection du jeu
                if (!game.getPostes().contains(poste)) {
                    game.getPostes().add(poste);
                }
                // Synchroniser la relation bidirectionnelle dans Poste si nécessaire
                if (!poste.getGames().contains(game)) {
                    poste.getGames().add(game);
                }
                em.merge(game); // Fusionne les changements
                em.merge(poste); // Fusionne les changements
            } else {
                System.err.println("Jeu ou Poste non trouvé pour l'ajout de relation.");
            }
            transaction.commit();
            System.out.println("Poste " + posteId + " ajouté au jeu " + gameId + " avec succès.");
        } catch (Exception e) {
            if (transaction != null && transaction.isActive()) {
                transaction.rollback();
            }
            System.err.println("Erreur lors de l'ajout du poste au jeu : " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Erreur lors de l'ajout du poste au jeu", e);
        } finally {
            em.close();
        }
    }

    @Override
    public void removePosteFromGame(int gameId, int posteId) {
        EntityManager em = JpaUtil.getEntityManager();
        EntityTransaction transaction = null;
        try {
            transaction = em.getTransaction();
            transaction.begin();

            Game game = em.find(Game.class, gameId);
            Poste posteToRemove = em.find(Poste.class, posteId);

            if (game != null && posteToRemove != null) {
                boolean removedFromGame = game.getPostes().remove(posteToRemove);
                boolean removedFromPoste = posteToRemove.getGames().remove(game); // Synchroniser l'autre côté

                if (removedFromGame || removedFromPoste) {
                    em.merge(game); // Fusionne les changements
                    em.merge(posteToRemove); // Fusionne les changements
                    System.out.println("Poste " + posteId + " supprimé du jeu " + gameId + " avec succès.");
                } else {
                    System.out.println("Le poste " + posteId + " n'était pas associé au jeu " + gameId + ".");
                }
            } else {
                System.err.println("Jeu ou Poste non trouvé pour la suppression de relation.");
            }
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null && transaction.isActive()) {
                transaction.rollback();
            }
            System.err.println("Erreur lors de la suppression du poste du jeu : " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Erreur lors de la suppression du poste du jeu", e);
        } finally {
            em.close();
        }
    }

    @Override
    public List<Poste> getPostesByGameId(int gameId) {
        EntityManager em = JpaUtil.getEntityManager();
        List<Poste> postes = null;
        try {
            // JPQL pour récupérer les postes associés à un jeu spécifique.
            // JOIN FETCH p.games est crucial pour charger la collection de jeux de chaque poste
            // si elle est LAZY, et pour s'assurer que la relation est bien établie.
            TypedQuery<Poste> query = em.createQuery(
                "SELECT p FROM Poste p JOIN FETCH p.games g WHERE g.id = :gameId", Poste.class);
            query.setParameter("gameId", gameId);
            postes = query.getResultList();
        } catch (Exception e) {
            System.err.println("Erreur lors de la récupération des postes par ID de jeu : " + e.getMessage());
            e.printStackTrace();
        } finally {
            em.close();
        }
        return postes;
    }
}
