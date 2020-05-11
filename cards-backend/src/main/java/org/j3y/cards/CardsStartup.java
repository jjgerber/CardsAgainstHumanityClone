package org.j3y.cards;

import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
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
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.IOException;
import java.util.Map;
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
        try {
            createDeck("Main Deck");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void createDeck(String deckName) throws IOException {
        CardDeck deck = new CardDeck();
        deck.setUuid(UUID.randomUUID().toString());
        deck.setDeckName(deckName);
        File file = ResourceUtils.getFile("classpath:maincahdeck.csv");
        //File file = new File("/home/cards/maincahdeck.csv");


        CsvMapper mapper = new CsvMapper();
        CsvSchema schema = CsvSchema.emptySchema().withHeader(); // use first row as header; otherwise defaults are fine
        MappingIterator<Map<String,String>> it = mapper.readerFor(Map.class)
                .with(schema)
                .readValues(file);
        while (it.hasNext()) {
            Map<String, String> rowAsMap = it.next();

            String type = rowAsMap.get("TYPE");
            String cardText = rowAsMap.get("CARD_TEXT");

            if ("BLACK".equals(type)) {
                int numPhrases = Integer.parseInt(rowAsMap.get("NUM_PHRASES"));
                Card card = new Card();
                card.setUuid(UUID.randomUUID().toString());
                card.setOwningDeck(deck);
                card.setText(cardText);
                card.setNumPhrases(numPhrases);
                deck.getCardSet().add(card);
            } else {
                Phrase phrase = new Phrase();
                phrase.setUuid(UUID.randomUUID().toString());
                phrase.setOwningDeck(deck);
                phrase.setText(cardText);
                deck.getPhraseSet().add(phrase);
            }
        }

        deck = deckRepository.save(deck);
        System.out.println("Saved Deck ID: " + deck.getUuid());
    }
}
