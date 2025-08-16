package com.repositories;

import com.entities.Reservation;
import com.entities.User;

import java.time.LocalDateTime;
import java.util.List;

public interface IReservationRepository {
    
    // Ajouter une réservation
    Reservation insert(Reservation reservation);
    
    // Récupérer toutes les réservations
    List<Reservation> findAll();
    
    // Récupérer une réservation par son ID
    Reservation findById(int id);

     // Nouvelle méthode : Récupérer les réservations d'un client dans une plage de dates
    List<Reservation> findByClientIdAndDateRange(int clientId, LocalDateTime startDate, LocalDateTime endDate);
    
    // Mettre à jour une réservation
    boolean update(Reservation reservation);
    
    // Supprimer une réservation
    boolean delete(int id);

    // Récupérer les réservations d'un client par son ID
    List<Reservation> findByClientId(int clientId);

    // Vérifier si une réservation existe avec un numéro de ticket donné
    boolean existsByTicketNumber(String numeroTicket);

    boolean existsById(int id);

    List<Reservation> findReservationsByUser(User user);
}
