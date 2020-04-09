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
                <v-btn rounded color="primary" @click="$router.push('create-lobby')"><v-icon>mdi-plus</v-icon>Create Lobby</v-btn>
              </v-col>
              <v-col class="pa-0">
                <v-text-field
                  v-model="search"
                  append-icon="mdi-magnify"
                  label="Search"
                  justify="center"
                  single-line
                  hide-details
                  clearable
                ></v-text-field>
              </v-col>
            </v-row>
          </v-card-title>
          <v-data-table
            class="lobby-table elevation-4"
            :headers="lobbyHeaders"
            :items="games"
            :search="search"
            :sort-by="sortBy"
            no-data-text="No Lobbies Found"
            :sort-desc="[false]"
            @click:row="handleLobbyClick"
          ></v-data-table>
        </v-card>
      </v-col>
    </v-row>
  </v-container>
</template>

<script>
import SockJS from 'sockjs-client'
import Stomp from 'webstomp-client'

import LobbyMixin from '../mixins/GamesMixin'

export default {
  name: 'HelloWorld',

  mixins: [
    LobbyMixin
  ],

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
      connected: false,
      socket: null
    }
  },

  methods: {
    handleLobbyClick (row) {
      this.$router.push(`/game/${row.name}`);
    },

    connect() {
      console.log("CONNECTING TO WEBSOCKET...");
      //let host = (window.location.protocol + '//' + window.location.host).replace("8081", "8080");
      this.socket = new SockJS('/socket');
      this.stompClient = Stomp.over(this.socket);
      this.stompClient.connect( {}, frame => {
          this.connected = true;
          this.stompClient.subscribe('/topic/lobbies', tick => {
            this.games = JSON.parse(tick.body);
          })
        },
        error => {
          console.error(error);
          this.connected = false;
        }
      )
    }
  },

  mounted () {
    this.connect();
    this.getAllLobbies().then((response) => {
      this.games = response.data;
    })
  }
}
</script>

<style>
  .lobby-table tr:hover {
    cursor: pointer !important;
  }
</style>
