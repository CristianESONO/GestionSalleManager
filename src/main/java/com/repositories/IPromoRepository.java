package com.repositories;

import com.entities.Promotion;
import com.entities.TypePromotion;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import javax.transaction.Transactional;

public interface IPromoRepository {
    Promotion insert(Promotion promo);
    void update(Promotion promo);
    void delete(Promotion promo);
    Promotion findById(int id);
    List<Promotion> findAll();
    List<Promotion> findValidPromotionsByTypeAndDate(TypePromotion type, LocalDate date);
    
        /**
     * Recherche une promotion par son nom.
     * @param nom Le nom de la promotion à rechercher.
     * @return Un Optional contenant la promotion si trouvée, ou un Optional vide sinon.
     */
    Optional<Promotion> findByNom(String nom);
       
    @Transactional
    Promotion findByIdWithProduits(int id);

    @Transactional
    List<Promotion> findAllWithProduits();
    boolean existsById(int id);

}
