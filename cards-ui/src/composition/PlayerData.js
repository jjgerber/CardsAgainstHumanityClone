import { computed } from 'vue';
import { store } from "@/store";

export default function playerData() {
  const playerInfo = computed(() => {
    return store.state.playerInfo;
  });

  const playerPhrases = computed(() => {
    return playerInfo.value.phrases;
  });

  return {
    playerInfo,
    playerPhrases
  };
}
