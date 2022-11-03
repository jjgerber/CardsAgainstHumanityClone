import { createWebHashHistory, createRouter } from "vue-router";

import Lobbies from '../views/Lobbies.vue'
import Game from '../views/Game.vue'
import Settings from '@/views/Settings.vue'
import About from '@/views/About.vue'
import CreateLobby from '@/views/CreateGame.vue'

const routes = [
  {
    path: '/',
    name: 'Lobbies',
    component: Lobbies
  },
  {
    path: '/game/:name',
    name: 'Game',
    component: Game
  },
  {
    path: '/settings',
    name: 'Settings',
    component: Settings
  },
  {
    path: '/about',
    name: 'About',
    component: About
  },
  {
    path: '/create-lobby',
    name: 'Create Lobby',
    component: CreateLobby
  }
]

const router = createRouter({
  history: createWebHashHistory(),
  base: import.meta.env.BASE_URL,
  routes
})

export default router;
