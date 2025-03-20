package com.services;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

import com.entities.Client;
import com.entities.Game;
import com.entities.GameSession;
import com.entities.Parrain;
import com.entities.Payment;
import com.entities.Poste;
import com.entities.Produit;
import com.entities.Reservation;
import com.entities.User;

public interface IService {
    //Game
    List<Game> getAllGames();
    void deleteGame(Game game) throws Exception;
    void updateGame(Game game) throws Exception;
    void addGame(Game game)throws Exception;

    //Poste
    List<Poste> getPostes();
    void addPoste(Poste poste)throws Exception;
    void deletePoste(Poste poste) throws Exception;
    void updatePoste(Poste poste) throws Exception;
    List<Poste> getPostesForGame(Game game); // Récupérer les postes pour un jeu

    // GameSession
    List<GameSession> getAllGameSessions();
    GameSession addGameSession(GameSession gameSession) throws Exception;
    GameSession getGameSessionById(int id);
    boolean updateGameSession(GameSession gameSession);
    boolean deleteGameSession(int id);
    boolean reduceRemainingTime(int gameSessionId, Duration timeElapsed) throws Exception;

    // Produit
    Produit insertProduit(Produit produit) throws Exception;
    List<Produit> findAllProduits();
    Produit findProduitById(int id);
    void updateProduit(Produit produit) throws Exception;
    void deleteProduit(Produit produit) throws Exception;
    List<Produit> findProduitsEnStock();
    boolean checkIfProduitExists(String nom);

    // Reservation
    Reservation insertReservation(Reservation reservation) throws Exception;
    List<Reservation> findAllReservations();
    Reservation findReservationById(int id);
    boolean updateReservation(Reservation reservation) throws Exception;
    boolean deleteReservation(int id) throws Exception;
    List<Reservation> findReservationsByClientId(int clientId);
    boolean existsByTicketNumber(String numeroTicket);

    // User
    User seConnecter(String login, String password);
    List<User> findAllUsers();
    List<Client> getAllClients();
    Client findByEmail(String email);
    Client findByTel(String tel);
    void addUser(User newUser)throws Exception;
    void addClient(Client client) throws Exception;
    void updateClient(Client client) throws Exception;
    void deleteClient(Client client) throws Exception;
    void updateUser(User user) throws Exception;
    void deleteUser(User user) throws Exception;

    //PosteGame
    void addPosteToGame(Poste poste, Game game) throws Exception;
    void removePosteFromGame(Poste poste, Game game) throws Exception;


    //Parraiange
    void addParrain(Parrain parrain) throws Exception;
    List<Parrain> getAllParrains();
    Parrain getParrainById(int id);
    void updateParrain(Parrain parrain) throws Exception;
    void deleteParrain(Parrain parrain) throws Exception;

    // Payment
    List<Payment> getAllPayments();
    void addPayment(Payment payment) throws Exception;
    Optional<Payment> getPaymentById(int id);
    void updatePayment(Payment payment) throws Exception;
    void deletePayment(int id) throws Exception;
    
}
