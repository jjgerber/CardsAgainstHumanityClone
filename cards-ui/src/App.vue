<template>
  <v-app v-if="playerInfoReady && socketReady">
    <v-app-bar>
      <v-app-bar-nav-icon @click="drawer = !drawer" />
      <v-app-bar-title>Cards vs. Humanity</v-app-bar-title>
      <v-btn
        icon
        @click="showSetNameDialog = true"
      >
        <v-icon>mdi-account</v-icon>
      </v-btn>
      <span class="pa-2 mr-4">{{ playerInfo.playerName }}</span>
    </v-app-bar>

    <v-navigation-drawer v-model="drawer">
      <v-list dense>
        <v-list-item
          link
          @click="$router.push('/')"
        >
          <template v-slot:prepend>
            <v-icon>mdi-account-group</v-icon>
          </template>
          <v-list-item-title>Lobbies</v-list-item-title>
        </v-list-item>
      </v-list>
    </v-navigation-drawer>

    <v-main>
      <router-view />
    </v-main>

    <set-name-dialog v-model="showSetNameDialog" />

    <v-footer app>
      <v-btn
        class="mr-2"
        x-small
        icon
        href="https://github.com/jjgerber/CardsAgainstHumanityClone"
        target="_blank"
      >
        <v-icon>mdi-github</v-icon>
      </v-btn> <span> &#169; {{ currentYear }} cardsvshumanity.com.</span>
    </v-footer>
  </v-app>
</template>

<script>
import SetNameDialog from './components/dialogs/SetNameDialog.vue';
import UserInfoMixin from './mixins/UserInfoMixin.js';
import { store, mutations } from './store.js';

export default {
  name: 'App',

  components: {
    SetNameDialog
  },

  mixins: [
    UserInfoMixin
  ],

  data: () => ({
    showSetNameDialog: false,
    drawer: null,
    right: null,
    playerInfoReady: false,
    socketReady: false,
    playerInfoSubscription: null
  }),

  computed: {
    currentYear() {
      return new Date().getFullYear();
    }
  },

  watch: {
    socketReady() {
      if (!this.socketReady) {
        console.log("Socket connection lost. Reconnecting...")
        this.connectSocket();
      }
    }
  },

  mounted() {
    this.retrievePlayerInfo().catch((error) => {
      console.error(error)
    }).finally(() => {
      this.playerInfoReady = true
    });

    this.connectSocket();
  },

  destroyed() {
    if (this.playerInfoSubscription && this.playerInfoSubscription.isConnected) {
      this.playerInfoSubscription.disconnect();
    }
  },

  methods: {
    connectSocket() {
      this.$stomp.connect( {}, frame => {
          this.socketReady = true;
          console.log(`Connecting to user information ${this.playerInfo.playerName}'s queue.`)
          this.playerInfoSubscription = this.$stomp.subscribe(`/user/${this.playerInfo.name}/userInfo`, tick => {
            console.log("Retrieved user info update!");
            mutations.setPlayerInfo(JSON.parse(tick.body));
          });
        },
        error => {
          console.error(error);
          this.socketReady = false;
        }
      )
    }
  }
}
</script>

<style scoped>

</style>
