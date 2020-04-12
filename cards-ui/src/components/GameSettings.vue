<template>
  <v-container>
    <v-card>
      <v-card-text>
        <v-form>
          <v-text-field
            v-if="!gameConfig"
            v-model="name"
            label="Lobby Name"
          />
          <v-slider
            v-if="!gameConfig"
            v-model="maxPlayers"
            label="Max Players"
            :disabled="!userIsOwner"
            :max="10"
            :min="3"
            :tick-size="1"
            :ticks="true">
            <template v-slot:append>
              <span>{{ maxPlayers }}</span>
            </template>
          </v-slider>
          <v-slider
            v-model="maxScore"
            label="Max Score (Game Winning Score)"
            :disabled="!userIsOwner"
            :max="15"
            :min="1"
            :tick-size="1"
            :ticks="true">
            <template v-slot:append>
              <span>{{ maxScore }}</span>
            </template>
          </v-slider>
          <v-slider
            v-model="turnTimeLimit"
            label="Turn Time Limit (seconds)"
            :disabled="!userIsOwner"
            :max="120"
            :min="15"
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
              :disabled="true"
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
                  :disabled="!userIsOwner"
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
                        @click="userIsOwner ? toggle : null"
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
      <v-card-actions v-if="userIsOwner">
        <v-spacer></v-spacer>
        <v-btn text color="red" @click="clear" v-if="!gameConfig">Clear</v-btn>
        <v-btn color="primary" @click="gameConfig ? update() : create()">{{ !!this.gameConfig ?  'Update' : 'Create'}}</v-btn>
      </v-card-actions>
    </v-card>
  </v-container>
</template>

<script>
  import axios from 'axios'
  import LobbyMixin from '../mixins/GamesMixin'

  export default {
    props: {
      gameName: String,
      gameConfig: {
        type: Object,
        default: null
      },
      userIsOwner: {
        type: Boolean,
        default: true
      }
    },

    mixins: [
      LobbyMixin
    ],

    data () {
      return {
        name: this.gameName,
        maxPlayers: this.gameConfig ? this.gameConfig.maxPlayers : 6,
        maxScore: this.gameConfig ? this.gameConfig.maxScore : 5,
        turnTimeLimit: this.gameConfig ? this.gameConfig.turnTimeout : 60,
        decks: [],
        selectedDecks: this.gameConfig ? this.gameConfig.deckIds : [],
        error: null
      }
    },

    watch: {
      gameConfig() {
        this.maxPlayers = this.gameConfig ? this.gameConfig.maxPlayers : 6;
        this.maxScore = this.gameConfig ? this.gameConfig.maxScore : 5;
        this.turnTimeLimit = this.gameConfig ? this.gameConfig.turnTimeout : 60;
        this.selectedDecks = this.gameConfig ? this.gameConfig.deckIds : [];
      }
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
        this.maxScore = 5;
        this.turnTimeLimit = 60;
        this.selectedDecks = [this.decks[0].uuid];
        this.error = null
      },

      create () {
        this.callCreateGame(this.name, this.maxPlayers, this.maxScore, this.turnTimeLimit, this.selectedDecks)
        this.$router.push(`/game/${this.name}`)
      },

      update () {
        this.callUpdateGame(this.name, this.maxPlayers, this.maxScore, this.turnTimeLimit, this.selectedDecks)
      }
    }
  }
</script>
