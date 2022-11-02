import { createApp } from 'vue';
import SockJS from "sockjs-client/dist/sockjs"
import Stomp from "webstomp-client";
import moment from "moment";

import App from './App.vue';
import router from './router';
import vuetify from './plugins/vuetify';

const app = createApp(App);
const sock = new SockJS('/socket');

app.config.globalProperties.$socket = sock;
app.config.globalProperties.$stomp = Stomp.over(sock);

app.use(vuetify);
app.use(router);

router.isReady().then(() => {
  app.mount('#app');
});
