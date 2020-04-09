package org.j3y.cards;

import org.j3y.cards.model.gameplay.Card;
import org.j3y.cards.model.gameplay.CardDeck;
import org.j3y.cards.model.gameplay.Phrase;
import org.j3y.cards.repository.CardRepository;
import org.j3y.cards.repository.DeckRepository;
import org.j3y.cards.repository.PhraseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class CardsStartup implements ApplicationListener<ApplicationReadyEvent> {

    private CardRepository cardRepository;
    private DeckRepository deckRepository;
    private PhraseRepository phraseRepository;

    @Autowired
    public CardsStartup(CardRepository cardRepository,
                        DeckRepository deckRepository,
                        PhraseRepository phraseRepository) {
        this.cardRepository = cardRepository;
        this.deckRepository = deckRepository;
        this.phraseRepository = phraseRepository;
    }

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        createDeck("Main Deck");
        createDeck("Bonus Deck");
        createDeck("2020 Deck");
        createDeck("Coronavirus Deck");
        createDeck("Politics Deck");
        createDeck("Tiger King Deck");
        createDeck("Game of Thrones Deck");
    }

    private void createDeck(String deckName) {
        CardDeck deck = new CardDeck();
        deck.setUuid(UUID.randomUUID().toString());
        deck.setDeckName(deckName);
        deck = deckRepository.save(deck);

        Card card = new Card();
        card.setOwningDeck(deck);
        card.setText("When you have _ you also have _.");
        cardRepository.save(card);

        card = new Card();
        card.setOwningDeck(deck);
        card.setText("I really like _ with some _.");
        cardRepository.save(card);

        card = new Card();
        card.setOwningDeck(deck);
        card.setText("What's better than ice cream?");
        cardRepository.save(card);

        card = new Card();
        card.setOwningDeck(deck);
        card.setText("Why have fun when you can have _?");
        cardRepository.save(card);

        card = new Card();
        card.setOwningDeck(deck);
        card.setText("I punched your mom and then shoved _ up her _.");
        cardRepository.save(card);


        Phrase phrase = new Phrase();
        phrase.setOwningDeck(deck);
        phrase.setText("Raw buttsex.");
        phraseRepository.save(phrase);

        phrase = new Phrase();
        phrase.setOwningDeck(deck);
        phrase.setText("Dildo meat.");
        phraseRepository.save(phrase);

        phrase = new Phrase();
        phrase.setOwningDeck(deck);
        phrase.setText("Dildo meat.");
        phraseRepository.save(phrase);

        phrase = new Phrase();
        phrase.setOwningDeck(deck);
        phrase.setText("Dildo meat.");
        phraseRepository.save(phrase);

        phrase = new Phrase();
        phrase.setOwningDeck(deck);
        phrase.setText("Dildo meat.");
        phraseRepository.save(phrase);

        phrase = new Phrase();
        phrase.setOwningDeck(deck);
        phrase.setText("Dildo meat.");
        phraseRepository.save(phrase);

        phrase = new Phrase();
        phrase.setOwningDeck(deck);
        phrase.setText("Dildo meat.");
        phraseRepository.save(phrase);

        phrase = new Phrase();
        phrase.setOwningDeck(deck);
        phrase.setText("Dildo meat.");
        phraseRepository.save(phrase);

        phrase = new Phrase();
        phrase.setOwningDeck(deck);
        phrase.setText("Dildo meat.");
        phraseRepository.save(phrase);

        phrase = new Phrase();
        phrase.setOwningDeck(deck);
        phrase.setText("Dildo meat.");
        phraseRepository.save(phrase);

        phrase = new Phrase();
        phrase.setOwningDeck(deck);
        phrase.setText("Dildo meat.");
        phraseRepository.save(phrase);

        phrase = new Phrase();
        phrase.setOwningDeck(deck);
        phrase.setText("Dildo meat.");
        phraseRepository.save(phrase);

        phrase = new Phrase();
        phrase.setOwningDeck(deck);
        phrase.setText("Dildo meat.");
        phraseRepository.save(phrase);

        phrase = new Phrase();
        phrase.setOwningDeck(deck);
        phrase.setText("Dildo meat.");
        phraseRepository.save(phrase);

        phrase = new Phrase();
        phrase.setOwningDeck(deck);
        phrase.setText("Dildo meat.");
        phraseRepository.save(phrase);

        System.out.println("Saved Deck ID: " + deck.getUuid());
    }
}
