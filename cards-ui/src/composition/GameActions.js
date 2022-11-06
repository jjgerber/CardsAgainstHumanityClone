import axios from 'axios'

export default function gameActions() {
  function callCreateGame(name, maxPlayers, maxScore, turnTimeout, deckIds) {
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
  }

  function callUpdateGame(name, maxPlayers, maxScore, turnTimeout, deckIds) {
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
  }

  function callGetGame(name) {
    return new Promise((resolve, reject) => {
      axios.get(`/api/v1/games/${name}`).then((response) => {
        resolve(response.data);
      }).catch((error) => {
        reject(error);
      });
    });
  }

  function callJoinGame(name) {
    return new Promise((resolve, reject) => {
      axios.post(`/api/v1/games/${name}/join`).then((response) => {
        resolve(response.data);
      }).catch((error) => {
        reject(error);
      });
    });
  }

  function callStartGame(name) {
    return new Promise((resolve, reject) => {
      axios.post(`/api/v1/games/${name}/start`).then((response) => {
        resolve(response.data);
      }).catch((error) => {
        reject(error);
      });
    });
  }

  function callSelectPhrases(name, phrases) {
    return new Promise((resolve, reject) => {
      axios.post(`/api/v1/games/${name}/select-phrases`, phrases).then((response) => {
        resolve(response.data);
      }).catch((error) => {
        reject(error);
      });
    });
  }

  function callJudgeVote(name, voteIdx) {
    return new Promise((resolve, reject) => {
      axios.post(`/api/v1/games/${name}/select-winner/${voteIdx}`).then((response) => {
        resolve(response.data);
      }).catch((error) => {
        reject(error);
      });
    });
  }

  function callLeaveGame(name) {
    return new Promise((resolve, reject) => {
      axios.post(`/api/v1/games/${name}/leave`).then((response) => {
        resolve(response.data);
      }).catch((error) => {
        reject(error);
      });
    });
  }

  function callGetAllLobbies () {
    return new Promise((resolve, reject) => {
      axios.get('/api/v1/games/all').then((response) => {
        resolve(response);
      }).catch((error) => {
        reject(error);
      });
    });
  }

  return {
    callCreateGame,
    callUpdateGame,
    callGetGame,
    callJoinGame,
    callStartGame,
    callSelectPhrases,
    callJudgeVote,
    callLeaveGame,
    callGetAllLobbies
  };
}
