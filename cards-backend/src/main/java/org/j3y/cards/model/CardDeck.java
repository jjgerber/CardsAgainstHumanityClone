package org.j3y.cards.model;

import com.fasterxml.jackson.annotation.JsonView;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
public class CardDeck {
    @Id
    @JsonView(Views.Limited.class)
    @Column(length = 36)
    private String uuid;

    @JsonView(Views.Limited.class)
    private String deckName;

    @JsonView(Views.Full.class)
    @OneToMany(mappedBy = "owningDeck", cascade = CascadeType.ALL)
    private Set<Card> cardSet;

    @JsonView(Views.Full.class)
    @OneToMany(mappedBy = "owningDeck", cascade = CascadeType.ALL)
    private Set<Phrase> phraseSet;

    public CardDeck() {
        this.cardSet = new HashSet<>();
        this.phraseSet = new HashSet<>();
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getDeckName() {
        return deckName;
    }

    public void setDeckName(String deckName) {
        this.deckName = deckName;
    }

    public Set<Card> getCardSet() {
        return cardSet;
    }

    public void setCardSet(Set<Card> cardSet) {
        this.cardSet = cardSet;
    }

    public Set<Phrase> getPhraseSet() {
        return phraseSet;
    }

    public void setPhraseSet(Set<Phrase> phraseSet) {
        this.phraseSet = phraseSet;
    }
}
