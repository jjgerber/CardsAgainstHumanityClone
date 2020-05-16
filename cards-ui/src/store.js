import Vue from 'vue'

export const store = Vue.observable({
  state: {
    playerInfo: null,
    game: null,
    timerStart: null,
    timer: null
  }
})

export const mutations = {
  setPlayerInfo(playerInfo) {
    Vue.set(store.state, 'playerInfo', playerInfo);
  },

  setGame(game) {
    Vue.set(store.state, 'game', game);
  },

  setTimer(timer) {
    Vue.set(store.state, 'timer', timer);
  },

  setTimerStart(timerStart) {
    Vue.set(store.state, 'timerStart', timerStart);
  }
}
