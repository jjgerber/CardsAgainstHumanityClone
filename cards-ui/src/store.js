import { reactive } from 'vue'

export const store = reactive({
  state: {
    playerInfo: null,
    game: null,
    games: [],
    socketConnectionTime: null
  }
})

export const mutations = {
  setPlayerInfo(playerInfo) {
    store.state['playerInfo'] = playerInfo;
  },

  setGame(game) {
    store.state['game'] = game;
  },

  setSocketConnectionTime(connectionTime) {
    store.state['socketConnectionTime'] = connectionTime;
  }
}
