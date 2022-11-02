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
        <v-card>
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
    modelValue: Boolean
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
    modelValue() {
      this.dialog = this.modelValue;
    },

    dialog() {
      if (this.dialog === false) {
        this.$emit('update:modelValue', false);
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
