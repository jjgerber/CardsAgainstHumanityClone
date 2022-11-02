<template>
  <v-card>
    <v-card-title>Chat</v-card-title>
    <div
      id="chat"
      ref="chat"
    >
      <v-card-text class="text-left pb-0 pt-0">
        <v-row
          v-for="(msg, idx) in chatMessages"
          :key="`message-${idx}`"
          dense
        >
          <v-col
            class="font-italic text-blue"
            cols="auto"
          >
            {{ formatTime(msg.messageTime) }}
          </v-col>
          <v-col
            v-if="msg.playerId !== 'SERVER'"
            class="font-weight-bold"
            cols="auto"
          >
            {{ msg.playerName }}:
          </v-col>
          <v-col :class="{'font-italic': msg.playerId === 'SERVER'}">
            {{ msg.message }}
          </v-col>
        </v-row>
      </v-card-text>
    </div>
    <div>
      <v-card-text class="mt-4 pt-0 text-left">
        <v-text-field
          density="compact"
          v-model="chatMessage"
          @keypress.enter="sendMessage()"
        /><v-btn
          color="primary"
          :disabled="!!!chatMessage"
          @click="sendMessage()"
        >
          Send
        </v-btn>
      </v-card-text>
    </div>
  </v-card>
</template>

<script>
  import moment from "moment";

  export default {
    name: 'Chat',

    props: {
      gameName: {
        type: String,
        required: true
      }
    },

    data: () => {
      return {
        chatMessage: '',
        chatMessages: [],
        chatSubscription: null
      }
    },

    computed: {
    },

    mounted() {
      this.connect();
    },

    destroyed() {
      if (this.chatSubscription) {
        console.log(`Unsubscribing from game ${this.gameName}'s chat.`);
        this.chatSubscription.unsubscribe();
      }
    },

    methods: {
      connect() {
        this.chatSubscription = this.$stomp.subscribe('/topic/chat/' + this.gameName, tick => {
          this.chatMessages.push(JSON.parse(tick.body));
          this.$nextTick(() => {
            var chat = this.$el.querySelector("#chat");
            chat.scrollTop = chat.scrollHeight;
          });
        });

      },

      sendMessage() {
        if (this.chatMessage && this.chatMessage.length > 0) {
          this.$stomp.send('/app/chat/' + this.gameName, this.chatMessage);
          this.chatMessage = '';
        }
      },

      formatTime(time) {
        return moment(time).format('h:mm:ss a');
      }
    }
  }
</script>

<style>

  #chat {
    max-height: 200px;
    overflow-y: auto;
  }
</style>
