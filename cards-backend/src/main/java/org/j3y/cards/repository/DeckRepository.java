package org.j3y.cards.repository;

import org.j3y.cards.model.gameplay.CardDeck;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DeckRepository extends JpaRepository<CardDeck, String> {

}
