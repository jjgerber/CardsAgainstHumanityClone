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

        <v-list-item link @click="$router.push('/settings')">
          <v-list-item-action>
            <v-icon>mdi-cog-outline</v-icon>
          </v-list-item-action>
            <v-list-item-content>
              <v-list-item-title>Settings</v-list-item-title>
          </v-list-item-content>
        </v-list-item>

        <v-list-item link @click="$router.push('/about')">
          <v-list-item-action>
            <v-icon>mdi-help-box</v-icon>
          </v-list-item-action>
          <v-list-item-content>
            <v-list-item-title>About</v-list-item-title>
          </v-list-item-content>
        </v-list-item>
      </v-list>
    </v-navigation-drawer>

    <v-app-bar
      app
      clipped-left
    >
      <v-app-bar-nav-icon @click.stop="drawer = !drawer"></v-app-bar-nav-icon>
      <v-toolbar-title>Cards Versus Humanity</v-toolbar-title>
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
      <span>&copy; 2020 j3y.org</span>
    </v-footer>
  </v-app>
</template>

<script>
import SetNameDialog from './components/dialogs/SetNameDialog'
import UserInfoMixin from './mixins/UserInfoMixin'
import { store, mutations } from './store'
import SockJS from "sockjs-client";
import Stomp from "webstomp-client";
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
    items: [
      { title: 'Dashboard', icon: 'mdi-view-dashboard' },
      { title: 'Photos', icon: 'mdi-image' },
      { title: 'About', icon: 'mdi-help-box' }
    ],
    right: null,
    playerInfoReady: false,
    socketReady: false,
    playerInfoSubscription: null
  }),

  mounted () {
    this.retrievePlayerInfo().catch((error) => {
      console.error(error)
    }).finally(() => {
      this.playerInfoReady = true
    })

    Vue.prototype.$stomp.connect( {}, frame => {
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
    state () {
      return store.state
    }
  }
}
</script>

<style scoped>

</style>
