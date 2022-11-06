<template>
  <v-container>
    <v-row class="text-center">
      <v-col class="mb-4">
        <div class="text-left">
          <h1>Lobbies</h1>
        </div>
        <v-card>
          <v-card-title>
            <v-row>
              <v-col md="auto">
                <v-btn
                  rounded
                  color="primary"
                  @click="$router.push('create-lobby')"
                >
                  <v-icon>mdi-plus</v-icon>Create Lobby
                </v-btn>
              </v-col>
              <v-col class="pa-0 mr-2">
                <v-text-field
                  v-model="search"
                  append-icon="mdi-magnify"
                  label="Search"
                  justify="center"
                  single-line
                  hide-details
                  clearable
                />
              </v-col>
            </v-row>
          </v-card-title>
        </v-card>

        <div
          v-if="filteredGames.length === 0"
          class="mt-5 mb-5"
        >
          No Lobbies Found.
        </div>
        <div v-else>
          <div class="d-flex justify-center mt-5 mb-5">
            <v-card
              v-for="game in filteredGames"
              class="ma-2 bg-blue"
              width="250"
              ripple
              @click="joinGame(game.name)"
            >
              <v-card-title>{{ game.name }}</v-card-title>
              <v-divider />
              <v-card-text class="ma-0 pt-1 pb-1">
                Players: {{ game.numPlayers }} / {{ game.gameConfig.maxPlayers }}
              </v-card-text>
              <v-divider />
              <v-card-text class="ma-0 pt-1 pb-1">
                Max Score: {{ game.gameConfig.maxScore }}
              </v-card-text>
              <v-divider />
              <v-card-text class="ma-0 pt-1 pb-1">
                {{ game.gameState }}
              </v-card-text>
            </v-card>
          </div>
        </div>

      </v-col>
    </v-row>
  </v-container>
</template>

<script>
  import GameActions from '@/composition/GameActions.js';

  export default {
    name: 'HelloWorld',

    setup() {
      const { callGetAllLobbies } = GameActions();

      return { callGetAllLobbies };
    },

    data() {
      return {
        search: '',
        lobbyHeaders: [
          { text: 'Lobby Name', align: 'start', value: 'name' },
          { text: 'Status', value: 'gameState' },
          { text: 'Players', value: 'numPlayers' },
          { text: 'Max Players', value: 'gameConfig.maxPlayers' },
          { text: 'Decks', value: 'gameConfig.deckNames' }
        ],
        sortBy: ['numPlayers'],
        games: [],
        stompSubscription: null
      }
    },

    computed: {
      filteredGames() {
        if (!this.search) {
          return this.games;
        }

        const searchTerm = this.search.toLowerCase().trim();

        return this.games.filter(game => {
          const gameName = game.name.toLowerCase();
          return gameName.indexOf(searchTerm) > -1;
        })
      }
    },

    mounted () {
      this.connect();
      this.callGetAllLobbies().then((response) => {
        this.games = response.data;
      })
    },

    unmounted() {
      console.log("Unsubscribing from lobby topic.");
      this.stompSubscription.unsubscribe();
    },

    methods: {
      joinGame(name) {
        this.$router.push(`/game/${name}`);
      },

      connect() {
        this.stompSubscription = this.$stomp.subscribe('/topic/lobbies', tick => {
          this.games = JSON.parse(tick.body);
        })
      }
    }
  }
</script>

<style>
  .lobby-table tr:hover {
    cursor: pointer !important;
  }
</style>
