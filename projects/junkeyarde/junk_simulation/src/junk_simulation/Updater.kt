package junk_simulation

fun updateWorld(world: World): World {
  return World(
      round = world.round + 1,
      wave = world.wave,
      characters = world.characters,
      turns = world.turns
  )
}
