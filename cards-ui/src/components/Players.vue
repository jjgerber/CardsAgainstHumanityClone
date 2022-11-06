<template>
  <v-card>
    <v-card-title>Players</v-card-title>
    <v-card-text class="text-left">
      <v-table :hover="true">
        <template v-slot:default>
          <tbody>
          <tr
            v-for="player in players"
            :key="`player-${player.name}`"
            :class="{'bg-green': isStateDoneJudging && judgeChoice !== null ? game.lastWinningPlayer.name === player.name : false}"
          >
            <td width="100%">
              <v-icon
                v-if="player.name === game.owner.name"
                size="16"
                color="primary"
              >
                mdi-crown
              </v-icon>
              {{ player.playerName }}
            </td>
            <td md="auto">
              <v-icon
                v-if="playersWhoHaveChosen.includes(player.name)"
                size="16"
                color="primary"
              >
                mdi-check
              </v-icon>
              <v-icon
                v-else-if="game.judgingPlayer ? player.name === game.judgingPlayer.name : false"
                size="16"
                color="primary"
              >
                mdi-gavel
              </v-icon>
            </td>
            <td md="auto">
              {{ player.score }}
            </td>
          </tr>
          </tbody>
        </template>
      </v-table>
    </v-card-text>
    <v-divider />
    <v-card-actions>
      <div class="pa-2">
        <span class="text-blue mr-1">{{ game.numPlayers }} / {{ game.gameConfig.maxPlayers }}</span> Players
      </div>
      <v-spacer />
      <div class="pa-2">
        Winning Score: <span class="text-blue">{{ game.gameConfig.maxScore }}</span>
      </div>
    </v-card-actions>
  </v-card>
</template>

<script>
  import GameData from "@/composition/GameData.js";
  import PlayerData from "@/composition/PlayerData.js";

  export default {
    name: 'Players',

    setup() {
      const {
        game,
        players,
        judgeChoice,
        playersWhoHaveChosen,
        isStateDoneJudging
      } = GameData();
      const { playerInfo } = PlayerData();

      return {
        game,
        judgeChoice,
        players,
        playersWhoHaveChosen,
        isStateDoneJudging,
        playerInfo
      }
    }
  }
</script>

<style>

</style>
