package org.j3y.cards.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;

import javax.persistence.*;
import java.util.Objects;
import java.util.UUID;

@Entity
public class Phrase {
    @Id
    @JsonView(Views.Limited.class)
    @Column(length = 36)
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Phrase phrase = (Phrase) o;
        return uuid.equals(phrase.uuid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uuid);
    }

    @Override
    public String toString() {
        return "Phrase{" +
                "uuid='" + uuid + '\'' +
                ", text='" + text + '\'' +
                '}';
    }
}
