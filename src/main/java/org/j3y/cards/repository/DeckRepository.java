package org.j3y.cards.repository;

import org.j3y.cards.model.gameplay.CardDeck;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DeckRepository extends CrudRepository<CardDeck, String> {
}
