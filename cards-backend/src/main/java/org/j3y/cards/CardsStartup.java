package org.j3y.cards;

import org.j3y.cards.model.Card;
import org.j3y.cards.model.CardDeck;
import org.j3y.cards.model.Phrase;
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
        card.setNumPhrases(2);
        cardRepository.save(card);

        card = new Card();
        card.setOwningDeck(deck);
        card.setText("I really like _ with some _.");
        card.setNumPhrases(2);
        cardRepository.save(card);

        card = new Card();
        card.setOwningDeck(deck);
        card.setText("What's better than ice cream?");
        card.setNumPhrases(1);
        cardRepository.save(card);

        card = new Card();
        card.setOwningDeck(deck);
        card.setText("Why smoke weed when you can _?");
        card.setNumPhrases(1);
        cardRepository.save(card);

        card = new Card();
        card.setOwningDeck(deck);
        card.setText("I punched your mom and then shoved _ up her _.");
        card.setNumPhrases(2);
        cardRepository.save(card);


        Phrase phrase = new Phrase();
        phrase.setOwningDeck(deck);
        phrase.setText("Raw buttsex. h");
        phraseRepository.save(phrase);

        phrase = new Phrase();
        phrase.setOwningDeck(deck);
        phrase.setText("Dildo meat. z");
        phraseRepository.save(phrase);

        phrase = new Phrase();
        phrase.setOwningDeck(deck);
        phrase.setText("Dildo meat. p");
        phraseRepository.save(phrase);

        phrase = new Phrase();
        phrase.setOwningDeck(deck);
        phrase.setText("Dildo meat. t");
        phraseRepository.save(phrase);

        phrase = new Phrase();
        phrase.setOwningDeck(deck);
        phrase.setText("Dildo meat. f");
        phraseRepository.save(phrase);

        phrase = new Phrase();
        phrase.setOwningDeck(deck);
        phrase.setText("Dildo meat. a");
        phraseRepository.save(phrase);

        phrase = new Phrase();
        phrase.setOwningDeck(deck);
        phrase.setText("Dildo meat. c");
        phraseRepository.save(phrase);

        phrase = new Phrase();
        phrase.setOwningDeck(deck);
        phrase.setText("Dildo meat. a");
        phraseRepository.save(phrase);

        phrase = new Phrase();
        phrase.setOwningDeck(deck);
        phrase.setText("Dildo meat. b");
        phraseRepository.save(phrase);

        phrase = new Phrase();
        phrase.setOwningDeck(deck);
        phrase.setText("Dildo meat whore");
        phraseRepository.save(phrase);

        phrase = new Phrase();
        phrase.setOwningDeck(deck);
        phrase.setText("Dildo meat shit");
        phraseRepository.save(phrase);

        phrase = new Phrase();
        phrase.setOwningDeck(deck);
        phrase.setText("Dildo meat lol");
        phraseRepository.save(phrase);

        phrase = new Phrase();
        phrase.setOwningDeck(deck);
        phrase.setText("Dildo meat yo");
        phraseRepository.save(phrase);

        phrase = new Phrase();
        phrase.setOwningDeck(deck);
        phrase.setText("Dildo meat");
        phraseRepository.save(phrase);

        phrase = new Phrase();
        phrase.setOwningDeck(deck);
        phrase.setText("Dildo meat..");
        phraseRepository.save(phrase);

        phrase = new Phrase();
        phrase.setOwningDeck(deck);
        phrase.setText("Raw buttsex.....");
        phraseRepository.save(phrase);

        phrase = new Phrase();
        phrase.setOwningDeck(deck);
        phrase.setText("Dildo meat...");
        phraseRepository.save(phrase);

        phrase = new Phrase();
        phrase.setOwningDeck(deck);
        phrase.setText("Dildo meat.............");
        phraseRepository.save(phrase);

        phrase = new Phrase();
        phrase.setOwningDeck(deck);
        phrase.setText("Dildo meat");
        phraseRepository.save(phrase);

        phrase = new Phrase();
        phrase.setOwningDeck(deck);
        phrase.setText("Dildo meat..");
        phraseRepository.save(phrase);

        phrase = new Phrase();
        phrase.setOwningDeck(deck);
        phrase.setText("Dildo meat.......");
        phraseRepository.save(phrase);

        phrase = new Phrase();
        phrase.setOwningDeck(deck);
        phrase.setText("Dildo meat....");
        phraseRepository.save(phrase);

        phrase = new Phrase();
        phrase.setOwningDeck(deck);
        phrase.setText("Dildo meat..........");
        phraseRepository.save(phrase);

        phrase = new Phrase();
        phrase.setOwningDeck(deck);
        phrase.setText("Dildo meat.....");
        phraseRepository.save(phrase);

        phrase = new Phrase();
        phrase.setOwningDeck(deck);
        phrase.setText("Dildo meat...........");
        phraseRepository.save(phrase);

        phrase = new Phrase();
        phrase.setOwningDeck(deck);
        phrase.setText("Dildo meat");
        phraseRepository.save(phrase);

        phrase = new Phrase();
        phrase.setOwningDeck(deck);
        phrase.setText("Dildo meat..");
        phraseRepository.save(phrase);

        phrase = new Phrase();
        phrase.setOwningDeck(deck);
        phrase.setText("Dildo meat....");
        phraseRepository.save(phrase);

        phrase = new Phrase();
        phrase.setOwningDeck(deck);
        phrase.setText("Dildo meat...");
        phraseRepository.save(phrase);

        phrase = new Phrase();
        phrase.setOwningDeck(deck);
        phrase.setText("Dildo meat......");
        phraseRepository.save(phrase);

        System.out.println("Saved Deck ID: " + deck.getUuid());
    }
}
