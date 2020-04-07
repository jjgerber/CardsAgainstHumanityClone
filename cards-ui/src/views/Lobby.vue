<template>
  <v-container>
    <v-row class="text-center">

      <v-col class="mb-4">
        <v-card>
          <v-card-title>
            Lobbies
            <v-spacer></v-spacer>
            <v-text-field
              v-model="search"
              append-icon="mdi-magnify"
              label="Search Lobbies"
              single-line
              hide-details
              clearable
            ></v-text-field>
          </v-card-title>
          <v-data-table
            class="lobby-table elevation-4"
            :headers="lobbyHeaders"
            :items="lobbies"
            :search="search"
            :sort-by="['players']"
            :sort-desc="[false]"
            multi-sort
            @click:row="handleLobbyClick"
          ></v-data-table>
        </v-card>
      </v-col>
    </v-row>
  </v-container>
</template>

<script>
export default {
  name: 'HelloWorld',

  data: () => ({
    search: '',
    lobbyHeaders: [
      { text: 'Lobby Name', align: 'start', value: 'name' },
      { text: 'Status', value: 'status' },
      { text: 'Players', value: 'players' },
      { text: 'Max Players', value: 'maxPlayers' },
      { text: 'Decks', value: 'decks' }
    ],
    lobbies: [
      {
        name: 'Buttstuff',
        status: 'Lobby',
        players: 1,
        maxPlayers: 8,
        decks: 'Main, Bonus 2020, Holiday 2020'
      },
      {
        name: 'Billys Bagels',
        status: 'In Progress',
        players: 6,
        maxPlayers: 6,
        decks: 'Bonus 2020, Holiday 2020'
      },
      {
        name: 'Herp Derp',
        status: 'In Progress',
        players: 10,
        maxPlayers: 10,
        decks: 'Main'
      },
      {
        name: 'Stuff butts',
        status: 'Lobby',
        players: 4,
        maxPlayers: 6,
        decks: 'Main, Bonus 2020'
      },
      {
        name: 'Test Lobby',
        status: 'Lobby',
        players: 5,
        maxPlayers: 5,
        decks: 'Main, Bonus 2020'
      },
      {
        name: 'Hey Dudes',
        status: 'Lobby',
        players: 2,
        maxPlayers: 6,
        decks: 'Holiday 2020, Bonus 2020'
      },
      {
        name: 'Boats n Hoes',
        status: 'Lobby',
        players: 1,
        maxPlayers: 8,
        decks: 'Main'
      },
      {
        name: 'Here Be Dragons',
        status: 'Lobby',
        players: 3,
        maxPlayers: 6,
        decks: 'Bonus 2020'
      },
      {
        name: 'Tiggo Bitties',
        status: 'Lobby',
        players: 2,
        maxPlayers: 8,
        decks: 'Holiday 2020'
      }
    ]
  }),

  methods: {
    handleLobbyClick: (data) => {
      alert(JSON.stringify(data))
    }
  }
}
</script>

<style>
  .lobby-table tr:hover {
    cursor: pointer !important;
  }
</style>
