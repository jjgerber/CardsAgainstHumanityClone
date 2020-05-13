package org.j3y.cards.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;

import javax.persistence.*;

@Entity
public class Card {
    @Id
    @JsonView(Views.Limited.class)
    @Column(length = 36)
    private String uuid;

    @JsonView(Views.Limited.class)
    private String text;

    @JsonView(Views.Limited.class)
    private int numPhrases;

    @ManyToOne(cascade = CascadeType.MERGE)
    @JoinColumn(name = "deckId")
    @JsonIgnore
    private CardDeck owningDeck;

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public int getNumPhrases() {
        return numPhrases;
    }

    public void setNumPhrases(int numPhrases) {
        this.numPhrases = numPhrases;
    }

    public CardDeck getOwningDeck() {
        return owningDeck;
    }

    public void setOwningDeck(CardDeck owningDeck) {
        this.owningDeck = owningDeck;
    }

    @Override
    public String toString() {
        return "Card{" +
                "uuid='" + uuid + '\'' +
                ", text='" + text + '\'' +
                ", numPhrases=" + numPhrases +
                '}';
    }
}
