<template>
  <v-container v-if="game">
    <v-row class="text-center">
      <v-col class="mb-4">
        <div class="mb-4">
          <v-row>
            <v-col class="text-left">
              <h1 class="d-inline-flex">{{ gameName }}</h1>
            </v-col>
            <v-col cols="auto">
              <v-btn
                v-if="!playerHasJoined"
                class="d-inline-block"
                rounded color="green"
                @click="joinGame()"
                :disabled="isGameFull"
              >
                <v-icon>mdi-account-plus</v-icon>Join Game
              </v-btn>
              <v-btn
                v-else
                class="d-inline-block ml-2"
                rounded color="red"
                @click="leaveGame()">
                <v-icon>mdi-account-minus</v-icon>Leave Game
              </v-btn>
              <v-btn
                v-if="playerIsOwner && isLobby"
                class="d-inline-block ml-2"
                rounded color="primary"
                @click="startGame()"
              >
                <v-icon>mdi-play</v-icon>Start Game
              </v-btn>
            </v-col>
            <v-col cols="auto">
              <v-scale-transition>
                <v-chip
                  v-if="timerCount > 0"
                  class="d-inline-block"
                  pill
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

          <v-slide-y-reverse-transition>
            <v-alert v-model="hasError" type="error" dismissible>{{ error }}</v-alert>
          </v-slide-y-reverse-transition>

          <h2 class="text-capitalize text-left">Status: {{ gameStateFormatted }}</h2>
          <game-settings :game-name="gameName" :game-config="game.gameConfig" :user-is-owner="playerIsOwner" v-if="isLobby"/>
          <div v-else-if="isStateGameOver">
            <v-alert color="green">{{ gameWinner.playerName }} has reached score {{ gameWinner.score }} and won the game!</v-alert>
          </div>
          <v-row v-else>
            <v-col cols="auto" class="text-center align-center">
              <v-card>
                <v-card-text v-if="!!currentCard">
                  <v-card color="black" class="game-card pa-2 text-center align-center d-inline-flex" raised>
                    <v-card-text class="pa-0" style="height: 100%">{{ currentCard.text }}</v-card-text>
                  </v-card>
                </v-card-text>
              </v-card>
            </v-col>
            <v-col>
              <v-card>
                <v-card-text class="text-center">
                  <v-alert v-if="judgeDidntChoose && game.phraseSelections.length === 0" mode="out-in" type="error">No players chose a phrase to be judged.</v-alert>
                  <v-alert v-else-if="judgeDidntChoose && game.phraseSelections.length > 0" mode="out-in" type="error">The judge didn't choose a winner in time. Your cards have been returned.</v-alert>
                  <v-alert v-else-if="playerIsJudge && isStateJudging && judgeSelection != null" mode="out-in" color="grey" text dense>
                    Confirm your selection? <v-btn color="green" @click="confirmJudgement()">Confirm</v-btn>
                  </v-alert>
                  <v-alert v-else-if="playerIsJudge && isStateJudging" mode="out-in" color="grey" text>Choose the winner.</v-alert>
                  <v-alert v-else-if="isStateDoneJudging && judgeChoice !== null" mode="out-in" class="text-center" color="green">
                    Player {{ game.lastWinningPlayer.playerName }} won!
                    They have {{ game.lastWinningPlayer.score }} point{{ game.lastWinningPlayer.score > 1 ? 's' : ''}}.
                  </v-alert>
                  <v-container fluid class="pa-0">
                    <v-row v-if="isStateChoosing">
                      <!-- In Choosing Mode -->
                      <v-col class="text-center" v-for="chosen in game.numPlayersSelectedPhrases" :key="`choices-${chosen}`">
                        <div class="card-group text-center d-inline">
                          <v-card
                            v-for="card in currentCard.numPhrases" :key="`phrase-${chosen}-${card}`"
                            color="white"
                            light
                            class="game-card pa-2 text-center align-center d-inline-flex ma-1"
                            raised
                          >
                          </v-card>
                        </div>
                      </v-col>
                    </v-row>
                    <v-row v-else>
                      <!-- In Judging Mode -->
                      <v-col class="text-center" v-for="(phraseSelection, idx) in game.phraseSelections" :key="`phrases-${idx}`">
                        <div class="card-group text-center d-inline">
                          <v-card
                            v-for="(phrase, phraseIdx) in phraseSelection" :key="`phrase-${idx}-${phraseIdx}`"
                            :color="isStateDoneJudging && judgeChoice  === idx || judgeSelection === idx ? 'green lighten-3' : 'white'"
                            light
                            class="game-card pa-2 text-center align-center d-inline-flex ma-1"
                            raised
                            @click="judgeClicked(idx)"
                          >
                            <v-card-text class="pa-0" style="height: 100%">{{ phrase.text }}</v-card-text>
                          </v-card>
                        </div>
                      </v-col>
                    </v-row>
                  </v-container>
                </v-card-text>
              </v-card>
            </v-col>
          </v-row>

          <v-scroll-y-transition>
            <div class="mt-4" v-if="playerPhrases && playerPhrases.length > 0">
              <h2 class="text-left">Your Phrases</h2>
              <v-card>
                <v-card-text class="text-center">
                  <v-scroll-y-transition mode="out-in">
                    <v-alert v-if="playerIsJudge" color="grey" text>You are judging.</v-alert>
                    <v-alert v-else-if="isStateChoosing && currentCard.numPhrases === selections.length" color="grey" text>
                      Confirm your selections? <v-btn color="green" @click="confirmSelections" small>Confirm</v-btn>
                    </v-alert>
                    <v-alert v-else-if="isStateChoosing" color="grey" text>
                      Choose {{ currentCard.numPhrases }} white card{{ currentCard.numPhrases > 1 ? 's' : ''}}.
                    </v-alert>
                  </v-scroll-y-transition>
                  <v-card
                    v-for="(phrase, idx) in playerPhrases" :key="`player-phrase-${idx}`"
                    :color="selections.includes(phrase.uuid) ? 'green lighten-3' : 'white'"
                    light
                    class="game-card pa-2 text-center align-center d-inline-flex ma-1"
                    :class="playerIsJudge ? 'grey' : 'white'"
                    @click="phraseClicked(phrase)"
                    raised
                  >
                    <v-card-text class="pa-0" style="height: 100%">{{ phrase.text }}</v-card-text>
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

        <!-- {{ game }} -->
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
        selections: [],
        judgeSelection: null,
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
        return this.game && this.game.owner ? this.game.owner.name === this.playerInfo.name : false;
      },

      playerIsJudge() {
        return this.game && this.game.judgingPlayer ? this.playerInfo.name === this.game.judgingPlayer.name : false;
      },

      playerHasJoined() {
        return this.game ? this.playerInfo.currentGameUuid === this.game.uuid : false;
      },

      isGameFull() {
        return this.game && (this.game.numPlayers >= this.game.gameConfig.maxPlayers);
      },

      gameStateFormatted() {
        return this.game ? this.game.gameState.replace('_', ' ').toLowerCase() : '';
      },

      isAbandoned() {
        return this.game && this.game.gameState === 'ABANDONED';
      },

      isLobby() {
        return this.game && this.game.gameState === 'LOBBY';
      },

      isStateChoosing() {
        return this.game && this.game.gameState === 'CHOOSING';
      },

      isStateJudging() {
        return this.game && this.game.gameState === 'JUDGING';
      },

      isStateDoneJudging() {
        return this.game && this.game.gameState === 'DONE_JUDGING';
      },

      isStateGameOver() {
        return this.game && this.game.gameState === 'GAME_OVER';
      },

      gameTimeout() {
        return this.game && this.game.gameTimeoutTime ? Date.parse(this.game.gameTimeoutTime) : null;
      },

      currentCard() {
        return this.game && this.game.currentCard ? this.game.currentCard : null;
      },

      judgeDidntChoose() {
        return this.game && this.game.gameState === 'DONE_JUDGING' && this.game.judgeChoiceWinner == null;
      },

      judgeChoice() {
        return this.game ? this.game.judgeChoiceWinner : null;
      },

      gameWinner() {
        if (!this.isStateGameOver) {
          return null;
        }
        return this.game.players.find(player => player.score === this.game.gameConfig.maxScore);
      }
    },

    watch: {
      gameTimeout() {
        if (this.gameTimeout) {
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
      },

      isStateChoosing() {
        // If we're done choosing, clear selections.
        if (!this.isStateChoosing) {
          this.selections = [];
        }
      },

      isStateJudging() {
        // If we're done judging, clear judge selection.
        if (!this.isStateJudging) {
          this.judgeSelection = null;
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

      phraseClicked(phrase) {
        if (this.isStateChoosing) {
          if (this.selections.includes(phrase.uuid)) {
            // User is clicking a card already selected, unselect it.
            this.selections = this.selections.filter((value => value !== phrase.uuid));
            return;
          }
          if (this.currentCard.numPhrases > this.selections.length) {
            this.selections.push(phrase.uuid);
          }
        }
      },

      judgeClicked(index) {
        if (this.isStateJudging && this.playerIsJudge) {
          if (this.judgeSelection === index) {
            this.judgeSelection = null;
          } else {
            this.judgeSelection = index;
          }
        }
      },

      confirmJudgement() {
        this.callJudgeVote(this.gameName, this.judgeSelection).catch(error => {
          this.error = error;
        });
      },

      confirmSelections() {
        this.callSelectPhrases(this.gameName, this.selections);
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
  .game-card {
    width: 150px;
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
    box-shadow: 0 0 10px 10px rgba(255,255,255,0.2);
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
