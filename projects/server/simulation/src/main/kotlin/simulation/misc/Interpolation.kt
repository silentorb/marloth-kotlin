package simulation.misc

import silentorb.mythic.physics.interpolateTables
import silentorb.mythic.characters.interpolateCharacterRigs
import silentorb.mythic.spatial.interpolate
import simulation.main.World
import simulation.updating.simulationDelta

fun interpolateWorlds(accumulator: Double, first: World, second: World): World? {
  val scalar = (accumulator / simulationDelta).toFloat()
  val bodies = interpolateTables(scalar, first.deck.bodies, second.deck.bodies) { s, body, next ->
    body.copy(
        position = interpolate(s, body.position, next.position),
        orientation = interpolate(s, body.orientation, next.orientation)
    )
  }

  return second.copy(
      deck = second.deck.copy(
          bodies = bodies,
          characterRigs = interpolateCharacterRigs(scalar, first.deck.characterRigs, second.deck.characterRigs)
      )
  )
}

fun interpolateWorlds(accumulator: Double, worlds: List<World>): World? =
    if (worlds.size < 2)
      worlds.firstOrNull()
    else
      interpolateWorlds(accumulator, worlds[0], worlds[1])

