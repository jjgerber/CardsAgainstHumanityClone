import { createApp } from 'vue';
import SockJS from "sockjs-client/dist/sockjs"
import { Client } from '@stomp/stompjs';
import { mutations} from '@/store';

import App from './App.vue';
import router from './router';
import vuetify from './plugins/vuetify';

const app = createApp(App);

const client = new Client({
  connectHeaders: {},
  debug: function (str) {
    console.log(str);
  },
  reconnectDelay: 5000,
  heartbeatIncoming: 4000,
  heartbeatOutgoing: 4000,
});

client.webSocketFactory = function () {
  return new SockJS('/socket');
};



client.onStompError = function (frame) {
  // Will be invoked in case of error encountered at Broker
  // Bad login/passcode typically will cause an error
  // Complaint brokers will set `message` header with a brief message. Body may contain details.
  // Compliant brokers will terminate the connection after any error
  console.log('Broker reported error: ' + frame.headers['message']);
  console.log('Additional details: ' + frame.body);
};

client.activate();

app.config.globalProperties.$stomp = client;

app.use(vuetify);
app.use(router);

client.onConnect = function (frame) {
  // Do something, all subscribes must be done is this callback
  // This is needed because this will be executed after a (re)connect
  console.log("WebSocket Connected!");
  mutations.setSocketConnectionTime(Date.now());
};

router.isReady().then(() => {
  app.mount('#app');
});
