package org.j3y.cards.model.gameplay;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import java.util.Set;

@Entity
public class CardDeck {

    @Id
    private String uuid;

    private String deckName;

    @OneToMany(mappedBy = "owningDeck", cascade = CascadeType.ALL)
    private Set<Card> cardSet;

    @OneToMany(mappedBy = "owningDeck", cascade = CascadeType.ALL)
    private Set<Phrase> phraseSet;

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
