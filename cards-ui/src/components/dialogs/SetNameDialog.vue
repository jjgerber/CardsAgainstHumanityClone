<template>
  <div class="text-center">
    <v-dialog
      v-model="dialog"
      width="500"
    >
      <v-form
        ref="form"
        v-model="valid"
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
            <v-alert
              v-if="error"
              type="error"
            >
              {{ error }}
            </v-alert>
          </v-slide-y-reverse-transition>

          <v-card-text>
            <p>
              Set your desired username:
            </p>
            <v-text-field
              v-model="userName"
              :rules="[v => /^[a-zA-Z0-9 \-'!?.,]{1,20}$/.test(v) || 'Numbers and letters only. Required.']"
              maxlength="20"
              counter
              :autofocus="true"
              @keypress.enter.prevent="setNewName"
            />
          </v-card-text>

          <v-divider />
          <v-card-actions>
            <v-spacer />
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
              :disabled="!valid"
              @click="setNewName()"
            >
              Accept Change
            </v-btn>
          </v-card-actions>
        </v-card>
      </v-form>
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
      error: null,
      valid: true
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
      this.$refs.form.validate()
      if (this.valid) {
        this.setName(this.userName).then(() => {
          this.error = null;
          this.dialog = false;
        }).catch((error) => {
          this.error = error.response.data.message;
        });
      }
    },

    cancel () {
      this.error = null
      this.userName = ''
      this.dialog = false
    }
  }
}
</script>
