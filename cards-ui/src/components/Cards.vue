<template>
  <v-card
    v-for="(phrase, idx) in cards"
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
</template>

<script>
export default {
  name: 'Cards',

  props: {
    cards: Array,
    selections: Array,
    playerIsJudge: Boolean
  },

  data: () => {
    return {
    }
  },

  methods: {
    phraseClicked(phrase) {
      this.$emit('phrase-clicked', phrase);
    }
  }

}
</script>

<style>

</style>
