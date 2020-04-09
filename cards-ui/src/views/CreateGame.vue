<template>
  <v-container>
    <v-row class="text-center">
      <v-col class="mb-4">
        <div class="text-left">
          <h1>Create Lobby</h1>
        </div>
        <v-card>
          <v-card-text>
            <v-form>
              <v-text-field
                v-model="name"
                label="Lobby Name"
              />
              <v-slider
                v-model="maxPlayers"
                label="Max Players"
                :max="10"
                :min="3"
                :tick-size="1"
                :ticks="true">
                <template v-slot:append>
                  <span>{{ maxPlayers }}</span>
                </template>
              </v-slider>
              <v-slider
                v-model="turnTimeLimit"
                label="Turn Time Limit (seconds)"
                :max="120"
                :min="30"
                :step="5"
                :tick-size="5"
                :ticks="true">
                  <template v-slot:append>
                    <span>{{ turnTimeLimit }}</span>
                  </template>
              </v-slider>
              <h2 class="text-left mb-2">Decks</h2>
              <v-list color="blue" max-height="310" style="overflow-y: auto">
                <v-list-item-group
                  v-model="selectedDecks"
                  multiple
                >
                  <template v-for="(deck, i) in decks">
                    <v-divider
                      v-if="!deck"
                      :key="`deck-divider-${i}`"
                    ></v-divider>
                    <v-list-item
                      v-else
                      :key="`deck-${i}`"
                      :value="deck.uuid"
                      active-class="text--accent-4"
                    >
                      <template v-slot:default="{ active, toggle }">
                        <v-list-item-content>
                          <v-list-item-title v-text="deck.deckName"></v-list-item-title>
                        </v-list-item-content>
                        <v-list-item-action>
                          <v-checkbox
                            :input-value="active"
                            :true-value="deck.uuid"
                            color="accent-4"
                            @click="toggle"
                          ></v-checkbox>
                        </v-list-item-action>
                      </template>
                    </v-list-item>
                  </template>
                </v-list-item-group>
              </v-list>
            </v-form>
          </v-card-text>
          <v-divider></v-divider>
          <v-card-actions>
            <v-spacer></v-spacer>
            <v-btn text color="red" @click="clear">Clear</v-btn>
            <v-btn color="primary" @click="create">Create</v-btn>
          </v-card-actions>
        </v-card>
      </v-col>
    </v-row>
  </v-container>
</template>

<script>
import axios from 'axios'
import LobbyMixin from '../mixins/GamesMixin'

export default {
  props: {
  },

  mixins: [
    LobbyMixin
  ],

  data () {
    return {
      name: '',
      maxPlayers: 6,
      turnTimeLimit: 60,
      decks: [],
      selectedDecks: [],
      error: null
    }
  },

  watch: {
  },

  mounted () {
    axios.get('/api/v1/decks/all').then((response) => {
      this.decks = response.data
      this.selectedDecks[0] = this.decks[0].uuid
    }).catch((error) => {
      console.error(error)
    })
  },

  methods: {
    clear () {
      this.name = '';
      this.maxPlayers = 6;
      this.turnTimeLimit = 60;
      this.selectedDecks = [this.decks[0].uuid];
      this.error = null
    },

    create () {
      this.createGame(this.name, this.maxPlayers, this.turnTimeLimit, this.selectedDecks)
      this.$router.push(`/game/${this.name}`)
    }
  }
}
</script>
