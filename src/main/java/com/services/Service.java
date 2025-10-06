package com.services;

import com.entities.*;
import com.repositories.*; // Importe toutes les interfaces de dépôt
import com.repositories.bd.*; // Importe les implémentations JPA spécifiques des dépôts
import com.utils.ReservationReceiptPrinter;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors; // Pour les opérations de stream

import javax.persistence.EntityNotFoundException;
import javax.transaction.Transactional;

import org.hibernate.Hibernate;
import org.mindrot.jbcrypt.BCrypt;

public class Service implements IService {

    // Injection des dépendances des dépôts JPA
    private IGameSessionRepository gameSessionRepository;
    private IProduitRepository produitRepository;
    private IReservationRepository reservationRepository;
    private IUserRepository userRepository; // Pour User, Client, Parrain via héritage
    private IPaymentRepository paymentRepository;
    private IClientRepository clientRepository; // Pour les opérations spécifiques aux clients
    private IGameRepository gameRepository;
    private IPosteRepository posteRepository;
    private IParrainRepository parrainRepository; // Pour les opérations spécifiques aux parrains
    private IPromoRepository promoRepository;

    // Champ pour l'utilisateur actuellement connecté
    private User currentUser;

    // Constructeur pour l'injection des dépôts
    // La Fabrique sera responsable de fournir ces implémentations concrètes (par exemple, UserRepositoryJpa)
    public Service(IGameSessionRepository gameSessionRepository, IProduitRepository produitRepository,
                   IReservationRepository reservationRepository, IUserRepository userRepository,
                   IPaymentRepository paymentRepository, IClientRepository clientRepository,
                   IGameRepository gameRepository, IPosteRepository posteRepository,
                   IParrainRepository parrainRepository, IPromoRepository promoRepository) {
        this.gameSessionRepository = gameSessionRepository;
        this.produitRepository = produitRepository;
        this.reservationRepository = reservationRepository;
        this.userRepository = userRepository;
        this.paymentRepository = paymentRepository;
        this.clientRepository = clientRepository;
        this.gameRepository = gameRepository;
        this.posteRepository = posteRepository;
        this.parrainRepository = parrainRepository;
        this.promoRepository = promoRepository;
    }

    // --- User methods ---
   @Override
    public User seConnecter(String login, String password) {
        // 1. Récupère l'utilisateur par login/email depuis la base de données
        User user = userRepository.findByEmail(login);
        if (user == null) {
            return null; // Utilisateur non trouvé
        }
        // 2. Vérifie si le mot de passe saisi correspond au hash stocké
        if (BCrypt.checkpw(password, user.getPassword())) {
            return user; // Mot de passe correct
        } else {
            return null; // Mot de passe incorrect
        }
    }


    @Override
    public List<User> findAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public void addUser(User newUser) throws Exception {
        // Logique métier avant l'ajout si nécessaire (ex: validation)
        if (userRepository.existsByName(newUser.getName())) {
            throw new Exception("Un utilisateur avec ce nom existe déjà.");
        }
        // Utilise la nouvelle méthode findByEmail du dépôt
        if (newUser.getEmail() != null && userRepository.findByEmail(newUser.getEmail()) != null) {
             throw new Exception("Un utilisateur avec cet email existe déjà.");
        }
        userRepository.addUser(newUser);
    }

    @Override
    public void deleteUser(User user) throws Exception {
        userRepository.delete(user.getId());
    }

    @Override
    public void updateUser(User user) throws Exception {
        userRepository.updateUser(user);
    }

    @Override
    public List<GameSession> findPausedSessionsByClientId(int clientId) {
        return gameSessionRepository.findGameSessionsByClientId(clientId)
            .stream()
            .filter(session -> "En pause".equals(session.getStatus()))
            .collect(Collectors.toList());
    }


    @Override
    public User getCurrentUser() {
        return currentUser;
    }

    @Override
    public void setCurrentUser(User user) {
        this.currentUser = user;
    }

    @Override
    public boolean existsUserById(int id) {
        return userRepository.existsById(id);
    }

    @Override
    public boolean existsUserByName(String name) {
        return userRepository.existsByName(name);
    }

    // --- Client methods ---
    @Override
    public List<Client> getAllClients() {
        return clientRepository.findAll();
    }

    @Override
    public Client addClient(Client client) throws Exception {
        if (clientRepository.findByTel(client.getPhone()) != null) {
            throw new Exception("Un client avec ce numéro de téléphone existe déjà.");
        }
        if (client.getEmail() != null && clientRepository.findByEmail(client.getEmail()) != null) {
            throw new Exception("Un client avec cet email existe déjà.");
        }
        return clientRepository.insert(client);
    }

    @Override
    public void updateClient(Client client) throws Exception {
        clientRepository.update(client);
    }

    @Override
    public void deleteClient(Client client) throws Exception {
        clientRepository.delete(client);
    }

    @Override
    public Client findByEmail(String email) {
        return clientRepository.findByEmail(email);
    }

    @Override
    public Client findByTel(String tel) {
        return clientRepository.findByTel(tel);
    }

    @Override
    public boolean existsClientById(int id) {
        return clientRepository.findById(id) != null;
    }

    @Override
    public boolean existsClientByName(String name) {
        return clientRepository.existsByName(name);
    }

    // --- Parrain methods ---
    @Override
    public Parrain addParrain(Parrain parrain) throws Exception {
        if (parrainRepository.findByCodeParrainage(parrain.getCodeParrainage()) != null) {
            throw new Exception("Un parrain avec ce code de parrainage existe déjà.");
        }
        if (parrainRepository.findByEmail(parrain.getEmail()) != null) {
            throw new Exception("Un parrain avec cet email existe déjà.");
        }
        return parrainRepository.insert(parrain);
    }

    @Override
    public List<Parrain> getAllParrains() {
        return parrainRepository.findAll();
    }

    @Override
    public Parrain getParrainById(int id) {
        return parrainRepository.findById(id);
    }

    @Override
    public void updateParrain(Parrain parrain) throws Exception {
        parrainRepository.update(parrain);
    }

    @Override
    public void deleteParrain(Parrain parrain) throws Exception {
        parrainRepository.delete(parrain);
    }

    @Override
    public Parrain getParrainByCodeParrainage(String codeParrainage) {
        return parrainRepository.findByCodeParrainage(codeParrainage);
    }

    @Override
    public boolean existsParrainByName(String name) {
        return parrainRepository.existsByName(name);
    }

    @Override
    public boolean existsParrainById(int id) {
        return parrainRepository.findById(id) != null;
    }

    // --- Game methods ---
    @Override
    public List<Game> getAllGames() {
        return gameRepository.findAll();
    }

    @Override
    public void deleteGame(Game game) throws Exception {
        gameRepository.delete(game.getId());
    }

    @Override
    public void updateGame(Game game) throws Exception {
        gameRepository.update(game);
    }

    @Override
    public void addGame(Game game) throws Exception {
        if (gameRepository.existsByName(game.getName())) {
            throw new Exception("Un jeu avec ce nom existe déjà.");
        }
        gameRepository.insert(game);
    }

    @Override
    public boolean existsGameByName(String name) {
        return gameRepository.existsByName(name);
    }

    @Override
    public boolean existsGameById(int id) {
        return gameRepository.existsById(id);
    }

    // --- Poste methods ---
    @Override
    public List<Poste> getPostes() {
        return posteRepository.findAll();
    }

    public List<Poste> getPostesSansJeux() 
    {
        return posteRepository.findPostesSansJeux();
    }


    @Override
    public void deletePoste(Poste poste) throws Exception {
        posteRepository.delete(poste);
    }

    @Override
    public void updatePoste(Poste poste) throws Exception {
        posteRepository.update(poste);
    }

    @Override
    public Poste addPoste(Poste poste) throws Exception {
        // 1. Insérer le poste pour que l'ID soit généré
        Poste newPoste = posteRepository.insert(poste);
        
        // 2. Mettre à jour le nom du poste avec l'ID généré
        newPoste.setName("N°" + newPoste.getId());
        
        // 3. Persister la mise à jour du nom
        posteRepository.update(newPoste);
        
        return newPoste;
    }

    @Override
    public List<Poste> getPostesForGame(Game game) {
        return posteRepository.findByGame(game);
    }

    @Override
    public void addPosteToGame(Poste poste, Game game) throws Exception {
        gameRepository.addPosteToGame(game.getId(), poste.getId());
    }

    @Override
    public void removePosteFromGame(Poste poste, Game game) throws Exception {
        gameRepository.removePosteFromGame(game.getId(), poste.getId());
    }

    @Override
    public boolean existsPosteById(int id) {
        return posteRepository.findById(id) != null;
    }

    // --- GameSession methods ---
    @Override
    public List<GameSession> getAllGameSessions() {
        return gameSessionRepository.getAllGameSessions();
    }

    @Override
    public GameSession addGameSession(GameSession gameSession) throws Exception {
        // Logique métier avant l'ajout si nécessaire (ex: vérifier la disponibilité du poste)
        if (!posteRepository.checkAvailability(gameSession.getPoste().getId())) {
            throw new Exception("Le poste sélectionné n'est pas disponible.");
        }
        return gameSessionRepository.addGameSession(gameSession);
    }

    @Override
    public GameSession getGameSessionById(int id) {
        return gameSessionRepository.getGameSessionById(id);
    }

    @Override
    public boolean updateGameSession(GameSession gameSession) throws Exception {
        return gameSessionRepository.updateGameSession(gameSession);
    }

    @Override
    public boolean deleteGameSession(int id) throws Exception {
        return gameSessionRepository.deleteGameSession(id);
    }

    @Override
    public GameSession getActiveSessionForPoste(Poste poste) {
        // Utilise la nouvelle méthode du dépôt pour une recherche plus efficace
        return gameSessionRepository.findActiveSessionForPoste(poste.getId());
    }

    @Override
    public boolean reduceRemainingTime(int gameSessionId, Duration timeElapsed) throws Exception {
        return gameSessionRepository.reduceRemainingTime(gameSessionId, timeElapsed);
    }

    @Override
    public List<GameSession> findGameSessionsByClientId(int clientId) {
        return gameSessionRepository.findGameSessionsByClientId(clientId);
    }

    @Override
    public boolean existsGameSessionById(int id) {
        return gameSessionRepository.existsById(id);
    }

    // --- Produit methods ---
    @Override
    public Produit insertProduit(Produit produit) throws Exception {
        if (produitRepository.checkIfProduitExists(produit.getNom())) {
            throw new Exception("Un produit avec ce nom existe déjà.");
        }
        return produitRepository.insert(produit);
    }

    @Override
    public List<Produit> findAllProduits() {
        return produitRepository.findAll();
    }

    @Override
    public Produit findProduitById(int id) {
        return produitRepository.findById(id);
    }

    @Override
    public void updateProduit(Produit produit) throws Exception {
        produitRepository.update(produit);
    }

    @Override
    public void deleteProduit(Produit produit) throws Exception {
        produitRepository.delete(produit);
    }

    @Override
    public List<Produit> findProduitsEnStock() {
        return produitRepository.findProduitsEnStock();
    }

    @Override
    public boolean checkIfProduitExists(String nom) {
        return produitRepository.checkIfProduitExists(nom);
    }

    @Override
    public void updateProduitStocks(Map<Produit, Integer> produitsEtQuantites) throws Exception {
        for (Map.Entry<Produit, Integer> entry : produitsEtQuantites.entrySet()) {
            Produit produit = entry.getKey();
            Integer quantite = entry.getValue();
            Produit managedProduit = produitRepository.findById(produit.getId()); // Récupérer l'entité gérée
            if (managedProduit != null) {
                if (managedProduit.reducerStock(quantite)) { // Utilise la logique métier de l'entité
                    produitRepository.update(managedProduit); // Persiste le changement
                } else {
                    throw new Exception("Stock insuffisant pour le produit : " + managedProduit.getNom());
                }
            } else {
                throw new Exception("Produit non trouvé pour la mise à jour du stock : " + produit.getNom());
            }
        }
    }



    @Override
    public boolean existsProduitById(int id) {
        return produitRepository.findById(id) != null;
    }

    // --- Reservation methods ---
    @Override
    public Reservation insertReservation(Reservation reservation) throws Exception {
        // Logique métier avant l'insertion (ex: vérifier la disponibilité du poste, du jeu)
        if (!posteRepository.checkAvailability(reservation.getPoste().getId())) {
            throw new Exception("Le poste sélectionné n'est pas disponible.");
        }
        // Assurez-vous que les entités liées (Client, Poste, Game, Promotion) sont gérées
        // ou rattachées si elles viennent d'un contexte détaché.
        // Le dépôt JPA gérera cela avec em.persist().
        return reservationRepository.insert(reservation);
    }

    @Override
    public List<Reservation> findAllReservations() {
        return reservationRepository.findAll();
    }

    @Override
    public Reservation findReservationById(int id) {
        return reservationRepository.findById(id);
    }

    @Override
    public boolean updateReservation(Reservation reservation) throws Exception {
        return reservationRepository.update(reservation);
    }

    @Override
    public boolean deleteReservation(int id) throws Exception {
        return reservationRepository.delete(id);
    }

    @Override
    public List<Reservation> findReservationsByClientId(int clientId) {
        return reservationRepository.findByClientId(clientId);
    }

    @Override
    public boolean existsByTicketNumber(String numeroTicket) {
        return reservationRepository.existsByTicketNumber(numeroTicket);
    }

    @Override
    public boolean existsReservationById(int id) {
        return reservationRepository.existsById(id);
    }

    @Override
    public List<Reservation> findReservationsByClientIdAndDateRange(int clientId, LocalDateTime startDate, LocalDateTime endDate) {
        return reservationRepository.findByClientIdAndDateRange(clientId, startDate, endDate);
    }

    @Override
    public double calculateReservationPrice(Reservation reservation) throws Exception {
        // La logique de calcul est déjà dans l'entité Reservation, il suffit de l'appeler.
        // Assurez-vous que 'appliedPromotion' est chargé si nécessaire pour le calcul.
        return reservation.calculatePriceBasedOnDuration();
    }

    @Override
    @Transactional
    public Reservation saveReservation(Reservation reservation, Optional<Promotion> promotion) throws Exception {
        // Applique la promotion si elle est présente
        if (promotion.isPresent()) {
            Promotion promo = promotion.get();

            // 1. Vérifie le type de promotion
            if (promo.getTypePromotion() != TypePromotion.RESERVATION) {
                throw new Exception("Seules les promotions de type 'Réservation' peuvent être appliquées à une réservation.");
            }

            // 2. Vérifie la validité de la promotion pour la date de réservation
            if (!promo.isValid(reservation.getReservationDate().toLocalDate())) {
                throw new Exception("La promotion n'est pas valide pour la date de cette réservation.");
            }

            // 3. Assigne la promotion à la réservation
            reservation.setAppliedPromotion(promo);
        }

        // Recalcule toujours le prix (avec ou sans promotion)
        reservation.setTotalPrice(reservation.calculatePriceBasedOnDuration());

        // Persiste la réservation
        return insertReservation(reservation);
    }


    @Override
    public List<Promotion> getValidReservationsPromotionsForDate(LocalDate date) {
        return promoRepository.findValidPromotionsByTypeAndDate(TypePromotion.RESERVATION, date);
    }


    // --- Payment methods ---
    @Override
    public List<Payment> getAllPayments() {
        return paymentRepository.getAllPayments();
    }

    @Override
    public void addPayment(Payment payment) throws Exception {
        paymentRepository.addPayment(payment);
    }

    @Override
    public Optional<Payment> getPaymentById(int id) {
        return paymentRepository.getPaymentById(id);
    }

    @Override
    public void updatePayment(Payment payment) throws Exception {
        paymentRepository.updatePayment(payment);
    }

    @Override
    public void deletePayment(int id) throws Exception {
        paymentRepository.deletePayment(id);
    }

    @Override
    public boolean existsPaymentById(int id) {
        return paymentRepository.existsById(id);
    }

    // --- Promotion methods ---
    @Override
    public Promotion addPromotion(Promotion promo) throws Exception {
        if (promoRepository.findByNom(promo.getNom()).isPresent()) {
            throw new Exception("Une promotion avec ce nom existe déjà.");
        }
        return promoRepository.insert(promo);
    }

    @Override
    public void updatePromotion(Promotion promo) throws Exception {
        promoRepository.update(promo);
    }

    @Override
    public void deletePromotion(Promotion promo) throws Exception {
        // Avant de supprimer la promotion, retirez-la des produits associés
        // Ceci est une logique métier qui peut nécessiter une transaction.
        // Si CascadeType.ALL est utilisé sur la relation Produit-Promotion,
        // la suppression de la promotion pourrait aussi supprimer les produits,
        // ce qui n'est probablement pas souhaité.
        // Il est préférable de gérer la dissociation manuellement ou via une requête JPQL.

        // Logique pour restaurer les prix des produits liés à cette promotion AVANT de la supprimer
        // Ceci nécessiterait de récupérer tous les produits liés à cette promotion
        // et de restaurer leurs prix.
        // Exemple (nécessiterait une méthode dans ProduitRepository pour trouver par promotion) :
        // List<Produit> produitsAssocies = produitRepository.findByPromotionId(promo.getId());
        // for (Produit p : produitsAssocies) {
        //     // Logique pour restaurer le prix du produit p
        //     // p.setPrix(p.getAncienPrix()); // Ou une autre logique de restauration
        //     // produitRepository.update(p);
        // }
        
        // Ou, si la relation ManyToMany est gérée côté Promotion, vous pouvez simplement vider la liste:
        // promo.getProduits().clear();
        // promoRepository.update(promo); // Pour persister le changement de relation

        promoRepository.delete(promo);
    }

    @Override
    public List<Promotion> getAllPromotions() {
        return promoRepository.findAll();
    }

    @Override
    public Promotion getPromotionById(int id) {
        // Utilise la méthode findById du dépôt
        return promoRepository.findById(id);
    }

    @Transactional
    @Override
    public void appliquerPromotion(Promotion promo) throws Exception {
        Promotion managedPromo = getPromotionByIdWithProduits(promo.getId());
        if (managedPromo == null) {
            throw new Exception("Promotion non trouvée");
        }

        // Activer la promotion
        managedPromo.setActif(true);
        promoRepository.update(managedPromo);

        // Appliquer la promotion
        if (managedPromo.getTypePromotion() == TypePromotion.PRODUIT) {
            for (Produit p : managedPromo.getProduits()) {
                // Trouver toutes les promotions actives pour ce produit
                List<Promotion> activePromotionsForProduit = p.getPromotions().stream()
                    .filter(pr -> pr.isActif() && pr.getTypePromotion() == TypePromotion.PRODUIT)
                    .collect(Collectors.toList());

                // Si d'autres promotions actives existent, comparer les taux de réduction
                if (!activePromotionsForProduit.isEmpty()) {
                    // Trouver la promotion avec le taux de réduction le plus élevé
                    Promotion highestRatePromo = activePromotionsForProduit.stream()
                        .max(Comparator.comparingDouble(Promotion::getTauxReduction))
                        .orElse(null);

                    // Si la promotion actuelle a un taux de réduction plus élevé, l'appliquer
                    if (highestRatePromo == null || managedPromo.getTauxReduction() > highestRatePromo.getTauxReduction()) {
                        // Appliquer la réduction au produit
                        BigDecimal prixOriginal = p.getAncienPrix() != null ? p.getAncienPrix() : p.getPrix();
                        BigDecimal nouveauPrix = prixOriginal.multiply(BigDecimal.ONE.subtract(BigDecimal.valueOf(managedPromo.getTauxReduction())));
                        p.setAncienPrix(prixOriginal);
                        p.setPrix(nouveauPrix);
                        produitRepository.update(p);
                    }
                } else {
                    // Aucune autre promotion active, appliquer la réduction
                    BigDecimal prixOriginal = p.getPrix();
                    BigDecimal nouveauPrix = prixOriginal.multiply(BigDecimal.ONE.subtract(BigDecimal.valueOf(managedPromo.getTauxReduction())));
                    p.setAncienPrix(prixOriginal);
                    p.setPrix(nouveauPrix);
                    produitRepository.update(p);
                }
            }
        } else if (managedPromo.getTypePromotion() == TypePromotion.RESERVATION) {
            // Pour les promotions de type RESERVATION, on ne fait rien ici
            // car elles sont appliquées directement lors de la création ou de la mise à jour d'une réservation
        }
    }

    @Transactional
    @Override
    public void retirerPromotion(Promotion promo) throws Exception {
        Promotion managedPromo = promoRepository.findByIdWithProduits(promo.getId());
        if (managedPromo == null) {
            throw new Exception("Promotion non trouvée");
        }

        // Désactiver la promotion
        managedPromo.setActif(false);
        promoRepository.update(managedPromo);

        // Restaurer les prix originaux des produits si nécessaire
        if (managedPromo.getTypePromotion() == TypePromotion.PRODUIT) {
            for (Produit p : managedPromo.getProduits()) {
                // Vérifier si le produit a encore d'autres promotions actives
                List<Promotion> activePromotionsForProduit = p.getPromotions().stream()
                    .filter(pr -> pr.isActif() && pr.getTypePromotion() == TypePromotion.PRODUIT && !pr.equals(managedPromo))
                    .collect(Collectors.toList());

                // Si aucune autre promotion active n'existe, restaurer le prix original
                if (activePromotionsForProduit.isEmpty()) {
                    if (p.getAncienPrix() != null) {
                        p.setPrix(p.getAncienPrix());
                        p.setAncienPrix(null);
                        produitRepository.update(p);
                    }
                } else {
                    // Sinon, appliquer la promotion avec le taux de réduction le plus élevé
                    Promotion highestRatePromo = activePromotionsForProduit.stream()
                        .max(Comparator.comparingDouble(Promotion::getTauxReduction))
                        .orElse(null);

                    if (highestRatePromo != null) {
                        BigDecimal prixOriginal = p.getAncienPrix() != null ? p.getAncienPrix() : p.getPrix();
                        BigDecimal nouveauPrix = prixOriginal.multiply(BigDecimal.ONE.subtract(BigDecimal.valueOf(highestRatePromo.getTauxReduction())));
                        p.setAncienPrix(prixOriginal);
                        p.setPrix(nouveauPrix);
                        produitRepository.update(p);
                    }
                }
            }
        } else if (managedPromo.getTypePromotion() == TypePromotion.RESERVATION) {
            // Pour les promotions de type RESERVATION, on ne fait rien ici
            // car elles ne modifient pas directement les prix des produits
        }
    }


   @Transactional
    @Override
    public void addProduitToPromotion(int produitId, int promotionId) throws Exception {
        // 1. Trouver les entités gérées avec leurs relations
        Promotion promo = promoRepository.findByIdWithProduits(promotionId);
        Produit produit = produitRepository.findByIdWithPromotions(produitId);

        if (promo == null || produit == null) {
            throw new Exception("Produit ou Promotion non trouvé pour l'ajout de relation.");
        }

        // 2. Vérifier que le produit n'est pas déjà associé
        if (promo.getProduits().contains(produit)) {
            throw new Exception("Le produit est déjà associé à cette promotion.");
        }

        // 3. Ajouter le produit à la promotion et vice-versa
        promo.addProduit(produit);
        produit.addPromotion(promo);

        // 4. Sauvegarder les modifications
        promoRepository.update(promo);
        produitRepository.update(produit);
    }

    @Override
    @Transactional
    public void removeProduitFromPromotion(int produitId, int promotionId) throws Exception {
        // 1. Trouver les entités gérées
        Promotion promo = promoRepository.findById(promotionId);
        Produit produit = produitRepository.findById(produitId);

        if (promo == null || produit == null) {
            throw new Exception("Produit ou Promotion non trouvé pour la suppression de relation.");
        }

        // 2. Vérifier que le produit est associé à la promotion
        if (!promo.getProduits().contains(produit)) {
            throw new Exception("Le produit n'est pas associé à cette promotion.");
        }

        // 3. Retirer le produit de la promotion
        promo.removeProduit(produit);
        promoRepository.update(promo);

        // 4. Restaurer le prix du produit si c'était la seule promotion active
        if (produit.getPromotions().isEmpty() && produit.getAncienPrix() != null) {
            produit.setPrix(produit.getAncienPrix());
            produit.setAncienPrix(null);
            produitRepository.update(produit);
        }
    }


    @Override
    public boolean existsProduitInPromotion(int produitId, int promotionId) {
        Promotion promo = promoRepository.findById(promotionId);
        Produit produit = produitRepository.findById(produitId);
        return promo != null && produit != null && promo.getProduits().contains(produit);
    }

    @Override
    @Transactional
    public void removeAllProduitsFromPromotion(int promotionId) throws Exception {
        // 1. Trouver la promotion gérée
        Promotion promo = promoRepository.findById(promotionId);
        if (promo == null) {
            throw new Exception("Promotion non trouvée.");
        }

        // 2. Restaurer les prix des produits et retirer la promotion
        if (promo.getProduits() != null) {
            // Utiliser une copie pour éviter ConcurrentModificationException
            for (Produit p : new HashSet<>(promo.getProduits())) {
                if (p.getAncienPrix() != null) {
                    p.setPrix(p.getAncienPrix());
                    p.setAncienPrix(null);
                    produitRepository.update(p);
                }
                // Retirer la promotion du côté du produit
                p.getPromotions().remove(promo);
                produitRepository.update(p);
            }
            promo.getProduits().clear(); // Vider la collection de la promotion
            promoRepository.update(promo);
        }
    }


    @Override
    public void updatePrixEtAncienPrix(int produitId, BigDecimal ancienPrix, BigDecimal nouveauPrix) throws Exception {
        produitRepository.updatePrixEtAncienPrix(produitId, ancienPrix, nouveauPrix);
    }

    @Override
    public void restaurerPrixInitial(int produitId, BigDecimal ancienPrixPassed) {
        try {
            produitRepository.restaurerPrixInitial(produitId, ancienPrixPassed);
        } catch (Exception e) {
            System.err.println("Erreur lors de la restauration du prix initial du produit : " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Erreur lors de la restauration du prix initial du produit", e);
        }
    }

    @Override
    public List<Promotion> getValidPromotionsForDate(LocalDate date) {
        return promoRepository.findAll().stream()
                .filter(p -> p.isValid(date))  // Utilise la méthode isValid(LocalDate) de Promotion
                .collect(Collectors.toList());
    }


    @Override
    public Optional<Promotion> getPromotionByNom(String nom) {
        return promoRepository.findByNom(nom);
    }

    @Override
    public List<Promotion> getActiveAndValidPromotions(LocalDate date) {
        return promoRepository.findAll().stream()
                .filter(p -> p.isActif() && p.isValid(date))  // Utilise la méthode isValid(LocalDate) de Promotion
                .collect(Collectors.toList());
    }


    @Override
    @Transactional
    public void restoreProductPricesAfterAllPromotionsRemoved(int produitId) throws Exception {
        Produit produit = produitRepository.findById(produitId);
        if (produit == null) {
            throw new Exception("Produit non trouvé pour la restauration des prix.");
        }

        // S'il n'y a plus de promotions associées et qu'un ancien prix existe
        if (produit.getPromotions().isEmpty() && produit.getAncienPrix() != null) {
            produit.setPrix(produit.getAncienPrix());
            produit.setAncienPrix(null);
            produitRepository.update(produit);
        }
    }

    // Dans ton Service.java
    public Optional<Promotion> getActivePromotionForToday() {
        LocalDate today = LocalDate.now();
        return promoRepository.findAll().stream()
            .filter(p -> p.isActif()
                && p.getTypePromotion() == TypePromotion.RESERVATION  // Filtre par type RESERVATION
                && p.isValid(today))
            .findFirst();
    }

    public Optional<Promotion> getBestActivePromotionForToday() {
        LocalDate today = LocalDate.now();
        return promoRepository.findAll().stream()
            .filter(p -> p.isActif()
                && p.getTypePromotion() == TypePromotion.RESERVATION
                && p.isValid(today))
            .max(Comparator.comparingDouble(Promotion::getTauxReduction)); // Sélectionne la promotion avec le taux le plus élevé
    }



    @Transactional
    public Promotion getPromotionByIdWithProduits(int id) {
        return promoRepository.findByIdWithProduits(id);
    }



    @Transactional
    public List<Produit> getProduitsByPromotionId(int promotionId) {
        Promotion promo = getPromotionByIdWithProduits(promotionId);
        if (promo != null) {
            return new ArrayList<>(promo.getProduits());
        }
        return Collections.emptyList();
    }




    @Override
    public boolean existsPromotionById(int id) {
        return promoRepository.findById(id) != null;
    }

    @Override
    public boolean existsPromotionByName(String name) {
        return promoRepository.findByNom(name).isPresent();
    }


    // --- Total Play Time (calculé à partir des réservations) ---
    @Override
    public Duration getTotalPlayTime(int clientId) {
        List<Reservation> reservations = reservationRepository.findByClientId(clientId);
        if (reservations == null || reservations.isEmpty()) {
            return Duration.ZERO;
        }
        return reservations.stream()
                .map(Reservation::getDuration)
                .filter(Objects::nonNull) // Assurez-vous que la durée n'est pas nulle
                .reduce(Duration.ZERO, Duration::plus);
    }

    @Override
    // Dans votre Service.java
    public List<Reservation> findReservationsByUser(User user) {
        if (user == null) return Collections.emptyList();
        
        // Solution 1: Utilisation directe du repository si vous utilisez Spring Data JPA
        // return reservationRepository.findByCreatedBy(user);
        
        // Solution 2: Filtrage manuel
        return reservationRepository.findAll().stream()
            .filter(r -> {
                User createdBy = r.getCreatedBy();
                return createdBy != null && createdBy.getId() == user.getId(); // Compare par ID
            })
            .collect(Collectors.toList());
    }

@Override
@Transactional
public void pauseGameSession(GameSession session) throws Exception {
    if (!"Active".equalsIgnoreCase(session.getStatus())) {
        throw new Exception("Seules les sessions actives peuvent être mises en pause.");
    }

    // Calculer le temps restant ACTUEL au moment de la pause
    LocalDateTime endTime = session.getStartTime().plus(session.getPaidDuration());
    Duration remainingTime = Duration.between(LocalDateTime.now(), endTime);

    // Sauvegarder le temps restant exact au moment de la pause
    session.setPausedRemainingTime(remainingTime.isNegative() ? Duration.ZERO : remainingTime);
    session.setPaused(true);
    session.setStatus("En pause");

    // NE PAS dissocier le poste - garder l'association pour la reprise
    // Le poste reste associé mais l'interface l'ignorera pour l'affichage "Disponible"
    Poste posteActuel = session.getPoste();

    // Mettre à jour la session (sans dissociation du poste)
    gameSessionRepository.updateGameSession(session);

    // Mettre à jour la réservation associée
    Reservation reservation = session.getReservation();
    if (reservation != null) {
        reservation.setStatus("En pause");
        reservationRepository.update(reservation);
    }

    // Logger pour débogage
    System.out.println("Session " + session.getId() + " mise en pause sur le poste " + 
                      (posteActuel != null ? posteActuel.getName() : "null") + 
                      ". Poste reste associé mais sera ignoré dans l'affichage.");
}

@Override
@Transactional
public void resumeGameSession(GameSession session) throws Exception {
    if (!"En pause".equalsIgnoreCase(session.getStatus())) {
        throw new Exception("Seules les sessions en pause peuvent être reprises.");
    }

    // Vérifier si le client a déjà une session active (autre que celle en cours de reprise)
    Client client = session.getClient();
    List<GameSession> clientActiveSessions = getAllGameSessions().stream()
        .filter(s -> s.getClient() != null && s.getClient().getId() == client.getId())
        .filter(s -> "Active".equalsIgnoreCase(s.getStatus()))
        .filter(s -> s.getId() != session.getId()) // Exclure la session actuelle (corrigé)
        .collect(Collectors.toList());

    if (!clientActiveSessions.isEmpty()) {
        throw new Exception("Ce client a déjà une session active sur le poste " + clientActiveSessions.get(0).getPoste().getName() + ". " +
                           "Veuillez terminer cette session avant de reprendre une autre.");
    }

    // IMPORTANT: La session en pause n'a plus de poste associé après la pause
    // On doit donc récupérer le poste depuis la réservation
    Poste poste = null;
    Reservation reservation = session.getReservation();
    if (reservation != null) {
        poste = reservation.getPoste();
    }
    if (poste == null) {
        throw new Exception("Aucun poste défini pour reprendre cette session. " +
                           "Utilisez resumePausedSessionForClient pour assigner un nouveau poste.");
    }

    // Vérifier que le poste est disponible (pas de session active)
    GameSession activeSessionOnPoste = getActiveSessionForPoste(poste);
    if (activeSessionOnPoste != null && "Active".equalsIgnoreCase(activeSessionOnPoste.getStatus())) {
        throw new Exception("Le poste " + poste.getName() + " est déjà occupé par une autre session active.");
    }

    // Vérifier qu'il n'y a pas de session en pause encore associée à ce poste
    List<GameSession> pausedSessionsOnPoste = gameSessionRepository.findPausedSessionsByPoste(poste);
    if (!pausedSessionsOnPoste.isEmpty()) {
        // Dissocier les sessions en pause de ce poste (nettoyage de sécurité)
        for (GameSession pausedSession : pausedSessionsOnPoste) {
            if (pausedSession.getId() != session.getId()) {  // Utilisez != au lieu de !equals()
                pausedSession.setPoste(null);
                gameSessionRepository.updateGameSession(pausedSession);
                System.out.println("Session en pause " + pausedSession.getId() + " dissociée du poste " + poste.getName());
            }
        }
    }

    // Vérifier que le temps restant est valide
    Duration remainingTime = session.getPausedRemainingTime();
    if (remainingTime == null || remainingTime.isNegative()) {
        throw new Exception("Le temps restant pour cette session est invalide.");
    }

    // Réassocier la session au poste original
    session.setPoste(poste);
    if (remainingTime.isZero()) {
        // Si le temps restant est zéro, terminer la session
        session.setStatus("Terminée");
        session.setEndTime(LocalDateTime.now());
        session.setPaused(false);
        session.setPausedRemainingTime(null);
    } else {
        // Reprendre la session avec le temps restant exact
        // Calculer le nouveau startTime pour que le temps restant soit respecté
        LocalDateTime newStartTime = LocalDateTime.now().minus(session.getPaidDuration().minus(remainingTime));
        session.setStartTime(newStartTime);
        session.setEndTime(LocalDateTime.now().plus(remainingTime));
        session.setPaused(false);
        session.setStatus("Active");
        session.setPausedRemainingTime(null); // Réinitialiser après reprise
    }

    // Mettre à jour la session
    gameSessionRepository.updateGameSession(session);

    // Mettre à jour la réservation associée
    if (reservation != null) {
        reservation.setStatus(session.getStatus());
        reservationRepository.update(reservation);
    }

    System.out.println("Session " + session.getId() + " reprise sur le poste " + poste.getName() +
                      " avec " + remainingTime.toMinutes() + " minutes restantes.");
}



@Override
@Transactional
public void resumePausedSessionForClient(int clientId, int posteId, int gameId, Duration remainingTime) throws Exception {
    
    // 1. Vérifier sessions ACTIVES uniquement
    if (hasActiveSession(clientId)) {
        throw new Exception("Client a déjà une session active. Terminez-la d'abord.");
    }

    // 2. Trouver la meilleure session en pause à reprendre
    GameSession sessionToResume = findBestPausedSession(clientId);
    
    // 3. Vérifier disponibilité poste
    Poste poste = posteRepository.findById(posteId);
    if (isPosteOccupied(poste)) {
        throw new Exception("Poste " + poste.getName() + " déjà occupé.");
    }

    Game game = gameRepository.findById(gameId);

    // 4. CORRECTION : Utiliser le paramètre remainingTime pour validation
    if (remainingTime == null || remainingTime.isNegative() || remainingTime.isZero()) {
        throw new Exception("Temps restant invalide pour la reprise.");
    }

    // 5. Fermer l'ancienne session
    closeOriginalPausedSession(sessionToResume);
    
    // 6. CORRECTION : Passer remainingTime à la méthode
    createNewActiveSession(sessionToResume, poste, game, remainingTime);
    
    System.out.println("✅ Session reprise sur poste " + poste.getName() + " avec " + remainingTime.toMinutes() + " minutes");
}

private void closeOriginalPausedSession(GameSession originalSession) {
    originalSession.setStatus("Terminée");
    originalSession.setPausedRemainingTime(Duration.ZERO);
    originalSession.setEndTime(LocalDateTime.now());
    originalSession.setPaused(false);
    gameSessionRepository.updateGameSession(originalSession);
    
    // CORRECTION : NE PAS fermer la réservation ici
    // Elle sera gérée dans createNewActiveSession si nécessaire
    System.out.println("🗑️ Ancienne session " + originalSession.getId() + " fermée");
}



private boolean hasActiveSession(int clientId) {
    return getAllGameSessions().stream()
        .anyMatch(s -> s.getClient() != null && 
                      s.getClient().getId() == clientId && 
                      "Active".equalsIgnoreCase(s.getStatus()));
}

// ✅ MÉTHODE 1: Trouver la meilleure session en pause
private GameSession findBestPausedSession(int clientId) throws Exception {
    List<GameSession> pausedSessions = gameSessionRepository.findPausedSessionsByClientIdWithRelations(clientId);
    
    if (pausedSessions.isEmpty()) {
        throw new Exception("Aucune session en pause trouvée pour ce client.");
    }

    return pausedSessions.stream()
        .filter(s -> s.getPausedRemainingTime() != null && 
                    !s.getPausedRemainingTime().isNegative() && 
                    !s.getPausedRemainingTime().isZero())
        .max(Comparator.comparing(GameSession::getPausedRemainingTime))
        .orElseThrow(() -> new Exception("Aucune session en pause avec du temps restant valide."));
}

// ✅ MÉTHODE 2: Vérifier si le poste est occupé
private boolean isPosteOccupied(Poste poste) {
    GameSession activeSession = getActiveSessionForPoste(poste);
    return activeSession != null && "Active".equalsIgnoreCase(activeSession.getStatus());
}

// ✅ CORRECTION : Ajouter le paramètre remainingTime
private void createNewActiveSession(GameSession oldSession, Poste newPoste, Game newGame, Duration remainingTime) throws Exception {
    
    // CORRECTION : Utiliser le paramètre remainingTime
    Duration actualRemainingTime = remainingTime;
    
    if (actualRemainingTime == null || actualRemainingTime.isNegative() || actualRemainingTime.isZero()) {
        throw new Exception("Temps restant invalide pour la reprise.");
    }

    // Créer nouvelle session
    GameSession newSession = new GameSession();
    newSession.setClient(oldSession.getClient());
    newSession.setPoste(newPoste);
    newSession.setGame(newGame);
    newSession.setStartTime(LocalDateTime.now());
    newSession.setPaidDuration(actualRemainingTime);
    newSession.setStatus("Active");
    
    // CORRECTION : Décider si on garde l'ancienne réservation ou pas
    // Option A: Garder l'ancienne réservation (recommandé pour la traçabilité)
    newSession.setReservation(oldSession.getReservation());

    // Sauvegarder
    gameSessionRepository.addGameSession(newSession);

    // CORRECTION : Mettre à jour la réservation UNIQUEMENT si elle existe
    if (oldSession.getReservation() != null) {
        updateReservationForNewSession(oldSession.getReservation(), newPoste, newGame, actualRemainingTime);
    }

    System.out.println("🆕 Nouvelle session " + newSession.getId() + " créée avec " + actualRemainingTime.toMinutes() + " minutes");
}

// ✅ MÉTHODE 4: Mettre à jour la réservation
private void updateReservationForNewSession(Reservation reservation, Poste newPoste, Game newGame, Duration remainingTime) {
    if (reservation != null) {
        reservation.setPoste(newPoste);
        reservation.setGame(newGame);
        reservation.setStatus("Active");
        reservation.setDuration(remainingTime);
        reservationRepository.update(reservation);
        System.out.println("📋 Réservation " + reservation.getNumeroTicket() + " mise à jour");
    }
}

// ✅ MÉTHODE 5: Nettoyer toutes les sessions en pause
private void closeAllPausedSessions(int clientId, int sessionToKeepId) {
    List<GameSession> allPausedSessions = gameSessionRepository.findPausedSessionsByClientIdWithRelations(clientId);
    
    allPausedSessions.forEach(session -> {
        if (session.getId() != sessionToKeepId) {
            // Fermer les autres sessions en pause
            session.setStatus("Terminée");
            session.setPausedRemainingTime(Duration.ZERO);
            session.setEndTime(LocalDateTime.now());
            gameSessionRepository.updateGameSession(session);
            
            // Fermer leurs réservations associées
            closeReservationIfNeeded(session.getReservation());
        }
    });
}

// ✅ MÉTHODE 6: Fermer une réservation si nécessaire
private void closeReservationIfNeeded(Reservation reservation) {
    if (reservation != null && !"Terminée".equals(reservation.getStatus())) {
        reservation.setStatus("Terminée");
        reservationRepository.update(reservation);
    }
}



    @Override
    public boolean superAdminExists() {
        return userRepository.existsByRole(Role.SuperAdmin);
    }


    @Override
    @Transactional
    public void terminateSessionAndReservation(GameSession session) throws Exception {
        session.setStatus("Terminée");
        session.setEndTime(LocalDateTime.now());
        gameSessionRepository.updateGameSession(session);

        // Recharger la session avec ses relations pour éviter les problèmes de LAZY loading
        GameSession managedSession = gameSessionRepository.findGameSessionByIdWithRelations(session.getId());
        Reservation reservation = managedSession.getReservation();

        if (reservation != null) {
            reservation.setStatus("Terminée");
            // Forcer le merge pour s'assurer que l'entité est attachée
            reservationRepository.update(reservation);
        }
    }


 @Override
@Transactional
public void extendGameSession(GameSession session, int additionalMinutes, String connectedUserName, String modePaiement) throws Exception {
    // Validation des paramètres
    Objects.requireNonNull(session, "La session ne peut pas être null.");
    if (additionalMinutes < 15) {
        throw new IllegalArgumentException("La durée minimale de prolongation est de 15 minutes.");
    }
    if (modePaiement == null || modePaiement.trim().isEmpty()) {
        throw new IllegalArgumentException("Le mode de paiement ne peut pas être vide.");
    }

    // 1. Charge la session avec toutes ses relations
    GameSession managedSession = gameSessionRepository.findGameSessionByIdWithRelations(session.getId());
    if (managedSession == null) {
        throw new Exception("Session introuvable avec l'ID : " + session.getId());
    }

    // 2. Vérifie que la réservation est chargée
    Reservation reservation = managedSession.getReservation();
    if (reservation == null) {
        throw new Exception("Aucune réservation associée à cette session.");
    }

    // 3. Vérifie que le jeu est chargé
    Game game = reservation.getGame();
    if (game == null) {
        throw new Exception("Aucun jeu associé à cette réservation.");
    }

    // 4. Vérifie que le client est chargé
    Client client = reservation.getClient();
    if (client == null) {
        throw new Exception("Aucun client associé à cette réservation.");
    }

    // 5. Calcule le prix supplémentaire
    double additionalPrice = calculateExtensionPrice(additionalMinutes, reservation);

    // 6. Stocke le prix original pour le ticket
    double originalAmount = reservation.getTotalPrice();

    // 7. Met à jour la durée de la session et de la réservation
    Duration extraDuration = Duration.ofMinutes(additionalMinutes);
    managedSession.addExtraTime(extraDuration);

    // 8. Met à jour le prix total de la réservation en ajoutant uniquement le prix supplémentaire
    reservation.setDuration(reservation.getDuration().plus(extraDuration));
    reservation.setTotalPrice(originalAmount + additionalPrice);

    // 9. Met à jour les points de fidélité
    updateLoyaltyPoints(reservation, additionalMinutes);

    // 10. Met à jour les points de parrainage
    updateParrainPoints(reservation, additionalMinutes);

    // 11. Récupère l'utilisateur connecté
    User currentUser = getCurrentUser();
    if (currentUser == null) {
        throw new Exception("Aucun utilisateur connecté trouvé.");
    }

    // 12. Sauvegarde les modifications
    gameSessionRepository.updateGameSession(managedSession);
    reservationRepository.update(reservation);

    // 13. Crée un paiement pour la prolongation
    String detailReservations = String.format(
        "Prolongation de %d minutes pour la réservation %s (Poste %d) - Jeu: %s",
        additionalMinutes,
        reservation.getNumeroTicket(),
        reservation.getPoste().getId(),
        game.getName()
    );

    Payment payment = new Payment(
        "EXT-" + reservation.getNumeroTicket(),
        new Date(),
        additionalPrice,
        modePaiement, // Utilise le mode de paiement sélectionné
        client,
        "",
        detailReservations,
        currentUser
    );

    paymentRepository.addPayment(payment);

    // 14. Imprime le ticket avec le mode de paiement
    ReservationReceiptPrinter printer = new ReservationReceiptPrinter(
        reservation,
        connectedUserName,
        true, // Indique que c'est une prolongation
        additionalMinutes,
        originalAmount,
        modePaiement // Utilise le mode de paiement sélectionné
    );
    printer.printReceipt();
}



private double calculateExtensionPrice(int additionalMinutes, Reservation reservation) {
    double additionalPrice = 0.0;
    int remainingMinutes = additionalMinutes;

    if (remainingMinutes <= 15) {
        additionalPrice += 300.0;
    } else if (remainingMinutes <= 30) {
        additionalPrice += 500.0;
    } else {
        additionalPrice += 500.0;
        remainingMinutes -= 30;
        int numberOf15MinBlocks = (int) Math.ceil((double) remainingMinutes / 15);
        additionalPrice += numberOf15MinBlocks * 250.0;
    }

    // Appliquer la promotion si elle est valide
    if (reservation.getAppliedPromotion() != null && reservation.getAppliedPromotion().isValid(LocalDate.now())) {
        additionalPrice *= (1 - reservation.getAppliedPromotion().getTauxReduction());
    }

    return additionalPrice;
}

private void updateLoyaltyPoints(Reservation reservation, int additionalMinutes) {
    if (reservation.getClient() != null) {
        Client client = reservation.getClient();
        int pointsEarned = additionalMinutes / 15;
        client.setLoyaltyPoints(client.getLoyaltyPoints() + pointsEarned);
        try {
            clientRepository.update(client);
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de la mise à jour des points de fidélité: " + e.getMessage(), e);
        }
    }
}

private void updateParrainPoints(Reservation reservation, int additionalMinutes) {
    if (reservation.getCodeParrainage() != null && !reservation.getCodeParrainage().isEmpty()) {
        String parrainCode = reservation.getCodeParrainage();
        Parrain parrain = parrainRepository.findByCodeParrainage(parrainCode);
        if (parrain != null) {
            int pointsEarned = additionalMinutes / 15;
            parrain.addParrainagePoints(pointsEarned);
            try {
                parrainRepository.update(parrain);
            } catch (Exception e) {
                throw new RuntimeException("Erreur lors de la mise à jour des points de parrainage: " + e.getMessage(), e);
            }
        }
    }
}







    @Override
    public List<Payment> getPaymentsByUser(User currentUser) {
         if (currentUser == null) return Collections.emptyList();
         return paymentRepository.getAllPayments().stream()
        .filter(r -> {
            User createdBy = r.getCreatedBy();
            return createdBy != null && createdBy.getId() == currentUser.getId(); // Compare par ID
        })
        .collect(Collectors.toList());
    }
}
