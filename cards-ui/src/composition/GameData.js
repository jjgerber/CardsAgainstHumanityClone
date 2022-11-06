import { computed, ref } from 'vue';
import { store } from "@/store";
import PlayerData from './PlayerData.js';

export default function gameData() {

  const { playerInfo } = PlayerData();

  const game = computed({
    get() {
      return store.state.game;
    },
    set(value) {
      store.state.game = value;
    }});

  const players = computed(() => {
    return game.value ? game.value.players : [];
  });

  const playerIsOwner = computed(() => {
    return game.value && game.value.owner ? game.value.owner.name === playerInfo.value.name : false;
  });

  const playerIsJudge = computed(() => {
    return game.value && game.value.judgingPlayer ? playerInfo.value.name === game.value.judgingPlayer.name : false;
  });

  const playerHasJoined = computed(() => {
    return game.value ? playerInfo.value.currentGameUuid === game.value.uuid : false;
  });

  const isGameFull = computed(() => {
    return game.value && (game.value.numPlayers >= game.value.gameConfig.maxPlayers);
  });

  const playersWhoHaveChosen = computed(() => {
    return game.value ? game.value.playersWhoHaveChosen.map(player => player.name) : [];
  });

  const gameStateFormatted = computed(() => {
    console.log("Game State: ", game.value);
    return game.value ? game.value.gameState.replace('_', ' ').toLowerCase() : '';
  });

  const isAbandoned = computed(() => {
    return game.value && game.value.gameState === 'ABANDONED';
  });

  const isLobby = computed(() => {
    return game.value && game.value.gameState === 'LOBBY';
  });

  const isStateChoosing = computed(() => {
    return game.value && game.value.gameState === 'CHOOSING';
  });

  const isStateJudging = computed(() => {
    return game.value && game.value.gameState === 'JUDGING';
  });

  const isStateDoneJudging = computed(() => {
    return game.value && game.value.gameState === 'DONE_JUDGING';
  });

  const isStateGameOver = computed(() => {
    return game.value && game.value.gameState === 'GAME_OVER';
  });

  const gameTimeout = computed(() => {
    return game.value && game.value.gameTimeoutTime ? Date.parse(game.value.gameTimeoutTime) : null;
  });

  const gameStateTime = computed(() => {
    return game.value && game.value.gameStateTime ? Date.parse(game.value.gameStateTime) : null;
  });

  const currentCard = computed(() => {
    return game.value && game.value.currentCard ? game.value.currentCard : null;
  });

  const judgeDidntChoose = computed(() => {
    return game.value && game.value.gameState === 'DONE_JUDGING' && game.value.judgeChoiceWinner == null;
  });

  const judgeChoice = computed(() => {
    return game.value ? game.value.judgeChoiceWinner : null;
  });

  const gameWinner = computed(() => {
    return isStateGameOver ? game.value.gameWinner : null;
  });

  return {
    game,
    players,
    playerIsOwner,
    playerIsJudge,
    playerHasJoined,
    isGameFull,
    playersWhoHaveChosen,
    gameStateFormatted,
    isAbandoned,
    isLobby,
    isStateChoosing,
    isStateJudging,
    isStateDoneJudging,
    isStateGameOver,
    gameTimeout,
    gameStateTime,
    currentCard,
    judgeDidntChoose,
    judgeChoice,
    gameWinner
  };
}
