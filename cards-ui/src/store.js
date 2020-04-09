import Vue from 'vue'

export const store = Vue.observable({
  state: {
    playerInfo: null
  }
})

export const mutations = {
  setPlayerInfo (playerInfo) {
    Vue.set(store.state, 'playerInfo', playerInfo)
  }
}
