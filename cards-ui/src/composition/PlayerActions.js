import { mutations } from "@/store";
import axios from "axios";

export default function playerData() {
  function retrievePlayerInfo() {
    return new Promise((resolve, reject) => {
      axios.get('/api/v1/player/info').then((response) => {
        mutations.setPlayerInfo(response.data)
        resolve()
      }).catch((error) => {
        reject(error)
      })
    })
  }

  function setName(newName) {
    return new Promise((resolve, reject) => {
      axios.post(`/api/v1/player/name`, newName,
        { withCredentials: true, headers: { 'Content-Type': 'text/plain' } })
        .then((response) => {
          mutations.setPlayerInfo(response.data);
          this.$emit('input', false);
          resolve();
        }).catch((error) => {
        reject(error);
      });
    })
  }

  return {
    retrievePlayerInfo,
    setName
  };
}
