<template>
  <v-container>
    <v-row class="text-center">
      <v-col class="mb-4">
        <div class="text-left">
          <h1>Game: {{ gameName }}</h1>

          <v-card>
            <v-slide-y-reverse-transition>
              <v-alert v-if="error" type="error">{{ error }}</v-alert>
            </v-slide-y-reverse-transition>
            <v-row>
              <v-col cols="auto">
                <v-card-text>
                  <v-card color="black" class="black-card pa-2 text-center align-center d-inline-flex" raised>
                    <v-card-text style="height: 100%">There's only one thing better than ____... ___.</v-card-text>
                  </v-card>
                </v-card-text>
              </v-col>
              <v-col class="pa-0">
                <v-card class="fill-height ml-2" color="grey darken-3">

                  <v-card-text>
                    <v-container fluid class="pa-0">
                      <v-row>
                          <v-col class="text-center" v-for="(phraseSelection, idx) in phraseSelections" :key="`phrases-${idx}`">
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
          </v-card>

          <div class="mt-4">
          <h2>Your Phrases</h2>
          <v-card>
            <v-card-text class="text-center">
              <v-card
                v-for="(phrase, idx) in playerPhrases" :key="`player-phrase-${idx}`"
                color="white"
                light
                class="white-card pa-2 text-center align-center d-inline-flex ma-1"
                raised
              >
                <v-card-text style="height: 100%">{{ phrase.text }}</v-card-text>
              </v-card>
            </v-card-text>
          </v-card>

          </div>

        </div>

        {{ game }}
      </v-col>
    </v-row>
  </v-container>
</template>

<script>

  import GamesMixin from "../mixins/GamesMixin";

  export default {
    name: 'HelloWorld',

    mixins: [
      GamesMixin
    ],

    data: () => ({
      game: null,
      hover: null,
      error: null,
      phraseSelections: [
        [{"text": "Punching your mom's fartbox"},{"text": "Anal rape, without lube"}],
        [{"text": "Racism"},{"text": "Hate crimes"}],
        [{"text": "Punching a midget"},{"text": "Smoking meth with Joe Exotic"}],
        [{"text": "Jacking off"},{"text": "Dead babies"}],
        [{"text": "Calling someone a nigger"},{"text": "Flinging dick cheese"}]
      ],
      playerPhrases: [
        {"text": "PLAYER WHITE CARD GOES HERE 1"},
        {"text": "PLAYER WHITE CARD GOES HERE 2"},
        {"text": "PLAYER WHITE CARD GOES HERE 3"},
        {"text": "PLAYER WHITE CARD GOES HERE 4"},
        {"text": "PLAYER WHITE CARD GOES HERE 5"},
        {"text": "PLAYER WHITE CARD GOES HERE 6"},
        {"text": "PLAYER WHITE CARD GOES HERE 7"},
        {"text": "PLAYER WHITE CARD GOES HERE 8"},
        {"text": "PLAYER WHITE CARD GOES HERE 9"},
        {"text": "PLAYER WHITE CARD GOES HERE 10"}
      ]
    }),

    computed: {
      gameName () {
        return this.$route.params.name;
      }
    },

    methods: {
      getGameData () {
        this.getGame(this.gameName).then((game) => {
          this.game = game;
        }).catch((error) => {
          this.error = error;
        });
      }
    },

    mounted() {
      this.getGameData();
    }
  }
</script>

<style>
  .black-card {
    width: 140px;
    height: 180px;
    font-size: 14px
  }

  .white-card {
    width: 140px;
    height: 180px;
    font-size: 14px
  }

  .card-group {
    white-space: nowrap;
    transition: 0.2s;
  }
  .card-group:hover {
    cursor: pointer;
    transform: scale(1.1);
    transition: 0.2s;
  }
  .card-group:hover .v-card {
    background-color: rgba(0,0,0,0.2);
    box-shadow: 0px 0px 10px 10px rgba(0,0,0,0.2);
    transition: 0.2s;
  }
  .card-group .v-card {
    box-shadow: none;
    transition: 0.2s;
  }
</style>
