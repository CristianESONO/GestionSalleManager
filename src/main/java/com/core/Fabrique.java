package com.core;

import com.repositories.IClientRepository;
import com.repositories.IGameRepository;
import com.repositories.IGameSessionRepository;
import com.repositories.IParrainRepository;
import com.repositories.IPaymentRepository;
import com.repositories.IPosteRepository;
import com.repositories.IProduitRepository;
import com.repositories.IPromoRepository;
import com.repositories.IReservationRepository;
import com.repositories.IUserRepository;
import com.repositories.bd.ClientRepositoryJpa;
import com.repositories.bd.GameRepositoryJpa;
import com.repositories.bd.GameSessionRepositoryJpa;
import com.repositories.bd.ParrainRepositoryJpa;
import com.repositories.bd.PaymentRepositoryJpa;
import com.repositories.bd.PosteRepositoryJpa;
import com.repositories.bd.ProduitRepositoryJpa;
import com.repositories.bd.PromotionRepositoryJpa;
import com.repositories.bd.ReservationRepositoryJpa;
import com.repositories.bd.UserRepositoryJpa;
import com.services.IService;
import com.services.Service;

public class Fabrique {
    private static IService serviceInstance;

    public static IService getService() {
        if (serviceInstance == null) {
            IClientRepository clientRepository = new ClientRepositoryJpa();
            IGameRepository gameRepository = new GameRepositoryJpa();
            IGameSessionRepository gameSessionRepository = new GameSessionRepositoryJpa();
            IPaymentRepository paymentRepository = new PaymentRepositoryJpa();
            IPosteRepository posteRepository = new PosteRepositoryJpa();
            IProduitRepository produitRepository = new ProduitRepositoryJpa();
            IReservationRepository reservationRepository = new ReservationRepositoryJpa();
            IUserRepository userRepository = new UserRepositoryJpa();
            IParrainRepository parrainRepository = new ParrainRepositoryJpa();
            IPromoRepository promoRepository = new PromotionRepositoryJpa();

            serviceInstance = new Service(gameSessionRepository,produitRepository,reservationRepository, userRepository,
                    paymentRepository,clientRepository,gameRepository, posteRepository, parrainRepository,promoRepository);
        }
        return serviceInstance;
    }
}

