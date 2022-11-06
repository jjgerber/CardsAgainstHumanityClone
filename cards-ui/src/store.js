import { reactive } from 'vue'

export const store = reactive({
  state: {
    playerInfo: null,
    game: null
  }
})

export const mutations = {
  setPlayerInfo(playerInfo) {
    store.state['playerInfo'] = playerInfo;
  },

  setGame(game) {
    store.state['game'] = game;
  },

  setTimer(timer) {
    store.state['timer'] = timer;
  },

  setTimerStart(timerStart) {
    store.state['timerStart'] = timerStart;
  }
}
