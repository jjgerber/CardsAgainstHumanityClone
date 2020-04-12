<template>
  <v-container v-if="game">
    <v-row class="text-center">
      <v-col class="mb-4">
        <div class="text-left">
          <div class="mb-4">
            <v-row>
              <v-col>
                <v-btn
                  v-if="!playerHasJoined"
                  class="d-inline-block ma-2"
                  rounded color="green"
                  @click="joinGame()"
                  :disabled="isGameFull"
                >
                  <v-icon>mdi-account-plus</v-icon>Join Game
                </v-btn>
                <v-btn
                  v-else
                  class="d-inline-block ma-2"
                  rounded color="red"
                  @click="leaveGame()">
                  <v-icon>mdi-account-minus</v-icon>Leave Game
                </v-btn>
                <v-btn
                  v-if="playerIsOwner && isLobby"
                  class="d-inline-block ma-2"
                  rounded color="primary"
                  @click="startGame()"
                >
                  <v-icon>mdi-play</v-icon>Start Game
                </v-btn>
                <h1 class="ml-7 d-inline-flex">{{ gameName }} - {{ game.gameState }}</h1>
              </v-col>
              <v-col cols="auto">
                <v-scale-transition>
                  <v-chip
                    v-if="timerCount > 0"
                    class="ma-2"
                    :color="timerCount > 10 ? 'green' : 'red'"
                    text-color="white"
                    ripple
                  >
                    <v-avatar left>
                      <v-icon>mdi-alarm</v-icon>
                    </v-avatar>
                    {{ timerCount }}
                </v-chip>
                </v-scale-transition>
              </v-col>
            </v-row>
          </div>

          <v-slide-y-reverse-transition>
            <v-alert v-model="hasError" type="error" dismissible>{{ error }}</v-alert>
          </v-slide-y-reverse-transition>

          <v-scroll-y-transition mode="out-in">
            <game-settings :game-name="gameName" :game-config="game.gameConfig" :user-is-owner="playerIsOwner" v-if="game.gameState === 'LOBBY'"/>
            <v-row v-else>
              <v-col cols="auto" class="text-center align-center">
                <v-card>
                  <v-card-text v-if="!!currentCard">
                    <v-card color="black" class="black-card pa-2 text-center align-center d-inline-flex" raised>
                      <v-card-text style="height: 100%">{{ currentCard.text }}</v-card-text>
                    </v-card>
                  </v-card-text>
                </v-card>
              </v-col>
              <v-col>
                <v-card>
                  <v-card-text>
                    <v-container fluid class="pa-0">
                      <v-row v-if="game.gameState === 'CHOOSING'">
                        <!-- In Choosing Mode -->
                        <v-col class="text-center" v-for="chosen in game.numPlayersSelectedPhrases" :key="`choices-${chosen}`">
                          <div class="card-group text-center d-inline">
                            <v-card
                              v-for="card in currentCard.numPhrases" :key="`phrase-${chosen}-${card}`"
                              color="white"
                              light
                              class="white-card pa-2 text-center align-center d-inline-flex ma-1"
                              raised
                            >
                            </v-card>
                          </div>
                        </v-col>
                      </v-row>
                      <v-row v-else>
                        <!-- In Judging Mode -->
                        <v-col class="text-center" v-for="(phraseSelection, idx) in game.phraseSelectons" :key="`phrases-${idx}`">
                          <div class="card-group text-center d-inline">
                            <v-card
                              v-for="(phrase, phraseIdx) in phraseSelection" :key="`phrase-${idx}-${phraseIdx}`"
                              color="white"
                              light
                              class="white-card pa-2 text-center align-center d-inline-flex ma-1"
                              raised
                            >
                              <v-card-text style="height: 100%">{{ phrase.text }}</v-card-text>
                            </v-card>
                          </div>
                        </v-col>
                      </v-row>
                    </v-container>
                  </v-card-text>
                </v-card>
              </v-col>
            </v-row>
          </v-scroll-y-transition>

          <v-scroll-y-transition>
            <div class="mt-4" v-if="playerPhrases && playerPhrases.length > 0">
              <h2>Your Phrases</h2>
              <v-card>
                <v-card-text class="text-center">
                  <v-scroll-y-transition>
                    <v-alert v-if="playerIsJudge" type="info">You are judging.</v-alert>
                  </v-scroll-y-transition>
                  <v-card
                    v-for="(phrase, idx) in playerPhrases" :key="`player-phrase-${idx}`"
                    color="white"
                    light
                    class="white-card pa-2 text-center align-center d-inline-flex ma-1"
                    :class="playerIsJudge ? 'grey' : 'white'"
                    raised
                  >
                    <v-card-text style="height: 100%">{{ phrase.text }}</v-card-text>
                  </v-card>
                </v-card-text>
              </v-card>
            </div>
          </v-scroll-y-transition>

          <div class="mt-4">
            <v-row>
              <v-col cols="4">
                <v-card>
                  <v-card-title>Players</v-card-title>
                  <v-card-text class="text-left">
                    <v-simple-table>
                      <template v-slot:default>
                        <tbody>
                          <tr
                            v-for="player in players"
                            :key="`player-${player.name}`"
                          >
                            <td width="100%">
                              <v-icon v-if="player.name === game.owner.name" size="16" color="primary">mdi-crown</v-icon>
                              <v-icon v-if="game.judgingPlayer ? player.name === game.judgingPlayer.name : false" size="16" color="primary" slot="activator">mdi-gavel</v-icon>
                              {{ player.playerName }}
                            </td>
                            <td md="auto">
                              {{ player.score }}
                            </td>
                          </tr>
                        </tbody>
                      </template>
                    </v-simple-table>
                  </v-card-text>
                </v-card>
              </v-col>
              <v-col>
                <chat :game-name="gameName" />
              </v-col>
            </v-row>

          </div>

        </div>

        {{ game }}
      </v-col>
    </v-row>
  </v-container>
</template>

<script>
  import Chat from "../components/Chat";
  import GameSettings from "../components/GameSettings";
  import GamesMixin from "../mixins/GamesMixin";
  import UserInfoMixin from "../mixins/UserInfoMixin";

  export default {
    name: 'Game',

    components: {
      Chat,
      GameSettings
    },

    mixins: [
      GamesMixin,
      UserInfoMixin
    ],

    comments: [
      Chat
    ],

    data: function () {
      return {
        game: null,
        hover: null,
        hasError: false,
        error: null,
        timerCount: 0,
        timer: null,
        gameSubscription: null
      }
    },

    computed: {
      gameName () {
        return this.$route.params.name;
      },

      players() {
        return this.game ? this.game.players : [];
      },

      playerPhrases() {
        return this.playerInfo.phrases;
      },

      playerIsOwner() {
        return this.game.owner ? this.game.owner.name === this.playerInfo.name : false;
      },

      playerIsJudge() {
        return this.game.judgingPlayer ? this.playerInfo.name === this.game.judgingPlayer.name : false;
      },

      playerHasJoined() {
        return this.playerInfo.currentGameUuid === this.game.uuid;
      },

      isGameFull() {
        return this.game.numPlayers >= this.game.gameConfig.maxPlayers;
      },

      isAbandoned() {
        return this.game.gameState === 'ABANDONED';
      },

      isLobby() {
        return this.game.gameState === 'LOBBY';
      },

      gameTimeout() {
        return this.game && this.game.gameTimeoutTime ? Date.parse(this.game.gameTimeoutTime) : null;
      },

      currentCard() {
        return this.game && this.game.currentCard ? this.game.currentCard : null;
      }
    },

    watch: {
      gameTimeout() {
        if (this.gameTimeout) {
          console.log("SETTING TIMER...")
          this.timerCount = Math.round((this.gameTimeout - new Date()) / 1000);
          clearInterval(this.timer);
          this.timer = setInterval(() => {
            if (this.timerCount > 0) {
              this.timerCount--;
            } else {
              clearInterval(this.timer);
            }
          }, 1000);
        } else {
          this.timerCount = 0;
        }
      }
    },

    methods: {
      getGameData () {
        this.callGetGame(this.gameName).then((game) => {
          this.game = game;
        }).catch((error) => {
          this.$router.push('/');
        });
      },

      joinGame() {
        this.callJoinGame(this.gameName).then((game) => {
          this.game = game;
        }).catch((error) => {
          this.hasError = true;
          this.error = error.response.data.message;
        });
      },

      leaveGame() {
        this.callLeaveGame(this.gameName).finally((error) => {
          this.$router.push('/');
        });
      },

      startGame() {
        this.callStartGame(this.gameName).catch((error) => {
          this.hasError = true;
          this.error = error.response.data.message;
        });
      },

      connect() {
        console.log(`Connecting to game ${this.gameName}'s topic.`)
        this.gameSubscription = this.$stomp.subscribe('/topic/game/' + this.gameName, tick => {
          this.game = JSON.parse(tick.body);
        });
      }
    },

    mounted() {
      this.getGameData();
      this.connect();
    },

    destroyed() {
      if (this.gameSubscription) {
        console.log(`Unsubscribing from game subscription.`)
        this.gameSubscription.unsubscribe();
      }
    }
  }
</script>

<style>
  .black-card {
    width: 140px;
    height: 180px;
    font-size: 12px;
  }

  .white-card {
    width: 140px;
    height: 180px;
    font-size: 12px;
  }

  .card-group {
    white-space: nowrap;
    transform: scale(1.0);
    transition: 0.2s;
  }
  .card-group:hover {
    cursor: pointer;
    transform: scale(1.1);
    transition: 0.2s;
  }
  .card-group:hover .v-card {
    user-select:  none;
    background-color: rgba(0,0,0,0.2);
    box-shadow: 0px 0px 10px 10px rgba(255,255,255,0.2);
    transition: 0.2s;
  }
  .card-group .v-card {
    box-shadow: none;
    transition: 0.2s;
  }

  .chat {
    max-height: 500px;
    overflow-y: auto;
  }
</style>
