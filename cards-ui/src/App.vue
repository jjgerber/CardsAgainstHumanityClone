<template>
  <v-app v-if="playerInfoReady && socketReady">
    <v-navigation-drawer
      v-model="drawer"
      app
      clipped
    >
      <v-list dense>
        <v-list-item link @click="$router.push('/')">
          <v-list-item-action>
            <v-icon>mdi-account-group</v-icon>
          </v-list-item-action>
          <v-list-item-content>
            <v-list-item-title>Lobbies</v-list-item-title>
          </v-list-item-content>
        </v-list-item>
      </v-list>
    </v-navigation-drawer>

    <v-app-bar
      app
      clipped-left
    >
      <v-app-bar-nav-icon @click.stop="drawer = !drawer"></v-app-bar-nav-icon>
      <v-toolbar-title>Cards vs. Humanity</v-toolbar-title>

      <v-progress-linear
        :active="!!timerPercent"
        v-model="timerPercent"
        absolute
        bottom
        :color="timer > 10 ? 'green' : 'red'"
      >
      </v-progress-linear>

      <v-spacer></v-spacer>
      <v-btn icon @click="showSetNameDialog = true">
        <v-icon>mdi-account</v-icon>
      </v-btn>
      <span class="pa-2">{{ playerInfo.playerName }}</span>

    </v-app-bar>

    <v-content>
      <v-fade-transition mode="out-in">
        <router-view></router-view>
      </v-fade-transition>
    </v-content>

    <set-name-dialog v-model="showSetNameDialog" />

    <v-footer app>
      <v-btn class="mr-2" x-small icon href="https://github.com/jjgerber/CardsAgainstHumanityClone" target="_blank"><v-icon>mdi-github</v-icon></v-btn> <span> &#169; 2020 cardsvshumanity.com.</span>
    </v-footer>
  </v-app>
</template>

<script>
import SetNameDialog from './components/dialogs/SetNameDialog';
import UserInfoMixin from './mixins/UserInfoMixin';
import { store, mutations } from './store';
import Vue from "vue";

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

  mounted() {
    this.retrievePlayerInfo().catch((error) => {
      console.error(error)
    }).finally(() => {
      this.playerInfoReady = true
    })

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
  },

  destroyed() {
    if (this.playerInfoSubscription) {
      this.playerInfoSubscription.disconnect();
    }
  },

  methods: {
  },

  computed: {
    timer() {
      return store.state.timer;
    },

    timerStart () {
      return store.state.timerStart;
    },

    timerPercent() {
      return this.timerStart > 0 ? (this.timer / this.timerStart) * 100 : null;
    },

  }
}
</script>

<style scoped>

</style>
