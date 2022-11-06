<template>
  <v-container v-if="game">
    <v-row class="text-center">
      <v-col class="mb-4">
        <div class="mb-4">
          <v-row>
            <v-col class="text-left">
              <h1 class="d-inline-flex">
                {{ gameName }}
              </h1>
            </v-col>
            <v-col cols="auto">
              <v-btn
                v-if="!playerHasJoined"
                class="d-inline-block"
                rounded
                color="green"
                :disabled="isGameFull"
                @click="joinGame()"
              >
                <v-icon>mdi-account-plus</v-icon>Join Game
              </v-btn>
              <v-btn
                v-else
                class="d-inline-block ml-2"
                rounded
                color="red"
                @click="leaveGame()"
              >
                <v-icon>mdi-account-minus</v-icon>Leave Game
              </v-btn>

              <v-tooltip
                v-if="playerIsOwner && isLobby"
                location="bottom"
                :disabled="players.length >= 3"
              >
                <template v-slot:activator="{ props }">
                  <div
                    class="d-inline-block"
                    v-bind="props"
                  >
                    <v-btn
                      class="d-inline-block ml-2"
                      rounded
                      color="primary"
                      :disabled="players.length <= 2"
                      @click="startGame()"
                    >
                      <v-icon>mdi-play</v-icon>Start Game
                    </v-btn>
                  </div>
                </template>
                <span>Need at least 3 players to start.</span>
              </v-tooltip>
            </v-col>
            <v-col cols="auto">
              <v-scale-transition>
                <v-chip
                  class="mt-1"
                  v-if="timerCount > 0"
                  variant="outlined"
                  :color="timerCount > 10 ? 'green' : 'red'"
                  text-color="white"
                  ripple
                >
                  <v-icon start :color="timerCount > 10 ? 'green' : 'red'" icon="mdi-alarm"></v-icon>
                  {{ timerCount }}
                </v-chip>
              </v-scale-transition>
            </v-col>
          </v-row>

          <v-progress-linear
            v-model="timerPercent"
            :active="!!timerPercent"
            :color="timerCount > 10 ? 'green' : 'red'"
            striped
            rounded
          />

          <v-slide-y-reverse-transition>
            <v-alert
              v-model="hasError"
              type="error"
              dismissible
            >
              {{ error }}
            </v-alert>
          </v-slide-y-reverse-transition>

          <h2 class="text-capitalize text-left mb-3 mt-2">
            {{ gameStateFormatted }}
          </h2>
          <game-settings
            v-if="isLobby"
            :game-name="gameName"
            :game-config="game.gameConfig"
            :user-is-owner="playerIsOwner"
          />
          <div v-else-if="isStateGameOver">
            <v-alert color="green">
              {{ gameWinner.playerName }} has reached score {{ gameWinner.score }} and won the game!
            </v-alert>
          </div>
          <v-row
            v-else
            class="justify-center"
          >
            <v-col
              cols="auto"
              class="text-center align-center"
            >
              <v-card
                v-if="!!currentCard"
                color="black"
                class="game-card-black pa-2 text-center align-center d-inline-flex align-center"
                raised
              >
                <v-card-text
                  class="pa-0 font-weight-bold"
                  style="height: 100%"
                  v-html="currentCard.text"
                />
              </v-card>
            </v-col>
            <v-col>
              <v-card class="white-card-holder">
                <v-card-text class="text-center">
                  <v-slide-y-transition mode="out-in">
                    <v-alert
                      v-if="judgeDidntChoose && game.phraseSelections.length === 0"
                      key="no-phrases"
                      class="mb-4 text-center"
                      type="error"
                    >
                      No players chose a phrase to be judged.
                    </v-alert>
                    <v-alert
                      v-else-if="judgeDidntChoose && game.phraseSelections.length > 0"
                      key="no-judgement"
                      class="mb-4 text-center"
                      type="error"
                    >
                      The judge didn't choose a winner in time. Your cards have been returned.
                    </v-alert>
                    <v-alert
                      v-else-if="playerIsJudge && isStateJudging && judgeSelection != null"
                      key="judge-confirm"
                      class="mb-4 text-center"
                      color="grey-darken-3"
                      dense
                    >
                      <span>Confirm your selection? </span>
                      <v-btn
                        color="green"
                        size="x-small"
                        @click="confirmJudgement()"
                      >
                        Confirm
                      </v-btn>
                    </v-alert>
                    <v-alert
                      v-else-if="playerIsJudge && isStateJudging"
                      key="judge-choose"
                      class="mb-4 text-center"
                      color="grey-darken-3"
                    >
                      Choose the winner.
                    </v-alert>
                    <v-alert
                      v-else-if="isStateDoneJudging && judgeChoice !== null"
                      key="winner"
                      class="mb-4 text-center"
                      color="green"
                    >
                      {{ game.lastWinningPlayer.playerName }} won!
                      They have {{ game.lastWinningPlayer.score }} point{{ game.lastWinningPlayer.score > 1 ? 's' : '' }}.
                    </v-alert>
                    <v-alert
                      v-else-if="isStateJudging"
                      key="currently-judging"
                      class="mb-4 text-center"
                      color="grey-darken-3"
                    >
                      {{ game.judgingPlayer.playerName }} is currently judging...
                    </v-alert>
                  </v-slide-y-transition>
                  <v-container
                    fluid
                    class="pa-0"
                  >
                    <v-row v-if="isStateChoosing">
                      <!-- In Choosing Mode -->
                      <v-col v-if="game.playersWhoHaveChosen.length === 0">
                        No players have chosen their cards yet.
                      </v-col>
                      <v-col
                        v-for="chosen in playersWhoHaveChosen.length"
                        v-else
                        :key="`choices-${chosen}`"
                        class="text-center"
                      >
                        <div class="card-group text-center d-inline">
                          <v-card
                            v-for="card in currentCard.numPhrases"
                            :key="`phrase-${chosen}-${card}`"
                            color="white"
                            light
                            class="game-card pa-2 text-center align-center d-inline-flex ma-1"
                            raised
                          />
                        </div>
                      </v-col>
                    </v-row>
                    <v-row v-else>
                      <!-- In Judging Mode -->
                      <v-col
                        v-for="(phraseSelection, idx) in game.phraseSelections"
                        :key="`phrases-${idx}`"
                        class="text-center"
                      >
                        <div class="card-group text-center d-inline">
                          <v-card
                            v-for="(phrase, phraseIdx) in phraseSelection"
                            :key="`phrase-${idx}-${phraseIdx}`"
                            :color="isStateDoneJudging && judgeChoice === idx || judgeSelection === idx ? 'green lighten-2' : 'white'"
                            light
                            class="game-card pa-2 text-center align-center d-inline-flex ma-1"
                            raised
                            @click="judgeClicked(idx)"
                          >
                            <v-card-text
                              class="pa-0"
                              style="height: 100%"
                              v-html="phrase.text"
                            />
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
            <div
              v-if="playerHasJoined && playerPhrases && playerPhrases.length > 0"
              class="mt-4"
            >
              <h2 class="text-left">
                Your Phrases
              </h2>
              <v-card>
                <v-card-text class="text-center">
                  <v-scroll-y-transition mode="out-in">
                    <v-alert
                      v-if="playerIsJudge"
                      class="mb-4"
                      key="choosing-judging"
                      color="grey-darken-3"
                    >
                      You are judging.
                    </v-alert>
                    <v-alert
                      v-else-if="isStateChoosing && currentCard.numPhrases === selections.length"
                      class="mb-4"
                      key="choosing-select"
                      color="grey-darken-3"
                    >
                      <span v-if="playersWhoHaveChosen.includes(playerInfo.name)">
                        You have made your selection for this round.
                      </span>
                      <span v-else>
                        Confirm your selections?
                        <v-btn
                          color="green"
                          size="x-small"
                          @click="confirmSelections"
                        >
                          Confirm
                        </v-btn>
                      </span>
                    </v-alert>
                    <v-alert
                      v-else-if="isStateChoosing"
                      class="mb-4"
                      key="choosing-need-to-pick"
                      color="grey-darken-3"
                    >
                      Choose {{ currentCard.numPhrases }} white card{{ currentCard.numPhrases > 1 ? 's' : '' }}.
                    </v-alert>
                  </v-scroll-y-transition>
                  <v-card
                    v-for="(phrase, idx) in playerPhrases"
                    :key="`player-phrase-${idx}`"
                    :color="selections.includes(phrase.uuid) ? 'green lighten-2' : 'white'"
                    light
                    class="game-card pa-2 text-center align-center d-inline-flex ma-1"
                    :class="playerIsJudge ? 'grey' : 'white'"
                    raised
                    @click="phraseClicked(phrase)"
                  >
                    <v-card-text
                      class="pa-0"
                      style="height: 100%"
                      v-html="phrase.text"
                    />
                  </v-card>
                </v-card-text>
              </v-card>
            </div>
          </v-scroll-y-transition>

          <div class="mt-4">

            <v-row>
              <v-col class="players-container">
                <v-card>
                  <v-card-title>Players</v-card-title>
                  <v-card-text class="text-left">
                    <v-table :hover="true">
                      <template v-slot:default>
                        <tbody>
                        <tr
                          v-for="player in players"
                          :key="`player-${player.name}`"
                          :class="{'bg-green': isStateDoneJudging && judgeChoice !== null ? game.lastWinningPlayer.name === player.name : false}"
                        >
                          <td width="100%">
                            <v-icon
                              v-if="player.name === game.owner.name"
                              size="16"
                              color="primary"
                            >
                              mdi-crown
                            </v-icon>
                            {{ player.playerName }}
                          </td>
                          <td md="auto">
                            <v-icon
                              v-if="playersWhoHaveChosen.includes(player.name)"
                              size="16"
                              color="primary"
                            >
                              mdi-check
                            </v-icon>
                            <v-icon
                              v-else-if="game.judgingPlayer ? player.name === game.judgingPlayer.name : false"
                              size="16"
                              color="primary"
                            >
                              mdi-gavel
                            </v-icon>
                          </td>
                          <td md="auto">
                            {{ player.score }}
                          </td>
                        </tr>
                        </tbody>
                      </template>
                    </v-table>
                  </v-card-text>
                  <v-divider />
                  <v-card-actions>
                    <div class="pa-2">
                      <span class="text-blue mr-1">{{ game.numPlayers }} / {{ game.gameConfig.maxPlayers }}</span> Players
                    </div>
                    <v-spacer />
                    <div class="pa-2">
                      Winning Score: <span class="text-blue">{{ game.gameConfig.maxScore }}</span>
                    </div>
                  </v-card-actions>
                </v-card>
              </v-col>

              <v-col class="chat-container">
                <chat :game-name="gameName" />
              </v-col>
            </v-row>

          </div>
        </div>
      </v-col>
    </v-row>
        <!-- {{ state }} -->
  </v-container>
</template>

<script>
  import { store, mutations } from "@/store.js";
  import Chat from "../components/Chat.vue";
  import GameSettings from "../components/GameSettings.vue";
  import GamesMixin from "../mixins/GamesMixin.js";
  import UserInfoMixin from "../mixins/UserInfoMixin.js";

  export default {
    name: 'Game',

    comments: [
      Chat
    ],

    components: {
      Chat,
      GameSettings
    },

    mixins: [
      GamesMixin,
      UserInfoMixin
    ],

    data: () => {
      return {
        selections: [],
        judgeSelection: null,
        hover: null,
        hasError: false,
        error: null,
        timer: null,
        gameSubscription: null
      }
    },

    computed: {
      game: {
        get: () => {
          return store.state.game;
        },
        set: (game) => {
          mutations.setGame(game);
        }
      },

      timerCount: {
        get: () => {
          return store.state.timer;
        },
        set: (timerCount) => {
          mutations.setTimer(timerCount);
        }
      },

      timerStart: {
        get: () => {
          return store.state.timerStart;
        },
        set: (timerStart) => {
          mutations.setTimerStart(timerStart);
        }
      },

      timerPercent() {
        return this.timerStart > 0 ? (this.timerCount / this.timerStart) * 100 : null;
      },

      state() {
        return store.state;
      },

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

      playersWhoHaveChosen() {
        return this.game ? this.game.playersWhoHaveChosen.map(player => player.name) : [];
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

      gameStateTime() {
        return this.game && this.game.gameStateTime ? Date.parse(this.game.gameStateTime) : null;
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
        return this.isStateGameOver ? this.game.gameWinner : null;
      }
    },

    watch: {
      gameTimeout() {
        if (this.gameTimeout) {
          clearInterval(this.timer);
          this.timerCount = Math.max(Math.round((this.gameTimeout - new Date()) / 1000), 0);
          this.timerStart = Math.round((this.gameTimeout - this.gameStateTime) / 1000);
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
        if (!this.isStateChoosing) {
          // If we're done choosing (was just set to false), clear selections.
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

    mounted() {
      this.getGameData();
      this.connect();
    },

    unmounted() {
      console.log(`Unsubscribing from game subscription.`)
      this.gameSubscription.unsubscribe();

      clearInterval(this.timer);
      this.game = null;
      this.timerStart = null;
      this.timerCount = null;
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
          mutations.setGame(null);
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
        if (this.isStateChoosing && !this.playerIsJudge) {
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
    }
  }
</script>

<style>
  .game-card-black {
    width: 200px;
    height: 220px;
    font-size: 12px;
    box-shadow: 0 0 8px 8px rgba(255,255,255,0.1) !important;
  }

  .game-card {
    width: 150px !important;
    max-width: 150px;
    height: 180px;
    font-size: 12px;
  }

  .card-group {
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

  .white-card-holder {
    min-width: 350px;
  }

  .chat {
    max-height: 500px;
    overflow-y: auto;
  }

  .players-container {
    min-width: 325px;
  }

  .chat-container {
    min-width: 350px;
  }

</style>
