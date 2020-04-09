import axios from 'axios'
import { mutations, store } from '@/store'

export default {
  methods: {
    retrievePlayerInfo () {
      return new Promise((resolve, reject) => {
        axios.get('/api/v1/player/info').then((response) => {
          mutations.setPlayerInfo(response.data)
          resolve()
        }).catch((error) => {
          reject(error)
        })
      })
    },

    setName (newName) {
      return new Promise((resolve, reject) => {
        axios.post(`/api/v1/player/name/${newName}`).then((response) => {
          mutations.setPlayerInfo(response.data)
          this.$emit('input', false)
          resolve()
        }).catch((error) => {
          reject(error)
        })
      })
    }
  },

  computed: {
    playerInfo () {
      return store.state.playerInfo
    }
  }
}
