package com.services;

import com.entities.*; // Assurez-vous d'importer toutes les entités nécessaires
import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set; 

public interface IService {

    // GameSession methods
    List<GameSession> getAllGameSessions();
    GameSession addGameSession(GameSession gameSession) throws Exception;
    GameSession getGameSessionById(int id);
    boolean updateGameSession(GameSession gameSession) throws Exception;
    boolean deleteGameSession(int id) throws Exception;
    GameSession getActiveSessionForPoste(Poste poste);
    boolean reduceRemainingTime(int gameSessionId, Duration timeElapsed) throws Exception;
    List<GameSession> findGameSessionsByClientId(int clientId);
    boolean existsGameSessionById(int id); // Ajouté pour cohérence avec le dépôt JPA
    void terminateSessionAndReservation(GameSession session) throws Exception;
    void extendGameSession(GameSession session, int additionalMinutes, String connectedUserName) throws Exception;
    void resumeGameSession(GameSession session) throws Exception;
    void pauseGameSession(GameSession session) throws Exception;

    // Produit methods
    Produit insertProduit(Produit produit) throws Exception;
    List<Produit> findAllProduits();
    Produit findProduitById(int id);
    void updateProduit(Produit produit) throws Exception;
    void deleteProduit(Produit produit) throws Exception;
    List<Produit> findProduitsEnStock();
    boolean checkIfProduitExists(String nom);
    void updateProduitStocks(Map<Produit, Integer> produitsEtQuantites) throws Exception;
    List<Produit> getProduitsByPromotionId(int promotionId); // Nécessitera une méthode dans IProduitRepository ou IPromoRepository
    boolean existsProduitById(int id); // Ajouté pour cohérence avec le dépôt JPA
    
    // Reservation methods
    Reservation insertReservation(Reservation reservation) throws Exception;
    List<Reservation> findAllReservations();
    Reservation findReservationById(int id);
    boolean updateReservation(Reservation reservation) throws Exception;
    boolean deleteReservation(int id) throws Exception;
    List<Reservation> findReservationsByClientId(int clientId);
    List<Reservation> findReservationsByUser(User user);
    boolean existsByTicketNumber(String numeroTicket);
    boolean existsReservationById(int id); // Ajouté pour cohérence avec le dépôt JPA
    List<Reservation> findReservationsByClientIdAndDateRange(int clientId, LocalDateTime startDate, LocalDateTime endDate);
    double calculateReservationPrice(Reservation reservation) throws Exception;
    Reservation saveReservation(Reservation reservation, Optional<Promotion> promotion) throws Exception;

    // User methods
    User seConnecter(String login, String password);
    List<User> findAllUsers();
    void addUser(User newUser) throws Exception;
    void deleteUser(User user) throws Exception; // Garde l'objet User pour la commodité de la couche de service
    void updateUser(User user) throws Exception;
    User getCurrentUser();
    void setCurrentUser(User user);
    boolean existsUserById(int id); // Ajouté pour cohérence avec le dépôt JPA
    boolean existsUserByName(String name); // Ajouté pour cohérence avec le dépôt JPA

    // Payment methods
    List<Payment> getAllPayments();
    void addPayment(Payment payment) throws Exception;
    Optional<Payment> getPaymentById(int id);
    void updatePayment(Payment payment) throws Exception;
    void deletePayment(int id) throws Exception;
    boolean existsPaymentById(int id); 
    List<Payment> getPaymentsByUser(User currentUser);

    // Client methods
    List<Client> getAllClients();
    Client addClient(Client client) throws Exception; // Type de retour changé pour Client
    void updateClient(Client client) throws Exception;
    void deleteClient(Client client) throws Exception;
    Client findByEmail(String email);
    Client findByTel(String tel);
    boolean existsClientById(int id); // Ajouté pour cohérence avec le dépôt JPA
    boolean existsClientByName(String name); // Ajouté pour cohérence avec le dépôt JPA

    // Game methods
    List<Game> getAllGames();
    void deleteGame(Game game) throws Exception; // Garde l'objet Game pour la commodité de la couche de service
    void updateGame(Game game) throws Exception;
    void addGame(Game game) throws Exception;
    boolean existsGameByName(String name); // Ajouté pour cohérence avec le dépôt JPA
    boolean existsGameById(int id); // Ajouté pour cohérence avec le dépôt JPA

    // Poste methods
    List<Poste> getPostes();
    List<Poste> getPostesSansJeux();
    void deletePoste(Poste poste) throws Exception;
    void updatePoste(Poste poste) throws Exception;
    Poste addPoste(Poste poste) throws Exception;
    List<Poste> getPostesForGame(Game game);
    void addPosteToGame(Poste poste, Game game) throws Exception;
    void removePosteFromGame(Poste poste, Game game) throws Exception;
    boolean existsPosteById(int id); // Ajouté pour cohérence avec le dépôt JPA

    // Parrain methods
    Parrain addParrain(Parrain parrain) throws Exception; // Type de retour changé pour Parrain
    List<Parrain> getAllParrains();
    Parrain getParrainById(int id);
    void updateParrain(Parrain parrain) throws Exception;
    void deleteParrain(Parrain parrain) throws Exception;
    Parrain getParrainByCodeParrainage(String codeParrainage);
    boolean existsParrainByName(String name); // Ajouté pour cohérence avec le dépôt JPA
    boolean existsParrainById(int id); // Ajouté pour cohérence (vient de IUserRepository via l'héritage)

    // Promotion methods
    Promotion addPromotion(Promotion promo) throws Exception;
    void updatePromotion(Promotion promo) throws Exception;
    void deletePromotion(Promotion promo) throws Exception;
    List<Promotion> getAllPromotions();
    Promotion getPromotionById(int id);
    void appliquerPromotion(Promotion promo) throws Exception;
    void retirerPromotion(Promotion promo) throws Exception;
    void addProduitToPromotion(int produitId, int promotionId) throws Exception; // Nécessitera une méthode dans IPromoRepository
    void removeProduitFromPromotion(int produitId, int promotionId) throws Exception; // Nécessitera une méthode dans IPromoRepository
    boolean existsProduitInPromotion(int produitId, int promotionId); // Nécessitera une méthode dans IPromoRepository
    void removeAllProduitsFromPromotion(int promotionId) throws Exception; // Nécessitera une méthode dans IPromoRepository
    void updatePrixEtAncienPrix(int produitId, BigDecimal ancienPrix, BigDecimal nouveauPrix) throws Exception;
    void restaurerPrixInitial(int produitId, BigDecimal ancienPrixPassed);
    List<Promotion> getValidPromotionsForDate(LocalDate date); // Nécessitera une méthode dans IPromoRepository
    Optional<Promotion> getPromotionByNom(String nom);
    List<Promotion> getActiveAndValidPromotions(LocalDate date); // Peut être une combinaison de méthodes de dépôt
    void restoreProductPricesAfterAllPromotionsRemoved(int produitId) throws Exception;
    boolean existsPromotionById(int id); // Ajouté pour cohérence avec le dépôt JPA
    boolean existsPromotionByName(String name); // Ajouté pour cohérence (si la promotion a un champ 'name')

    /**
     * Calcule et retourne la durée totale de jeu pour un client donné,
     * basée sur les durées de ses réservations.
     * @param clientId L'ID du client.
     * @return La durée totale de jeu du client.
     */
    Duration getTotalPlayTime(int clientId); // CONSERVER CETTE MÉTHODE
    Optional<Promotion> getActivePromotionForToday();
    Promotion getPromotionByIdWithProduits(int id);
    
}
