package org.j3y.cards;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
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

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
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
            InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream("allofficial.json");
            assert is != null;
            String text = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            ObjectMapper mapper = new ObjectMapper();
            JsonNode cardsData = mapper.readTree(text);
            parseJson(cardsData);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void parseJson(JsonNode cardsData) {
        ArrayNode deckNames = (ArrayNode) cardsData.get("order");
        ArrayNode whiteCards = (ArrayNode) cardsData.get("whiteCards");
        ArrayNode blackCards = (ArrayNode) cardsData.get("blackCards");

        deckNames.forEach(deckName -> {
            String deckKey = deckName.asText();
            JsonNode deckData = cardsData.get(deckKey);
            parseDeck(deckData, whiteCards, blackCards);
        });
    }

    void parseDeck(JsonNode deckData, ArrayNode whiteCards, ArrayNode blackCards) {
        String deckName = deckData.path("name").asText();
        System.out.println("Deck: " + deckName);

        ArrayNode whiteCardIndices = (ArrayNode) deckData.path("white");
        ArrayNode blackCardIndices = (ArrayNode) deckData.path("black");

        CardDeck cardDeck = new CardDeck();
        cardDeck.setDeckName(deckName);
        cardDeck.setUuid(UUID.randomUUID().toString());

        whiteCardIndices.forEach(jsonNode -> {
            int index = jsonNode.asInt();
            String cardText = whiteCards.path(index).asText();
            Phrase phrase = new Phrase();

            phrase.setUuid(UUID.randomUUID().toString());
            phrase.setOwningDeck(cardDeck);
            phrase.setText(cardText);

            cardDeck.getPhraseSet().add(phrase);

            System.out.println(phrase);
        });

        blackCardIndices.forEach(jsonNode -> {
            int index = jsonNode.asInt();
            String cardText = blackCards.path(index).path("text").asText();
            int numPhrases = blackCards.path(index).path("pick").asInt();

            Card card = new Card();
            card.setUuid(UUID.randomUUID().toString());
            card.setOwningDeck(cardDeck);
            card.setText(cardText);
            card.setNumPhrases(numPhrases);

            cardDeck.getCardSet().add(card);

            System.out.println(card);
        });

        deckRepository.save(cardDeck);
    }
}
