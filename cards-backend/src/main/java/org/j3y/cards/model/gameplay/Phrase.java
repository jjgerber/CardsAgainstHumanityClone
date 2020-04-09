package org.j3y.cards.model.gameplay;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;
import org.j3y.cards.model.Views;

import javax.persistence.*;
import java.util.UUID;

@Entity
public class Phrase {
    @Id
    @JsonView(Views.Limited.class)
    private String uuid;

    @JsonView(Views.Limited.class)
    private String text;

    @ManyToOne(cascade = CascadeType.MERGE)
    @JoinColumn(name = "deckId")
    @JsonIgnore
    private CardDeck owningDeck;

    public Phrase() {
        this.uuid = UUID.randomUUID().toString();
    }

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
