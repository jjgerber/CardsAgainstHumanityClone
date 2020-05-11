import Vue from 'vue';
import VueMoment from 'vue-moment';
import SockJS from "sockjs-client";
import Stomp from "webstomp-client";

import App from './App.vue';
import router from './router';
import vuetify from './plugins/vuetify';

Vue.config.productionTip = false;
Vue.prototype.$socket = new SockJS('/socket');
Vue.prototype.$stomp = Stomp.over(Vue.prototype.$socket);

Vue.use(VueMoment);

new Vue({
  router,
  vuetify,
  render: h => h(App),
}).$mount('#app');
