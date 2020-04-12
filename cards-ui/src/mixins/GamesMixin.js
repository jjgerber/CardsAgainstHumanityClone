import axios from 'axios'

export default {
  methods: {
    callCreateGame(name, maxPlayers, maxScore, turnTimeout, deckIds) {
      return new Promise((resolve, reject) => {
        axios.post(`/api/v1/games/${name}`, {
          name,
          maxPlayers,
          maxScore,
          turnTimeout,
          deckIds
        }).then((response) => {
          resolve(response.data);
        }).catch((error) => {
          reject(error);
        });
      });
    },

    callUpdateGame(name, maxPlayers, maxScore, turnTimeout, deckIds) {
      return new Promise((resolve, reject) => {
        axios.put(`/api/v1/games/${name}`, {
          name,
          maxPlayers,
          maxScore,
          turnTimeout,
          deckIds
        }).then((response) => {
          resolve(response.data);
        }).catch((error) => {
          reject(error);
        });
      });
    },

    callGetGame(name) {
      return new Promise((resolve, reject) => {
        axios.get(`/api/v1/games/${name}`).then((response) => {
          resolve(response.data);
        }).catch((error) => {
          reject(error);
        });
      });
    },

    callJoinGame(name) {
      return new Promise((resolve, reject) => {
        axios.post(`/api/v1/games/${name}/join`).then((response) => {
          resolve(response.data);
        }).catch((error) => {
          reject(error);
        });
      });
    },

    callStartGame(name) {
      return new Promise((resolve, reject) => {
        axios.post(`/api/v1/games/${name}/start`).then((response) => {
          resolve(response.data);
        }).catch((error) => {
          reject(error);
        });
      });
    },

    callLeaveGame(name) {
      return new Promise((resolve, reject) => {
        axios.post(`/api/v1/games/${name}/leave`).then((response) => {
          resolve(response.data);
        }).catch((error) => {
          reject(error);
        });
      });
    },

    callGetAllLobbies () {
      return new Promise((resolve, reject) => {
        axios.get('/api/v1/games/all').then((response) => {
          resolve(response);
        }).catch((error) => {
          reject(error);
        });
      });
    }
  }
}
