package junk_simulation

fun updateWorld(world: World): World {
  return World(
      round = world.round + 1,
      wave = world.wave,
      creatures = world.creatures,
      turns = world.turns
  )
}
