<template>
  <v-app v-if="playerInfoReady">
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
        size="small"
        icon
        variant="plain"
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
import PlayerData from '@/composition/PlayerData.js';
import PlayerActions from '@/composition/PlayerActions.js';
import {mutations, store} from './store.js';

export default {
  name: 'App',

  setup() {
    const { playerInfo } = PlayerData();
    const { retrievePlayerInfo } = PlayerActions();

    return { playerInfo, retrievePlayerInfo };
  },

  components: {
    SetNameDialog
  },

  data: () => ({
    showSetNameDialog: false,
    drawer: null,
    right: null,
    playerInfoReady: false,
    playerInfoSubscription: null
  }),

  computed: {
    currentYear() {
      return new Date().getFullYear();
    },

    socketConnectionTime() {
      return store.state.socketConnectionTime;
    }
  },

  watch: {
    socketConnectionTime() {
      this.subscribePlayerInfo();
    }
  },

  mounted() {
    this.retrievePlayerInfo().catch((error) => {
      console.error(error)
    }).finally(() => {
      this.playerInfoReady = true
      if (this.socketConnectionTime && !this.playerInfoSubscription) {
        this.subscribePlayerInfo();
      }
    });
  },

  unmounted() {
    if (this.playerInfoSubscription) {
      console.log("Unsubscribing from player info...")
      this.playerInfoSubscription.unsubscribe();
    }
  },

  methods: {
    subscribePlayerInfo() {
      console.log(`Subscribing to player information ${this.playerInfo.playerName}'s topic.`)
      this.playerInfoSubscription = this.$stomp.subscribe(`/user/${this.playerInfo.name}/userInfo`, message => {
        mutations.setPlayerInfo(JSON.parse(message.body));
      });
    }
  }
}
</script>

<style scoped>

</style>
