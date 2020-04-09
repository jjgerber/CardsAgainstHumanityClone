import axios from 'axios'

export default {
  methods: {
    createGame(name, maxPlayers, turnTimeout, deckIds) {
      return new Promise((resolve, reject) => {
        axios.post(`/api/v1/games/${name}`, {
          name,
          maxPlayers,
          turnTimeout,
          deckIds
        }).then((response) => {
          resolve(response.data);
        }).catch((error) => {
          reject(error);
        })
      })
    },

    getGame(name) {
      return new Promise((resolve, reject) => {
        axios.get(`/api/v1/games/${name}`).then((response) => {
          resolve(response.data);
        }).catch((error) => {
          reject(error);
        })
      })
    },

    getAllLobbies () {
      return new Promise((resolve, reject) => {
        axios.get('/api/v1/games/all').then((response) => {
          resolve(response);
        }).catch((error) => {
          reject(error);
        })
      })
    }
  },

  computed: {
  }
}
