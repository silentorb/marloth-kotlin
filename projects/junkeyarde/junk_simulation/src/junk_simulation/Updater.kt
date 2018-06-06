package junk_simulation

fun updateWorld(world: World): World {
  return World(
      turn = world.turn + 1,
      wave = world.wave,
      characters = world.characters
  )
}

fun updateGameState(state: GameState): GameState {
  return state.copy()
}