package org.j3y.cards.model.gameplay;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;

@Entity
public class Card {
    @Id
    @GeneratedValue(generator = "system-uuid")
    @GenericGenerator(name = "system-uuid", strategy="uuid")
    private String uuid;

    private String text;

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

    public CardDeck getOwningDeck() {
        return owningDeck;
    }

    public void setOwningDeck(CardDeck owningDeck) {
        this.owningDeck = owningDeck;
    }
}
