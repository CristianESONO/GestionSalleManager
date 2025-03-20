package com.core;

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
import com.repositories.bd.ClientRepository;
import com.repositories.bd.GameRepository;
import com.repositories.bd.GameSessionRepository;
import com.repositories.bd.ParrainRepository;
import com.repositories.bd.PaymentRepository;
import com.repositories.bd.PosteGameRepository;
import com.repositories.bd.PosteRepository;
import com.repositories.bd.ProduitRepository;
import com.repositories.bd.ReservationRepository;
import com.repositories.bd.UserRepository;
import com.services.IService;
import com.services.Service;

public class Fabrique {

    public static IService getService(){
        IClientRepository clientRepository = new ClientRepository();
        IGameRepository gameRepository = new GameRepository();
        IGameSessionRepository gameSessionRepository = new GameSessionRepository();
        IPaymentRepository paymentRepository = new PaymentRepository();
        IPosteRepository posteRepository = new PosteRepository();
        IProduitRepository produitRepository = new ProduitRepository();
        IReservationRepository reservationRepository = new ReservationRepository();
        IUserRepository userRepository = new UserRepository();
        IParrainRepository parrainRepository = new ParrainRepository();
        IPosteGameRepository posteGameRepository = new PosteGameRepository();
        return new Service(gameSessionRepository, gameRepository, clientRepository, 
                    paymentRepository, posteRepository, produitRepository, reservationRepository,
                     userRepository, parrainRepository, posteGameRepository);
    }
    
}
