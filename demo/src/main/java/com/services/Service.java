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
import com.repositories.IClientRepository;
import com.repositories.IGameRepository;
import com.repositories.IGameSessionRepository;
import com.repositories.IParrainRepository;
import com.repositories.IPaymentRepository;
import com.repositories.IPosteGameRepository;
import com.repositories.IPosteRepository;
import com.repositories.IProduitRepository;
import com.repositories.IReservationRepository;
import com.repositories.IUserRepository;

public class Service implements IService {

    //injection de dépendance
    IGameSessionRepository gameSessionRepo;
    IGameRepository gameRepo;
    IClientRepository clientRepo;
    IPaymentRepository paymentRepo;
    IPosteRepository posteRepo;
    IProduitRepository produitRepo;
    IReservationRepository reservationRepo;
    IUserRepository userRepo;
    IParrainRepository parrainRepo;
    IPosteGameRepository posteGameRepo;

    public Service(IGameSessionRepository gameSessionRepo, IGameRepository gameRepo, IClientRepository clientRepo,
                   IPaymentRepository paymentRepo, IPosteRepository posteRepo, IProduitRepository produitRepo,
                   IReservationRepository reservationRepo, IUserRepository userRepo, IParrainRepository parrainRepo, IPosteGameRepository posteGameRepo) {
        this.gameSessionRepo = gameSessionRepo;
        this.gameRepo = gameRepo;
        this.clientRepo = clientRepo;
        this.paymentRepo = paymentRepo;
        this.posteRepo = posteRepo;
        this.produitRepo = produitRepo;
        this.reservationRepo = reservationRepo;
        this.userRepo = userRepo;
        this.parrainRepo = parrainRepo;
        this.posteGameRepo = posteGameRepo;
    }

    // GameSession methods
    @Override
    public List<GameSession> getAllGameSessions() {
        return gameSessionRepo.getAllGameSessions();
    }

    @Override
    public GameSession addGameSession(GameSession gameSession) throws Exception {
        return gameSessionRepo.addGameSession(gameSession);
    }

    @Override
    public GameSession getGameSessionById(int id) {
        return gameSessionRepo.getGameSessionById(id);
    }

    @Override
    public boolean updateGameSession(GameSession gameSession) {
        if (gameSessionRepo.existsById(gameSession.getId())) {
            gameSessionRepo.updateGameSession(gameSession);
            return true;
        }
        return false;
    }

    @Override
    public boolean deleteGameSession(int id) {
        if (gameSessionRepo.existsById(id)) {
            gameSessionRepo.deleteGameSession(id);
            return true;
        }
        return false;
    }

    @Override
    public boolean reduceRemainingTime(int gameSessionId, Duration timeElapsed) throws Exception {
        GameSession gameSession = getGameSessionById(gameSessionId);
        if (gameSession != null) {
            gameSession.setRemainingTime(gameSession.getRemainingTime().minus(timeElapsed));
            gameSessionRepo.updateGameSession(gameSession);
            return true;
        }
        return false;
    }

    // Produit methods
    @Override
    public Produit insertProduit(Produit produit) throws Exception {
        return produitRepo.insert(produit);
    }

    @Override
    public List<Produit> findAllProduits() {
        return produitRepo.findAll();
    }

    @Override
    public Produit findProduitById(int id) {
        return produitRepo.findById(id);
    }

    @Override
    public void updateProduit(Produit produit) throws Exception {
        if (produitRepo.checkIfProduitExists(produit.getNom())) {
            produitRepo.update(produit);
        } else {
            throw new Exception("Produit non trouvé");
        }
    }

    @Override
    public void deleteProduit(Produit produit) throws Exception {
        if (produitRepo.checkIfProduitExists(produit.getNom())) {
            produitRepo.delete(produit);
        } else {
            throw new Exception("Produit non trouvé");
        }
    }

    @Override
    public List<Produit> findProduitsEnStock() {
        return produitRepo.findProduitsEnStock();
    }

    @Override
    public boolean checkIfProduitExists(String nom) {
        return produitRepo.checkIfProduitExists(nom);
    }

    // Reservation methods
    @Override
    public Reservation insertReservation(Reservation reservation) throws Exception {
        return reservationRepo.insert(reservation);
    }

    @Override
    public List<Reservation> findAllReservations() {
        return reservationRepo.findAll();
    }

    @Override
    public Reservation findReservationById(int id) {
        return reservationRepo.findById(id);
    }

    @Override
    public boolean updateReservation(Reservation reservation) throws Exception {
        if (reservationRepo.existsByTicketNumber(reservation.getNumeroTicket())) {
            reservationRepo.update(reservation);
            return true;
        }
        return false;
    }

    @Override
    public boolean deleteReservation(int id) throws Exception {
        if (reservationRepo.existsById(id)) {
            reservationRepo.delete(id);
            return true;
        }
        return false;
    }

    @Override
    public List<Reservation> findReservationsByClientId(int clientId) {
        return reservationRepo.findByClientId(clientId);
    }

    @Override
    public boolean existsByTicketNumber(String numeroTicket) {
        return reservationRepo.existsByTicketNumber(numeroTicket);
    }

    // User methods
    @Override
    public User seConnecter(String login, String password) {
        return userRepo.findUserByLoginAndPassword(login, password);
    }

    @Override
    public List<User> findAllUsers() {
        return userRepo.findAll();
    }

    // Payment methods
    @Override
    public List<Payment> getAllPayments() {
        return paymentRepo.getAllPayments();
    }

    @Override
    public void addPayment(Payment payment) throws Exception {
        paymentRepo.addPayment(payment);
    }

    @Override
    public Optional<Payment> getPaymentById(int id) {
        return paymentRepo.getPaymentById(id);
    }

    @Override
    public void updatePayment(Payment payment) throws Exception {
        if (paymentRepo.existsById(payment.getId())) {
            paymentRepo.updatePayment(payment);
        } else {
            throw new Exception("Payment not found");
        }
    }

    @Override
    public void deletePayment(int id) throws Exception {
        if (paymentRepo.existsById(id)) {
            paymentRepo.deletePayment(id);
        } else {
            throw new Exception("Payment not found");
        }
    }

    @Override
    public void addUser(User newUser) throws Exception {
        userRepo.addUser(newUser);
    }

    @Override
    public List<Client> getAllClients() {
        return clientRepo.findAll();
    }

    @Override
    public List<Game> getAllGames() {
        return gameRepo.findAll();
    }

    @Override
    public List<Poste> getPostes() {
        return posteRepo.findAll();
    }

    @Override
    public void deleteGame(Game game) throws Exception {
        if (gameRepo.existsByName(game.getName())) {
            gameRepo.delete(game.getId());
        } else {
            throw new Exception("Jeu non trouvé");
        }
    }

    @Override
    public void updateGame(Game game) throws Exception {
        if (gameRepo.existsByName(game.getName())) {
            gameRepo.update(game);
        } else {
            throw new Exception("Jeu not found");
        }
    }

    @Override
    public void addGame(Game game) throws Exception {
        gameRepo.insert(game);
    }

    @Override
    public void deletePoste(Poste poste) throws Exception {
        // Vérifier si le poste existe dans la base de données
        Poste existingPoste = posteRepo.findById(poste.getId());
        
        if (existingPoste != null) {
            // Si le poste existe, on le supprime
            posteRepo.delete(existingPoste);
        } else {
            // Si le poste n'est pas trouvé, on lance une exception
            throw new Exception("Poste non trouvé");
        }
    }

    @Override
    public void updatePoste(Poste poste) throws Exception {
        Poste existingPoste = posteRepo.findById(poste.getId());
        
        if (existingPoste != null) {
            // Si le poste existe, on le supprime
            posteRepo.update(existingPoste);
        } else {
            // Si le poste n'est pas trouvé, on lance une exception
            throw new Exception("Poste non trouvé");
        }
    }

    @Override
    public void addPoste(Poste poste) throws Exception {
        posteRepo.insert(poste);
    }

    @Override
    public void deleteUser(User user) throws Exception {
        if (userRepo.existsByName(user.getName())) {
            userRepo.delete(user.getId());
        } else {
            throw new Exception("Utilisateur non trouvé");
        }
    }

    @Override
    public void updateUser(User user) throws Exception {
        userRepo.updateUser(user);
    }

    @Override
    public void addClient(Client client) throws Exception {
       clientRepo.insert(client);
    }

    @Override
    public void updateClient(Client client) throws Exception {
        clientRepo.update(client);
    }

    @Override
    public void deleteClient(Client client) throws Exception {
        if (clientRepo.existsByName(client.getName())) {
            clientRepo.delete(client);
        } else {
            throw new Exception("Utilisateur non trouvé");
        }
    }

    @Override
    public List<Poste> getPostesForGame(Game game) {
        return posteRepo.findByGame(game);
    }

    @Override
    public void addParrain(Parrain parrain) throws Exception {
       parrainRepo.insert(parrain);
    }

    @Override
    public List<Parrain> getAllParrains() {
        return parrainRepo.findAll();
    }

    @Override
    public Parrain getParrainById(int id) {
       return parrainRepo.findById(id);
    }

    @Override
    public void updateParrain(Parrain parrain) throws Exception {
        parrainRepo.update(parrain);
    }

    @Override
    public void deleteParrain(Parrain parrain) throws Exception {
        if (parrainRepo.existsByName(parrain.getName())) {
            parrainRepo.delete(parrain);
        } else {
            throw new Exception("Parrain non trouvé");
        }
    }

    @Override
    public void addPosteToGame(Poste poste, Game game) throws Exception {
        if (!posteGameRepo.exists(game.getId(), poste.getId())) {
            posteGameRepo.addPosteToGame(game.getId(), poste.getId());
        } else {
            throw new Exception("Ce poste est déjà associé à ce jeu.");
        }
    }

    @Override
    public void removePosteFromGame(Poste poste, Game game) throws Exception {
        if (posteGameRepo.exists(game.getId(), poste.getId())) {
            posteGameRepo.removePosteFromGame(game.getId(), poste.getId());
        } else {
            throw new Exception("Ce poste n'est pas associé à ce jeu.");
        }
    }

    @Override
    public Client findByEmail(String email) {
        return clientRepo.findByEmail(email);
    }

    @Override
    public Client findByTel(String tel) {
       return clientRepo.findByTel(tel);
    }
}
