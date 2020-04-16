<template>
  <div class="text-center">
    <v-dialog
      v-model="dialog"
      width="500"
    >
      <template v-slot:activator="{ on }">
        <v-btn
          color="red lighten-2"
          dark
          v-on="on"
        >
          Click Me
        </v-btn>
      </template>

      <v-card color="grey darken-3">
        <v-card-title
          class="headline"
          primary-title
        >
          Set Player Name
        </v-card-title>

        <v-slide-y-reverse-transition>
          <v-alert v-if="error" type="error">{{ error }}</v-alert>
        </v-slide-y-reverse-transition>

        <v-card-text>
          <p>
            Set your desired username:
          </p>
           <v-text-field :autofocus="true" :clearable="true" v-model="userName" @keypress.enter="setNewName" />
        </v-card-text>

        <v-divider></v-divider>
        <v-card-actions>
          <v-spacer></v-spacer>
          <v-btn
            color="red"
            text
            @click="cancel()"
          >
            Cancel
          </v-btn>
          <v-btn
            color="primary"
            text
            @click="setNewName()"
          >
            Accept Change
          </v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>
  </div>
</template>

<script>
import UserInfoMixin from '@/mixins/UserInfoMixin'

export default {
  mixins: [
    UserInfoMixin
  ],

  props: {
    value: Boolean
  },

  data () {
    return {
      dialog: this.value,
      userName: '',
      error: null
    }
  },

  watch: {
    value () {
      this.dialog = this.value
    },

    dialog () {
      if (this.dialog === false) {
        this.$emit('input', false)
      } else {
        this.userName = this.playerInfo.playerName;
      }
    }
  },

  methods: {
    setNewName () {
      this.setName(this.userName).then(() => {
        this.dialog = false
      }).catch((error) => {
        this.error = error
      });
    },

    cancel () {
      this.error = null
      this.userName = ''
      this.dialog = false
    }
  }
}
</script>
