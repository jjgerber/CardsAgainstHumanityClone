package org.j3y.cards.repository;

import org.j3y.cards.model.gameplay.Card;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CardRepository extends CrudRepository<Card, String> {
}
