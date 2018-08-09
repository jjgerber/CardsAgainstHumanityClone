package org.j3y.cards.repository;

import org.j3y.cards.model.gameplay.Phrase;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PhraseRepository extends CrudRepository<Phrase, String> {
}
