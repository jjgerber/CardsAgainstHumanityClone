<template>
  <v-container>
    <v-row class="text-center">
      <v-col class="mb-4">
        <div class="text-left">
          <h1>Create Lobby</h1>
        </div>
        <game-settings />
      </v-col>
    </v-row>
  </v-container>
</template>

<script>
import GameSettings from "../components/GameSettings";

export default {
  props: {
  },

  components: {
    GameSettings
  },

  data () {
    return {
    }
  },

  watch: {
  },

  methods: {
  }
}
</script>
